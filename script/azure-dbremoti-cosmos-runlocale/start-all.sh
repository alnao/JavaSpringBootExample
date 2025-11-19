#!/bin/bash
echo "🚀 Avvio creazione risorse Azure CosmosDB + SQL Server..."
#./script/azure-dbremoti-cosmos-runlocale/start-all.sh  

# 1. Variabili di configurazione
RESOURCE_GROUP="gestioneannotazioni-rg-cosmos-mssql"
LOCATION="northeurope"  # Alternative: francecentral, germanywestcentral, uksouth, swedencentral
COSMOSDB_ACCOUNT="gestioneannotazioni-cosmos"
COSMOSDB_DATABASE="annotazioni"
SQLSERVER_NAME="gestioneannotazioni-sql"
SQLSERVER_DATABASE="annotazioni"
SQLSERVER_ADMIN="sqladmin"
SQLSERVER_PASSWORD="P@ssw0rd123!"
SERVICEBUS_NAMESPACE="gestioneannotazioni-servicebus"
SERVICEBUS_QUEUE="gestioneannotazioni-queue"
REDIS_NAME="gestioneannotazioni-redis"

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

# Funzione per attendere che SQL Server sia pronto
wait_for_sql_server() {
    local server=$1
    local rg=$2
    local max_attempts=60  # 10 minuti
    local attempt=0
    
    echo "⏳ Attesa completamento provisioning SQL Server..."
    
    while [ $attempt -lt $max_attempts ]; do
        attempt=$((attempt + 1))
        
        status=$(az sql server show \
            --name $server \
            --resource-group $rg \
            --query "state" \
            --output tsv 2>/dev/null)
        
        if [ "$status" = "Ready" ]; then
            echo "✅ SQL Server pronto!"
            return 0
        fi
        
        echo "   Tentativo $attempt/$max_attempts - Stato: ${status:-Initializing}"
        sleep 10
    done
    
    echo "❌ Timeout: SQL Server non è diventato disponibile"
    return 1
}

# 2. Login ad Azure
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

# 4. Avvio creazione CosmosDB con API SQL (tier Free)
echo "🌐 Avvio creazione CosmosDB..."
COSMOS_EXISTS=$(az cosmosdb check-name-exists --name $COSMOSDB_ACCOUNT --output tsv)
if [ "$COSMOS_EXISTS" = "true" ]; then
  echo "   CosmosDB già esistente, skip creazione"
  wait_for_cosmos_db $COSMOSDB_ACCOUNT $RESOURCE_GROUP
else
  az cosmosdb create \
    --name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --kind GlobalDocumentDB \
    --default-consistency-level Session \
    --enable-free-tier true \
    --locations regionName=$LOCATION failoverPriority=0 isZoneRedundant=false 
  
  echo "✅ Creazione Cosmos DB avviata"
  
  # Attesa attiva che CosmosDB sia completamente disponibile
  wait_for_cosmos_db $COSMOSDB_ACCOUNT $RESOURCE_GROUP
  check_error "Cosmos DB disponibile"
fi

# 5. Creazione database SQL in CosmosDB
echo "💾 Creazione database in CosmosDB..."
DB_EXISTS=$(az cosmosdb sql database exists \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --name $COSMOSDB_DATABASE \
  --output tsv 2>/dev/null || echo "false")
if [ "$DB_EXISTS" = "true" ]; then
  echo "   Database Cosmos già esistente, skip creazione"
else
  az cosmosdb sql database create \
    --account-name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --name $COSMOSDB_DATABASE
  check_error "Database Cosmos creato"
fi

# 6. Creazione container per annotazioni
echo "📋 Creazione container annotazioni..."
CONTAINER_EXISTS=$(az cosmosdb sql container exists \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --database-name $COSMOSDB_DATABASE \
  --name annotazioni \
  --output tsv 2>/dev/null || echo "false")
if [ "$CONTAINER_EXISTS" = "true" ]; then
  echo "   Container annotazioni già esistente, skip creazione"
else
  az cosmosdb sql container create \
    --account-name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --database-name $COSMOSDB_DATABASE \
    --name annotazioni \
    --partition-key-path "/id" \
    --throughput 400
  check_error "Container annotazioni creato"
fi

# 7. Creazione container per storico NON SERVE? non so!
echo "📜 Creazione container storico..."
CONTAINER_STORICO_EXISTS=$(az cosmosdb sql container exists \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --database-name $COSMOSDB_DATABASE \
  --name annotazione_storico_stati \
  --output tsv 2>/dev/null || echo "false")
if [ "$CONTAINER_STORICO_EXISTS" = "true" ]; then
  echo "   Container storico già esistente, skip creazione"
else
  az cosmosdb sql container create \
    --account-name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --database-name $COSMOSDB_DATABASE \
    --name annotazione_storico_stati \
    --partition-key-path "/id" \
    --throughput 400
  check_error "Container storico creato"
fi

# 8. Recupero endpoint e key CosmosDB
echo "🔑 Recupero credenziali CosmosDB..."
AZURE_COSMOS_URI=$(az cosmosdb show \
  --name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --query "documentEndpoint" \
  --output tsv)
check_error "URI Cosmos recuperato"

AZURE_COSMOS_KEY=$(az cosmosdb keys list \
  --name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --query "primaryMasterKey" \
  --output tsv)
check_error "Key Cosmos recuperata"

echo "Cosmos DB URI: $AZURE_COSMOS_URI"
echo "Cosmos DB Key: ${AZURE_COSMOS_KEY:0:20}..."

# 9. Creazione SQL Server (tier Free: Basic con 5 DTU, 2GB storage)
echo "🗄️  Creazione SQL Server..."
SQLSERVER_EXISTS=$(az sql server list \
  --resource-group $RESOURCE_GROUP \
  --query "[?name=='$SQLSERVER_NAME'].name" \
  --output tsv 2>/dev/null)
if [ -n "$SQLSERVER_EXISTS" ]; then
  echo "   SQL Server già esistente, skip creazione"
  wait_for_sql_server $SQLSERVER_NAME $RESOURCE_GROUP
else
  az sql server create \
    --name $SQLSERVER_NAME \
    --resource-group $RESOURCE_GROUP \
    --location $LOCATION \
    --admin-user $SQLSERVER_ADMIN \
    --admin-password $SQLSERVER_PASSWORD
  
  check_error "Creazione SQL Server avviata"
  
  # Attesa che SQL Server sia pronto
  wait_for_sql_server $SQLSERVER_NAME $RESOURCE_GROUP
  check_error "SQL Server disponibile"
fi

# 10. Configurazione firewall per accesso locale (IMPORTANTE!)
echo "🔥 Configurazione firewall SQL Server..."
MY_IP=$(curl -s ifconfig.me)
echo "Il tuo IP pubblico: $MY_IP"

FIREWALL_RULE_EXISTS=$(az sql server firewall-rule list \
  --resource-group $RESOURCE_GROUP \
  --server $SQLSERVER_NAME \
  --query "[?name=='AllowMyIP'].name" \
  --output tsv 2>/dev/null)
if [ -n "$FIREWALL_RULE_EXISTS" ]; then
  echo "   Regola firewall AllowMyIP già esistente, aggiornamento IP..."
  az sql server firewall-rule update \
    --resource-group $RESOURCE_GROUP \
    --server $SQLSERVER_NAME \
    --name AllowMyIP \
    --start-ip-address $MY_IP \
    --end-ip-address $MY_IP
  check_error "Regola firewall per IP locale aggiornata"
else
  az sql server firewall-rule create \
    --resource-group $RESOURCE_GROUP \
    --server $SQLSERVER_NAME \
    --name AllowMyIP \
    --start-ip-address $MY_IP \
    --end-ip-address $MY_IP
  check_error "Regola firewall per IP locale creata"
fi

# 11. Abilitazione accesso servizi Azure
FIREWALL_AZURE_EXISTS=$(az sql server firewall-rule list \
  --resource-group $RESOURCE_GROUP \
  --server $SQLSERVER_NAME \
  --query "[?name=='AllowAzureServices'].name" \
  --output tsv 2>/dev/null)
if [ -n "$FIREWALL_AZURE_EXISTS" ]; then
  echo "   Regola firewall AllowAzureServices già esistente, skip creazione"
else
  az sql server firewall-rule create \
    --resource-group $RESOURCE_GROUP \
    --server $SQLSERVER_NAME \
    --name AllowAzureServices \
    --start-ip-address 0.0.0.0 \
    --end-ip-address 0.0.0.0
  check_error "Regola firewall per servizi Azure creata"
fi

# 12. Creazione database SQL Server (tier Free: Basic con 5 DTU)
echo "💾 Creazione database SQL Server..."
SQLDB_EXISTS=$(az sql db list \
  --resource-group $RESOURCE_GROUP \
  --server $SQLSERVER_NAME \
  --query "[?name=='$SQLSERVER_DATABASE'].name" \
  --output tsv 2>/dev/null)
if [ -n "$SQLDB_EXISTS" ]; then
  echo "   Database SQL Server già esistente, skip creazione"
else
  az sql db create \
    --resource-group $RESOURCE_GROUP \
    --server $SQLSERVER_NAME \
    --name $SQLSERVER_DATABASE \
    --service-objective Basic \
    --max-size 2GB
  check_error "Database SQL Server creato"
fi

# 13. Recupero connection string SQL Server
SQLSERVER_HOST="${SQLSERVER_NAME}.database.windows.net"
echo "SQL Server Host: $SQLSERVER_HOST"
echo "SQL Server Connection String: Server=tcp:${SQLSERVER_HOST},1433;Database=${SQLSERVER_DATABASE};User ID=${SQLSERVER_ADMIN};Password=***;Encrypt=true;TrustServerCertificate=false;Connection Timeout=30;"
# 12.3. Esecuzione script di inizializzazione SQL
if [ -n "$SQLDB_EXISTS" ]; then
  echo "   Database SQL Server già esistente, skip creazione utenti con script SQL"
else
  echo "📝 Esecuzione script di inizializzazione SQL..."
  INIT_SQL_PATH="./script/init-database/init-mssql.sql"
  # Leggi il contenuto dello script
  #SQL_CONTENT=$(cat $INIT_SQL_PATH)
  # Esegui tramite Docker con sqlcmd
  docker run --rm \
      -v "$(pwd):/workspace" \
      -w /workspace \
      mcr.microsoft.com/mssql-tools \
      /opt/mssql-tools/bin/sqlcmd \
      -S $SQLSERVER_HOST \
      -d $SQLSERVER_DATABASE \
      -U $SQLSERVER_ADMIN \
      -P "$SQLSERVER_PASSWORD" \
      -i $INIT_SQL_PATH
  check_error "Script SQL eseguito con successo"
fi

# 14. Creazione Service Bus namespace e coda EventBus
echo "📨 Creazione Service Bus namespace e coda EventBus..."

SERVICEBUS_AVAILABLE=$(az servicebus namespace exists \
  --name $SERVICEBUS_NAMESPACE \
  --query "nameAvailable" \
  --output tsv 2>/dev/null || echo "false")
  # Namespace name available        True    None
SERVICEBUS_AVAILABLE=$(echo "$SERVICEBUS_AVAILABLE" | tr '[:upper:]' '[:lower:]')

if [ "$SERVICEBUS_AVAILABLE" = "true" ]; then 
  az servicebus namespace create \
    --resource-group $RESOURCE_GROUP \
    --name $SERVICEBUS_NAMESPACE \
    --location $LOCATION \
    --sku Standard
  check_error "Service Bus namespace creato"
else
  echo "   Service Bus namespace già esistente, skip creazione"
fi

QUEUE_EXISTS=$(az servicebus queue show \
  --resource-group $RESOURCE_GROUP \
  --namespace-name $SERVICEBUS_NAMESPACE \
  --name $SERVICEBUS_QUEUE \
  --output tsv 2>/dev/null)
if [ -z "$QUEUE_EXISTS" ]; then
  az servicebus queue create \
    --resource-group $RESOURCE_GROUP \
    --namespace-name $SERVICEBUS_NAMESPACE \
    --name $SERVICEBUS_QUEUE
  check_error "Service Bus queue creata"
else
  echo "   Service Bus queue già esistente, skip creazione"
fi

# Recupero connection string Service Bus
AZURE_SERVICEBUS_CONNECTION_STRING=$(az servicebus namespace authorization-rule keys list \
  --resource-group $RESOURCE_GROUP \
  --namespace-name $SERVICEBUS_NAMESPACE \
  --name RootManageSharedAccessKey \
  --query 'primaryConnectionString' \
  --output tsv)
check_error "Connection string Service Bus recuperata"

# 14b. Creazione Azure Cache for Redis (tier Basic C0 - 250MB)
echo "🔴 Creazione Azure Cache for Redis..."

REDIS_EXISTS=$(az redis list \
  --resource-group $RESOURCE_GROUP \
  --query "[?name=='$REDIS_NAME'].name" \
  --output tsv 2>/dev/null)
if [ -n "$REDIS_EXISTS" ]; then
  echo "   Azure Cache for Redis già esistente, skip creazione"
else
  echo "Parto la creazione di Azure Cache for Redis..."
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

# 15. Verifica risorse create
echo ""
echo "📊 Riepilogo risorse create:"
az resource list \
  --resource-group $RESOURCE_GROUP \
  --output table

# 16. Salvataggio variabili d'ambiente in file .env
echo ""
echo "💾 Salvataggio variabili d'ambiente in .env..."
cat > .env-azure-dbremoti-cosmos-runlocale << EOF
SPRING_PROFILES_ACTIVE=azure
RESOURCE_GROUP=$RESOURCE_GROUP
SQLSERVER_HOST=$SQLSERVER_HOST
SQLSERVER_ADMIN=$SQLSERVER_ADMIN
SQLSERVER_PASSWORD=$SQLSERVER_PASSWORD
SQLSERVER_DATABASE=$SQLSERVER_DATABASE
AZURE_COSMOS_URI=$AZURE_COSMOS_URI
AZURE_COSMOS_KEY=$AZURE_COSMOS_KEY
AZURE_COSMOS_DATABASE=$COSMOSDB_DATABASE
AZURE_SERVICEBUS_CONNECTION_STRING=$AZURE_SERVICEBUS_CONNECTION_STRING
SERVICEBUS_QUEUE=$SERVICEBUS_QUEUE
SERVICEBUS_NAMESPACE=$SERVICEBUS_NAMESPACE
COSMOSDB_DATABASE="annotazioni"
REDIS_HOST=$REDIS_HOST
REDIS_PORT=$REDIS_PORT
REDIS_PASSWORD=$REDIS_KEY
REDIS_SSL=$REDIS_SSL
EOF
echo "✅ File .env-azure-dbremoti-cosmos-runlocale creato"

# 17. Stampa riepilogo connessioni
echo ""
echo "🔗 Parametri di connessione:"
echo "   SQL Server: $SQLSERVER_HOST"
echo "   SQL Database: $SQLSERVER_DATABASE"
echo "   Cosmos URI: $AZURE_COSMOS_URI"
echo "   Cosmos Database: $COSMOSDB_DATABASE"
echo "   Redis: $REDIS_HOST:$REDIS_PORT"
echo "   Service Bus: $SERVICEBUS_NAMESPACE"
echo ""

# 18. Avvio servizio con i parametri corretti
# mvn clean package -DskipTests
# docker build -t alnao/gestioneannotazioni:latest .
# docker push alnao/gestioneannotazioni:latest
echo "🚀 Avvio container Docker..."

# Verifica se il container è già in esecuzione
if docker ps -a --format '{{.Names}}' | grep -q "^azure-dbremoti-cosmos-runlocale$"; then
  echo "   Container già esistente, rimozione..."
  docker stop azure-dbremoti-cosmos-runlocale 2>/dev/null || true
  docker rm azure-dbremoti-cosmos-runlocale 2>/dev/null || true
fi

docker run --rm -p 8082:8080 --name azure-dbremoti-cosmos-runlocale  \
    -e SPRING_PROFILES_ACTIVE=azure \
    -e MSSQL_SQLSERVER_HOST=$SQLSERVER_HOST \
    -e MSSQL_SQLSERVER_PORT=1433 \
    -e MSSQL_SQLSERVER_ENCRYPT=true \
    -e MSSQL_SQLSERVER_TRUST_SERVER_CERTIFICATE=trustServerCertificate=false \
    -e MSSQL_SPRING_DATASOURCE_USERNAME=$SQLSERVER_ADMIN \
    -e MSSQL_SPRING_DATASOURCE_PASSWORD=$SQLSERVER_PASSWORD \
    -e MSSQL_SQLSERVER_DATABASE=$SQLSERVER_DATABASE \
    -e AZURE_COSMOS_URI=$AZURE_COSMOS_URI \
    -e AZURE_COSMOS_KEY=$AZURE_COSMOS_KEY \
    -e AZURE_COSMOS_DATABASE=$COSMOSDB_DATABASE \
    -e AZURE_COSMOS_ENABLED=true \
    -e AZURE_COSMOS_DISABLE_SSL_VERIFICATION=false \
    -e ANNOTAZIONE_INVIO_ENABLED=true \
    -e AZURE_SERVICEBUS_CONNECTION_STRING=$AZURE_SERVICEBUS_CONNECTION_STRING \
    -e AZURE_SERVICEBUS_QUEUE_NAME=$SERVICEBUS_QUEUE \
    -e REDIS_HOST=$REDIS_HOST \
    -e REDIS_PORT=$REDIS_PORT \
    -e REDIS_PASSWORD=$REDIS_KEY \
    -e REDIS_SSL=$REDIS_SSL \
    alnao/gestioneannotazioni:latest &

echo "✅ Applicazione avviata su http://localhost:8082"
echo "🎉 Tutto pronto! Puoi iniziare a utilizzare l'applicazione con Azure Cosmos DB e SQL Server remoti."
