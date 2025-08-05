package com.dormung.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristSpotDetailDto {
    private String imgPath;    // 이미지 경로
    private String audioUrl;   // 오디오 URL
    private String script;     // 스크립트
    private String name;      // 관광지 제목
    private String external_id;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private String category;
    private String tag;
    private String introduction;
    private Double distance; // km 단위
}