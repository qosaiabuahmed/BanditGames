# Simple single-stage Dockerfile
# Build the JAR locally first with: ./gradlew bootJar
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy the pre-built JAR file
COPY ../build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
