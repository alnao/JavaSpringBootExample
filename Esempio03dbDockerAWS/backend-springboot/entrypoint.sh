#!/bin/sh
set -e

echo "🚀 Avvio del microservizio Spring Boot con MySQL... ${MYSQL_HOST}:${MYSQL_PORT}"  # -${MYSQL_ROOT_PASSWORD}-"

# Attesa che MySQL sia pronto
echo "⏳ Attesa che MySQL sia raggiungibile..."
until mysql -h"${MYSQL_HOST}" -p"${MYSQL_PORT}" -u"root" -p"${MYSQL_ROOT_PASSWORD}" -e 'SELECT 1'; do
  echo "⏳ MySQL non ancora pronto... riprovo tra 5 secondi."
  sleep 5
done

# Esegui lo script SQL
echo "📁 Esecuzione script SQL..."
mysql -h"${MYSQL_HOST}" -p"${MYSQL_PORT}" -u"root" -p"${MYSQL_ROOT_PASSWORD}" < /schema.sql

# Avvia l'app Spring Boot
echo "🚀 Avvio del microservizio Spring Boot..."
exec java -jar /app/app.jar
echo "✅ Microservizio Spring Boot avviato con successo!"
# Fine dello script
