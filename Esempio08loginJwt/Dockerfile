#COMPILAZIONE con maven dentro ad un docker specifico
#FROM maven:3-eclipse-temurin-21  AS build
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Compila l'applicazione Spring Boot in un file JAR eseguibile
RUN mvn clean package -DskipTests 

# Usa JDK come immagine base #ex #
#FROM eclipse-temurin:17-jre-alpine
#FROM eclipse-temurin:17-jdk-jammy 
FROM openjdk:21-jdk-slim

# Crea directory per l'app
WORKDIR /app

# Installa curl e il client MySQL in un'unica riga per efficienza e consistenza
#RUN apk update && apk add curl mysql-client && rm -rf /var/cache/apk/*
RUN apt-get update && \
    apt-get install -y default-mysql-client curl iputils-ping  && \
    rm -rf /var/lib/apt/lists/*

# Copia il jar generato da Maven #ex COPY target/*.jar app.jar
COPY --from=build /app/target/*.jar app.jar

# Espone la porta
EXPOSE 8047

# Rendi eseguibile l'entrypoint
# COPY init/start_microservice.sh /entrypoint.sh
# RUN chmod +x /entrypoint.sh
# Comando di avvio # ex  #ENTRYPOINT ["/entrypoint.sh"]

ENTRYPOINT ["java", "-jar", "app.jar"]

