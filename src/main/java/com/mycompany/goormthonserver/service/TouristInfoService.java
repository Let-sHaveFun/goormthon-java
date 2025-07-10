package com.mycompany.goormthonserver.service;

import com.mycompany.goormthonserver.dto.VisitJejuResponseDto;
import com.mycompany.goormthonserver.entity.TouristSpot;
import com.mycompany.goormthonserver.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 관광지 정보 통합 서비스
 * Redis → MySQL → 비짓제주 API 순서로 데이터 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TouristInfoService {

    private final TouristSpotRepository touristSpotRepository;
    private final RedisCacheService redisCacheService;
    private final MySqlCacheService mySqlCacheService;
    private final VisitJejuApiClient visitJejuApiClient;

    // 기본 검색 반경 (km)
    private static final double DEFAULT_RADIUS_KM = 1.0;

    /**
     * 위경도로 관광지 정보 조회 (메인 메서드)
     *
     * @param latitude 위도
     * @param longitude 경도
     * @param radiusKm 검색 반경 (km), null이면 기본값 1km
     * @return 관광지 정보 (캐시 또는 API에서 조회)
     */
    public Optional<VisitJejuResponseDto> getTouristInfoByLocation(
            double latitude, double longitude, Double radiusKm) {

        long startTime = System.currentTimeMillis();

        try {
            log.info("🎯 관광지 정보 조회 시작: ({}, {}) 반경 {}km",
                    latitude, longitude, radiusKm != null ? radiusKm : DEFAULT_RADIUS_KM);

            // 1. 위치로 가장 가까운 관광지 찾기
            Optional<TouristSpot> nearestSpot = findNearestTouristSpot(latitude, longitude, radiusKm);

            if (nearestSpot.isEmpty()) {
                log.warn("⚠️ 해당 위치 주변에 관광지가 없습니다: ({}, {})", latitude, longitude);
                return Optional.empty();
            }

            TouristSpot touristSpot = nearestSpot.get();
            String externalId = touristSpot.getExternalId();

            log.info("📍 가장 가까운 관광지 발견: {} (ID: {})", touristSpot.getName(), externalId);

            // 2. 캐시 및 API 조회 (Redis → MySQL → API 순서)
            Optional<VisitJejuResponseDto> result = getDetailInfoWithCache(touristSpot);

            if (result.isPresent()) {
                long responseTime = System.currentTimeMillis() - startTime;
                result.get().setResponseTime(responseTime);

                log.info("✅ 관광지 정보 조회 완료: {} ({}ms, 소스: {})",
                        result.get().getTitle(), responseTime, result.get().getSource());
            }

            return result;

        } catch (Exception e) {
            log.error("❌ 관광지 정보 조회 실패: ({}, {}) - {}", latitude, longitude, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 외부 ID로 직접 관광지 정보 조회
     */
    public Optional<VisitJejuResponseDto> getTouristInfoByExternalId(String externalId) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("🔍 외부 ID로 관광지 정보 조회: {}", externalId);

            // 관광지 엔티티 조회
            Optional<TouristSpot> spotOpt = touristSpotRepository.findByExternalId(externalId);
            if (spotOpt.isEmpty()) {
                log.warn("⚠️ 해당 외부 ID의 관광지가 없습니다: {}", externalId);
                return Optional.empty();
            }

            // 캐시 및 API 조회
            Optional<VisitJejuResponseDto> result = getDetailInfoWithCache(spotOpt.get());

            if (result.isPresent()) {
                long responseTime = System.currentTimeMillis() - startTime;
                result.get().setResponseTime(responseTime);

                log.info("✅ 관광지 정보 조회 완료: {} ({}ms, 소스: {})",
                        result.get().getTitle(), responseTime, result.get().getSource());
            }

            return result;

        } catch (Exception e) {
            log.error("❌ 관광지 정보 조회 실패 (외부 ID: {}): {}", externalId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 캐시 및 API를 통한 상세 정보 조회
     * Redis → MySQL → API 순서
     */
    private Optional<VisitJejuResponseDto> getDetailInfoWithCache(TouristSpot touristSpot) {
        String externalId = touristSpot.getExternalId();

        // 1단계: Redis 캐시 확인
        log.debug("🔍 1단계: Redis 캐시 확인...");
        Optional<VisitJejuResponseDto> redisResult = redisCacheService.getByExternalId(externalId);
        if (redisResult.isPresent()) {
            log.info("⚡ Redis 캐시에서 조회 성공: {}", externalId);
            return redisResult;
        }

        // 2단계: MySQL 캐시 확인
        log.debug("🔍 2단계: MySQL 캐시 확인...");
        Optional<VisitJejuResponseDto> mysqlResult = mySqlCacheService.getValidByExternalId(externalId);
        if (mysqlResult.isPresent()) {
            log.info("🗄️ MySQL 캐시에서 조회 성공: {}", externalId);

            // Redis에 캐시 저장 (다음 조회 속도 향상)
            redisCacheService.saveByExternalId(externalId, mysqlResult.get());

            return mysqlResult;
        }

        // 3단계: 비짓제주 API 호출
        log.debug("🔍 3단계: 비짓제주 API 호출...");
        Optional<VisitJejuResponseDto> apiResult = visitJejuApiClient.getContentById(externalId);
        if (apiResult.isPresent()) {
            log.info("🌐 비짓제주 API에서 조회 성공: {}", externalId);

            VisitJejuResponseDto data = apiResult.get();

            // 캐시에 저장 (Redis와 MySQL 모두)
            saveToAllCaches(touristSpot, data);

            return Optional.of(data);
        }

        log.warn("❌ 모든 소스에서 데이터를 찾을 수 없습니다: {}", externalId);
        return Optional.empty();
    }

    /**
     * 모든 캐시에 데이터 저장
     */
    private void saveToAllCaches(TouristSpot touristSpot, VisitJejuResponseDto data) {
        try {
            // Redis 캐시 저장
            redisCacheService.saveByExternalId(data.getContentsId(), data);

            // 위치 매핑 캐시 저장
            if (touristSpot.getLatitude() != null && touristSpot.getLongitude() != null) {
                redisCacheService.saveLocationMapping(
                        touristSpot.getLatitude().doubleValue(),
                        touristSpot.getLongitude().doubleValue(),
                        data.getContentsId()
                );
            }

            // MySQL 캐시 저장
            mySqlCacheService.saveOrUpdate(touristSpot, data);

            log.info("💾 캐시 저장 완료: {}", data.getContentsId());

        } catch (Exception e) {
            log.error("❌ 캐시 저장 실패: {} - {}", data.getContentsId(), e.getMessage());
        }
    }

    /**
     * 위치 기반 가장 가까운 관광지 찾기
     */
    private Optional<TouristSpot> findNearestTouristSpot(double latitude, double longitude, Double radiusKm) {
        try {
            double searchRadius = radiusKm != null ? radiusKm : DEFAULT_RADIUS_KM;

            // 1. 반경 내 관광지 검색
            List<TouristSpot> nearbySpots = touristSpotRepository.findSpotsWithinRadius(
                    BigDecimal.valueOf(latitude),
                    BigDecimal.valueOf(longitude),
                    searchRadius
            );

            if (!nearbySpots.isEmpty()) {
                log.info("📍 반경 {}km 내 관광지 {}개 발견", searchRadius, nearbySpots.size());
                return Optional.of(nearbySpots.get(0)); // 가장 가까운 것 반환
            }

            // 2. 반경 내에 없으면 가장 가까운 관광지 1개 조회
            List<TouristSpot> nearestSpots = touristSpotRepository.findNearestSpots(
                    BigDecimal.valueOf(latitude),
                    BigDecimal.valueOf(longitude),
                    1
            );

            if (!nearestSpots.isEmpty()) {
                TouristSpot nearest = nearestSpots.get(0);
                double distance = nearest.calculateDistance(
                        BigDecimal.valueOf(latitude),
                        BigDecimal.valueOf(longitude)
                );

                log.info("📍 가장 가까운 관광지: {} (거리: {:.2f}km)", nearest.getName(), distance);
                return Optional.of(nearest);
            }

            return Optional.empty();

        } catch (Exception e) {
            log.error("❌ 관광지 검색 실패: ({}, {}) - {}", latitude, longitude, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 캐시 통계 조회
     */
    public CacheStatistics getCacheStatistics() {
        try {
            RedisCacheService.CacheStats redisStats = redisCacheService.getCacheStats();
            MySqlCacheService.CacheStatistics mysqlStats = mySqlCacheService.getCacheStatistics();

            return CacheStatistics.builder()
                    .redisStats(redisStats)
                    .mysqlStats(mysqlStats)
                    .totalTouristSpots(touristSpotRepository.count())
                    .build();

        } catch (Exception e) {
            log.error("❌ 캐시 통계 조회 실패: {}", e.getMessage());
            return CacheStatistics.builder().build();
        }
    }

    /**
     * 캐시 정리 (관리용)
     */
    public CacheCleanupResult cleanupCaches() {
        try {
            log.info("🧹 캐시 정리 시작...");

            // MySQL 만료 캐시 정리
            int expiredCount = mySqlCacheService.cleanupExpiredCaches();
            int oldCount = mySqlCacheService.cleanupOldCaches();

            log.info("✅ 캐시 정리 완료: 만료 {}개, 오래된 {}개", expiredCount, oldCount);

            return CacheCleanupResult.builder()
                    .expiredCachesCleaned(expiredCount)
                    .oldCachesCleaned(oldCount)
                    .totalCleaned(expiredCount + oldCount)
                    .build();

        } catch (Exception e) {
            log.error("❌ 캐시 정리 실패: {}", e.getMessage());
            return CacheCleanupResult.builder().build();
        }
    }

    /**
     * 캐시 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheStatistics {
        private RedisCacheService.CacheStats redisStats;
        private MySqlCacheService.CacheStatistics mysqlStats;
        private Long totalTouristSpots;
    }

    /**
     * 캐시 정리 결과 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheCleanupResult {
        private int expiredCachesCleaned;
        private int oldCachesCleaned;
        private int totalCleaned;
    }
}