# Azure Container Instances (ACI) - Deployment Guide

Deployment dell'applicazione su **Azure Container Instances** con **Cosmos DB** e **SQL Server** Azure.

## 📋 Prerequisiti

- Azure CLI installato e autenticato (`az login`)
- Docker installato (per esecuzione script SQL tramite immagine)
- Accesso ad Azure subscription
- Immagine Docker disponibile su DockerHub: `alnao/gestioneannotazioni:latest`

## 🚀 Quick Start

### 1. Deployment completo

```bash
chmod +x script/azure-dbremoti-cosmos-aci/start-all.sh
./script/azure-dbremoti-cosmos-aci/start-all.sh
```

Lo script creerà automaticamente:
- ✅ Resource Group
- ✅ Cosmos DB (Free Tier)
- ✅ SQL Server + Database
- ✅ Azure Container Instance
- ✅ Virtual Network + Network Security Group
- ✅ Accesso limitato al tuo IP pubblico

### 2. Visualizza logs

```bash
# Leggi logs in tempo reale
az container logs \
  --resource-group gestioneannotazioni-aci-rg \
  --name gestioneannotazioni-aci \
  --follow

# Stato container
az container show -d \
  --resource-group gestioneannotazioni-aci-rg \
  --name gestioneannotazioni-aci
```

### 3. Pulizia risorse

```bash
chmod +x script/azure-dbremoti-cosmos-aci/stop-all.sh
./script/azure-dbremoti-cosmos-aci/stop-all.sh
```

## 🔒 Sicurezza - IP Whitelisting

Il container è **accessibile SOLO dal tuo IP pubblico**.

### Come funziona:

1. **Detection automatico IP**: Lo script recupera il tuo IP pubblico
   ```bash
   MY_IP=$(curl -s ifconfig.me)
   ```

2. **Virtual Network Privata**: 
   - VNet: `10.0.0.0/16`
   - Subnet: `10.0.0.0/24`
   - Container: IP privato nella subnet

3. **Network Security Group (NSG)**:
   - ❌ Nega tutto di default
   - ✅ Consenti solo dal tuo IP pubblico

4. **Firewall SQL Server**:
   - ✅ Tuo IP locale
   - ✅ IP del Container
   - ✅ Servizi Azure

### Esempio NSG Rules:

```
Priority 100: DenyAll     (tutte le direzioni)
Priority 200: AllowMyIP   (solo dal tuo IP pubblico)
```

## 📊 Architettura

```
┌─────────────────────────────────────────────────────────┐
│              Azure Resource Group                       │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │         Virtual Network (10.0.0.0/16)           │   │
│  │  ┌──────────────────────────────────────────┐   │   │
│  │  │  Subnet (10.0.0.0/24)  [NSG Attached]   │   │   │
│  │  │  ┌──────────────────────────────────┐    │   │   │
│  │  │  │  Azure Container Instance        │    │   │   │
│  │  │  │  • Port: 8080 (PRIVATE)          │    │   │   │
│  │  │  │  • Image: alnao/gestioneannotazioni   │   │   │
│  │  │  │  • Environment: Spring/Azure    │    │   │   │
│  │  │  └──────────────────────────────────┘    │   │   │
│  │  └──────────────────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌──────────────────┐   ┌──────────────────────────┐   │
│  │  Cosmos DB       │   │  SQL Server              │   │
│  │  (Free Tier)     │   │  • Database: gestioneannotazioni   │   │
│  │  • Database: gestioneannotazioni        │   │
│  │  • Container: annotazioni    │   │  • DTU: 5 (Basic)│   │
│  └──────────────────┘   └──────────────────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘
         ↓ Solo dal tuo IP via NSG
    [Tuo Computer]
```

## 💰 Costi stimati (€/mese)

| Risorsa | Tier | Costo |
|---------|------|-------|
| **Azure Container Instances** | 1 vCPU, 2GB RAM | ~€9.00 |
| **Cosmos DB SQL API** | Free Tier | €0.00 |
| **SQL Server** | Basic (5 DTU, 2GB) | ~€5.00 |
| **Storage Account** | Standard | ~€7.50 |
| **Virtual Network** | Standard | €0.00 |
| **Network Security Group** | Standard | €0.00 |
| **TOTALE** | | **~€21.50** |

> ⚠️ Prezzi approssimativi per Europa (North Europe). Verificare su Azure Portal.

## 🔧 Variabili d'Ambiente Salvate

Lo script salva la configurazione in `.env-azure-aci`:

```env
SPRING_PROFILES_ACTIVE=azure
RESOURCE_GROUP=gestioneannotazioni-aci-rg
SQLSERVER_HOST=gestioneannotazioni-sql-XXXXXXXXX.database.windows.net
SQLSERVER_ADMIN=sqladmin
SQLSERVER_PASSWORD=P@ssw0rd123!
SQLSERVER_DATABASE=gestioneannotazioni
AZURE_COSMOS_URI=https://gestioneannotazioni-cosmos-XXXXXXXXX.documents.azure.com:443/
AZURE_COSMOS_KEY=...
CONTAINER_PRIVATE_IP=10.0.0.X
MY_IP=XXX.XXX.XXX.XXX
```

## 📝 Comandi Utili

```bash
# Visualizza tutti i container
az container list --resource-group gestioneannotazioni-aci-rg --output table

# Eliminazione singolo container
az container delete \
  --resource-group gestioneannotazioni-aci-rg \
  --name gestioneannotazioni-aci \
  --yes

# Visualizza NSG rules
az network nsg rule list \
  --resource-group gestioneannotazioni-aci-rg \
  --nsg-name gestioneannotazioni-aci-nsg \
  --output table

# Modifica NSG rule (aggiungere un altro IP)
az network nsg rule create \
  --resource-group gestioneannotazioni-aci-rg \
  --nsg-name gestioneannotazioni-aci-nsg \
  --name AllowAdditionalIP \
  --priority 300 \
  --direction Inbound \
  --access Allow \
  --protocol tcp \
  --source-address-prefixes YYY.YYY.YYY.YYY \
  --destination-port-ranges 8080

# Visualizza SQL Server firewall rules
az sql server firewall-rule list \
  --resource-group gestioneannotazioni-aci-rg \
  --server gestioneannotazioni-sql-XXXXXXXXX \
  --output table
```

## 🆚 Confronto con altri deployment

| Aspetto | **ACI** | VM | ECS Fargate | App Service |
|---------|--------|----|-----------|----|
| **Gestione Infra** | Serverless ✅ | Manuale | Managed | Managed |
| **IP Whitelisting** | ✅ Facile (NSG) | ✅ Security Groups | ⚠️ Complesso | ⚠️ Complesso |
| **Scaling** | Manuale | Manuale | Auto | Auto |
| **Networking** | ✅ VNet privata | ✅ VNet | ⚠️ Limited | ⚠️ Limited |
| **Complessità** | 🟢 Bassa | 🟡 Media | 🔴 Alta | 🟡 Media |
| **Costo** | 💰 Basso | 💰💰 Medio | 💰💰💰 Alto | 💰💰 Medio |

## ⚠️ Note Importanti

1. **IP pubblico variabile**: Se il tuo IP pubblico cambia, aggiorna la regola NSG
   ```bash
   az network nsg rule update \
     --resource-group gestioneannotazioni-aci-rg \
     --nsg-name gestioneannotazioni-aci-nsg \
     --name AllowMyIPOnly \
     --source-address-prefixes NEW_IP
   ```

2. **Password SQL Server**: Cambia la password di default nel file `.env-azure-aci`

3. **Immagine Docker**: Assicurati che `alnao/gestioneannotazioni:latest` sia sempre aggiornata

4. **VNet Peering**: Se hai altre VNet, puoi fare peering per acceso diretto

## 🐛 Troubleshooting

### Container non si avvia
```bash
az container logs \
  --resource-group gestioneannotazioni-aci-rg \
  --name gestioneannotazioni-aci
```

### Errore: "Resource Group already exists"
```bash
# Aumenta il timestamp dello script per evitare conflitti
# Oppure elimina il resource group esistente
az group delete --name gestioneannotazioni-aci-rg --yes
```

### Errore: "Cannot reach database"
```bash
# Verifica NSG rules
az network nsg rule list \
  --resource-group gestioneannotazioni-aci-rg \
  --nsg-name gestioneannotazioni-aci-nsg \
  --output table

# Verifica SQL Server firewall
az sql server firewall-rule list \
  --resource-group gestioneannotazioni-aci-rg \
  --server SQLSERVER_NAME
```

## 📚 Riferimenti

- [Azure Container Instances Documentation](https://docs.microsoft.com/en-us/azure/container-instances/)
- [Azure Cosmos DB SQL API](https://docs.microsoft.com/en-us/azure/cosmos-db/sql/)
- [Azure SQL Database](https://docs.microsoft.com/en-us/azure/azure-sql/database/)
- [Network Security Groups](https://docs.microsoft.com/en-us/azure/virtual-network/network-security-groups-overview)
