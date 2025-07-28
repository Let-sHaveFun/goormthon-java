# 1단계: 빌드용
FROM eclipse-temurin:21-jdk as builder
WORKDIR /app
COPY . .
RUN ./gradlew build -x test

# 2단계: 실행용
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/goormthon-server-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]


