# Stage 1: Build stage (Maven use karke JAR banayega)
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run stage (Sirf JAR ko chalayega)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# --from=build likhna compulsory hai
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]