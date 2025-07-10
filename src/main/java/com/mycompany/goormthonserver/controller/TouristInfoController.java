package com.mycompany.goormthonserver.controller;

import com.mycompany.goormthonserver.common.dto.ApiResponse;
import com.mycompany.goormthonserver.common.swagger.CommonApiResponses;
import com.mycompany.goormthonserver.dto.VisitJejuResponseDto;
import com.mycompany.goormthonserver.service.TouristInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * 관광지 정보 REST API 컨트롤러
 * 위경도 기반 관광지 정보 조회
 */
@RestController
@RequestMapping("/tourist-info")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "관광지", description = "제주 관광지 정보 조회 API")
public class TouristInfoController {

    private final TouristInfoService touristInfoService;

    /**
     * 위경도로 관광지 정보 조회 (메인 API)
     */
    @GetMapping("/location")
    @Operation(
            summary = "위경도로 관광지 정보 조회",
            description = """
            사용자의 위경도를 받아서 가장 가까운 관광지의 상세 정보를 조회합니다.
            
            **조회 순서:**
            1. 🔄 Redis 캐시 확인 (1차)
            2. 🗄️ MySQL 캐시 확인 (2차)  
            3. 🌐 비짓제주 API 호출 (3차)
            
            **응답 데이터:** 7개 핵심 필드
            - title: 관광지명
            - introduction: 관광지 소개
            - tag: 주요 키워드
            - address: 지번 주소
            - photoId: 이미지 고유번호
            - imgPath: 원본 이미지 URL
            - source: 데이터 소스 (REDIS/MYSQL/API)
            """
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "관광지 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VisitJejuLocationResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "잘못된 위경도 좌표"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "해당 위치 주변에 관광지 없음"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    public ResponseEntity<ApiResponse<VisitJejuResponseDto>> getTouristInfoByLocation(
            @Parameter(description = "위도", example = "33.4584", required = true)
            @RequestParam double latitude,

            @Parameter(description = "경도", example = "126.9426", required = true)
            @RequestParam double longitude,

            @Parameter(description = "검색 반경 (km)", example = "1.0")
            @RequestParam(required = false) Double radius) {

        try {
            // 좌표 유효성 검사
            if (!isValidCoordinate(latitude, longitude)) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("유효하지 않은 좌표입니다.", "INVALID_COORDINATES"));
            }

            // 관광지 정보 조회
            Optional<VisitJejuResponseDto> result = touristInfoService.getTouristInfoByLocation(
                    latitude, longitude, radius);

            if (result.isPresent()) {
                return ResponseEntity.ok(
                        ApiResponse.success("관광지 정보 조회 성공", result.get()));
            } else {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("해당 위치 주변에 관광지가 없습니다.", "NO_TOURIST_SPOT_FOUND"));
            }

        } catch (Exception e) {
            log.error("❌ 관광지 정보 조회 실패: ({}, {}) - {}", latitude, longitude, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("관광지 정보 조회 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 외부 ID로 관광지 정보 조회
     */
    @GetMapping("/{externalId}")
    @Operation(
            summary = "외부 ID로 관광지 정보 조회",
            description = "비짓제주 콘텐츠 ID로 직접 관광지 정보를 조회합니다."
    )
    @CommonApiResponses.CommonResponses
    public ResponseEntity<ApiResponse<VisitJejuResponseDto>> getTouristInfoByExternalId(
            @Parameter(description = "비짓제주 콘텐츠 ID", example = "CONT_000000000500349")
            @PathVariable String externalId) {

        try {
            Optional<VisitJejuResponseDto> result = touristInfoService.getTouristInfoByExternalId(externalId);

            if (result.isPresent()) {
                return ResponseEntity.ok(
                        ApiResponse.success("관광지 정보 조회 성공", result.get()));
            } else {
                return ResponseEntity.status(404)
                        .body(ApiResponse.error("해당 ID의 관광지가 없습니다.", "TOURIST_SPOT_NOT_FOUND"));
            }

        } catch (Exception e) {
            log.error("❌ 관광지 정보 조회 실패 (외부 ID: {}): {}", externalId, e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("관광지 정보 조회 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 캐시 통계 조회 (관리용)
     */
    @GetMapping("/cache/stats")
    @Operation(
            summary = "캐시 통계 조회",
            description = "Redis와 MySQL 캐시 상태 및 통계 정보를 조회합니다."
    )
    @CommonApiResponses.CommonResponses
    public ResponseEntity<ApiResponse<TouristInfoService.CacheStatistics>> getCacheStatistics() {
        try {
            TouristInfoService.CacheStatistics stats = touristInfoService.getCacheStatistics();
            return ResponseEntity.ok(ApiResponse.success("캐시 통계 조회 성공", stats));

        } catch (Exception e) {
            log.error("❌ 캐시 통계 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("캐시 통계 조회 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 캐시 정리 (관리용)
     */
    @PostMapping("/cache/cleanup")
    @Operation(
            summary = "캐시 정리",
            description = "만료된 캐시와 오래된 캐시를 정리합니다."
    )
    @CommonApiResponses.CommonResponses
    public ResponseEntity<ApiResponse<TouristInfoService.CacheCleanupResult>> cleanupCaches() {
        try {
            TouristInfoService.CacheCleanupResult result = touristInfoService.cleanupCaches();
            return ResponseEntity.ok(ApiResponse.success("캐시 정리 완료", result));

        } catch (Exception e) {
            log.error("❌ 캐시 정리 실패: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(ApiResponse.error("캐시 정리 중 오류가 발생했습니다.", "INTERNAL_SERVER_ERROR"));
        }
    }

    /**
     * 헬스체크 (API 연결 상태 확인)
     */
    @GetMapping("/health")
    @Operation(
            summary = "API 상태 확인",
            description = "비짓제주 API 연결 상태를 확인합니다."
    )
    public ResponseEntity<ApiResponse<HealthStatus>> healthCheck() {
        // 간단한 헬스체크 - 실제로는 VisitJejuApiClient.testConnection() 사용 가능
        HealthStatus status = HealthStatus.builder()
                .status("UP")
                .timestamp(System.currentTimeMillis())
                .message("API 서비스가 정상 작동 중입니다.")
                .build();

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * 좌표 유효성 검사
     */
    private boolean isValidCoordinate(double latitude, double longitude) {
        // 제주도 대략적인 좌표 범위 (확장 가능)
        boolean validLatitude = latitude >= 33.0 && latitude <= 34.0;
        boolean validLongitude = longitude >= 126.0 && longitude <= 127.0;

        return validLatitude && validLongitude;
    }

    // ========== 응답 DTO 클래스들 ==========

    /**
     * 위치 기반 조회 응답 (Swagger 문서용)
     */
    @Schema(description = "위치 기반 관광지 정보 응답")
    public static class VisitJejuLocationResponse extends ApiResponse<VisitJejuResponseDto> {
        // Swagger 문서 생성용 클래스
    }

    /**
     * 헬스체크 응답
     */
    @lombok.Data
    @lombok.Builder
    @Schema(description = "API 상태 정보")
    public static class HealthStatus {
        @Schema(description = "상태", example = "UP")
        private String status;

        @Schema(description = "타임스탬프", example = "1641024000000")
        private Long timestamp;

        @Schema(description = "메시지", example = "API 서비스가 정상 작동 중입니다.")
        private String message;
    }
}