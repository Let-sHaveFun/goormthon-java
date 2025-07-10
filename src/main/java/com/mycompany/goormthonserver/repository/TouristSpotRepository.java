package com.mycompany.goormthonserver.repository;

import com.mycompany.goormthonserver.entity.TouristSpot;  // 이 부분 수정
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
//...
@Repository
public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {

    // 기존 위경도 기반 검색
    @Query(value = """
        SELECT id, external_id, name, address, latitude, longitude, 
               description, category, tag, introduction, imgpath, script,
               created_at, updated_at,
               (6371 * acos(
                   cos(radians(:latitude)) * cos(radians(latitude)) * 
                   cos(radians(longitude) - radians(:longitude)) + 
                   sin(radians(:latitude)) * sin(radians(latitude))
               )) AS distance
        FROM tourist_spots
        HAVING distance <= :radius
        ORDER BY distance
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findNearbyTouristSpots(
            @Param("latitude") BigDecimal latitude,
            @Param("longitude") BigDecimal longitude,
            @Param("radius") double radius,
            @Param("limit") int limit
    );

    // 키워드 검색 (이름 기반) - 거리 계산 포함
    @Query(value = """
        SELECT id, external_id, name, address, latitude, longitude, 
               description, category, tag, introduction, imgpath, script, `audioUrl`,
               created_at, updated_at,
               (6371 * acos(
                   cos(radians(:userLatitude)) * cos(radians(latitude)) * 
                   cos(radians(longitude) - radians(:userLongitude)) + 
                   sin(radians(:userLatitude)) * sin(radians(latitude))
               )) AS distance
        FROM tourist_spots 
        WHERE name LIKE CONCAT('%', :keyword, '%')
        ORDER BY distance, name
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findByNameContainingWithDistance(
            @Param("keyword") String keyword,
            @Param("userLatitude") BigDecimal userLatitude,
            @Param("userLongitude") BigDecimal userLongitude,
            @Param("limit") int limit
    );

    // 키워드 검색 (거리 정보 없는 버전) - 사용자 위치를 모를 때
    @Query(value = """
        SELECT id, external_id, name, address, latitude, longitude, 
               description, category, tag, introduction, imgpath, script, `audioUrl`,
               created_at, updated_at,
               0.0 AS distance
        FROM tourist_spots 
        WHERE name LIKE CONCAT('%', :keyword, '%')
        ORDER BY 
            CASE 
                WHEN name LIKE CONCAT(:keyword, '%') THEN 1
                WHEN name LIKE CONCAT('%', :keyword, '%') THEN 2
                ELSE 3
            END,
            name
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findByNameContaining(
            @Param("keyword") String keyword,
            @Param("limit") int limit
    );
}
