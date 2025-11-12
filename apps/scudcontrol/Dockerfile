# Этап сборки
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy Maven wrapper files and pom.xml first to leverage Docker cache
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (if pom.xml changes, this layer needs to be rebuilt)
RUN ./mvnw dependency:go-offline -B

# Copy the rest of the application code
COPY src src

# Build the Spring Boot application (creates the executable JAR)
RUN ./mvnw package -Dmaven.test.skip=true

# Stage 2: Create the final, smaller runtime image
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the port your Spring Boot application runs on (e.g., 8080)
EXPOSE 8088

# Command to run the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]