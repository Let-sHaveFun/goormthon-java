package com.mycompany.goormthonserver.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 에러 응답 DTO
 * 표준 에러 응답 형식
 */
@Schema(description = "에러 응답")
public class ErrorResponse {

    @Schema(description = "에러 코드", example = "TOURIST_SPOT_NOT_FOUND")
    private String code;

    @Schema(description = "에러 메시지", example = "관광지를 찾을 수 없습니다.")
    private String message;

    @Schema(description = "타임스탬프", example = "2024-01-01T12:00:00Z")
    private String timestamp;

    // 기본 생성자
    public ErrorResponse() {}

    // 전체 필드 생성자
    public ErrorResponse(String code, String message, String timestamp) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}