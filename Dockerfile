FROM openjdk:11-jdk-slim
VOLUME /tmp
COPY target/vertx-microservices-1.0-SNAPSHOT.jar /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

