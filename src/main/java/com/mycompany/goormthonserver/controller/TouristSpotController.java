package com.mycompany.goormthonserver.controller;
import com.mycompany.goormthonserver.dto.TouristSpotDetailDto;
import com.mycompany.goormthonserver.dto.TouristSpotLocationDto;
import com.mycompany.goormthonserver.service.TouristSpotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/tour-spots")
@RequiredArgsConstructor
@Slf4j
public class TouristSpotController {

    private final TouristSpotService touristSpotService;

    @GetMapping("/location")
    public ResponseEntity<List<TouristSpotLocationDto>> findNearbyTouristSpots(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "10") double radius) {

        // 입력 유효성 검증
        if (latitude == null || longitude == null) {
            return ResponseEntity.badRequest().build();
        }

        // 위경도 범위 검증
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 ||
                latitude.compareTo(BigDecimal.valueOf(90)) > 0 ||
                longitude.compareTo(BigDecimal.valueOf(-180)) < 0 ||
                longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            return ResponseEntity.badRequest().build();
        }

        // 반경 검증 (최대 10km)
        if (radius <= 0 || radius > 10) {
            radius = 10;
        }

        List<TouristSpotLocationDto> nearbySpots = touristSpotService.findNearbyTouristSpots(
                latitude, longitude, radius, 10);

        log.info("반경 {}km 내 관광지 {}개 조회 완료", radius, nearbySpots.size());

        return ResponseEntity.ok(nearbySpots);
    }

    // 키워드 검색 (거리 포함)
    @GetMapping("/search")
    public ResponseEntity<List<TouristSpotLocationDto>> searchByKeyword(
            @RequestParam String keyword,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude,
            @RequestParam(defaultValue = "10") int limit) {

        // 입력 유효성 검증
        if (keyword == null || keyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        // 검색어 길이 제한 (너무 긴 검색어 방지)
        if (keyword.length() > 100) {
            return ResponseEntity.badRequest().build();
        }

        // limit 범위 검증
        if (limit <= 0 || limit > 50) {
            limit = 10;
        }

        // 위경도가 제공되었을 때 범위 검증
        if (latitude != null && longitude != null) {
            if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 ||
                    latitude.compareTo(BigDecimal.valueOf(90)) > 0 ||
                    longitude.compareTo(BigDecimal.valueOf(-180)) < 0 ||
                    longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
                return ResponseEntity.badRequest().build();
            }
        }

        List<TouristSpotLocationDto> searchResults = touristSpotService.searchByKeyword(
                keyword, latitude, longitude, limit);

        if (latitude != null && longitude != null) {
            log.info("키워드 '{}' 검색 결과 {}개 반환 (거리순 정렬)", keyword, searchResults.size());
        } else {
            log.info("키워드 '{}' 검색 결과 {}개 반환 (이름순 정렬)", keyword, searchResults.size());
        }

        return ResponseEntity.ok(searchResults);
    }

    // contentId 기반 상세 정보 조회
    @GetMapping("/detail")
    public ResponseEntity<TouristSpotDetailDto> getDetailByContentId(
            @RequestParam String contentId,
            @RequestParam(required = false) BigDecimal latitude,
            @RequestParam(required = false) BigDecimal longitude) {

        // 입력 유효성 검증
        if (contentId == null || contentId.trim().isEmpty()) {
            log.warn("contentId 파라미터가 비어있음");
            return ResponseEntity.badRequest().build();
        }

        // contentId 길이 제한
        if (contentId.length() > 100) {
            log.warn("contentId가 너무 긺: {}", contentId);
            return ResponseEntity.badRequest().build();
        }

        Optional<TouristSpotDetailDto> detail = touristSpotService.findDetailByContentId(contentId, latitude, longitude);

        if (detail.isEmpty()) {
            log.info("contentId '{}'에 해당하는 데이터를 찾을 수 없음", contentId);
            return ResponseEntity.notFound().build();
        }

        log.info("contentId '{}' 상세 정보 반환 완료", contentId);
        return ResponseEntity.ok(detail.get());
    }

}