# Test Azure Cosmos DB con Spring Boot
# Questo script avvia l'applicazione Spring Boot configurata per utilizzare un'istanza di Azure Cosmos DB remota.
#!/bin/bash

# --- Logging: scrive su automatic-test-YYYYMMDD.log
if [ -z "$LOG_FILE" ]; then
  LOG_FILE="./automatic-test-$(date +%Y%m%d).log"
  exec > >(tee -a "$LOG_FILE") 2>&1
fi
export LOG_FILE
echo "[$(date '+%Y-%m-%d %H:%M:%S')] === INIZIO: test-azure-cosmos-runlocale.sh ==="

#echo "🚀 Avvio test applicazione con Azure Cosmos DB remoto..."

# TODO: Implementare il test automatico dell'applicazione con Azure Cosmos DB remoto