package com.mycompany.goormthonserver.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * .env 파일 로드 설정
 */
@Configuration
public class DotEnvConfig {

    @Bean
    public Dotenv dotenv() {
        return Dotenv.configure()
                .directory("./")  // .env 파일 위치 (프로젝트 루트)
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();
    }
}