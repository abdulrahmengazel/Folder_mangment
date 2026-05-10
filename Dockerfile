# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY pom.xml .
# Download dependencies
RUN mvn dependency:go-offline -B

# Copy the source code and build the war file
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Deploy to Payara Micro (Jakarta EE 10 compliant)
FROM payara/micro:6.2023.11-jdk21

# Create the upload directory required by the application
USER root
RUN mkdir -p /home/abdulrahman/cloud_uploads && \
    chown -R payara:payara /home/abdulrahman/cloud_uploads
USER payara

# Copy the built war from the builder stage
COPY --from=builder /app/target/Folder_mangment-1.0-SNAPSHOT.war $DEPLOY_DIR

# Expose the default HTTP port
EXPOSE 8080

# The container will automatically deploy the war placed in $DEPLOY_DIR
