// 수정된 Service 클래스
package com.mycompany.goormthonserver.service;

import com.mycompany.goormthonserver.dto.TouristSpotLocationDto;
import com.mycompany.goormthonserver.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TouristSpotService {

    private final TouristSpotRepository touristSpotRepository;

    public List<TouristSpotLocationDto> findNearbyTouristSpots(
            BigDecimal latitude, BigDecimal longitude, double radius, int limit) {

        log.info("위경도 기반 관광지 조회 - lat: {}, lng: {}, radius: {}km, limit: {}",
                latitude, longitude, radius, limit);

        List<Object[]> results = touristSpotRepository.findNearbyTouristSpots(
                latitude, longitude, radius, limit);

        return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TouristSpotLocationDto convertToDto(Object[] row) {
        return TouristSpotLocationDto.builder()
                .externalId((String) row[1])  // external_id
                .name((String) row[2])        // name
                .address((String) row[3])     // address
                .latitude((BigDecimal) row[4]) // latitude
                .longitude((BigDecimal) row[5]) // longitude
                .description((String) row[6]) // description
                .category((String) row[7])    // category
                .tag((String) row[8])         // tag
                .introduction((String) row[9]) // introduction
                .imgPath((String) row[10])    // imgpath
                .distance(((Number) row[14]).doubleValue()) // distance (calculated)
                .build();
    }
}
