# Use a base image that includes both JDK for running your application and Gradle for building it
FROM gradle:8.6-jdk17 as build

# Set the working directory in the Docker image
WORKDIR /app

# Copy the Gradle configuration files first (for caching purposes)
COPY build.gradle settings.gradle gradlew /app/
COPY gradle /app/gradle

# Give permission to execute the Gradle wrapper
RUN chmod +x ./gradlew

# Copy your source code into the Docker image
COPY src /app/src

# Build your application using Gradle
# This step can be omitted if you prefer to build the application on your host machine and mount the output directory
RUN ./gradlew build

# For the final image, use an OpenJDK base image to run the application
FROM openjdk:17-oracle

WORKDIR /app

# Copy the built JAR from the previous stage into this new stage
COPY --from=build /app/build/libs/*.jar /app/api_dynamic.jar

# Expose the port your app will run on
EXPOSE 8080

# Command to run your app
CMD ["java", "-jar", "api_dynamic.jar"]
