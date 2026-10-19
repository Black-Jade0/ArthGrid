# Stage 1: Build (Maven se JAR banana)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run (JAR chalana)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Dhyaan rakho: --from=build hona hi chahiye
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]