package com.mycompany.goormthonserver.dto.visitjeju;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 비짓제주 API 지역 코드
 */
@Data
class RegionCode {

    @JsonProperty("value")
    private String value;

    @JsonProperty("label")
    private String label;

    @JsonProperty("refId")
    private String refId;
}