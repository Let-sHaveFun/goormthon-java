package com.mycompany.goormthonserver.service;

import com.mycompany.goormthonserver.dto.VisitJejuResponseDto;
import com.mycompany.goormthonserver.entity.TouristSpot;
import com.mycompany.goormthonserver.entity.VisitJejuCache;
import com.mycompany.goormthonserver.repository.VisitJejuCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * MySQL 캐시 서비스 (2차 저장소)
 * Redis 캐시 실패시 백업 역할
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MySqlCacheService {

    private final VisitJejuCacheRepository cacheRepository;

    /**
     * 외부 ID로 유효한 캐시 조회
     */
    public Optional<VisitJejuResponseDto> getValidByExternalId(String externalId) {
        try {
            Optional<VisitJejuCache> cacheOpt = cacheRepository.findValidByExternalId(
                    externalId, LocalDateTime.now());

            if (cacheOpt.isPresent()) {
                log.info("✅ MySQL 캐시 히트: {}", externalId);
                VisitJejuResponseDto dto = cacheOpt.get().toDto();
                dto.setSource(VisitJejuResponseDto.CacheSource.MYSQL);
                return Optional.of(dto);
            }

            log.info("❌ MySQL 캐시 미스: {}", externalId);
            return Optional.empty();

        } catch (Exception e) {
            log.error("❌ MySQL 캐시 조회 오류: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 관광지 ID로 유효한 캐시 조회
     */
    public Optional<VisitJejuResponseDto> getValidByTouristSpotId(Long touristSpotId) {
        try {
            Optional<VisitJejuCache> cacheOpt = cacheRepository.findValidByTouristSpotId(
                    touristSpotId, LocalDateTime.now());

            if (cacheOpt.isPresent()) {
                log.info("✅ MySQL 캐시 히트 (관광지 ID: {})", touristSpotId);
                VisitJejuResponseDto dto = cacheOpt.get().toDto();
                dto.setSource(VisitJejuResponseDto.CacheSource.MYSQL);
                return Optional.of(dto);
            }

            log.info("❌ MySQL 캐시 미스 (관광지 ID: {})", touristSpotId);
            return Optional.empty();

        } catch (Exception e) {
            log.error("❌ MySQL 캐시 조회 오류 (관광지 ID: {}): {}", touristSpotId, e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 캐시 저장 또는 업데이트
     */
    @Transactional
    public void saveOrUpdate(TouristSpot touristSpot, VisitJejuResponseDto data) {
        try {
            // 기존 캐시 확인
            Optional<VisitJejuCache> existingCache = cacheRepository.findByExternalId(data.getContentsId());

            if (existingCache.isPresent()) {
                // 기존 캐시 업데이트
                VisitJejuCache cache = existingCache.get();
                updateCacheData(cache, data);
                cache.refresh(); // 만료시간 갱신
                cacheRepository.save(cache);
                log.info("✅ MySQL 캐시 업데이트: {}", data.getContentsId());
            } else {
                // 새 캐시 생성
                VisitJejuCache newCache = VisitJejuCache.fromDto(touristSpot, data);
                cacheRepository.save(newCache);
                log.info("✅ MySQL 캐시 저장: {}", data.getContentsId());
            }

        } catch (Exception e) {
            log.error("❌ MySQL 캐시 저장 오류: {}", e.getMessage());
        }
    }

    /**
     * 캐시 데이터 업데이트
     */
    private void updateCacheData(VisitJejuCache cache, VisitJejuResponseDto data) {
        cache.setTitle(data.getTitle());
        cache.setIntroduction(data.getIntroduction());
        cache.setTag(data.getTag());
        cache.setAddress(data.getAddress());
        cache.setPhotoId(data.getPhotoId());
        cache.setImgPath(data.getImgPath());
    }

    /**
     * 특정 캐시 삭제
     */
    @Transactional
    public void deleteByExternalId(String externalId) {
        try {
            Optional<VisitJejuCache> cacheOpt = cacheRepository.findByExternalId(externalId);
            if (cacheOpt.isPresent()) {
                cacheRepository.delete(cacheOpt.get());
                log.info("✅ MySQL 캐시 삭제: {}", externalId);
            } else {
                log.warn("⚠️ MySQL 캐시 삭제 실패 (없음): {}", externalId);
            }
        } catch (Exception e) {
            log.error("❌ MySQL 캐시 삭제 오류: {}", e.getMessage());
        }
    }

    /**
     * 만료된 캐시 정리
     */
    @Transactional
    public int cleanupExpiredCaches() {
        try {
            int deletedCount = cacheRepository.deleteExpiredCaches(LocalDateTime.now());
            log.info("✅ 만료된 MySQL 캐시 정리 완료: {}개", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("❌ 만료된 캐시 정리 오류: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 오래된 캐시 정리 (7일 이전)
     */
    @Transactional
    public int cleanupOldCaches() {
        try {
            LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
            int deletedCount = cacheRepository.deleteOldCaches(sevenDaysAgo);
            log.info("✅ 오래된 MySQL 캐시 정리 완료: {}개", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("❌ 오래된 캐시 정리 오류: {}", e.getMessage());
            return 0;
        }
    }

    /**
     * 캐시 통계 조회
     */
    public CacheStatistics getCacheStatistics() {
        try {
            Object[] stats = cacheRepository.getCacheStatistics(LocalDateTime.now());
            if (stats != null && stats.length >= 3) {
                return CacheStatistics.builder()
                        .totalCount(((Number) stats[0]).longValue())
                        .validCount(((Number) stats[1]).longValue())
                        .expiredCount(((Number) stats[2]).longValue())
                        .build();
            }
        } catch (Exception e) {
            log.error("❌ 캐시 통계 조회 오류: {}", e.getMessage());
        }

        return CacheStatistics.builder()
                .totalCount(0L)
                .validCount(0L)
                .expiredCount(0L)
                .build();
    }

    /**
     * 키워드로 캐시 검색
     */
    public List<VisitJejuResponseDto> searchByKeyword(String keyword) {
        try {
            List<VisitJejuCache> caches = cacheRepository.searchByKeyword(keyword, LocalDateTime.now());
            return caches.stream()
                    .map(VisitJejuCache::toDto)
                    .toList();
        } catch (Exception e) {
            log.error("❌ 캐시 검색 오류: {}", e.getMessage());
            return List.of();
        }
    }

    /**
     * 캐시 존재 여부 확인
     */
    public boolean existsByExternalId(String externalId) {
        return cacheRepository.existsByExternalId(externalId);
    }

    /**
     * 캐시 통계 DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class CacheStatistics {
        private Long totalCount;   // 전체 캐시 개수
        private Long validCount;   // 유효한 캐시 개수
        private Long expiredCount; // 만료된 캐시 개수

        public double getValidRatio() {
            if (totalCount == 0) return 0.0;
            return (double) validCount / totalCount * 100;
        }
    }
}