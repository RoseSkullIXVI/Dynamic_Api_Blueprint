# For the final image, use an OpenJDK base image to run the application
FROM openjdk:17-oracle

WORKDIR /app

# Copy the built JAR from the previous stage into this new stage
COPY build/libs/*.jar /app/api_dynamic.jar

# Expose the port your app will run on
EXPOSE 8080

# Command to run your app
CMD ["java", "-jar", "api_dynamic.jar"]
