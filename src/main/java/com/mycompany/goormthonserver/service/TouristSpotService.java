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

        // 디버깅을 위한 로그 추가
        if (!results.isEmpty()) {
            Object[] firstRow = results.get(0);
            log.info("첫 번째 행 데이터 개수: {}", firstRow.length);
            for (int i = 0; i < firstRow.length; i++) {
                log.info("Index {}: {} ({})", i, firstRow[i],
                        firstRow[i] != null ? firstRow[i].getClass().getSimpleName() : "null");
            }
        }

        return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private TouristSpotLocationDto convertToDto(Object[] row) {
        try {
            // 올바른 인덱스로 수정
            return TouristSpotLocationDto.builder()
                    .externalId(safeToString(row[0]))       // external_id
                    .name(safeToString(row[1]))             // name
                    .address(safeToString(row[2]))          // address
                    .latitude(safeToBigDecimal(row[3]))     // latitude
                    .longitude(safeToBigDecimal(row[4]))    // longitude
                    .description(safeToString(row[5]))      // description
                    .category(safeToString(row[6]))         // category
                    .tag(safeToString(row[7]))              // tag
                    .introduction(safeToString(row[8]))     // introduction
                    .imgPath(safeToString(row[9]))          // imgpath
                    .distance(safeToDouble(row[14]))        // distance (calculated)
                    .build();
        } catch (Exception e) {
            log.error("DTO 변환 중 오류 발생: {}", e.getMessage());
            log.error("Row 데이터: {}", java.util.Arrays.toString(row));
            throw e;
        }
    }

    // 안전한 타입 변환 헬퍼 메서드들
    private String safeToString(Object obj) {
        return obj != null ? obj.toString() : null;
    }

    private BigDecimal safeToBigDecimal(Object obj) {
        if (obj == null) return null;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Number) return BigDecimal.valueOf(((Number) obj).doubleValue());
        return new BigDecimal(obj.toString());
    }

    private Double safeToDouble(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Double) return (Double) obj;
        if (obj instanceof Number) return ((Number) obj).doubleValue();
        return Double.valueOf(obj.toString());
    }
}