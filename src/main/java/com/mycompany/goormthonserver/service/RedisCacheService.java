package com.mycompany.goormthonserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycompany.goormthonserver.dto.VisitJejuResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Redis 캐시 서비스
 * 비짓제주 API 응답 데이터 캐싱 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // 캐시 키 프리픽스
    private static final String CACHE_KEY_PREFIX = "visitjeju:";
    private static final String LOCATION_KEY_PREFIX = "location:";

    // 캐시 TTL (24시간)
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    /**
     * 외부 ID로 캐시 조회
     */
    public Optional<VisitJejuResponseDto> getByExternalId(String externalId) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + externalId;
            String cachedData = redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                log.info("✅ Redis 캐시 히트: {}", externalId);
                VisitJejuResponseDto dto = objectMapper.readValue(cachedData, VisitJejuResponseDto.class);
                return Optional.of(dto);
            }

            log.info("❌ Redis 캐시 미스: {}", externalId);
            return Optional.empty();

        } catch (JsonProcessingException e) {
            log.error("❌ Redis 캐시 읽기 오류: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 위치 기반 캐시 조회 (위도, 경도로 external_id 찾기)
     */
    public Optional<String> getExternalIdByLocation(double latitude, double longitude) {
        try {
            // 위치를 키로 사용 (소수점 6자리까지)
            String locationKey = LOCATION_KEY_PREFIX +
                    String.format("%.6f:%.6f", latitude, longitude);

            String externalId = redisTemplate.opsForValue().get(locationKey);

            if (externalId != null) {
                log.info("✅ 위치 캐시 히트: ({}, {}) -> {}", latitude, longitude, externalId);
                return Optional.of(externalId);
            }

            log.info("❌ 위치 캐시 미스: ({}, {})", latitude, longitude);
            return Optional.empty();

        } catch (Exception e) {
            log.error("❌ 위치 캐시 읽기 오류: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 캐시 저장 (외부 ID 기준)
     */
    public void saveByExternalId(String externalId, VisitJejuResponseDto data) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + externalId;
            String jsonData = objectMapper.writeValueAsString(data);

            redisTemplate.opsForValue().set(cacheKey, jsonData, CACHE_TTL);
            log.info("✅ Redis 캐시 저장: {} (TTL: 24시간)", externalId);

        } catch (JsonProcessingException e) {
            log.error("❌ Redis 캐시 저장 오류: {}", e.getMessage());
        }
    }

    /**
     * 위치-외부ID 매핑 캐시 저장
     */
    public void saveLocationMapping(double latitude, double longitude, String externalId) {
        try {
            String locationKey = LOCATION_KEY_PREFIX +
                    String.format("%.6f:%.6f", latitude, longitude);

            redisTemplate.opsForValue().set(locationKey, externalId, CACHE_TTL);
            log.info("✅ 위치 매핑 캐시 저장: ({}, {}) -> {}", latitude, longitude, externalId);

        } catch (Exception e) {
            log.error("❌ 위치 매핑 캐시 저장 오류: {}", e.getMessage());
        }
    }

    /**
     * 특정 캐시 삭제
     */
    public void deleteByExternalId(String externalId) {
        try {
            String cacheKey = CACHE_KEY_PREFIX + externalId;
            Boolean deleted = redisTemplate.delete(cacheKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("✅ Redis 캐시 삭제: {}", externalId);
            } else {
                log.warn("⚠️ Redis 캐시 삭제 실패 (키 없음): {}", externalId);
            }

        } catch (Exception e) {
            log.error("❌ Redis 캐시 삭제 오류: {}", e.getMessage());
        }
    }

    /**
     * 위치 매핑 캐시 삭제
     */
    public void deleteLocationMapping(double latitude, double longitude) {
        try {
            String locationKey = LOCATION_KEY_PREFIX +
                    String.format("%.6f:%.6f", latitude, longitude);

            Boolean deleted = redisTemplate.delete(locationKey);

            if (Boolean.TRUE.equals(deleted)) {
                log.info("✅ 위치 매핑 캐시 삭제: ({}, {})", latitude, longitude);
            } else {
                log.warn("⚠️ 위치 매핑 캐시 삭제 실패: ({}, {})", latitude, longitude);
            }

        } catch (Exception e) {
            log.error("❌ 위치 매핑 캐시 삭제 오류: {}", e.getMessage());
        }
    }

    /**
     * 캐시 통계 조회
     */
    public CacheStats getCacheStats() {
        try {
            // 비짓제주 캐시 키 개수
            var visitjejuKeys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            int visitjejuCacheCount = visitjejuKeys != null ? visitjejuKeys.size() : 0;

            // 위치 매핑 캐시 키 개수
            var locationKeys = redisTemplate.keys(LOCATION_KEY_PREFIX + "*");
            int locationCacheCount = locationKeys != null ? locationKeys.size() : 0;

            return CacheStats.builder()
                    .visitjejuCacheCount(visitjejuCacheCount)
                    .locationCacheCount(locationCacheCount)
                    .totalCacheCount(visitjejuCacheCount + locationCacheCount)
                    .build();

        } catch (Exception e) {
            log.error("❌ 캐시 통계 조회 오류: {}", e.getMessage());
            return CacheStats.builder()
                    .visitjejuCacheCount(0)
                    .locationCacheCount(0)
                    .totalCacheCount(0)
                    .build();
        }
    }

    /**
     * 모든 캐시 삭제 (관리용)
     */
    public void clearAllCache() {
        try {
            // 비짓제주 캐시 삭제
            var visitjejuKeys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
            if (visitjejuKeys != null && !visitjejuKeys.isEmpty()) {
                redisTemplate.delete(visitjejuKeys);
                log.info("✅ 비짓제주 캐시 모두 삭제: {}개", visitjejuKeys.size());
            }

            // 위치 매핑 캐시 삭제
            var locationKeys = redisTemplate.keys(LOCATION_KEY_PREFIX + "*");
            if (locationKeys != null && !locationKeys.isEmpty()) {
                redisTemplate.delete(locationKeys);
                log.info("✅ 위치 매핑 캐시 모두 삭제: {}개", locationKeys.size());
            }

        } catch (Exception e) {
            log.error("❌ 전체 캐시 삭제 오류: {}", e.getMessage());
        }
    }

    /**
     * 캐시 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheStats {
        private int visitjejuCacheCount;    // 비짓제주 데이터 캐시 개수
        private int locationCacheCount;     // 위치 매핑 캐시 개수
        private int totalCacheCount;        // 전체 캐시 개수
    }
}