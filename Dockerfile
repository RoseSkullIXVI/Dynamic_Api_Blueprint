# Use a base image with Java installed
FROM openjdk:17-oracle

# Set the working directory inside the container
WORKDIR /app

# Copy the JAR file into the container
COPY build/libs/api_dynamic-0.0.1-SNAPSHOT.jar /app/api_dynamic-0.0.1-SNAPSHOT.jar

# Expose the port your app will be running on
EXPOSE 8080

# Set the command to run your app when the container starts
CMD ["java", "-jar", "api_dynamic-0.0.1-SNAPSHOT.jar"]
