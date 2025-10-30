# Stage 1: Build con JDK
FROM openjdk:17-jdk-slim AS builder

LABEL maintainer="alnao.it"
LABEL description="Sistema Gestione Annotazioni - Build Stage"

# Installa Maven
RUN apt-get update && \
    apt-get install -y maven && \
    rm -rf /var/lib/apt/lists/*

# Directory di lavoro per il build
WORKDIR /workspace

# Copia i file di configurazione Maven
COPY pom.xml .
COPY core/pom.xml core/
COPY adapter-api/pom.xml adapter-api/
COPY adapter-web/pom.xml adapter-web/
COPY adapter-aws/pom.xml adapter-aws/
COPY adapter-mongodb/pom.xml adapter-mongodb/
COPY adapter-postgresql/pom.xml adapter-postgresql/
COPY adapter-sqlite/pom.xml adapter-sqlite/
COPY adapter-kafka/pom.xml adapter-kafka/
COPY adapter-azure/pom.xml adapter-azure/
COPY application/pom.xml application/

# Download delle dipendenze (layer cache optimization)
RUN mvn dependency:go-offline -B

# Copia tutto il codice sorgente
COPY . .

# Compila il progetto
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime con JRE
#FROM openjdk:17-jre-slim AS runtime
#FROM openjdk:8u342-jre AS runtime
FROM openjdk:17-jdk-slim AS runtime

LABEL maintainer="alnao.it"
LABEL description="Sistema Gestione Annotazioni - Runtime"

# Installa curl per health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Crea utente non-root
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Directory di lavoro
WORKDIR /app

# Copia il JAR compilato dal stage di build
COPY --from=builder /workspace/application/target/application-*.jar app.jar

# Cambia ownership
RUN chown appuser:appuser /app/app.jar

# Gestione certificato CosmosDB (opzionale)
# Usa un wildcard per evitare errore se il file non esiste
#COPY . /tmp/build-context/
# Importa certificato nel truststore Java (solo se il file esiste)
#USER root
#RUN if [ -f /cosmos-certs/cosmosdb-cert.crt ]; then \
#      echo '📋 Importing CosmosDB certificate...'; \
#      cp /cosmos-certs/cosmosdb-cert.crt /usr/local/share/ca-certificates/ && \
#      update-ca-certificates && \
#      echo '✅ Certificate imported successfully'; \
#    else \
#      echo "⚠️ Certificato CosmosDB non trovato, passo saltato"; \
#    fi
#RUN rm -rf /tmp/build-context

# Cambia utente
USER appuser

# Esponi porta
EXPOSE 8080

# Variabili d'ambiente
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=kube

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

#ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts -Djavax.net.ssl.trustStorePassword=changeit"

# Comando di avvio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
