#!/bin/bash
# Nota: questo file serve per eseguire il profili kube in locale, con solo docker
#   usa docker e non docker-compose e non usa minikube
#   script solo di esempio , meglio usare docker-compose
# questo script fa partire come immagine docker : mongo, postgres, kafka, redis, applicazione!

echo "---------------------------------------------------------------------"
echo "Start annotazioni services in docker with kube profile"
echo "---------------------------------------------------------------------"

# Credenziali dal file .env della root, lo stesso del docker-compose.yml (change openspec secret-kube).
# Le password passano ai container con "-e NOME" senza valore: docker le legge dall'ambiente, cosi'
# non compaiono negli argomenti dei processi.
ENV_FILE="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/.env"
if [ ! -f "$ENV_FILE" ]; then
    echo "❌ File $ENV_FILE non trovato: crealo da .env.example e sostituisci tutti i valori"
    exit 1
fi
set -a
. "$ENV_FILE"
set +a
for VARIABILE in POSTGRES_USERNAME POSTGRES_PASSWORD MONGO_ROOT_USERNAME MONGO_ROOT_PASSWORD JWT_SECRET \
                 APP_USER_ADMIN_PASSWORD APP_USER_MODERATORE_PASSWORD APP_USER_ALNAO_PASSWORD; do
    if [ -z "${!VARIABILE}" ]; then
        echo "❌ Nel file $ENV_FILE manca la variabile $VARIABILE"
        exit 1
    fi
done
export MONGO_INITDB_ROOT_USERNAME="$MONGO_ROOT_USERNAME" MONGO_INITDB_ROOT_PASSWORD="$MONGO_ROOT_PASSWORD"
export POSTGRES_USER="$POSTGRES_USERNAME"
export MONGODB_URI="mongodb://${MONGO_ROOT_USERNAME}:${MONGO_ROOT_PASSWORD}@annotazioni-mongo:27017/gestioneannotazioni_db?authSource=admin"

# Rete
    docker network create annotazioni-network
    echo "Rete creata"

# mongo
    docker run -d --name annotazioni-mongo \
      --network annotazioni-network \
      -p 27017:27017 \
      -e MONGO_INITDB_DATABASE=gestioneannotazioni \
      -e MONGO_INITDB_ROOT_USERNAME \
      -e MONGO_INITDB_ROOT_PASSWORD \
      mongo:4.4
    echo "Mongo creato"

# postgresql
    docker run -d --name annotazioni-postgres \
      --network annotazioni-network \
      -p 5432:5432 \
      -e POSTGRES_DB=gestioneannotazioni \
      -e POSTGRES_USER \
      -e POSTGRES_PASSWORD \
      postgres:13
    echo "PostgreSQL creato e configurato"

# Kafka e Zookeeper
    docker run  -d \
      --network annotazioni-network \
      --name annotazioni-zookeeper \
      -e ZOOKEEPER_CLIENT_PORT=2181 \
      -e ZOOKEEPER_TICK_TIME=2000 \
      -p 2181:2181 \
      confluentinc/cp-zookeeper:7.4.0

    echo "Zookeeper avviato, attendo che sia pronto..."
    sleep 25

    # Zookeeper
    docker run  -d \
      --network annotazioni-network \
      --name annotazioni-kafka \
      -p 9092:9092 \
      -e KAFKA_BROKER_ID=1 \
      -e KAFKA_ZOOKEEPER_CONNECT=annotazioni-zookeeper:2181 \
      -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://annotazioni-kafka:9092 \
      -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 \
      confluentinc/cp-kafka:7.4.0

    echo "Kafka avviato, attendo che sia pronto..."
    sleep 25    

    # Kafka-ui
    docker run  -d -p 8085:8080 \
      --name annotazioni-kafka-ui \
      --network annotazioni-network \
      -e KAFKA_CLUSTERS_0_NAME=gestioneannotazioni-cluster \
      -e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=annotazioni-kafka:9092 \
      -e KAFKA_CLUSTERS_0_ZOOKEEPER=annotazioni-zookeeper:2181 \
      provectuslabs/kafka-ui:latest
    echo "Kafka e Zookeeper creati e configurati"

# Redis
    docker run -d --name annotazioni-redis \
      --network annotazioni-network \
      -p 6379:6379 \
      redis:7-alpine #ex redis:6
    echo "Redis creato e configurato"


    echo "Configuro document mongo e database postgres"
# document dentro Mongo
    docker cp script/init-database/init-mongodb.js annotazioni-mongo:/init-mongodb.js
    docker exec annotazioni-mongo sh -c 'mongo -u "$MONGO_INITDB_ROOT_USERNAME" -p "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin /init-mongodb.js'
# database nel postgresql
    docker cp script/init-database/init-postgres.sql annotazioni-postgres:/init-postgres.sql
    docker exec annotazioni-postgres sh -c 'psql -U "$POSTGRES_USER" -d gestioneannotazioni -f /init-postgres.sql'

# Esecuzione servizio con profilo kube
    echo "Faccio partire il servizio"
    docker run  -d -p 8082:8080 --name annotazioni-app \
      --network annotazioni-network \
      -e SPRING_PROFILES_ACTIVE="kube" \
      -e POSTGRES_URL="jdbc:postgresql://annotazioni-postgres:5432/gestioneannotazioni" \
      -e POSTGRES_USERNAME \
      -e POSTGRES_PASSWORD \
      -e MONGODB_URI \
      -e JWT_SECRET \
      -e APP_USER_ADMIN_PASSWORD \
      -e APP_USER_MODERATORE_PASSWORD \
      -e APP_USER_ALNAO_PASSWORD \
      -e KAFKA_BROKER_URL="annotazioni-kafka:9092" \
      -e REDIS_HOST="annotazioni-redis" \
      -e REDIS_PORT=6379 \
      alnao/gestioneannotazioni:latest

echo "---------------------------------------------------------------------"
echo "✅ Start annotazioni docker con profilo kube terminato correttamente!"