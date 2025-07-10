package com.mycompany.goormthonserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 비짓제주 API 응답 DTO (간소화 버전)
 * 7개 핵심 필드만 포함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "비짓제주 관광지 정보")
public class VisitJejuResponseDto {

    @Schema(description = "관광지명", example = "성산일출봉(UNESCO 세계자연유산)")
    @JsonProperty("title")
    private String title;

    @Schema(description = "관광지 간단 소개", example = "바다위에 우뚝 솟아난 수성화산·유네스코 세계자연유산")
    @JsonProperty("introduction")
    private String introduction;

    @Schema(description = "주요 키워드", example = "일출,오름,경관/포토,부모")
    @JsonProperty("tag")
    private String tag;

    @Schema(description = "지번 주소", example = "제주특별자치도 서귀포시 성산읍 성산리 1")
    @JsonProperty("address")
    private String address;

    @Schema(description = "이미지 고유번호", example = "2018052306801")
    @JsonProperty("photoId")
    private Long photoId;

    @Schema(description = "원본 이미지 URL", example = "https://api.cdn.visitjeju.net/photomng/imgpath/201810/17/c072ee1a-2a02-4be7-b0cd-62f4daf2f847.gif")
    @JsonProperty("imgPath")
    private String imgPath;

    @Schema(description = "콘텐츠 ID (내부 참조용)", example = "CONT_000000000500349")
    @JsonProperty("contentsId")
    private String contentsId;

    // 캐시 정보 (선택적)
    @Schema(description = "데이터 소스", example = "REDIS", allowableValues = {"REDIS", "MYSQL", "API"})
    @JsonProperty("source")
    private String source;

    @Schema(description = "응답 시간 (ms)", example = "15")
    @JsonProperty("responseTime")
    private Long responseTime;

    /**
     * 캐시 소스 설정 편의 메서드
     */
    public void setSource(CacheSource cacheSource) {
        this.source = cacheSource.name();
    }

    /**
     * 캐시 소스 enum
     */
    public enum CacheSource {
        REDIS,   // Redis 캐시에서 조회
        MYSQL,   // MySQL 백업에서 조회
        API      // 비짓제주 API에서 직접 조회
    }
}