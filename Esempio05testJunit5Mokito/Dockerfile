#COMPILAZIONE con maven dentro ad un docker specifico
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Usa JDK come immagine base #ex #FROM eclipse-temurin:17-jre-alpine
FROM openjdk:17-jdk-slim

# Crea directory per l'app
WORKDIR /app

# Installa il client mysql e curl  #ex RUN apk add --no-cache url
RUN apt-get update && \
    apt-get install -y default-mysql-client curl iputils-ping  && \
    rm -rf /var/lib/apt/lists/*

# Copia il jar generato da Maven #ex COPY target/*.jar app.jar
COPY --from=build /app/target/*.jar app.jar

# Espone la porta
EXPOSE 8045

# Rendi eseguibile l'entrypoint
COPY init/start_microservice.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Comando di avvio # ex ENTRYPOINT ["java", "-jar", "app.jar"]
ENTRYPOINT ["/entrypoint.sh"]
