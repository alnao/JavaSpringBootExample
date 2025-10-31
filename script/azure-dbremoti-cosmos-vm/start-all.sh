#!/bin/bash
echo "🚀 Avvio creazione risorse Azure CosmosDB + SQL Server..."
#./script/azure-dbremoti-cosmos-vm/start-all.sh  

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
SERVICEBUS_QUEUE="eventbus-annotazioni"

# Parametri VM
VM_NAME="gestioneannotazioni-vm"
VM_USER="gestioneannotazioni"
VM_IMAGE="Debian11" #Debian11', 'OpenSuseLeap154Gen2', 'RHELRaw8LVMGen2', 'SuseSles15SP5', 'Ubuntu2204', 'Ubuntu2404', 'Ubuntu2404Pro',
VM_SIZE="Standard_B1s"
SSH_KEY_PATH="$HOME/.ssh/azure-vm-key.pub" # Modifica se necessario
VM_PUBLIC_IP="" # Sarà valorizzato dopo la creazione

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

# 4. Avvio creazione CosmosDB con API SQL (tier Free)
echo "🌐 Avvio creazione CosmosDB..."
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

# 5. Creazione database SQL in CosmosDB
echo "💾 Creazione database in CosmosDB..."
az cosmosdb sql database create \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --name $COSMOSDB_DATABASE
check_error "Database Cosmos creato"

# 6. Creazione container per annotazioni
echo "📋 Creazione container annotazioni..."
az cosmosdb sql container create \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --database-name $COSMOSDB_DATABASE \
  --name annotazioni \
  --partition-key-path "/id" \
  --throughput 400
check_error "Container annotazioni creato"

# 7. Creazione container per storico NON SERVE? non so!
echo "📜 Creazione container storico..."
az cosmosdb sql container create \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --database-name $COSMOSDB_DATABASE \
  --name annotazione_storico_stati \
  --partition-key-path "/id" \
  --throughput 400
check_error "Container storico creato"

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

# 10. Configurazione firewall per accesso locale (IMPORTANTE!)
echo "🔥 Configurazione firewall SQL Server..."
MY_IP=$(curl -s ifconfig.me)
echo "Il tuo IP pubblico: $MY_IP"

az sql server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --server $SQLSERVER_NAME \
  --name AllowMyIP \
  --start-ip-address $MY_IP \
  --end-ip-address $MY_IP
check_error "Regola firewall per IP locale creata"

# 11. Abilitazione accesso servizi Azure
az sql server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --server $SQLSERVER_NAME \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0
check_error "Regola firewall per servizi Azure creata"

# 12. Creazione database SQL Server (tier Free: Basic con 5 DTU)
echo "💾 Creazione database SQL Server..."
az sql db create \
  --resource-group $RESOURCE_GROUP \
  --server $SQLSERVER_NAME \
  --name $SQLSERVER_DATABASE \
  --service-objective Basic \
  --max-size 2GB
check_error "Database SQL Server creato"

# 13. Recupero connection string SQL Server
SQLSERVER_HOST="${SQLSERVER_NAME}.database.windows.net"
echo "SQL Server Host: $SQLSERVER_HOST"
echo "SQL Server Connection String: Server=tcp:${SQLSERVER_HOST},1433;Database=${SQLSERVER_DATABASE};User ID=${SQLSERVER_ADMIN};Password=***;Encrypt=true;TrustServerCertificate=false;Connection Timeout=30;"
# 12.3. Esecuzione script di inizializzazione SQL
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

# 14. Creazione Service Bus namespace e coda EventBus
echo "📨 Creazione Service Bus namespace e coda EventBus..."

az servicebus namespace create \
  --resource-group $RESOURCE_GROUP \
  --name $SERVICEBUS_NAMESPACE \
  --location $LOCATION \
  --sku Standard
check_error "Service Bus namespace creato"

az servicebus queue create \
  --resource-group $RESOURCE_GROUP \
  --namespace-name $SERVICEBUS_NAMESPACE \
  --name $SERVICEBUS_QUEUE
check_error "Service Bus queue creata"

# Recupero connection string Service Bus
AZURE_SERVICEBUS_CONNECTION_STRING=$(az servicebus namespace authorization-rule keys list \
  --resource-group $RESOURCE_GROUP \
  --namespace-name $SERVICEBUS_NAMESPACE \
  --name RootManageSharedAccessKey \
  --query 'primaryConnectionString' \
  --output tsv)
check_error "Connection string Service Bus recuperata"

# 15. Verifica risorse create
echo ""
echo "📊 Riepilogo risorse create:"
az resource list \
  --resource-group $RESOURCE_GROUP \
  --output table

# 16. Salvataggio variabili d'ambiente in file .env
echo ""
echo "💾 Salvataggio variabili d'ambiente in .env..."
cat > .env-azure-dbremoti-cosmos-vm << EOF
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

MSSQL_SQLSERVER_HOST=$SQLSERVER_HOST
MSSQL_SQLSERVER_PORT=1433
MSSQL_SQLSERVER_ENCRYPT=true
MSSQL_SQLSERVER_TRUST_SERVER_CERTIFICATE=trustServerCertificate=false
MSSQL_SPRING_DATASOURCE_USERNAME=$SQLSERVER_ADMIN
MSSQL_SPRING_DATASOURCE_PASSWORD=$SQLSERVER_PASSWORD
MSSQL_SQLSERVER_DATABASE=$SQLSERVER_DATABASE
AZURE_COSMOS_ENABLED=true
AZURE_COSMOS_DISABLE_SSL_VERIFICATION=false
ANNOTAZIONE_INVIO_ENABLED=true
AZURE_SERVICEBUS_QUEUE_NAME=$SERVICEBUS_QUEUE
EOF
echo "✅ File .env-azure-dbremoti-cosmos-vm creato"

# 17. Stampa riepilogo connessioni
echo ""
echo "🔗 Parametri di connessione:"
echo "   SQL Server: $SQLSERVER_HOST"
echo "   SQL Database: $SQLSERVER_DATABASE"
echo "   Cosmos URI: $AZURE_COSMOS_URI"
echo "   Cosmos Database: $COSMOSDB_DATABASE"
echo ""

# 18. Avvio servizio con i parametri corretti# Parametri VM Azure

# --- AVVIO SU VM DEBIAN IN AZURE ---
  # 1. Crea la VM Debian (se non esiste già)
  echo "🌩️  Creazione VM Debian su Azure..."
  az vm create \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --image $VM_IMAGE \
    --admin-username $VM_USER \
    --size $VM_SIZE \
    --ssh-key-values $SSH_KEY_PATH \
    --public-ip-sku Standard \
    --output json > vm_create_output.json
  check_error "VM $VM_IMAGE creata"

  # 2. Recupera l'IP pubblico della VM
  VM_PUBLIC_IP=$(az vm show -d -g $RESOURCE_GROUP -n $VM_NAME --query publicIps -o tsv)
  echo "IP pubblico VM: $VM_PUBLIC_IP"

  # 2.1 Apertura porta 8082 per l'applicazione
  echo "🔓 Apertura porta 8082 sul Network Security Group..."
  az vm open-port \
    --resource-group $RESOURCE_GROUP \
    --name $VM_NAME \
    --port 8082 \
    --priority 1001
  check_error "Porta 8082 aperta"

  # 2.2 Configurazione firewall SQL Server per VM
  echo "🔥 Aggiunta IP della VM al firewall SQL Server..."
  az sql server firewall-rule create \
    --resource-group $RESOURCE_GROUP \
    --server $SQLSERVER_NAME \
    --name AllowVMIP \
    --start-ip-address $VM_PUBLIC_IP \
    --end-ip-address $VM_PUBLIC_IP
  check_error "Regola firewall per VM aggiunta a SQL Server"


  # 3. Installa Docker sulla VM (se non già presente)
  echo "🔧 Installazione Docker su VM..."
  ssh -o StrictHostKeyChecking=no -i $SSH_KEY_PATH $VM_USER@$VM_PUBLIC_IP "sudo apt-get update && sudo apt-get install -y docker.io && sudo usermod -aG docker $VM_USER"
  check_error "Docker installato su VM"

  # 4. Copia il file .env sulla VM
  echo "📤 Copia file .env sulla VM..."
  scp -i $SSH_KEY_PATH .env-azure-dbremoti-cosmos-vm $VM_USER@$VM_PUBLIC_IP:/home/$VM_USER/
  check_error "File .env copiato"

  # 5. Avvia il container Docker sulla VM
  echo "🚀 Avvio container Docker sulla VM..."
  ssh -i $SSH_KEY_PATH $VM_USER@$VM_PUBLIC_IP "docker pull alnao/gestioneannotazioni:latest && docker run --rm -d -p 8082:8080 --name azure-dbremoti-cosmos-vm --env-file /home/$VM_USER/.env-azure-dbremoti-cosmos-vm alnao/gestioneannotazioni:latest"
  check_error "Container avviato sulla VM"


echo "🎉 Tutto pronto! Puoi iniziare a utilizzare l'applicazione con Azure Cosmos DB e SQL Server remoti."
echo "✅ Applicazione avviata su http://$VM_PUBLIC_IP:8082"
