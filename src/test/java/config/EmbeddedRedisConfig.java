// src/test/java/config/EmbeddedRedisConfig.java

package config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import redis.embedded.RedisServer;

import java.io.IOException;

@TestConfiguration
@Profile("test")
public class EmbeddedRedisConfig {

    private RedisServer redisServer;
    private final int redisPort = 6370;

    @PostConstruct
    public void startRedis() throws IOException {
        try {
            // 사용 가능한 포트 찾기
            redisServer = RedisServer.builder()
                    .port(redisPort)
                    .setting("maxmemory 128M")
                    .build();

            redisServer.start();
            System.out.println("✅ Embedded Redis started on port " + redisPort);
        } catch (Exception e) {
            System.err.println("❌ Failed to start embedded Redis: " + e.getMessage());
            // CI 환경에서는 Redis 없이도 테스트 진행
        }
    }

    @PreDestroy
    public void stopRedis() {
        try {
            if (redisServer != null && redisServer.isActive()) {
                redisServer.stop();
                System.out.println("✅ Embedded Redis stopped");
            }
        } catch (Exception e) {
            System.err.println("❌ Error stopping embedded Redis: " + e.getMessage());
        }
    }
}