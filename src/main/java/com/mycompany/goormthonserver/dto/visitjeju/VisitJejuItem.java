package com.mycompany.goormthonserver.dto.visitjeju;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 비짓제주 API 개별 관광지 정보
 */
@Data
public class VisitJejuItem {

    @JsonProperty("contentsid")
    private String contentsid;

    @JsonProperty("title")
    private String title;

    @JsonProperty("introduction")
    private String introduction;

    @JsonProperty("address")
    private String address;

    @JsonProperty("roadaddress")
    private String roadaddress;

    @JsonProperty("tag")
    private String tag;

    @JsonProperty("alltag")
    private String alltag;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("phoneno")
    private String phoneno;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("repPhoto")
    private RepPhoto repPhoto;

    @JsonProperty("contentscd")
    private ContentsCode contentscd;

    @JsonProperty("region1cd")
    private RegionCode region1cd;

    @JsonProperty("region2cd")
    private RegionCode region2cd;
}