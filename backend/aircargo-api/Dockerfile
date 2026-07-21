FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy parent pom + ALL module poms (Maven resolves the full reactor)
COPY pom.xml .
COPY aircargo-common/pom.xml   aircargo-common/
COPY aircargo-api/pom.xml      aircargo-api/
COPY aircargo-gateway/pom.xml  aircargo-gateway/
COPY aircargo-auth-service/pom.xml     aircargo-auth-service/
COPY aircargo-flight-service/pom.xml   aircargo-flight-service/
COPY aircargo-booking-service/pom.xml  aircargo-booking-service/
COPY aircargo-mawb-service/pom.xml     aircargo-mawb-service/
COPY aircargo-warehouse-service/pom.xml aircargo-warehouse-service/
COPY aircargo-uld-service/pom.xml      aircargo-uld-service/
COPY aircargo-load-planning-service/pom.xml aircargo-load-planning-service/
COPY aircargo-export-service/pom.xml   aircargo-export-service/
COPY aircargo-notification-service/pom.xml aircargo-notification-service/

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B -pl aircargo-api -am

# Copy source for the modules we actually build
COPY aircargo-common/src aircargo-common/src
COPY aircargo-api/src    aircargo-api/src

RUN mvn package -B -pl aircargo-api -am -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /build/aircargo-api/target/*.jar app.jar
EXPOSE 9091
ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
