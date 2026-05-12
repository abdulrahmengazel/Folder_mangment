FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests


FROM payara/micro:latest

USER root
RUN mkdir -p /home/abdulrahman/cloud_uploads && \
    chown -R payara:payara /home/abdulrahman/cloud_uploads

# نسخ الـ entrypoint
COPY entrypoint.sh /opt/payara/entrypoint.sh
RUN chmod +x /opt/payara/entrypoint.sh

USER payara

COPY --from=builder /app/target/app.war /opt/payara/deployments/app.war

ENTRYPOINT ["/opt/payara/entrypoint.sh"]

EXPOSE 8080