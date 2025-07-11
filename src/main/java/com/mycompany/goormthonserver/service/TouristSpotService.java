// 수정된 Service 클래스
package com.mycompany.goormthonserver.service;

import com.mycompany.goormthonserver.dto.TouristSpotDetailDto;
import com.mycompany.goormthonserver.dto.TouristSpotLocationDto;
import com.mycompany.goormthonserver.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

    // 키워드 검색 (거리 포함)
    public List<TouristSpotLocationDto> searchByKeyword(String keyword,
                                                        BigDecimal userLatitude,
                                                        BigDecimal userLongitude,
                                                        int limit) {

        log.info("키워드 검색 (거리 포함) - keyword: '{}', userLat: {}, userLng: {}, limit: {}",
                keyword, userLatitude, userLongitude, limit);

        // 키워드 유효성 검증
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("검색 키워드가 비어있음");
            return List.of();
        }

        // 키워드 정리 (앞뒤 공백 제거)
        String cleanKeyword = keyword.trim();

        List<Object[]> results;

        // 사용자 위치 정보가 있으면 거리 기준으로 정렬
        if (userLatitude != null && userLongitude != null) {
            results = touristSpotRepository.findByNameContainingWithDistance(
                    cleanKeyword, userLatitude, userLongitude, limit);
            log.info("키워드 '{}' 검색 결과 (거리순): {}개", cleanKeyword, results.size());
        } else {
            // 사용자 위치 정보가 없으면 이름 기준으로 정렬
            results = touristSpotRepository.findByNameContaining(cleanKeyword, limit);
            log.info("키워드 '{}' 검색 결과 (이름순): {}개", cleanKeyword, results.size());
        }

        return results.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 키워드 검색 (거리 정보 없는 버전) - 하위 호환성
    public List<TouristSpotLocationDto> searchByKeyword(String keyword, int limit) {
        return searchByKeyword(keyword, null, null, limit);
    }

    public Optional<TouristSpotDetailDto> findDetailByContentId(String contentId) {

        log.info("contentId 기반 상세 정보 조회 - contentId: '{}'", contentId);

        if (contentId == null || contentId.trim().isEmpty()) {
            log.warn("contentId가 비어있음");
            return Optional.empty();
        }

        List<Object[]> results = touristSpotRepository.findDetailByContentId(contentId.trim());

        if (results.isEmpty()) {
            log.warn("contentId '{}' 에 해당하는 데이터가 없음", contentId);
            return Optional.empty();
        }

        Object[] row = results.get(0);

        try {
            log.info("Row 길이: {}", row.length);
            for (int i = 0; i < row.length; i++) {
                log.info("Index {}: {} ({})", i, row[i],
                        row[i] != null ? row[i].getClass().getSimpleName() : "null");
            }

            TouristSpotDetailDto detail = TouristSpotDetailDto.builder()
                    .imgPath(safeToString(row[0]))    // imgpath
                    .audioUrl(safeToString(row[1]))   // audioUrl
                    .script(safeToString(row[2]))     // script
                    .build();

            log.info("contentId '{}' 상세 정보 조회 완료", contentId);
            return Optional.of(detail);

        } catch (Exception e) {
            log.error("DTO 변환 중 오류 발생: {}", e.getMessage());
            log.error("Row 데이터: {}", java.util.Arrays.toString(row));
            return Optional.empty();
        }
    }

    private String safeToString(Object obj) {
        return obj != null ? obj.toString() : null;
    }
}
