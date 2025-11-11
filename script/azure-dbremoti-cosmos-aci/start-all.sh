#!/bin/bash
set -e
#export AWS_PAGER=""
#az cosmosdb list-usage --location "northeurope" --subscription 90840abb-d216-4b44-bb4f-dd620b0fbedb

# Variabili di configurazione
RESOURCE_GROUP="gestioneannotazioni-aci-rg"
LOCATION="northeurope"  # "westeurope"  #eastus - northeurope - southcentralus (Stati Uniti, Texas)

# Container Instance
CONTAINER_NAME="gestioneannotazioni-aci"
CONTAINER_IMAGE="alnao/gestioneannotazioni:latest"
CONTAINER_PORT=8080

# Sql server      #ex COSMOSDB_ACCOUNT="gestioneannotazioni-cosmos-$(date +%s)" e -sql-$(date +%s)
COSMOSDB_ACCOUNT="gestioneannotazioni-cosmos-1762872042"
SQLSERVER_NAME="gestioneannotazioni-sql-1762872042"
SQLSERVER_ADMIN="sqladmin"
SQLSERVER_PASSWORD="P@ssw0rd123!"
SQLSERVER_DATABASE="gestioneannotazioni"
INIT_SQL_PATH="./script/init-database/init-mssql.sql"

# Storage Account per log container   #ex "gestioneannotazioni-sa-$(date +%s | tail -c 10)"
STORAGE_ACCOUNT="gestioneannotazioniacisa" 
STORAGE_KEY="gestioneannotazioni"

# Crea Virtual Network e Subnet per ACI
NSG_NAME="gestioneannotazioni-aci-nsg"
VNET_NAME="gestioneannotazioni-aci-vnet"
SUBNET_NAME="gestioneannotazioni-aci-subnet"

#Azure Container Registry
ACR_NAME="gestioneannotazioniacr"  # Nome univoco (solo lettere e numeri)
CONTAINER_NAME="gestioneannotazioni-aci"
CONTAINER_IMAGE_SOURCE="alnao/gestioneannotazioni:latest"  # Immagine originale da Docker Hub
CONTAINER_IMAGE="$ACR_NAME.azurecr.io/gestioneannotazioni:latest"  # Immagine su ACR
LOG_ANALYTICS_WORKSPACE="gestioneannotazioni-logs"

#SERVICE BUS_TOPIC="gestioneannotazioni-topic"
SERVICEBUS_NAMESPACE="gestioneannotazioni-servicebus"
SERVICEBUS_QUEUE="gestioneannotazioni-queue"

# NUOVA VARIABILE: Abilita VNet/Subnet: se valozziata con SI crea la VNet/Subnet per ACI
CREATE_VNET="${CREATE_VNET:-NO}"  # Default: NO, può essere impostato a SI tramite: export CREATE_VNET=SI
# NOTA: la versione con "SI" non è mai stata testate completamente, usala con cautela!

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

# 2. Creazione Resource Group
  echo "📦 Creazione Resource Group..."
  if az group exists --name $RESOURCE_GROUP | grep -q "true"; then
    echo "✅ Resource Group '$RESOURCE_GROUP' già esistente, skip creazione"
  else
    az group create \
      --name $RESOURCE_GROUP \
      --location $LOCATION
    check_error "Resource Group creato"
  fi

# 3. Creazione Log Analytics Workspace (se non esiste)
  echo "📊 Verifica Log Analytics Workspace..."
  # Verifica se esiste già
  WORKSPACE_EXISTS=$(az monitor log-analytics workspace show \
    --resource-group $RESOURCE_GROUP \
    --workspace-name $LOG_ANALYTICS_WORKSPACE \
    --query "name" \
    --output tsv 2>/dev/null || echo "")
  if [ -n "$WORKSPACE_EXISTS" ]; then
    echo "✅ Log Analytics Workspace '$LOG_ANALYTICS_WORKSPACE' già esistente"
  else
    echo "📊 Creazione Log Analytics Workspace..."
    az monitor log-analytics workspace create \
      --resource-group $RESOURCE_GROUP \
      --workspace-name $LOG_ANALYTICS_WORKSPACE \
      --location $LOCATION
    check_error "Log Analytics Workspace creato"
  fi
  # Recupera ID e chiave del workspace (sempre, sia che esista o sia nuovo)
  echo "🔑 Recupero credenziali Log Analytics Workspace..."
  WORKSPACE_ID=$(az monitor log-analytics workspace show \
    --resource-group $RESOURCE_GROUP \
    --workspace-name $LOG_ANALYTICS_WORKSPACE \
    --query "customerId" \
    --output tsv)
  check_error "Workspace ID recuperato"
  WORKSPACE_KEY=$(az monitor log-analytics workspace get-shared-keys \
    --resource-group $RESOURCE_GROUP \
    --workspace-name $LOG_ANALYTICS_WORKSPACE \
    --query "primarySharedKey" \
    --output tsv)
  check_error "Workspace Key recuperata"
  echo "Workspace ID: $WORKSPACE_ID"
  echo "Workspace Key: ${WORKSPACE_KEY:0:20}..."


# 4a. Creazione CosmosDB (Free Tier) - se non esiste
  echo "🌐 Verifica CosmosDB..."

  # Controlla se CosmosDB esiste
  COSMOS_EXISTS=$(az cosmosdb show \
    --name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --query "name" \
    --output tsv 2>/dev/null || echo "")

  if [ -n "$COSMOS_EXISTS" ]; then
    echo "✅ CosmosDB '$COSMOSDB_ACCOUNT' già esistente"
    
    # Verifica stato
    COSMOS_STATE=$(az cosmosdb show \
      --name $COSMOSDB_ACCOUNT \
      --resource-group $RESOURCE_GROUP \
      --query "provisioningState" \
      --output tsv)
    
    if [ "$COSMOS_STATE" != "Succeeded" ]; then
      echo "⚠️  CosmosDB in stato: $COSMOS_STATE, attendo completamento..."
      wait_for_cosmos_db $COSMOSDB_ACCOUNT $RESOURCE_GROUP
    fi
  else
    echo "🌐 Creazione nuovo CosmosDB..."
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

  # 4b. Creazione database in CosmosDB (se non esiste)
  echo "💾 Verifica database CosmosDB..."

  DB_EXISTS=$(az cosmosdb sql database show \
    --account-name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --name gestioneannotazioni \
    --query "name" \
    --output tsv 2>/dev/null || echo "")

  if [ -n "$DB_EXISTS" ]; then
    echo "✅ Database 'gestioneannotazioni' già esistente"
  else
    echo "💾 Creazione database CosmosDB..."
    az cosmosdb sql database create \
      --account-name $COSMOSDB_ACCOUNT \
      --resource-group $RESOURCE_GROUP \
      --name gestioneannotazioni
    check_error "Database Cosmos creato"
  fi

  # 4c. Creazione container 'annotazioni' (se non esiste)
  echo "📦 Verifica container 'annotazioni'..."

  CONTAINER1_EXISTS=$(az cosmosdb sql container show \
    --account-name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --database-name gestioneannotazioni \
    --name annotazioni \
    --query "name" \
    --output tsv 2>/dev/null || echo "")

  if [ -n "$CONTAINER1_EXISTS" ]; then
    echo "✅ Container 'annotazioni' già esistente"
  else
    echo "📦 Creazione container 'annotazioni'..."
    az cosmosdb sql container create \
      --account-name $COSMOSDB_ACCOUNT \
      --resource-group $RESOURCE_GROUP \
      --database-name gestioneannotazioni \
      --name annotazioni \
      --partition-key-path "/id" \
      --throughput 400
    check_error "Container annotazioni creato"
  fi

  # 4d. Creazione container 'annotazione_storico_stati' (se non esiste)
  echo "📦 Verifica container 'annotazione_storico_stati'..."

  CONTAINER2_EXISTS=$(az cosmosdb sql container show \
    --account-name $COSMOSDB_ACCOUNT \
    --resource-group $RESOURCE_GROUP \
    --database-name gestioneannotazioni \
    --name annotazione_storico_stati \
    --query "name" \
    --output tsv 2>/dev/null || echo "")

  if [ -n "$CONTAINER2_EXISTS" ]; then
    echo "✅ Container 'annotazione_storico_stati' già esistente"
  else
    echo "📦 Creazione container 'annotazione_storico_stati'..."
    az cosmosdb sql container create \
      --account-name $COSMOSDB_ACCOUNT \
      --resource-group $RESOURCE_GROUP \
      --database-name gestioneannotazioni \
      --name annotazione_storico_stati \
      --partition-key-path "/id" \
      --throughput 400
    check_error "Container storico creato"
  fi

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


# 6. Creazione SQL Server (se non esiste)
  echo "🗄️  Verifica SQL Server..."
  SQL_SERVER_EXISTS=$(az sql server show \
    --resource-group $RESOURCE_GROUP \
    --name $SQLSERVER_NAME \
    --query "name" \
    --output tsv 2>/dev/null || echo "")
  if [ -n "$SQL_SERVER_EXISTS" ]; then
    echo "✅ SQL Server '$SQLSERVER_NAME' già esistente"
    # Verifica stato
    SQL_STATE=$(az sql server show \
      --resource-group $RESOURCE_GROUP \
      --name $SQLSERVER_NAME \
      --query "state" \
      --output tsv)
    if [ "$SQL_STATE" != "Ready" ]; then
      echo "⚠️  SQL Server in stato: $SQL_STATE, attendo completamento..."
      wait_for_sql_server $SQLSERVER_NAME $RESOURCE_GROUP
    fi
  else
    echo "🗄️  Creazione nuovo SQL Server..."
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
  fi
  # 7. Configurazione Firewall SQL Server
  echo "🔒 Configurazione Firewall SQL Server..."
  MY_IP=$(curl -s ifconfig.me)
  echo "Il tuo IP pubblico: $MY_IP"
  # Funzione per creare regola firewall se non esiste
  create_firewall_rule_if_not_exists() {
      local server=$1
      local rg=$2
      local rule_name=$3
      local start_ip=$4
      local end_ip=$5
      
      RULE_EXISTS=$(az sql server firewall-rule show \
        --resource-group $rg \
        --server $server \
        --name "$rule_name" \
        --query "name" \
        --output tsv 2>/dev/null || echo "")
      
      if [ -n "$RULE_EXISTS" ]; then
          echo "✅ Regola firewall '$rule_name' già esistente"
      else
          echo "🔓 Creazione regola firewall '$rule_name'..."
          az sql server firewall-rule create \
            --resource-group $rg \
            --server $server \
            --name "$rule_name" \
            --start-ip-address $start_ip \
            --end-ip-address $end_ip
          check_error "Regola firewall '$rule_name' creata"
      fi
  }

  # Crea regole firewall
  create_firewall_rule_if_not_exists \
    $SQLSERVER_NAME \
    $RESOURCE_GROUP \
    "AllowMyIP" \
    $MY_IP \
    $MY_IP

  create_firewall_rule_if_not_exists \
    $SQLSERVER_NAME \
    $RESOURCE_GROUP \
    "AllowAzureServices" \
    "0.0.0.0" \
    "0.0.0.0"

  # 8. Creazione database SQL (se non esiste)
  echo "💾 Verifica database SQL Server..."

  DB_SQL_EXISTS=$(az sql db show \
    --resource-group $RESOURCE_GROUP \
    --server $SQLSERVER_NAME \
    --name $SQLSERVER_DATABASE \
    --query "name" \
    --output tsv 2>/dev/null || echo "")

  if [ -n "$DB_SQL_EXISTS" ]; then
    echo "✅ Database '$SQLSERVER_DATABASE' già esistente"
  else
    echo "💾 Creazione database SQL Server..."
    az sql db create \
      --resource-group $RESOURCE_GROUP \
      --server $SQLSERVER_NAME \
      --name $SQLSERVER_DATABASE \
      --service-objective Basic \
      --max-size 2GB
    check_error "Database SQL Server creato"
  fi

# 9. Inizializzazione database SQL (usando immagine Docker sqlcmd)
  echo "📝 Esecuzione script di inizializzazione SQL..."
  SQLSERVER_HOST="${SQLSERVER_NAME}.database.windows.net"
  if [ -n "$DB_SQL_EXISTS" ]; then
    echo "✅ Database '$SQLSERVER_DATABASE' già esistente"
  else
    echo "Connessione a SQL Server: $SQLSERVER_HOST"
    # Esecuzione tramite Docker con sqlcmd
    #docker run --rm \
    #    -v "$(pwd):/workspace" \
    #    -w /workspace \
    #    mcr.microsoft.com/mssql-tools \
    #    /opt/mssql-tools/bin/sqlcmd \
    #    -S $SQLSERVER_HOST \
    #    -d $SQLSERVER_DATABASE \
    #    -U $SQLSERVER_ADMIN \
    #    -P "$SQLSERVER_PASSWORD" \
    #    -i $INIT_SQL_PATH
    #check_error "Script SQL eseguito con successo"

    # Leggi il contenuto dello script SQL
    SQL_SCRIPT=$(cat $INIT_SQL_PATH | base64 -w 0)

    # Crea un ACI temporaneo per eseguire sqlcmd
    az container create \
      --resource-group $RESOURCE_GROUP \
      --name "sql-init-$(date +%s)" \
      --image mcr.microsoft.com/mssql-tools \
      --restart-policy Never \
      --os-type Linux \
      --cpu 0.5 \
      --memory 1 \
      --environment-variables \
        SQLSERVER_HOST="$SQLSERVER_HOST" \
        SQLSERVER_DATABASE="$SQLSERVER_DATABASE" \
        SQLSERVER_ADMIN="$SQLSERVER_ADMIN" \
        SQLSERVER_PASSWORD="$SQLSERVER_PASSWORD" \
        SQL_SCRIPT_BASE64="$SQL_SCRIPT" \
      --command-line "/bin/bash -c 'echo \$SQL_SCRIPT_BASE64 | base64 -d > /tmp/init.sql && /opt/mssql-tools/bin/sqlcmd -S \$SQLSERVER_HOST -d \$SQLSERVER_DATABASE -U \$SQLSERVER_ADMIN -P \$SQLSERVER_PASSWORD -i /tmp/init.sql'"

    check_error "Container ACI per inizializzazione SQL creato"

    # Attendi completamento
    echo "⏳ Attesa completamento script SQL..."
    sleep 30
    # Verifica log e stato
    INIT_CONTAINER_NAME=$(az container list \
      --resource-group $RESOURCE_GROUP \
      --query "[?starts_with(name, 'sql-init')].name | [0]" \
      --output tsv)


    # Verifica stato finale
    CONTAINER_STATE=$(az container show \
      --resource-group $RESOURCE_GROUP \
      --name $INIT_CONTAINER_NAME \
      --query "instanceView.state" \
      --output tsv)

    if [ "$CONTAINER_STATE" = "Terminated" ]; then
      EXIT_CODE=$(az container show \
        --resource-group $RESOURCE_GROUP \
        --name $INIT_CONTAINER_NAME \
        --query "instanceView.events[?type=='ContainerStopped'].exitCode | [0]" \
        --output tsv)
      
      if [ "$EXIT_CODE" = "0" ]; then
        echo "✅ Script SQL eseguito con successo!"
      else
        echo "❌ Script SQL terminato con errore (exit code: $EXIT_CODE)"
        exit 1
      fi
    else
      echo "⚠️  Stato container: $CONTAINER_STATE"
    fi

    # Elimina container temporaneo
    echo "🗑️  Eliminazione container temporaneo..."
    az container delete \
      --resource-group $RESOURCE_GROUP \
      --name $INIT_CONTAINER_NAME \
      --yes

    check_error "Container temporaneo eliminato"
  fi

# 10. Creazione Storage Account per log container
  echo "📦 Creazione Storage Account..."
  STORAGE_EXISTS=$(az storage account show \
    --resource-group $RESOURCE_GROUP \
    --name $STORAGE_ACCOUNT \
    --query "name" \
    --output tsv 2>/dev/null || echo "")

  if [ -n "$STORAGE_EXISTS" ]; then
    echo "✅ Storage Account '$STORAGE_ACCOUNT' già esistente"
  else
    echo "📦 Creazione Storage Account..."
    az storage account create \
      --resource-group $RESOURCE_GROUP \
      --name $STORAGE_ACCOUNT \
      --location $LOCATION \
      --sku Standard_LRS
    check_error "Storage Account creato"
  fi
  # Recupera Storage Account key (sempre necessaria)
  echo "🔑 Recupero Storage Account key..."
  STORAGE_KEY=$(az storage account keys list \
    --resource-group $RESOURCE_GROUP \
    --account-name $STORAGE_ACCOUNT \
    --query "[0].value" \
    --output tsv)
  check_error "Storage Key recuperata"
  # Crea share per log (se non esiste)
  echo "📁 Verifica share 'aci-logs'..."
  SHARE_EXISTS=$(az storage share exists \
    --name aci-logs \
    --account-name $STORAGE_ACCOUNT \
    --account-key $STORAGE_KEY \
    --query "exists" \
    --output tsv 2>/dev/null || echo "false")
  if [ "$SHARE_EXISTS" = "true" ]; then
    echo "✅ Share 'aci-logs' già esistente"
  else
    echo "📁 Creazione share 'aci-logs'..."
    az storage share create \
      --name aci-logs \
      --account-name $STORAGE_ACCOUNT \
      --account-key $STORAGE_KEY
    check_error "Share per log creata"
  fi

# 11. Creazione Container Instance con accesso limitato al tuo IP
  if [ "$CREATE_VNET" = "SI" ]; then
    echo "🌐 Modalità VNet ABILITATA - Creazione Network Security Group, VNet e Subnet..."
    
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

    echo "🌐 Creazione VNet "
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

    echo "✅ VNet e Subnet creati - Container sarà in rete privata"
    NETWORK_TYPE="Private"
    VNET_PARAMS="--vnet $VNET_NAME --subnet $SUBNET_NAME"

  else
    echo "🌐 Modalità VNet DISABILITATA - Container con IP pubblico"
    NETWORK_TYPE="Public"
    VNET_PARAMS=""
    MY_IP=$(curl -s ifconfig.me)
    echo "Il tuo IP pubblico: $MY_IP"
  fi

# 12 Creazione Azure Container Registry
  echo "📦 Verifica Azure Container Registry..."
  ACR_EXISTS=$(az acr show \
    --name $ACR_NAME \
    --resource-group $RESOURCE_GROUP \
    --query "name" \
    --output tsv 2>/dev/null || echo "")
  if [ -n "$ACR_EXISTS" ]; then
    echo "✅ ACR '$ACR_NAME' già esistente"
  else
    echo "📦 Creazione Azure Container Registry..."
    az acr create \
      --resource-group $RESOURCE_GROUP \
      --name $ACR_NAME \
      --sku Basic \
      --location $LOCATION \
      --admin-enabled true
    check_error "ACR creato"
  fi
  # 12b. Import immagine da Docker Hub ad ACR (se non esiste)
  echo "🔄 Verifica immagine in ACR..."
  # Verifica se l'immagine esiste già in ACR
  IMAGE_EXISTS=$(az acr repository show \
    --name $ACR_NAME \
    --image gestioneannotazioni:latest \
    --query "name" \
    --output tsv 2>/dev/null || echo "")
  if [ -n "$IMAGE_EXISTS" ]; then
    echo "✅ Immagine 'gestioneannotazioni:latest' già presente in ACR"
  else
    echo "🔄 Import immagine da Docker Hub ad ACR..."
    az acr import \
      --name $ACR_NAME \
      --source docker.io/$CONTAINER_IMAGE_SOURCE \
      --image gestioneannotazioni:latest \
      --resource-group $RESOURCE_GROUP
    check_error "Immagine importata su ACR"
  fi
  # 12c. Recupera credenziali ACR
  echo "🔑 Recupero credenziali ACR..."
  ACR_USERNAME=$(az acr credential show \
    --name $ACR_NAME \
    --resource-group $RESOURCE_GROUP \
    --query "username" \
    --output tsv)
  check_error "Username ACR recuperato"
  ACR_PASSWORD=$(az acr credential show \
    --name $ACR_NAME \
    --resource-group $RESOURCE_GROUP \
    --query "passwords[0].value" \
    --output tsv)
  check_error "Password ACR recuperata"
  echo "ACR Login Server: $ACR_NAME.azurecr.io"
  echo "ACR Username: $ACR_USERNAME"
  echo "ACR Password: ${ACR_PASSWORD:0:20}..."

# 13. Creazione Service Bus namespace e coda EventBus (se non esistono)
  echo "📨 Gestione Service Bus..."
  # Verifica se namespace esiste
  echo "📦 Verifica Service Bus namespace..."
  SERVICEBUS_EXISTS=$(az servicebus namespace show \
    --resource-group $RESOURCE_GROUP \
    --name $SERVICEBUS_NAMESPACE \
    --query "name" \
    --output tsv 2>/dev/null || echo "")
  if [ -n "$SERVICEBUS_EXISTS" ]; then
    echo "✅ Service Bus namespace '$SERVICEBUS_NAMESPACE' già esistente"
    # Verifica stato
    SB_STATE=$(az servicebus namespace show \
      --resource-group $RESOURCE_GROUP \
      --name $SERVICEBUS_NAMESPACE \
      --query "provisioningState" \
      --output tsv)
    
    if [ "$SB_STATE" != "Succeeded" ]; then
      echo "⚠️  Service Bus in stato: $SB_STATE, attendo completamento..."
      sleep 10
    fi
  else
    echo "📨 Creazione Service Bus namespace..."
    az servicebus namespace create \
      --resource-group $RESOURCE_GROUP \
      --name $SERVICEBUS_NAMESPACE \
      --location $LOCATION \
      --sku Standard
    check_error "Service Bus namespace creato"
  fi
  # Verifica se queue esiste
  echo "📬 Verifica Service Bus queue..."
  QUEUE_EXISTS=$(az servicebus queue show \
    --resource-group $RESOURCE_GROUP \
    --namespace-name $SERVICEBUS_NAMESPACE \
    --name $SERVICEBUS_QUEUE \
    --query "name" \
    --output tsv 2>/dev/null || echo "")
  if [ -n "$QUEUE_EXISTS" ]; then
    echo "✅ Service Bus queue '$SERVICEBUS_QUEUE' già esistente"
  else
    echo "📬 Creazione Service Bus queue..."
    az servicebus queue create \
      --resource-group $RESOURCE_GROUP \
      --namespace-name $SERVICEBUS_NAMESPACE \
      --name $SERVICEBUS_QUEUE
    check_error "Service Bus queue creata"
  fi
  # Recupero connection string Service Bus
  echo "🔑 Recupero connection string Service Bus..."
  AZURE_SERVICEBUS_CONNECTION_STRING=$(az servicebus namespace authorization-rule keys list \
    --resource-group $RESOURCE_GROUP \
    --namespace-name $SERVICEBUS_NAMESPACE \
    --name RootManageSharedAccessKey \
    --query 'primaryConnectionString' \
    --output tsv)
  check_error "Connection string Service Bus recuperata"
  echo "Service Bus Connection String: ${AZURE_SERVICEBUS_CONNECTION_STRING:0:100}..."

# 14. Creazione/Aggiornamento Container Instance
  echo "🐳 Gestione Azure Container Instance..."
  # Verifica se il container esiste già
  CONTAINER_EXISTS=$(az container show \
    --resource-group $RESOURCE_GROUP \
    --name $CONTAINER_NAME \
    --query "name" \
    --output tsv 2>/dev/null || echo "")
  if [ -n "$CONTAINER_EXISTS" ]; then
    echo "⚠️  Container Instance '$CONTAINER_NAME' già esistente"
    # Verifica stato
    CONTAINER_STATE=$(az container show \
      --resource-group $RESOURCE_GROUP \
      --name $CONTAINER_NAME \
      --query "instanceView.state" \
      --output tsv)
    if [ "$CONTAINER_STATE" = "Running" ]; then
      echo "✅ Container in esecuzione - procedo con l'eliminazione per ricreazione"
    else
      echo "⚠️  Container in stato: $CONTAINER_STATE - procedo con l'eliminazione per ricreazione"
    fi
    # Elimina il container esistente
    echo "🗑️  Eliminazione Container Instance esistente..."
    az container delete \
      --resource-group $RESOURCE_GROUP \
      --name $CONTAINER_NAME \
      --yes
    check_error "Container eliminato"
    
    echo "⏳ Attesa 10 secondi prima di ricreare..."
    sleep 10
  fi
  echo "🐳 Creazione Azure Container Instance con immagine da ACR..."
  az container create \
    --resource-group $RESOURCE_GROUP \
    --name $CONTAINER_NAME \
    --image $CONTAINER_IMAGE \
    --os-type Linux \
    --cpu 1 \
    --memory 2 \
    --ports $CONTAINER_PORT \
    --ip-address $NETWORK_TYPE \
    $VNET_PARAMS \
    --dns-name-label "$CONTAINER_NAME-$(date +%s)" \
    --registry-login-server "$ACR_NAME.azurecr.io" \
    --registry-username $ACR_USERNAME \
    --registry-password $ACR_PASSWORD \
    --log-analytics-workspace $WORKSPACE_ID \
    --log-analytics-workspace-key $WORKSPACE_KEY \
    --environment-variables \
      SPRING_PROFILES_ACTIVE="azure" \
      AZURE_COSMOS_URI="$AZURE_COSMOS_URI" \
      AZURE_COSMOS_KEY="$AZURE_COSMOS_KEY" \
      AZURE_COSMOS_DATABASE="gestioneannotazioni" \
      AZURE_COSMOS_ENABLED="true" \
      MSSQL_SQLSERVER_HOST="$SQLSERVER_HOST" \
      MSSQL_SQLSERVER_PORT="1433" \
      MSSQL_SQLSERVER_DATABASE="$SQLSERVER_DATABASE" \
      MSSQL_SQLSERVER_ENCRYPT="true" \
      MSSQL_SQLSERVER_TRUST_SERVER_CERTIFICATE="trustServerCertificate=false" \
      MSSQL_SQLSERVER_HOST_NAME_IN_CERTIFICATE="*.database.windows.net" \
      MSSQL_SPRING_DATASOURCE_USERNAME="$SQLSERVER_ADMIN" \
      MSSQL_SPRING_DATASOURCE_PASSWORD="$SQLSERVER_PASSWORD" \
      AZURE_SERVICEBUS_CONNECTION_STRING="$AZURE_SERVICEBUS_CONNECTION_STRING" \
      AZURE_SERVICEBUS_QUEUE_NAME="$SERVICEBUS_QUEUE" \
    --azure-file-volume-account-name $STORAGE_ACCOUNT \
    --azure-file-volume-account-key $STORAGE_KEY \
    --azure-file-volume-share-name aci-logs \
    --azure-file-volume-mount-path /var/log/app
  
  check_error "Container Instance creato"
  
  # Aspetto 60 secondi che il server sia partito correttamente
  echo "⏳ Attesa avvio container di dieci secondi..."
  sleep 10

# 15. Recupera IP del container
  echo "🔍 Recupero IP del container..."
  CONTAINER_IP=$(az container show \
    --resource-group $RESOURCE_GROUP \
    --name $CONTAINER_NAME \
    --query "ipAddress.ip" \
    --output tsv 2>/dev/null || echo "")
  echo "IP del container: ${CONTAINER_IP:-in fase di recupero}"

# 16. Aggiungi IP di ACI al firewall SQL Server (se non esiste già)
  if [ -n "$CONTAINER_IP" ]; then
    echo "🔥 Verifica regola firewall per IP container..."
    
    CONTAINER_FW_EXISTS=$(az sql server firewall-rule show \
      --resource-group $RESOURCE_GROUP \
      --server $SQLSERVER_NAME \
      --name "AllowContainerIP" \
      --query "name" \
      --output tsv 2>/dev/null || echo "")
    
    if [ -z "$CONTAINER_FW_EXISTS" ]; then
      echo "🔓 Creazione regola firewall per container..."
      az sql server firewall-rule create \
        --resource-group $RESOURCE_GROUP \
        --server $SQLSERVER_NAME \
        --name AllowContainerIP \
        --start-ip-address $CONTAINER_IP \
        --end-ip-address $CONTAINER_IP
      check_error "Regola firewall per container creata"
    else
      echo "✅ Regola firewall 'AllowContainerIP' già esistente"
      
      # Verifica se l'IP è cambiato
      EXISTING_IP=$(az sql server firewall-rule show \
        --resource-group $RESOURCE_GROUP \
        --server $SQLSERVER_NAME \
        --name "AllowContainerIP" \
        --query "startIpAddress" \
        --output tsv)
      
      if [ "$EXISTING_IP" != "$CONTAINER_IP" ]; then
        echo "⚠️  IP container cambiato da $EXISTING_IP a $CONTAINER_IP"
        echo "🔄 Aggiornamento regola firewall..."
        
        az sql server firewall-rule update \
          --resource-group $RESOURCE_GROUP \
          --server $SQLSERVER_NAME \
          --name AllowContainerIP \
          --start-ip-address $CONTAINER_IP \
          --end-ip-address $CONTAINER_IP
        check_error "Regola firewall aggiornata"
      fi
    fi
  else
    echo "⚠️  Avviso: IP container non disponibile, impossibile configurare firewall"
  fi

# 17. Output finale e salvataggio configurazione
  echo ""
  echo "✅ Stack Azure ACI completato!"
  echo ""
  echo "🌐 Accesso all'applicazione:"
  echo "   ⚠️  Il container è ACCESSIBILE SOLO dal tuo IP: $MY_IP"
  echo "   🔒 IP container:  ${CONTAINER_IP:-in fase di recupero}"
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
  echo "📊 Log Analytics:"
  echo "   • Workspace:       $LOG_ANALYTICS_WORKSPACE"
  echo "   • Workspace ID:    $WORKSPACE_ID"
  echo ""
  echo "📋 Query log (Azure Portal):"
  echo "   ContainerInstanceLog_CL"
  echo "   | where Name_s == \"$CONTAINER_NAME\""
  echo "   | order by TimeGenerated desc"
  echo ""
  echo "🔒 Sicurezza:"
  echo "   • NSG Name:            $NSG_NAME"
  echo "   • VNet Name:           $VNET_NAME"
  echo "   • Subnet Name:         $SUBNET_NAME"
  echo "   • Solo il tuo IP può accedere: $MY_IP"
  echo ""

#18. Salva configurazione in file .env
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
CONTAINER_IP=$CONTAINER_IP
CONTAINER_PORT=$CONTAINER_PORT
MY_IP=$MY_IP
NSG_NAME=$NSG_NAME
VNET_NAME=$VNET_NAME
SUBNET_NAME=$SUBNET_NAME
STORAGE_ACCOUNT=$STORAGE_ACCOUNT
STORAGE_KEY=$STORAGE_KEY
LOG_ANALYTICS_WORKSPACE=$LOG_ANALYTICS_WORKSPACE
WORKSPACE_ID=$WORKSPACE_ID
WORKSPACE_KEY=$WORKSPACE_KEY
AZURE_SERVICEBUS_CONNECTION_STRING=$AZURE_SERVICEBUS_CONNECTION_STRING
EOF

# 19. FINE!
echo "💾 Configurazione salvata in .env-azure-aci"
echo ""
echo "✅ Script completato!"
echo ""