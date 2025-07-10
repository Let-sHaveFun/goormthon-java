package com.mycompany.goormthonserver.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 설정
 * 해커톤용 빠른 API 문서화
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(getApiInfo())
                .servers(getServers())
                .tags(getTags());
    }

    /**
     * API 기본 정보
     */
    private Info getApiInfo() {
        return new Info()
                .title("도르멍 드르멍 API")
                .description(getApiDescription())
                .version("v1.0.0")
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"))
                .contact(new Contact()
                        .name("제주 오디오 가이드 팀")
                        .email("team@jejuaudio.com")
                        .url("https://github.com/jeju-audio-guide"));
    }

    /**
     * API 설명
     */
    private String getApiDescription() {
        return """
                🎵 제주 관광지 사투리 & 외국어 오디오 스토리 가이드 서비스
                
                ## 주요 기능
                - 📍 위치 기반 관광지 정보 조회
                - 🔍 실시간 검색 및 자동완성
                - 🎭 AI 기반 사투리 스크립트 생성
                - 🎵 TTS 오디오 콘텐츠 제공
                - 📱 QR 코드 연동
                - 🧩 조각 수집 게임
                
                ## 해커톤 개발 정보
                - 개발기간: 20시간
                - 팀구성: FE 2명, BE 1명
                - 기술스택: Spring Boot, MySQL, Redis, Docker
                """;
    }

    /**
     * 서버 정보
     */
    private List<Server> getServers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("로컬 개발 서버"),
                new Server()
                        .url("https://api.jejuaudio.com")
                        .description("운영 서버")
        );
    }

    /**
     * API 태그 분류
     */
    private List<Tag> getTags() {
        return List.of(
                new Tag()
                        .name("관광지")
                        .description("제주 관광지 정보 관련 API"),
                new Tag()
                        .name("검색")
                        .description("관광지 검색 관련 API"),
                new Tag()
                        .name("오디오")
                        .description("오디오 콘텐츠 관련 API"),
                new Tag()
                        .name("AI")
                        .description("AI 스크립트 생성 관련 API"),
                new Tag()
                        .name("QR")
                        .description("QR 코드 연동 관련 API"),
                new Tag()
                        .name("수집")
                        .description("조각 수집 관련 API")
        );
    }
}