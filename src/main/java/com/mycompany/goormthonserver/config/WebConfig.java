package com.mycompany.goormthonserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "https://dormung.netlify.app",
                        "https://*--dormung.netlify.app",  // 모든 prefix 패턴 허용
                        "http://localhost:*",              // 로컬 개발 환경 (모든 포트)
                        "https://localhost:*"              // HTTPS 로컬 개발 환경
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}