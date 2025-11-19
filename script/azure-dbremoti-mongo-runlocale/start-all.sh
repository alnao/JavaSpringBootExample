#!/bin/bash
echo "🚀 Avvio creazione risorse Azure CosmosDB MongoDB + PostgreSQL..."
#./script/azure-dbremoti-mongo-runlocale/start-all.sh  

# 1. Variabili di configurazione
RESOURCE_GROUP="gestioneannotazioni-rg-mongo-postgres"
LOCATION="northeurope"  # Alternative: francecentral, germanywestcentral, uksouth, swedencentral
COSMOSDB_ACCOUNT="gestioneannotazioni-mongo"
COSMOSDB_DATABASE="annotazioni"
POSTGRES_NAME="gestioneannotazioni-postgres"
POSTGRES_DATABASE="annotazioni"
POSTGRES_ADMIN="sqladmin"
POSTGRES_PASSWORD="P@ssw0rd123!"
REDIS_NAME="gestioneannotazioni-redis-mongo"
EVENT_HUBS_NAMESPACE="gestioneannotazioni-eventhubs"
EVENT_HUB_NAME="annotazioni-export"

# Funzione per gestire gli errori
check_error() {
    if [ $? -ne 0 ]; then
        echo "❌ ERRORE: $1"
        exit 1
    else
        echo "✅ SUCCESS: $1"
    fi
}

# Funzione per attendere che Cosmos DB sia pronto
wait_for_cosmos_db() {
    local account=$1
    local rg=$2
    local max_attempts=10  # 60 minuti (10 * 60 secondi)
    local attempt=0

    echo "⏳ Attesa completamento provisioning Cosmos DB (può richiedere 60 minuti)..."

    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt + 1))
        
        # Verifica se la risorsa esiste e il suo stato
        status=$(az cosmosdb show \
            --name $account \
            --resource-group $rg \
            --query "provisioningState" \
            --output tsv 2>/dev/null)
        
        if [ "$status" = "Succeeded" ]; then
            echo "✅ Cosmos DB pronto e disponibile!"
            return 0
        elif [ "$status" = "Failed" ]; then
            echo "❌ Creazione Cosmos DB fallita!"
            az cosmosdb show --name $account --resource-group $rg
            return 1
        elif [ -z "$status" ]; then
            echo "   Tentativo $attempt/$max_attempts - Risorsa in fase di inizializzazione..."
        else
            echo "   Tentativo $attempt/$max_attempts - Stato: $status"
        fi
        
        sleep 60
    done
    
    echo "❌ Timeout: Cosmos DB non è diventato disponibile in 10 minuti"
    return 1
}

# Funzione per attendere che PostgreSQL sia pronto
wait_for_postgres() {
    local server=$1
    local rg=$2
    local max_attempts=60  # 10 minuti
    local attempt=0
    
    echo "⏳ Attesa completamento provisioning PostgreSQL..."
    
    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt + 1))
        
        status=$(az postgres flexible-server show \
            --name $server \
            --resource-group $rg \
            --query "state" \
            --output tsv 2>/dev/null)
        
        if [ "$status" = "Ready" ]; then
            echo "✅ PostgreSQL pronto!"
            return 0
        fi
        
        echo "   Tentativo $attempt/$max_attempts - Stato: ${status:-Initializing}"
        sleep 10
    done
    
    echo "❌ Timeout: PostgreSQL non è diventato disponibile"
    return 1
}

# 2. Login ad Azure (se non già autenticato)
  echo "🔐 Login ad Azure..."
  if az account show &>/dev/null; then
    CURRENT_ACCOUNT=$(az account show --query "name" --output tsv)
    CURRENT_USER=$(az account show --query "user.name" --output tsv)
    echo "✅ Già autenticato come: $CURRENT_USER"
    echo "   Subscription: $CURRENT_ACCOUNT"
  else
    echo "🔐 Login ad Azure richiesto..."
    az login
    check_error "Login completato"
  fi
  check_error "Login completato"



# 3. Creazione Resource Group
echo "📦 Creazione Resource Group..."
if az group exists --name $RESOURCE_GROUP | grep -q "true"; then
  echo "   Resource Group già esistente, skip creazione"
else
  az group create \
    --name $RESOURCE_GROUP \
    --location $LOCATION
  check_error "Resource Group creato"
fi

# 4. Avvio creazione CosmosDB con API MongoDB (tier Free)
echo "🌐 Avvio creazione CosmosDB MongoDB..."
COSMOS_EXISTS=$(az cosmosdb check-name-exists --name $COSMOSDB_ACCOUNT --output tsv)
if [ "$COSMOS_EXISTS" = "true" ]; then
  echo "   CosmosDB già esistente, skip creazione"
  wait_for_cosmos_db $COSMOSDB_ACCOUNT $RESOURCE_GROUP
else
  az cosmosdb create \
    --name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --kind MongoDB \
    --server-version 4.2 \
    --default-consistency-level Session \
    --enable-free-tier true \
    --locations regionName=$LOCATION failoverPriority=0 isZoneRedundant=false
  
  echo "✅ Creazione Cosmos DB MongoDB avviata"
  
  # Attesa attiva che CosmosDB sia completamente disponibile
  wait_for_cosmos_db $COSMOSDB_ACCOUNT $RESOURCE_GROUP
  check_error "Cosmos DB disponibile"
fi

# 5. Creazione database MongoDB in CosmosDB
echo "💾 Creazione database MongoDB in CosmosDB..."
MONGODB_DB_EXISTS=$(az cosmosdb mongodb database exists \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --name $COSMOSDB_DATABASE \
  --output tsv 2>/dev/null || echo "false")
if [ "$MONGODB_DB_EXISTS" = "true" ]; then
  echo "   Database MongoDB già esistente, skip creazione"
else
  az cosmosdb mongodb database create \
    --account-name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --name $COSMOSDB_DATABASE
  check_error "Database MongoDB creato"
fi

# 6. Creazione collection per annotazioni
echo "📋 Creazione collection annotazioni..."
COLLECTION_EXISTS=$(az cosmosdb mongodb collection exists \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --database-name $COSMOSDB_DATABASE \
  --name annotazioni \
  --output tsv 2>/dev/null || echo "false")
if [ "$COLLECTION_EXISTS" = "true" ]; then
  echo "   Collection annotazioni già esistente, skip creazione"
else
  az cosmosdb mongodb collection create \
    --account-name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --database-name $COSMOSDB_DATABASE \
    --name annotazioni \
    --shard "_id" \
    --throughput 400
  check_error "Collection annotazioni creata"
fi

# 7. Creazione collection per storico
echo "📜 Creazione collection storico..."
COLLECTION_STORICO_EXISTS=$(az cosmosdb mongodb collection exists \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --database-name $COSMOSDB_DATABASE \
  --name annotazioni_storico \
  --output tsv 2>/dev/null || echo "false")
if [ "$COLLECTION_STORICO_EXISTS" = "true" ]; then
  echo "   Collection storico già esistente, skip creazione"
else
  az cosmosdb mongodb collection create \
    --account-name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --database-name $COSMOSDB_DATABASE \
    --name annotazioni_storico \
    --shard "_id" \
    --throughput 400
  check_error "Collection storico creata"
fi

# 8. Recupero connection string MongoDB
echo "🔑 Recupero connection string MongoDB..."
MONGODB_URI=$(az cosmosdb keys list \
  --name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --type connection-strings \
  --query "connectionStrings[0].connectionString" \
  --output tsv)
check_error "Connection string MongoDB recuperata"

echo "MongoDB URI: ${MONGODB_URI:0:50}..."

# 9. Creazione PostgreSQL Flexible Server (tier Burstable B1ms)
echo "🐘 Creazione PostgreSQL Flexible Server..."
POSTGRES_EXISTS=$(az postgres flexible-server list \
  --resource-group $RESOURCE_GROUP \
  --query "[?name=='$POSTGRES_NAME'].name" \
  --output tsv 2>/dev/null)
if [ -n "$POSTGRES_EXISTS" ]; then
  echo "   PostgreSQL già esistente, skip creazione"
  wait_for_postgres $POSTGRES_NAME $RESOURCE_GROUP
else
  az postgres flexible-server create \
    --name $POSTGRES_NAME \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --admin-user $POSTGRES_ADMIN \
    --admin-password $POSTGRES_PASSWORD \
    --sku-name Standard_B1ms \
    --tier Burstable \
    --storage-size 32 \
    --version 14 \
    --public-access 0.0.0.0-255.255.255.255
  
  check_error "PostgreSQL creato"
  
  # Attesa che PostgreSQL sia pronto
  wait_for_postgres $POSTGRES_NAME $RESOURCE_GROUP
  check_error "PostgreSQL disponibile"
fi

# 10. Creazione database PostgreSQL
echo "💾 Creazione database PostgreSQL..."
POSTGRES_DB_EXISTS=$(az postgres flexible-server db list \
  --resource-group $RESOURCE_GROUP \
  --server-name $POSTGRES_NAME \
  --query "[?name=='$POSTGRES_DATABASE'].name" \
  --output tsv 2>/dev/null)
if [ -n "$POSTGRES_DB_EXISTS" ]; then
  echo "   Database PostgreSQL già esistente, skip creazione"
else
  az postgres flexible-server db create \
    --resource-group $RESOURCE_GROUP \
    --server-name $POSTGRES_NAME \
    --database-name $POSTGRES_DATABASE
  check_error "Database PostgreSQL creato"
fi

# 11. Recupero connection string PostgreSQL
POSTGRES_HOST="${POSTGRES_NAME}.postgres.database.azure.com"
echo "PostgreSQL Host: $POSTGRES_HOST"
echo "PostgreSQL Connection String: jdbc:postgresql://${POSTGRES_HOST}:5432/${POSTGRES_DATABASE}?user=${POSTGRES_ADMIN}&password=***&sslmode=require"

# 12. Esecuzione script di inizializzazione PostgreSQL
echo "📝 Esecuzione script di inizializzazione PostgreSQL..."

INIT_SQL_PATH="/sql/script/init-database/init-postgres.sql"

# Esegui tramite Docker con psql
#docker run --rm \
#    -v "$(pwd):/workspace" \
#    -w /workspace \
#    postgres:14 \
#    psql \
#    "postgresql://${POSTGRES_ADMIN}:${POSTGRES_PASSWORD}@${POSTGRES_HOST}:5432/${POSTGRES_DATABASE}?sslmode=require" \
#    -f $INIT_SQL_PATH
if [ -n "$POSTGRES_DB_EXISTS" ]; then
  echo "   Database PostgreSQL già esistente, skip creazione utenti con script"
else
  docker run --rm -v $(pwd):/sql postgres:14 \
  bash -c "PGPASSWORD=${POSTGRES_PASSWORD} psql -h ${POSTGRES_HOST} -p 5432 -U ${POSTGRES_ADMIN} -d ${POSTGRES_DATABASE} -f ${INIT_SQL_PATH}"
  check_error "Script PostgreSQL eseguito con successo"
fi

# 12b. Creazione Azure Cache for Redis (tier Basic C0 - 250MB)
echo "🔴 Creazione Azure Cache for Redis..."

REDIS_EXISTS=$(az redis list \
  --resource-group $RESOURCE_GROUP \
  --query "[?name=='$REDIS_NAME'].name" \
  --output tsv 2>/dev/null)
if [ -n "$REDIS_EXISTS" ]; then
  echo "   Azure Cache for Redis già esistente, skip creazione"
else
  az redis create \
    --resource-group $RESOURCE_GROUP \
    --name $REDIS_NAME \
    --location $LOCATION \
    --sku Basic \
    --vm-size C0 \
    --enable-non-ssl-port 
  check_error "Azure Cache for Redis creato"
fi

# Attendi che Redis sia disponibile
echo "⏳ Attesa che Redis sia disponibile..."
MAX_ATTEMPTS=600
ATTEMPT=0
while [ $ATTEMPT -lt $MAX_ATTEMPTS ]; do
  REDIS_STATE=$(az redis show \
    --resource-group $RESOURCE_GROUP \
    --name $REDIS_NAME \
    --query 'provisioningState' \
    --output tsv 2>/dev/null)
  
  if [ "$REDIS_STATE" = "Succeeded" ]; then
    echo "✅ Redis disponibile!"
    break
  fi
  
  ATTEMPT=$((ATTEMPT + 1))
  echo "   Tentativo $ATTEMPT/$MAX_ATTEMPTS - Stato: ${REDIS_STATE:-Creating}"
  sleep 10
done

if [ $ATTEMPT -eq $MAX_ATTEMPTS ]; then
  echo "❌ Timeout: Redis non è diventato disponibile"
  exit 1
fi

# Recupero endpoint e chiavi Redis
REDIS_HOST=$(az redis show \
  --resource-group $RESOURCE_GROUP \
  --name $REDIS_NAME \
  --query 'hostName' \
  --output tsv)
check_error "Redis host recuperato"

REDIS_PORT=$(az redis show \
  --resource-group $RESOURCE_GROUP \
  --name $REDIS_NAME \
  --query 'sslPort' \
  --output tsv)
check_error "Redis SSL port recuperato"
REDIS_SSL="true"

REDIS_KEY=$(az redis list-keys \
  --resource-group $RESOURCE_GROUP \
  --name $REDIS_NAME \
  --query 'primaryKey' \
  --output tsv)
check_error "Redis key recuperata"

echo "Redis Host: $REDIS_HOST"
echo "Redis Port: $REDIS_PORT"
echo "Redis Key: ${REDIS_KEY:0:20}..."

# 12c. Creazione Azure Event Hubs (Kafka-compatible)
echo "📨 Creazione Azure Event Hubs Namespace (Kafka-compatible)..."
# Verifica se il namespace esiste già
EVENTHUBS_EXISTS=$(az eventhubs namespace exists \
  --name "$EVENT_HUBS_NAMESPACE" \
  --output json 2>/dev/null | jq -r '.nameAvailable')

if [ "$EVENTHUBS_EXISTS" = "false" ]; then
  echo "   Event Hubs Namespace già esistente, skip creazione"
else
  az eventhubs namespace create \
    --resource-group $RESOURCE_GROUP \
    --name $EVENT_HUBS_NAMESPACE \
    --location $LOCATION \
    --sku Standard
    # Basic
    # Il tuo namespace Event Hubs è stato creato con il tier Basic, che non supporta il protocollo Kafka.
  check_error "Event Hubs Namespace creato"
fi

# Attendi che Event Hubs sia disponibile
echo "⏳ Attesa che Event Hubs Namespace sia disponibile..."
MAX_ATTEMPTS_EH=60
ATTEMPT_EH=0
while [ $ATTEMPT_EH -lt $MAX_ATTEMPTS_EH ]; do
  EVENTHUBS_STATE=$(az eventhubs namespace show \
    --resource-group $RESOURCE_GROUP \
    --name $EVENT_HUBS_NAMESPACE \
    --query 'provisioningState' \
    --output tsv 2>/dev/null)
  
  if [ "$EVENTHUBS_STATE" = "Succeeded" ]; then
    echo "✅ Event Hubs Namespace disponibile!"
    break
  fi
  
  ATTEMPT_EH=$((ATTEMPT_EH + 1))
  echo "   Tentativo $ATTEMPT_EH/$MAX_ATTEMPTS_EH - Stato: ${EVENTHUBS_STATE:-Creating}"
  sleep 10
done

if [ $ATTEMPT_EH -eq $MAX_ATTEMPTS_EH ]; then
  echo "❌ Timeout: Event Hubs Namespace non è diventato disponibile"
  exit 1
fi

# Creazione Event Hub (topic Kafka)
echo "📋 Creazione Event Hub 'annotazioni'..."
EVENTHUB_EXISTS=$(az eventhubs eventhub list \
  --resource-group $RESOURCE_GROUP \
  --namespace-name $EVENT_HUBS_NAMESPACE \
  --query "[?name=='$EVENT_HUB_NAME'].name" \
  --output tsv 2>/dev/null)

if [ -n "$EVENTHUB_EXISTS" ]; then
  echo "   Event Hub già esistente, skip creazione"
else
  az eventhubs eventhub create \
    --resource-group $RESOURCE_GROUP \
    --namespace-name $EVENT_HUBS_NAMESPACE \
    --name $EVENT_HUB_NAME \
    --retention-time 1 \
    --cleanup-policy Delete \
    --partition-count 2
  check_error "Event Hub 'annotazioni' creato"
fi

# Recupero connection string Event Hubs
echo "🔑 Recupero connection string Event Hubs..."
EVENTHUBS_CONNECTION_STRING=$(az eventhubs namespace authorization-rule keys list \
  --resource-group $RESOURCE_GROUP \
  --namespace-name $EVENT_HUBS_NAMESPACE \
  --name RootManageSharedAccessKey \
  --query 'primaryConnectionString' \
  --output tsv)
check_error "Event Hubs connection string recuperata"

# Tolto perchè non serve
# EVENTHUBS_CONNECTION_STRING_ORIG=$EVENTHUBS_CONNECTION_STRING
# # Se la connection string inizia per 'Endpoint=', rimuovi la parte 'Endpoint=...;'
# if [[ $EVENTHUBS_CONNECTION_STRING == Endpoint=* ]]; then
#   EVENTHUBS_CONNECTION_STRING=$(echo "$EVENTHUBS_CONNECTION_STRING" | sed 's/^Endpoint=[^;]*;//')
# fi
# 
# # Estrai SharedAccessKeyName e SharedAccessKey in variabili distinte
# EVENTHUBS_KEY_NAME=$(echo "$EVENTHUBS_CONNECTION_STRING" | sed -n 's/.*SharedAccessKeyName=\([^;]*\);.*/\1/p')
# EVENTHUBS_KEY=$(echo "$EVENTHUBS_CONNECTION_STRING" | sed -n 's/.*SharedAccessKey=\([^;]*\).*/\1/p')

# Estrai endpoint Kafka-compatible
EVENTHUBS_KAFKA_ENDPOINT="${EVENT_HUBS_NAMESPACE}.servicebus.windows.net:9093"

echo "Event Hubs Namespace: $EVENT_HUBS_NAMESPACE"
echo "Event Hub Name: $EVENT_HUB_NAME"
echo "Kafka Endpoint: $EVENTHUBS_KAFKA_ENDPOINT"
echo "Connection String: ${EVENTHUBS_CONNECTION_STRING:0:50}..."

# 13. Verifica risorse create
echo ""
echo "📊 Riepilogo risorse create:"
az resource list \
  --resource-group $RESOURCE_GROUP \
  --output table

# 14. Salvataggio variabili d'ambiente in file .env
echo ""
echo "💾 Salvataggio variabili d'ambiente in .env..."
cat > .env-azure-dbremoti-mongo-runlocale << EOF
SPRING_PROFILES_ACTIVE=kube
POSTGRES_HOST=$POSTGRES_HOST
POSTGRES_URL=jdbc:postgresql://${POSTGRES_HOST}:5432/${POSTGRES_DATABASE}?sslmode=require
POSTGRES_DATABASE=$POSTGRES_DATABASE
SPRING_DATASOURCE_USERNAME=$POSTGRES_ADMIN
SPRING_DATASOURCE_PASSWORD=$POSTGRES_PASSWORD
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
MONGODB_URI=${MONGODB_URI}
SPRING_DATA_MONGODB_URI=mongodb://demo:demo@${MONGODB_URI}:27017/${COSMOSDB_DATABASE}?authSource=admin
COSMOSDB_DATABASE=$COSMOSDB_DATABASE
REDIS_HOST=$REDIS_HOST
REDIS_PORT=$REDIS_PORT
REDIS_PASSWORD=$REDIS_KEY
REDIS_SSL=$REDIS_SSL
KAFKA_BOOTSTRAP_SERVERS=$EVENTHUBS_KAFKA_ENDPOINT
KAFKA_TOPIC=$EVENT_HUB_NAME
EVENTHUBS_CONNECTION_STRING=$EVENTHUBS_CONNECTION_STRING
EVENTHUBS_KAFKA_ENDPOINT=$EVENTHUBS_KAFKA_ENDPOINT
EOF
echo "✅ File .env-azure-dbremoti-mongo-runlocale creato"

# 15. Stampa riepilogo connessioni
echo ""
echo "🔗 Parametri di connessione:"
echo "   PostgreSQL: $POSTGRES_HOST"
echo "   PostgreSQL Database: $POSTGRES_DATABASE"
echo "   MongoDB URI: ${MONGODB_URI:0:50}..."
echo "   MongoDB Database: $COSMOSDB_DATABASE"
echo "   Redis: $REDIS_HOST:$REDIS_PORT (SSL)"
echo "   Event Hubs (Kafka): $EVENTHUBS_KAFKA_ENDPOINT"
echo "   Event Hub Name: $EVENT_HUB_NAME"
echo ""

# 16. Avvio servizio con i parametri corretti
echo "🚀 Avvio container Docker..."

# Verifica se il container è già in esecuzione
if docker ps -a --format '{{.Names}}' | grep -q "^azure-dbremoti-mongo-runlocale$"; then
  echo "   Container già esistente, rimozione..."
  docker stop azure-dbremoti-mongo-runlocale 2>/dev/null || true
  docker rm azure-dbremoti-mongo-runlocale 2>/dev/null || true
fi

docker run --rm -p 8082:8080  --name azure-dbremoti-mongo-runlocale \
    -e SPRING_PROFILES_ACTIVE=kube \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://${POSTGRES_HOST}:5432/${POSTGRES_DATABASE}?sslmode=require" \
    -e SPRING_DATASOURCE_USERNAME=$POSTGRES_ADMIN \
    -e SPRING_DATASOURCE_PASSWORD=$POSTGRES_PASSWORD \
    -e SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver \
    -e SPRING_DATA_MONGODB_URI=$MONGODB_URI \
    -e SPRING_DATA_MONGODB_DATABASE=$COSMOSDB_DATABASE \
    -e REDIS_HOST=$REDIS_HOST \
    -e REDIS_PORT=$REDIS_PORT \
    -e REDIS_PASSWORD=$REDIS_KEY \
    -e REDIS_SSL=$REDIS_SSL \
    -e SPRING_KAFKA_BOOTSTRAP_SERVERS=$EVENTHUBS_KAFKA_ENDPOINT \
    -e KAFKA_SASL_MECHANISM=PLAIN \
    -e KAFKA_SASL_JAAS_CONFIG="org.apache.kafka.common.security.plain.PlainLoginModule required username=\"\$ConnectionString\" password=\"${EVENTHUBS_CONNECTION_STRING}\";" \
    -e KAFKA_SECURITY_PROTOCOL=SASL_SSL \
    -e KAFKA_TOPIC_NAME=$EVENT_HUB_NAME \
    -e KAFKA_BROKER_URL=$EVENTHUBS_KAFKA_ENDPOINT \
    -e ANNOTAZIONE_INVIO_ENABLED=true \
    alnao/gestioneannotazioni:latest &

echo "✅ Applicazione avviata su http://localhost:8082"