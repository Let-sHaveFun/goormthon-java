# Multi-stage build for SpringBoot application
FROM openjdk:21-jdk-slim as builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper와 build 파일들 복사
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# 의존성 다운로드 (캐싱 최적화)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# 애플리케이션 빌드
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM openjdk:21-jre-slim

# 시스템 패키지 업데이트 및 curl 설치 (헬스체크용)
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

# 비루트 사용자 생성
RUN groupadd -r spring && useradd -r -g spring spring

# 작업 디렉토리 설정
WORKDIR /app

# JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 권한 설정
RUN chown spring:spring app.jar

# 사용자 전환
USER spring

# JVM 옵션 설정 (라즈베리파이 최적화)
ENV JAVA_OPTS="-Xms256m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 포트 노출
EXPOSE 8080

# 헬스체크 설정
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]