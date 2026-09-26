
# Stage 1: Build the Spring Boot application
FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration and source code
COPY pom.xml .
COPY src ./src

# Package the application without running tests
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy the executable JAR from the build stage
COPY --from=build /app/target/copyright-complaint-portal-0.0.1-SNAPSHOT.jar app.jar

# Application port
EXPOSE 8081

# Default configuration (can be overridden at runtime)
ENV SERVER_PORT=8081
ENV DB_USERNAME=root
ENV DB_PASSWORD=

# Start the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]