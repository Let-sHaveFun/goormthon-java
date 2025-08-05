package com.dormung.dto.visitjeju;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 비짓제주 API 대표 사진 정보
 */
@Data
public class RepPhoto {

    @JsonProperty("descseo")
    private String descseo;

    @JsonProperty("photoid")
    private PhotoId photoid;

    /**
     * 사진 ID 및 경로 정보
     */
    @Data
    public static class PhotoId {
        @JsonProperty("photoid")
        private Long photoid;

        @JsonProperty("imgpath")
        private String imgpath;

        @JsonProperty("thumbnailpath")
        private String thumbnailpath;
    }
}