# =========================
# 1️⃣ Build stage
# =========================
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
WORKDIR /app/hirelink
RUN mvn clean package -DskipTests

# =========================
# 2️⃣ Runtime stage
# =========================
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=build /app/hirelink/target/hirelink-*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]
