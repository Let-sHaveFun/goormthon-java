package com.mycompany.goormthonserver.repository;

import com.mycompany.goormthonserver.entity.VisitJejuCache;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 비짓제주 API 캐시 레포지토리
 * MySQL 백업 저장소 관리
 */
@Repository
public interface VisitJejuCacheRepository extends JpaRepository<VisitJejuCache, Long> {

    /**
     * 외부 ID로 유효한 캐시 조회
     */
    @Query("SELECT vc FROM VisitJejuCache vc " +
            "WHERE vc.externalId = :externalId " +
            "AND vc.isActive = true " +
            "AND vc.expiresAt > :now")
    Optional<VisitJejuCache> findValidByExternalId(
            @Param("externalId") String externalId,
            @Param("now") LocalDateTime now);

    /**
     * 외부 ID로 캐시 조회 (만료 여부 무관)
     */
    Optional<VisitJejuCache> findByExternalId(String externalId);

    /**
     * 관광지 ID로 유효한 캐시 조회
     */
    @Query("SELECT vc FROM VisitJejuCache vc " +
            "WHERE vc.touristSpot.id = :touristSpotId " +
            "AND vc.isActive = true " +
            "AND vc.expiresAt > :now")
    Optional<VisitJejuCache> findValidByTouristSpotId(
            @Param("touristSpotId") Long touristSpotId,
            @Param("now") LocalDateTime now);

    /**
     * 관광지 ID로 캐시 조회 (만료 여부 무관)
     */
    Optional<VisitJejuCache> findByTouristSpotId(Long touristSpotId);

    /**
     * 만료된 캐시 조회
     */
    @Query("SELECT vc FROM VisitJejuCache vc " +
            "WHERE vc.expiresAt <= :now " +
            "OR vc.isActive = false")
    List<VisitJejuCache> findExpiredCaches(@Param("now") LocalDateTime now);

    /**
     * 유효한 캐시 개수 조회
     */
    @Query("SELECT COUNT(vc) FROM VisitJejuCache vc " +
            "WHERE vc.isActive = true " +
            "AND vc.expiresAt > :now")
    long countValidCaches(@Param("now") LocalDateTime now);

    /**
     * 만료된 캐시 일괄 삭제
     */
    @Modifying
    @Query("DELETE FROM VisitJejuCache vc " +
            "WHERE vc.expiresAt <= :now " +
            "OR vc.isActive = false")
    int deleteExpiredCaches(@Param("now") LocalDateTime now);

    /**
     * 특정 기간 이전 캐시 삭제
     */
    @Modifying
    @Query("DELETE FROM VisitJejuCache vc " +
            "WHERE vc.cachedAt < :beforeDate")
    int deleteOldCaches(@Param("beforeDate") LocalDateTime beforeDate);

    /**
     * 캐시 통계 조회
     */
    @Query("SELECT " +
            "COUNT(vc) as totalCount, " +
            "SUM(CASE WHEN vc.isActive = true AND vc.expiresAt > :now THEN 1 ELSE 0 END) as validCount, " +
            "SUM(CASE WHEN vc.expiresAt <= :now OR vc.isActive = false THEN 1 ELSE 0 END) as expiredCount " +
            "FROM VisitJejuCache vc")
    Object[] getCacheStatistics(@Param("now") LocalDateTime now);

    /**
     * 특정 관광지의 캐시 존재 여부 확인
     */
    boolean existsByTouristSpotId(Long touristSpotId);

    /**
     * 외부 ID로 캐시 존재 여부 확인
     */
    boolean existsByExternalId(String externalId);

    /**
     * 활성 상태인 캐시 목록 조회
     */
    @Query("SELECT vc FROM VisitJejuCache vc " +
            "WHERE vc.isActive = true " +
            "ORDER BY vc.cachedAt DESC")
    List<VisitJejuCache> findActiveCaches();

    /**
     * 최근 캐시된 데이터 조회
     */
    @Query("SELECT vc FROM VisitJejuCache vc " +
            "WHERE vc.isActive = true " +
            "ORDER BY vc.cachedAt DESC")
    List<VisitJejuCache> findRecentCaches();

    /**
     * 특정 키워드가 포함된 캐시 검색
     */
    @Query("SELECT vc FROM VisitJejuCache vc " +
            "WHERE vc.isActive = true " +
            "AND vc.expiresAt > :now " +
            "AND (LOWER(vc.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(vc.tag) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY vc.cachedAt DESC")
    List<VisitJejuCache> searchByKeyword(
            @Param("keyword") String keyword,
            @Param("now") LocalDateTime now);

    /**
     * 캐시 갱신 (만료시간 연장)
     */
    @Modifying
    @Query("UPDATE VisitJejuCache vc " +
            "SET vc.cachedAt = :now, vc.expiresAt = :expiresAt, vc.isActive = true " +
            "WHERE vc.externalId = :externalId")
    int refreshCache(
            @Param("externalId") String externalId,
            @Param("now") LocalDateTime now,
            @Param("expiresAt") LocalDateTime expiresAt);
}