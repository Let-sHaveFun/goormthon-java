package com.mycompany.goormthonserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristSpotDetailDto {
    private String imgPath;    // 이미지 경로
    private String audioUrl;   // 오디오 URL
    private String script;     // 스크립트
    private String name;      // 관광지 제목
}