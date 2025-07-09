# 1단계: Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# 프로젝트 전체 복사
COPY . .

# Maven 빌드 (테스트 생략)
RUN mvn clean package -DskipTests
RUN ./gradlew build -x test

# 2단계: Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# 빌드된 jar를 app.jar로 이름 변경해서 복사
COPY --from=builder /app/target/*-jar-with-dependencies.jar ./app.jar

# 포트 오픈
EXPOSE 8080

# jar 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
