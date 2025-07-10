package com.mycompany.goormthonserver.controller;
import com.mycompany.goormthonserver.dto.TouristSpotLocationDto;
import com.mycompany.goormthonserver.service.TouristSpotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/tour-spots")
@RequiredArgsConstructor
@Slf4j
public class TouristSpotController {

    private final TouristSpotService touristSpotService;

    @GetMapping("/location")
    public ResponseEntity<List<TouristSpotLocationDto>> findNearbyTouristSpots(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam(defaultValue = "10") double radius) {

        // 디버깅 로그 추가
        log.info("요청 파라미터 - latitude: {}, longitude: {}, radius: {}", latitude, longitude, radius);

        // 입력 유효성 검증
        if (latitude == null || longitude == null) {
            log.warn("필수 파라미터 누락 - latitude: {}, longitude: {}", latitude, longitude);
            return ResponseEntity.badRequest().build();
        }

        // 위경도 범위 검증
        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 ||
                latitude.compareTo(BigDecimal.valueOf(90)) > 0 ||
                longitude.compareTo(BigDecimal.valueOf(-180)) < 0 ||
                longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            return ResponseEntity.badRequest().build();
        }

        // 반경 검증 (최대 10km)
        if (radius <= 0 || radius > 10) {
            radius = 10;
        }

        List<TouristSpotLocationDto> nearbySpots = touristSpotService.findNearbyTouristSpots(
                latitude, longitude, radius, 10);

        log.info("반경 {}km 내 관광지 {}개 조회 완료", radius, nearbySpots.size());

        return ResponseEntity.ok(nearbySpots);
    }
}