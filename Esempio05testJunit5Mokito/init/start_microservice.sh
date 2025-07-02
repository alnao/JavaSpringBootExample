#!/bin/sh
set -e

echo "> Avvio del microservizio Spring Boot con MySQL... ${MYSQL_DATASOURCE_HOST}:${MYSQL_DATASOURCE_PORT}"  # -${MYSQL_DATASOURCE_PASSWORD}-"

# Attesa che MySQL sia pronto
echo ">Attesa che MySQL sia raggiungibile..."
counter=0
until mysql -h"${MYSQL_DATASOURCE_HOST}" -p"${MYSQL_DATASOURCE_PORT}" -u"${MYSQL_DATASOURCE_USERNAME}" -p"${MYSQL_DATASOURCE_PASSWORD}" --ssl-verify-server-cert=0 -e 'SELECT 1'; do
  counter=$((counter + 1))
  if [ $counter -ge 10 ]; then
      echo "> MySQL non raggiungibile dopo 10 tentativi. Proseguo comunque..."
      break
  fi
  echo "> MySQL non ancora pronto... riprovo tra 10 secondi. (tentativo $counter/10)"
  sleep 10
done

# Esegui lo script SQL
#echo "📁 Esecuzione script SQL..."
#mysql -h"${MYSQL_DATASOURCE_HOST}" -p"${MYSQL_DATASOURCE_PORT}" -u"${MYSQL_DATASOURCE_USERNAME}" -p"${MYSQL_DATASOURCE_PASSWORD}" < /schema.sql

# Avvia l'app Spring Boot
echo "> Avvio del microservizio Spring Boot..."
exec java -jar /app/app.jar
echo "> Microservizio Spring Boot avviato con successo! <"
# Fine dello script
