# Dockerfile
FROM maven:3.9.5-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Crea un utente non-root per sicurezza
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

    # Installa curl
RUN apk add --no-cache curl

COPY --from=build /app/target/*.jar app.jar

# Cambia ownership del file jar
RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]