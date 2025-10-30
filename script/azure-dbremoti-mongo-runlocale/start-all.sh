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
az login
check_error "Login completato"

# 3. Creazione Resource Group
echo "📦 Creazione Resource Group..."
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION
check_error "Resource Group creato"

# 4. Avvio creazione CosmosDB con API MongoDB (tier Free)
echo "🌐 Avvio creazione CosmosDB MongoDB..."
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

# 5. Creazione database MongoDB in CosmosDB
echo "💾 Creazione database MongoDB in CosmosDB..."
az cosmosdb mongodb database create \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --name $COSMOSDB_DATABASE
check_error "Database MongoDB creato"

# 6. Creazione collection per annotazioni
echo "📋 Creazione collection annotazioni..."
az cosmosdb mongodb collection create \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --database-name $COSMOSDB_DATABASE \
  --name annotazioni \
  --shard "_id" \
  --throughput 400
check_error "Collection annotazioni creata"

# 7. Creazione collection per storico
echo "📜 Creazione collection storico..."
az cosmosdb mongodb collection create \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --database-name $COSMOSDB_DATABASE \
  --name annotazioni_storico \
  --shard "_id" \
  --throughput 400
check_error "Collection storico creata"

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

# 10. Creazione database PostgreSQL
echo "💾 Creazione database PostgreSQL..."
az postgres flexible-server db create \
  --resource-group $RESOURCE_GROUP \
  --server-name $POSTGRES_NAME \
  --database-name $POSTGRES_DATABASE
check_error "Database PostgreSQL creato"

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
docker run --rm -v $(pwd):/sql postgres:14 \
  bash -c "PGPASSWORD=${POSTGRES_PASSWORD} psql -h ${POSTGRES_HOST} -p 5432 -U ${POSTGRES_ADMIN} -d ${POSTGRES_DATABASE} -f ${INIT_SQL_PATH}"

check_error "Script PostgreSQL eseguito con successo"

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
EOF
echo "✅ File .env-azure-dbremoti-mongo-runlocale creato"

# 15. Stampa riepilogo connessioni
echo ""
echo "🔗 Parametri di connessione:"
echo "   PostgreSQL: $POSTGRES_HOST"
echo "   PostgreSQL Database: $POSTGRES_DATABASE"
echo "   MongoDB URI: ${MONGODB_URI:0:50}..."
echo "   MongoDB Database: $COSMOSDB_DATABASE"
echo ""

# 16. Avvio servizio con i parametri corretti
echo "🚀 Avvio container Docker..."
docker run --rm -p 8082:8080  --name azure-dbremoti-mongo-runlocale \
    -e SPRING_PROFILES_ACTIVE=kube \
    -e SPRING_DATASOURCE_URL="jdbc:postgresql://${POSTGRES_HOST}:5432/${POSTGRES_DATABASE}?sslmode=require" \
    -e SPRING_DATASOURCE_USERNAME=$POSTGRES_ADMIN \
    -e SPRING_DATASOURCE_PASSWORD=$POSTGRES_PASSWORD \
    -e SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver \
    -e SPRING_DATA_MONGODB_URI=$MONGODB_URI \
    -e SPRING_DATA_MONGODB_DATABASE=$COSMOSDB_DATABASE \
    -e ANNOTAZIONE_INVIO_ENABLED=false \
    alnao/gestioneannotazioni:latest &

echo "✅ Applicazione avviata su http://localhost:8082"