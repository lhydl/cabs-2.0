# Use Java 21 (matches your project)
FROM eclipse-temurin:21-jdk-alpine

# Set working directory inside container
WORKDIR /app

# Copy the built JAR into container
COPY build/libs/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8084

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]