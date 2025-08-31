FROM openjdk:21-jdk-slim
LABEL maintainer="alnao.it"
LABEL description="Sistema Gestione Annotazioni"
# Installa curl per health check
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*
# Crea utente non-root
RUN groupadd -r appuser && useradd -r -g appuser appuser
# Directory di lavoro
WORKDIR /app
# Copia il JAR dell'applicazione
COPY target/module-app-*.jar app.jar
# Cambia ownership
RUN chown appuser:appuser /app/app.jar
# Cambia utente
USER appuser
# Esponi porta
EXPOSE 8080
# Variabili d'ambiente
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
ENV SPRING_PROFILES_ACTIVE=onprem
# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando di avvio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
