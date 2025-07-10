package com.mycompany.goormthonserver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TouristSpotLocationDto {
    private String externalId;
    private String name;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String description;
    private String category;
    private String tag;
    private String introduction;
    private String imgPath;
    private Double distance; // km 단위
}