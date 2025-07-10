package com.mycompany.goormthonserver.dto.visitjeju;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * 비짓제주 API 전체 응답
 */
@Data
public class VisitJejuApiResponse {

    @JsonProperty("result")
    private String result;

    @JsonProperty("resultMessage")
    private String resultMessage;

    @JsonProperty("totalCount")
    private Integer totalCount;

    @JsonProperty("resultCount")
    private Integer resultCount;

    @JsonProperty("pageSize")
    private Integer pageSize;

    @JsonProperty("pageCount")
    private Integer pageCount;

    @JsonProperty("currentPage")
    private Integer currentPage;

    @JsonProperty("items")
    private List<VisitJejuItem> items;
}