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
}
