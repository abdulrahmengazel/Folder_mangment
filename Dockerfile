# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Payara Micro
FROM payara/micro:6.2023.11-jdk21

USER root
RUN mkdir -p /home/abdulrahman/cloud_uploads && \
    chown -R payara:payara /home/abdulrahman/cloud_uploads
USER payara


COPY setup.pyara /opt/payara/deployments/setup.pyara
COPY --from=builder /app/target/Folder_mangment-1.0-SNAPSHOT.war /opt/payara/deployments/app.war


ENTRYPOINT ["java", "-jar", "/opt/payara/payara-micro.jar", "--postbootcommandfile", "/opt/payara/deployments/setup.pyara", "--deploy", "/opt/payara/deployments/app.war"]

EXPOSE 8080