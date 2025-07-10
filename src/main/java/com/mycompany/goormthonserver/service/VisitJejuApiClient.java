package com.mycompany.goormthonserver.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.goormthonserver.dto.VisitJejuResponseDto;
import com.mycompany.goormthonserver.dto.visitjeju.VisitJejuApiResponse;
import com.mycompany.goormthonserver.dto.visitjeju.VisitJejuItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * 비짓제주 API 클라이언트
 * 외부 API 호출 및 데이터 변환
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VisitJejuApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${JEJU_VISIT_API_URL:}")
    private String apiBaseUrl;

    @Value("${JEJU_VISIT_API_KEY:}")
    private String apiKey;

    @Value("${external-api.jeju-visit.timeout:10000}")
    private int timeoutMs;

    /**
     * 콘텐츠 ID로 관광지 정보 조회
     */
    public Optional<VisitJejuResponseDto> getContentById(String contentsId) {
        try {
            log.info("🌐 비짓제주 API 호출 시작: {}", contentsId);
            long startTime = System.currentTimeMillis();

            // 완전한 URL 구성
            String fullUrl = apiBaseUrl + "/vsjApi/contents/searchList";

            // API 호출
            VisitJejuApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("api.visitjeju.net")
                            .path("/vsjApi/contents/searchList")
                            .queryParam("apiKey", apiKey)
                            .queryParam("locale", "kr")
                            .queryParam("page", 1)
                            .queryParam("cid", contentsId)
                            .build())
                    .retrieve()
                    .bodyToMono(VisitJejuApiResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .doOnError(error -> log.error("❌ 비짓제주 API 호출 중 오류: {}", error.getMessage()))
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        log.error("❌ 비짓제주 API HTTP 오류: {} - {}", ex.getStatusCode(), ex.getMessage());
                        log.error("❌ 응답 본문: {}", ex.getResponseBodyAsString());
                        return Mono.empty();
                    })
                    .block();

            long responseTime = System.currentTimeMillis() - startTime;

            if (response != null && "200".equals(response.getResult()) &&
                    response.getItems() != null && !response.getItems().isEmpty()) {

                // 첫 번째 아이템 변환
                VisitJejuItem item = response.getItems().get(0);
                VisitJejuResponseDto dto = convertToDto(item);
                dto.setSource(VisitJejuResponseDto.CacheSource.API);
                dto.setResponseTime(responseTime);

                log.info("✅ 비짓제주 API 응답 성공: {} ({}ms)", contentsId, responseTime);
                return Optional.of(dto);
            }

            log.warn("⚠️ 비짓제주 API 응답 없음: {} ({}ms)", contentsId, responseTime);
            return Optional.empty();

        } catch (Exception e) {
            log.error("❌ 비짓제주 API 호출 실패: {} - {}", contentsId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * 검색어로 관광지 목록 조회
     */
    public List<VisitJejuResponseDto> searchContents(String keyword, int page, int size) {
        try {
            log.info("🔍 비짓제주 API 검색: {} (페이지: {})", keyword, page);

            VisitJejuApiResponse response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("api.visitjeju.net")
                            .path("/vsjApi/contents/searchList")
                            .queryParam("apiKey", apiKey)
                            .queryParam("locale", "kr")
                            .queryParam("page", page)
                            .queryParam("pageSize", size)
                            .queryParam("q", keyword)
                            .build())
                    .retrieve()
                    .bodyToMono(VisitJejuApiResponse.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            if (response != null && "200".equals(response.getResult()) &&
                    response.getItems() != null && !response.getItems().isEmpty()) {

                List<VisitJejuResponseDto> results = response.getItems().stream()
                        .map(this::convertToDto)
                        .peek(dto -> dto.setSource(VisitJejuResponseDto.CacheSource.API))
                        .toList();

                log.info("✅ 비짓제주 API 검색 성공: {} ({}개)", keyword, results.size());
                return results;
            }

            log.warn("⚠️ 비짓제주 API 검색 결과 없음: {}", keyword);
            return List.of();

        } catch (Exception e) {
            log.error("❌ 비짓제주 API 검색 실패: {} - {}", keyword, e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * API 연결 테스트
     */
    public boolean testConnection() {
        try {
            log.info("🔗 비짓제주 API 연결 테스트...");

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("api.visitjeju.net")
                            .path("/vsjApi/contents/searchList")
                            .queryParam("apiKey", apiKey)
                            .queryParam("locale", "kr")
                            .queryParam("page", 1)
                            .queryParam("pageSize", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofMillis(timeoutMs))
                    .block();

            boolean isConnected = response != null && response.contains("\"result\"");

            if (isConnected) {
                log.info("✅ 비짓제주 API 연결 성공");
                log.debug("📄 응답 내용: {}", response);
            } else {
                log.warn("⚠️ 비짓제주 API 연결 실패 - 응답 없음");
            }

            return isConnected;

        } catch (Exception e) {
            log.error("❌ 비짓제주 API 연결 테스트 실패: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * API 키 유효성 검사
     */
    public boolean validateApiKey() {
        if (apiKey == null || apiKey.trim().isEmpty() || "your_key_here".equals(apiKey)) {
            log.warn("⚠️ 비짓제주 API 키가 설정되지 않았습니다. 현재 값: {}",
                    apiKey == null ? "null" : apiKey.length() > 0 ? "****" : "empty");
            return false;
        }
        log.info("✅ API 키 설정 확인: {}****", apiKey.substring(0, Math.min(4, apiKey.length())));
        return testConnection();
    }

    /**
     * API 상태 정보 조회
     */
    public ApiStatus getApiStatus() {
        try {
            boolean hasValidKey = apiKey != null && !apiKey.trim().isEmpty() && !"your_key_here".equals(apiKey);
            boolean connected = hasValidKey && testConnection();

            return ApiStatus.builder()
                    .connected(connected)
                    .baseUrl(apiBaseUrl)
                    .hasApiKey(hasValidKey)
                    .timeout(timeoutMs)
                    .status(connected ? "UP" : "DOWN")
                    .apiKeyMasked(hasValidKey ? apiKey.substring(0, Math.min(4, apiKey.length())) + "****" : "NOT_SET")
                    .build();
        } catch (Exception e) {
            return ApiStatus.builder()
                    .connected(false)
                    .baseUrl(apiBaseUrl)
                    .hasApiKey(false)
                    .timeout(timeoutMs)
                    .status("ERROR")
                    .error(e.getMessage())
                    .apiKeyMasked("ERROR")
                    .build();
        }
    }

    /**
     * API 응답을 DTO로 변환
     */
    private VisitJejuResponseDto convertToDto(VisitJejuItem item) {
        // 사진 정보 추출
        Long photoId = null;
        String imgPath = null;

        if (item.getRepPhoto() != null && item.getRepPhoto().getPhotoid() != null) {
            photoId = item.getRepPhoto().getPhotoid().getPhotoid();
            imgPath = item.getRepPhoto().getPhotoid().getImgpath();
        }

        // 소개 정보 처리 (null 또는 빈 문자열 처리)
        String introduction = item.getIntroduction();
        if (introduction == null || introduction.trim().isEmpty()) {
            introduction = item.getTitle() + "에 대한 상세 정보입니다.";
        }

        // 태그 정보 처리
        String tag = item.getTag();
        if (tag == null || tag.trim().isEmpty()) {
            tag = item.getAlltag(); // 전체 태그에서 추출
            if (tag != null && tag.length() > 100) {
                // 태그가 너무 길면 처음 100자만 사용
                tag = tag.substring(0, 100) + "...";
            }
        }

        // 주소 정보 처리 (도로명주소 우선, 없으면 지번주소)
        String address = item.getRoadaddress();
        if (address == null || address.trim().isEmpty()) {
            address = item.getAddress();
        }

        return VisitJejuResponseDto.builder()
                .contentsId(item.getContentsid())
                .title(item.getTitle())
                .introduction(introduction)
                .tag(tag)
                .address(address)
                .photoId(photoId)
                .imgPath(imgPath)
                .source(VisitJejuResponseDto.CacheSource.API.name())
                .build();
    }

    /**
     * API 상태 정보
     */
    @lombok.Data
    @lombok.Builder
    public static class ApiStatus {
        private boolean connected;
        private String baseUrl;
        private boolean hasApiKey;
        private int timeout;
        private String status;
        private String error;
        private String apiKeyMasked;
    }
}