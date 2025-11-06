#!/bin/bash

# filepath: script/azure-dbremoti-cosmos-aci/start-all.sh

set -e
#export AWS_PAGER=""

# Variabili di configurazione
RESOURCE_GROUP="gestioneannotazioni-aci-rg"
LOCATION="westeurope"  #eastus - northeurope - southcentralus (Stati Uniti, Texas)
    #Per evitare problemi di saturazione e costi elevati, usa preferibilmente "westeurope" o "francecentral".
    # az cosmosdb list-locations
CONTAINER_NAME="gestioneannotazioni-aci"
CONTAINER_IMAGE="alnao/gestioneannotazioni:latest"
CONTAINER_PORT=8080
COSMOSDB_ACCOUNT="gestioneannotazioni-cosmos-$(date +%s)"
SQLSERVER_NAME="gestioneannotazioni-sql-$(date +%s)"
SQLSERVER_ADMIN="sqladmin"
SQLSERVER_PASSWORD="P@ssw0rd123!"
SQLSERVER_DATABASE="gestioneannotazioni"
INIT_SQL_PATH="./script/init-database/init-mssql.sql"
STORAGE_ACCOUNT="gestioneannotazioni-sa-$(date +%s | tail -c 10)"
# Crea Virtual Network e Subnet per ACI
NSG_NAME="gestioneannotazioni-aci-nsg"
VNET_NAME="gestioneannotazioni-aci-vnet"
SUBNET_NAME="gestioneannotazioni-aci-subnet"

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
    local max_attempts=10  # 10 minuti (10 * 60 secondi)
    local attempt=0

    echo "⏳ Attesa completamento provisioning Cosmos DB (può richiedere fino a 10 minuti)..."

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

# 1. Login ad Azure
echo "🔐 Login ad Azure..."
az login
check_error "Login completato"

# 2. Creazione Resource Group
echo "📦 Creazione Resource Group..."
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION
check_error "Resource Group creato"

# 3. Creazione CosmosDB (Free Tier)
echo "🌐 Creazione CosmosDB..."
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

# 4. Creazione database e container in CosmosDB
echo "💾 Creazione database e container..."
az cosmosdb sql database create \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --name gestioneannotazioni
check_error "Database Cosmos creato"

az cosmosdb sql container create \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --database-name gestioneannotazioni \
  --name annotazioni \
  --partition-key-path "/id" \
  --throughput 400
check_error "Container annotazioni creato"

az cosmosdb sql container create \
  --account-name $COSMOSDB_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --database-name gestioneannotazioni \
  --name annotazione_storico_stati \
  --partition-key-path "/id" \
  --throughput 400
check_error "Container storico creato"

# 5. Recupero CosmosDB credentials
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

# 6. Creazione SQL Server
echo "🗄️  Creazione SQL Server..."


az sql server create \
  --resource-group $RESOURCE_GROUP \
  --name $SQLSERVER_NAME \
  --location $LOCATION \
  --admin-user $SQLSERVER_ADMIN \
  --admin-password $SQLSERVER_PASSWORD

check_error "Creazione SQL Server avviata"

# Attesa che SQL Server sia pronto
wait_for_sql_server $SQLSERVER_NAME $RESOURCE_GROUP
check_error "SQL Server disponibile"

# 7. Configurazione Firewall SQL Server
echo "🔒 Configurazione Firewall..."
MY_IP=$(curl -s ifconfig.me)
echo "Il tuo IP pubblico: $MY_IP"

az sql server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --server $SQLSERVER_NAME \
  --name AllowMyIP \
  --start-ip-address $MY_IP \
  --end-ip-address $MY_IP
check_error "Regola firewall per IP locale creata"

az sql server firewall-rule create \
  --resource-group $RESOURCE_GROUP \
  --server $SQLSERVER_NAME \
  --name AllowAzureServices \
  --start-ip-address 0.0.0.0 \
  --end-ip-address 0.0.0.0
check_error "Regola firewall per servizi Azure creata"

# 8. Creazione database SQL
echo "� Creazione database SQL Server..."
az sql db create \
  --resource-group $RESOURCE_GROUP \
  --server $SQLSERVER_NAME \
  --name $SQLSERVER_DATABASE \
  --service-objective Basic \
  --max-size 2GB
check_error "Database SQL Server creato"

# 9. Inizializzazione database SQL (usando immagine Docker sqlcmd)
echo "📝 Esecuzione script di inizializzazione SQL..."
SQLSERVER_HOST="${SQLSERVER_NAME}.database.windows.net"

# Esecuzione tramite Docker con sqlcmd
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

# 10. Creazione Storage Account per log container
echo "📦 Creazione Storage Account..."

az storage account create \
  --resource-group $RESOURCE_GROUP \
  --name $STORAGE_ACCOUNT \
  --location $LOCATION
check_error "Storage Account creato"

# 11. Creazione Container Instance con accesso limitato al tuo IP
echo "🐳 Creazione Azure Container Instance..."

# Crea Network Security Group per ACI

echo "🔐 Creazione Network Security Group..."
az network nsg create \
  --resource-group $RESOURCE_GROUP \
  --name $NSG_NAME
check_error "Network Security Group creato"

# Crea regola NSG: Nega tutto in ingresso
echo "🔒 Configurazione default: NEGA traffico in ingresso..."
az network nsg rule create \
  --resource-group $RESOURCE_GROUP \
  --nsg-name $NSG_NAME \
  --name DenyAllInbound \
  --priority 100 \
  --direction Inbound \
  --access Deny \
  --protocol '*' \
  --source-address-prefixes '*' \
  --destination-address-prefixes '*'
check_error "Regola DenyAll creata"

# Crea regola NSG: Permetti solo dal tuo IP
echo "🔓 Configurazione: CONSENTI traffico dal tuo IP ($MY_IP)..."
az network nsg rule create \
  --resource-group $RESOURCE_GROUP \
  --nsg-name $NSG_NAME \
  --name AllowMyIPOnly \
  --priority 200 \
  --direction Inbound \
  --access Allow \
  --protocol 'tcp' \
  --source-address-prefixes $MY_IP \
  --source-port-ranges '*' \
  --destination-address-prefixes '*' \
  --destination-port-ranges $CONTAINER_PORT
check_error "Regola AllowMyIP creata"



echo "🌐 Creazione Virtual Network..."
az network vnet create \
  --resource-group $RESOURCE_GROUP \
  --name $VNET_NAME \
  --address-prefix 10.0.0.0/16 \
  --subnet-name $SUBNET_NAME \
  --subnet-prefix 10.0.0.0/24
check_error "Virtual Network creato"

# Associa NSG alla subnet
echo "⛓️  Associazione NSG alla subnet..."
az network vnet subnet update \
  --resource-group $RESOURCE_GROUP \
  --vnet-name $VNET_NAME \
  --name $SUBNET_NAME \
  --network-security-group $NSG_NAME
check_error "NSG associato alla subnet"

# Recupera subnet ID
SUBNET_ID=$(az network vnet subnet show \
  --resource-group $RESOURCE_GROUP \
  --vnet-name $VNET_NAME \
  --name $SUBNET_NAME \
  --query "id" \
  --output tsv)
check_error "Subnet ID recuperato"

# Crea Container Instance con IP privato nella subnet
echo "🐳 Creazione Container Instance in subnet con IP privato..."
az container create \
  --resource-group $RESOURCE_GROUP \
  --name $CONTAINER_NAME \
  --image $CONTAINER_IMAGE \
  --cpu 1 \
  --memory 2 \
  --ports $CONTAINER_PORT \
  --vnet $VNET_NAME \
  --subnet $SUBNET_NAME \
  --ip-address "Private" \
  --environment-variables \
    SPRING_PROFILES_ACTIVE="azure" \
    AZURE_COSMOS_URI="$AZURE_COSMOS_URI" \
    AZURE_COSMOS_KEY="$AZURE_COSMOS_KEY" \
    AZURE_COSMOS_DATABASE="gestioneannotazioni" \
    AZURE_COSMOS_ENABLED="true" \
    MSSQL_SQLSERVER_HOST="$SQLSERVER_HOST" \
    MSSQL_SQLSERVER_PORT="1433" \
    MSSQL_SQLSERVER_ENCRYPT="true" \
    MSSQL_SQLSERVER_TRUST_SERVER_CERTIFICATE="false" \
    MSSQL_SPRING_DATASOURCE_USERNAME="$SQLSERVER_ADMIN" \
    MSSQL_SPRING_DATASOURCE_PASSWORD="$SQLSERVER_PASSWORD" \
    MSSQL_SQLSERVER_DATABASE="$SQLSERVER_DATABASE"

check_error "Container Instance creato"

# 12. Aggiungi IP di ACI al firewall SQL Server
echo "🔥 Aggiunta IP del container al firewall SQL Server..."
sleep 5

# Recupera IP privato del container
CONTAINER_PRIVATE_IP=$(az container show \
  --resource-group $RESOURCE_GROUP \
  --name $CONTAINER_NAME \
  --query "ipAddress.ip" \
  --output tsv 2>/dev/null || echo "")

if [ -n "$CONTAINER_PRIVATE_IP" ]; then
  az sql server firewall-rule create \
    --resource-group $RESOURCE_GROUP \
    --server $SQLSERVER_NAME \
    --name AllowContainerIP \
    --start-ip-address $CONTAINER_PRIVATE_IP \
    --end-ip-address $CONTAINER_PRIVATE_IP
  check_error "IP Container aggiunto al firewall SQL Server"
else
  echo "⚠️  Avviso: IP privato container non ancora disponibile"
fi

# 13. Output finale e salvataggio configurazione
echo ""
echo "✅ Stack Azure ACI completato!"
echo ""
echo "🌐 Accesso all'applicazione:"
echo "   ⚠️  Il container è ACCESSIBILE SOLO dal tuo IP: $MY_IP"
echo "   🔒 IP privato container (subnet Azure):  ${CONTAINER_PRIVATE_IP:-in fase di recupero}"
echo "   📍 La connessione avviene tramite tunneling/VPN Azure"
echo ""
echo "🗄️ Database:"
echo "   • SQL Server:     ${SQLSERVER_HOST}"
echo "   • Database:       ${SQLSERVER_DATABASE}"
echo "   • Username:       ${SQLSERVER_ADMIN}"
echo "   • Password:       (vedi .env-azure-aci)"
echo ""
echo "🌐 CosmosDB:"
echo "   • URI:            ${AZURE_COSMOS_URI}"
echo "   • Database:       gestioneannotazioni"
echo ""
echo "📋 Comandi utili:"
echo "   • Visualizza log:      az container logs --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME --follow"
echo "   • Descrizione:         az container show --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME"
echo "   • Stato container:     az container show -d --resource-group $RESOURCE_GROUP --name $CONTAINER_NAME"
echo ""
echo "🔒 Sicurezza:"
echo "   • NSG Name:            $NSG_NAME"
echo "   • VNet Name:           $VNET_NAME"
echo "   • Subnet Name:         $SUBNET_NAME"
echo "   • Solo il tuo IP può accedere: $MY_IP"
echo ""

# Salva configurazione in file .env
cat > .env-azure-aci << EOF
RESOURCE_GROUP=$RESOURCE_GROUP
CONTAINER_NAME=$CONTAINER_NAME
CONTAINER_IMAGE=$CONTAINER_IMAGE
AZURE_COSMOS_URI=$AZURE_COSMOS_URI
AZURE_COSMOS_KEY=$AZURE_COSMOS_KEY
SQLSERVER_HOST=$SQLSERVER_HOST
SQLSERVER_ADMIN=$SQLSERVER_ADMIN
SQLSERVER_PASSWORD=$SQLSERVER_PASSWORD
SQLSERVER_DATABASE=$SQLSERVER_DATABASE
CONTAINER_PRIVATE_IP=$CONTAINER_PRIVATE_IP
CONTAINER_PORT=$CONTAINER_PORT
MY_IP=$MY_IP
NSG_NAME=$NSG_NAME
VNET_NAME=$VNET_NAME
SUBNET_NAME=$SUBNET_NAME
EOF

echo "💾 Configurazione salvata in .env-azure-aci"