# 1단계: Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

# 2단계: Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app
# app.jar로 복사
COPY --from=builder /app/build/libs/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]