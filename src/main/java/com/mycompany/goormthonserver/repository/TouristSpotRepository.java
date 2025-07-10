package com.mycompany.goormthonserver.repository;

import com.mycompany.goormthonserver.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 관광지 레포지토리
 * 위치 기반 검색 및 관광지 정보 조회
 */
@Repository
public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {

    /**
     * 외부 API ID로 관광지 조회
     */
    Optional<TouristSpot> findByExternalId(String externalId);

    /**
     * 카테고리별 관광지 조회
     */
    List<TouristSpot> findByCategoryOrderByCreatedAtDesc(String category);

    /**
     * 이름으로 관광지 검색 (LIKE 검색)
     */
    List<TouristSpot> findByNameContainingIgnoreCaseOrderByName(String name);

    /**
     * 이름 또는 주소로 관광지 검색
     */
    @Query("SELECT t FROM TouristSpot t " +
            "WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(t.address) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "ORDER BY t.name")
    List<TouristSpot> findByNameOrAddressContaining(@Param("keyword") String keyword);

    /**
     * 위치 기반 반경 내 관광지 조회 (Haversine 공식 사용)
     * @param latitude 위도
     * @param longitude 경도
     * @param radiusKm 반경 (km)
     */
    @Query(value = """
        SELECT * FROM tourist_spots t
        WHERE (
            6371 * ACOS(
                COS(RADIANS(:latitude)) * 
                COS(RADIANS(t.latitude)) * 
                COS(RADIANS(t.longitude) - RADIANS(:longitude)) + 
                SIN(RADIANS(:latitude)) * 
                SIN(RADIANS(t.latitude))
            )
        ) <= :radiusKm
        ORDER BY (
            6371 * ACOS(
                COS(RADIANS(:latitude)) * 
                COS(RADIANS(t.latitude)) * 
                COS(RADIANS(t.longitude) - RADIANS(:longitude)) + 
                SIN(RADIANS(:latitude)) * 
                SIN(RADIANS(t.latitude))
            )
        )
        """, nativeQuery = true)
    List<TouristSpot> findSpotsWithinRadius(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") double radiusKm
    );

    /**
     * 위치 기반 가장 가까운 관광지 N개 조회
     */
    @Query(value = """
        SELECT * FROM tourist_spots t
        ORDER BY (
            6371 * ACOS(
                COS(RADIANS(:latitude)) * 
                COS(RADIANS(t.latitude)) * 
                COS(RADIANS(t.longitude) - RADIANS(:longitude)) + 
                SIN(RADIANS(:latitude)) * 
                SIN(RADIANS(t.latitude))
            )
        )
        LIMIT :limit
        """, nativeQuery = true)
    List<TouristSpot> findNearestSpots(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("limit") int limit
    );

    /**
     * 카테고리별 위치 기반 검색
     */
    @Query(value = """
        SELECT * FROM tourist_spots t
        WHERE t.category = :category
        AND (
            6371 * ACOS(
                COS(RADIANS(:latitude)) * 
                COS(RADIANS(t.latitude)) * 
                COS(RADIANS(t.longitude) - RADIANS(:longitude)) + 
                SIN(RADIANS(:latitude)) * 
                SIN(RADIANS(t.latitude))
            )
        ) <= :radiusKm
        ORDER BY (
            6371 * ACOS(
                COS(RADIANS(:latitude)) * 
                COS(RADIANS(t.latitude)) * 
                COS(RADIANS(t.longitude) - RADIANS(:longitude)) + 
                SIN(RADIANS(:latitude)) * 
                SIN(RADIANS(t.latitude))
            )
        )
        """, nativeQuery = true)
    List<TouristSpot> findSpotsByCategoryWithinRadius(
            @Param("category") String category,
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") double radiusKm
    );

    /**
     * 전체 카테고리 목록 조회
     */
    @Query("SELECT DISTINCT t.category FROM TouristSpot t WHERE t.category IS NOT NULL ORDER BY t.category")
    List<String> findAllCategories();

    /**
     * 오디오 콘텐츠가 있는 관광지만 조회
     */
    @Query("SELECT DISTINCT t FROM TouristSpot t " +
            "JOIN t.audioContents ac " +
            "WHERE ac.generationStatus = 'COMPLETED'")
    List<TouristSpot> findSpotsWithCompletedAudio();

    /**
     * 인기 관광지 조회 (수집 횟수 기준)
     */
    @Query("SELECT t FROM TouristSpot t " +
            "LEFT JOIN t.userCollections uc " +
            "GROUP BY t " +
            "ORDER BY COUNT(uc) DESC")
    List<TouristSpot> findPopularSpots();

    /**
     * 외부 ID 존재 여부 확인
     */
    boolean existsByExternalId(String externalId);

    /**
     * 위치 기반 관광지 개수 조회
     */
    @Query(value = """
        SELECT COUNT(*) FROM tourist_spots t
        WHERE (
            6371 * ACOS(
                COS(RADIANS(:latitude)) * 
                COS(RADIANS(t.latitude)) * 
                COS(RADIANS(t.longitude) - RADIANS(:longitude)) + 
                SIN(RADIANS(:latitude)) * 
                SIN(RADIANS(t.latitude))
            )
        ) <= :radiusKm
        """, nativeQuery = true)
    long countSpotsWithinRadius(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radiusKm") double radiusKm
    );
}