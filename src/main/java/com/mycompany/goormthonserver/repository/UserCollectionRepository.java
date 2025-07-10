package com.mycompany.goormthonserver.repository;

import com.mycompany.goormthonserver.entity.UserCollection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 사용자 수집 레포지토리
 * 조각 수집 게임 관련 데이터 관리
 */
@Repository
public interface UserCollectionRepository extends JpaRepository<UserCollection, Long> {

    /**
     * 사용자별 수집 현황 조회
     */
    List<UserCollection> findByUserIdOrderByCollectedAtDesc(String userId);

    /**
     * 사용자가 특정 관광지를 수집했는지 확인
     */
    Optional<UserCollection> findByUserIdAndTouristSpotId(String userId, Long touristSpotId);

    /**
     * 사용자가 특정 관광지를 수집했는지 확인 (boolean)
     */
    boolean existsByUserIdAndTouristSpotId(String userId, Long touristSpotId);

    /**
     * 사용자별 수집 개수
     */
    @Query("SELECT COUNT(uc) FROM UserCollection uc WHERE uc.userId = :userId")
    long countByUserId(@Param("userId") String userId);

    /**
     * 관광지별 수집 횟수
     */
    @Query("SELECT COUNT(uc) FROM UserCollection uc WHERE uc.touristSpot.id = :spotId")
    long countByTouristSpotId(@Param("spotId") Long spotId);

    /**
     * 사용자별 오늘 수집한 관광지
     */
    @Query("SELECT uc FROM UserCollection uc " +
            "WHERE uc.userId = :userId " +
            "AND DATE(uc.collectedAt) = DATE(:today) " +
            "ORDER BY uc.collectedAt DESC")
    List<UserCollection> findTodayCollectionsByUserId(
            @Param("userId") String userId,
            @Param("today") LocalDateTime today);

    /**
     * 특정 기간 동안의 사용자 수집 현황
     */
    @Query("SELECT uc FROM UserCollection uc " +
            "WHERE uc.userId = :userId " +
            "AND uc.collectedAt >= :startDate " +
            "AND uc.collectedAt <= :endDate " +
            "ORDER BY uc.collectedAt DESC")
    List<UserCollection> findByUserIdAndCollectedAtBetween(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * 인기 관광지 순위 (수집 횟수 기준)
     */
    @Query("SELECT uc.touristSpot, COUNT(uc) as collectionCount FROM UserCollection uc " +
            "GROUP BY uc.touristSpot " +
            "ORDER BY COUNT(uc) DESC")
    List<Object[]> findPopularTouristSpots();

    /**
     * 최근 수집된 관광지들
     */
    @Query("SELECT uc FROM UserCollection uc " +
            "ORDER BY uc.collectedAt DESC")
    List<UserCollection> findRecentCollections();

    /**
     * 사용자별 수집 완료율 계산용 데이터 (간소화)
     */
    @Query("SELECT COUNT(uc) as collectedCount FROM UserCollection uc WHERE uc.userId = :userId")
    Long getUserCollectionCount(@Param("userId") String userId);

    /**
     * 전체 관광지 수 조회
     */
    @Query("SELECT COUNT(t) FROM TouristSpot t")
    Long getTotalTouristSpotCount();

    /**
     * 오디오와 함께 수집된 관광지
     */
    List<UserCollection> findByUserIdAndAudioContentIsNotNullOrderByCollectedAtDesc(String userId);

    /**
     * 특정 오디오 콘텐츠로 수집한 사용자 수
     */
    @Query("SELECT COUNT(DISTINCT uc.userId) FROM UserCollection uc " +
            "WHERE uc.audioContent.id = :audioContentId")
    long countDistinctUsersByAudioContentId(@Param("audioContentId") Long audioContentId);

    /**
     * 사용자별 수집 통계 (일별)
     */
    @Query("SELECT DATE(uc.collectedAt) as collectionDate, COUNT(uc) as dailyCount " +
            "FROM UserCollection uc " +
            "WHERE uc.userId = :userId " +
            "AND uc.collectedAt >= :startDate " +
            "GROUP BY DATE(uc.collectedAt) " +
            "ORDER BY DATE(uc.collectedAt) DESC")
    List<Object[]> getUserDailyCollectionStats(
            @Param("userId") String userId,
            @Param("startDate") LocalDateTime startDate);

    /**
     * 전체 사용자 수집 통계 (간소화 버전)
     */
    @Query("SELECT COUNT(DISTINCT uc.userId) as totalUsers, COUNT(uc) as totalCollections " +
            "FROM UserCollection uc")
    Object[] getOverallCollectionStatistics();

    /**
     * 특정 날짜 이후 수집하지 않은 사용자들
     */
    @Query("SELECT DISTINCT uc.userId FROM UserCollection uc " +
            "WHERE uc.userId NOT IN (" +
            "    SELECT uc2.userId FROM UserCollection uc2 " +
            "    WHERE uc2.collectedAt >= :sinceDate" +
            ")")
    List<String> findInactiveUsersSince(@Param("sinceDate") LocalDateTime sinceDate);
}