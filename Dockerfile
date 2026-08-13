# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven project files
COPY pom.xml .
COPY broadcastsim-core/pom.xml broadcastsim-core/pom.xml
COPY broadcastsim-web/pom.xml broadcastsim-web/pom.xml

# Copy source code
COPY broadcastsim-core/src broadcastsim-core/src
COPY broadcastsim-web/src broadcastsim-web/src

# Build web module and its core dependency
RUN mvn clean package -pl broadcastsim-web -am -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the Spring Boot jar
COPY --from=build /app/broadcastsim-web/target/broadcastsim-web-1.0.0.jar app.jar

# Render uses PORT; Spring Boot reads it from application.properties
EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]