FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/quote-approval-workflow-shred-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]