FROM openjdk:17-jdk-slim

# Installa SQLite
RUN apt-get update && apt-get install -y sqlite3 && rm -rf /var/lib/apt/lists/*

# Directory di lavoro
WORKDIR /app

# Copia il jar dell'applicazione
COPY application/target/application-*.jar app.jar

# Crea directory per i dati
RUN mkdir -p /app/data

# Espone la porta
EXPOSE 8080

# Variabili ambiente di default per SQLite locale
ENV SPRING_PROFILES_ACTIVE=sqlite
#ENV REPLIT_DB_URL=http://replit-db-mock:3000

# Comando di avvio
ENTRYPOINT ["java", "-jar", "app.jar"]
