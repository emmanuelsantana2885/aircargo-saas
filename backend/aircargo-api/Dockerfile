FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/aircargo-api-1.0.0-SNAPSHOT.jar app.jar

EXPOSE 9091

ENTRYPOINT ["java", "-jar", "app.jar"]