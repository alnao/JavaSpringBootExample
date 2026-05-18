#!/bin/bash

# Nota questo script non è autonomo: richiede che il sistema di gestione annotazioni sia in esecuzione eseguito da altri
# Script di test per la funzionalità di IMPORT annotazioni da coda Kafka (topic annotazioni-import)
# Invia un messaggio JSON AnnotazioneCompleta sul topic di import e verifica che l'annotazione
# venga salvata nel sistema con stato IMPORTATA.
# Parametri:
#   $1 = BASE_URL (default: http://localhost:8082)
#   $2 = KAFKA_TYPE: "docker" (default) oppure "kube"

BASE_URL="http://localhost:8082/api"
if [ ! -z "$1" ]; then
  BASE_URL="$1/api"
fi

KAFKA_TYPE="docker"
if [ ! -z "$2" ]; then
  KAFKA_TYPE="$2"
fi

if [ -z "$LOG_FILE" ]; then
  LOG_FILE="./automatic-test-$(date +%Y%m%d).log"
  exec > >(tee -a "$LOG_FILE") 2>&1
fi
export LOG_FILE

# Colori per output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')] Test Import Kafka Annotazioni${NC}"
echo -e "${BLUE}BASE_URL: $BASE_URL  KAFKA_TYPE: $KAFKA_TYPE${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# 1. Login utente admin
echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] 1. Login utente admin...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin"}')

TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
    echo -e "${RED}✗ Errore: Login fallito${NC}"
    echo "Risposta: $LOGIN_RESPONSE"
    exit 1
fi
echo -e "${GREEN}✓ Login admin effettuato con successo${NC}"
echo ""

# 2. Generazione UUID e invio messaggio sul topic annotazioni-import
echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] 2. Invio messaggio AnnotazioneCompleta sul topic annotazioni-import...${NC}"
ANNOTATION_ID=$(uuidgen 2>/dev/null || cat /proc/sys/kernel/random/uuid 2>/dev/null || python3 -c "import uuid; print(uuid.uuid4())")
TIME=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
VALORE_NOTA="Annotazione importata da Kafka $TIME"

KAFKA_MESSAGE=$(cat <<EOF
{"annotazione":{"id":"$ANNOTATION_ID","valoreNota":"$VALORE_NOTA","versioneNota":"1.0"},"metadata":{"id":"$ANNOTATION_ID","utenteCreazione":"admin","utenteUltimaModifica":"admin","descrizione":"Test import da Kafka","stato":"INSERITA"}}
EOF
)

if [ "$KAFKA_TYPE" = "kube" ]; then
    KAFKA_POD=$(kubectl get pods -n gestioneannotazioni --no-headers 2>/dev/null | grep "^kafka-service" | awk '{print $1}')
    if [ -z "$KAFKA_POD" ]; then
        echo -e "${RED}✗ Errore: Pod Kafka non trovato in Kubernetes${NC}"
        exit 1
    fi
    echo "$KAFKA_MESSAGE" | kubectl exec -i "$KAFKA_POD" -n gestioneannotazioni -- \
        kafka-console-producer --bootstrap-server localhost:9092 --topic annotazioni-import 2>&1
else
    echo "$KAFKA_MESSAGE" | docker exec -i gestioneannotazioni-kafka \
        kafka-console-producer --bootstrap-server localhost:29092 --topic annotazioni-import 2>&1
fi

if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Errore: Invio messaggio Kafka fallito${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Messaggio inviato sul topic annotazioni-import (ID: $ANNOTATION_ID)${NC}"
echo ""

# 3. Attesa e verifica che l'annotazione venga importata con stato IMPORTATA (max 5 minuti)
echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] 3. Verifica stato annotazione IMPORTATA (max 5 minuti)...${NC}"
max_attempts=60
attempt=0
stato_finale=""

while [ $attempt -lt $max_attempts ]; do
    attempt=$((attempt + 1))
    STATO_RESPONSE=$(curl -s "$BASE_URL/annotazioni/$ANNOTATION_ID" \
      -H "Authorization: Bearer $TOKEN")
    stato_finale=$(echo "$STATO_RESPONSE" | grep -o '"stato":"[^"]*' | cut -d'"' -f4)

    if [ "$stato_finale" = "IMPORTATA" ]; then
        echo -e "${GREEN}✓ Annotazione importata correttamente con stato IMPORTATA al tentativo $attempt${NC}"
        break
    fi

    if [ -n "$stato_finale" ] && [ "$stato_finale" != "null" ]; then
        echo "  Stato attuale: $stato_finale - attesa consumer Kafka..."
    else
        elapsed=$((attempt * 5))
        echo "  [${elapsed}s/300s] Annotazione non ancora visibile tramite API - attesa consumer..."
    fi
    sleep 5
done

if [ "$stato_finale" != "IMPORTATA" ]; then
    echo -e "${RED}✗ Annotazione non risulta con stato IMPORTATA dopo 5 minuti (stato: $stato_finale)${NC}"
    exit 1
fi
echo ""

# 4. Cleanup: eliminazione annotazione importata
echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] 4. Pulizia: eliminazione annotazione importata...${NC}"
DELETE_RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X DELETE "$BASE_URL/annotazioni/$ANNOTATION_ID" \
  -H "Authorization: Bearer $TOKEN")

HTTP_CODE=$(echo "$DELETE_RESPONSE" | grep "HTTP_CODE" | cut -d':' -f2)
if [ "$HTTP_CODE" = "204" ]; then
    echo -e "${GREEN}✓ Annotazione eliminata${NC}"
else
    echo -e "${YELLOW}⚠ Eliminazione annotazione: HTTP $HTTP_CODE (non bloccante)${NC}"
fi
echo ""

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')] ✓ Test Import Kafka completati${NC}"
echo -e "${BLUE}========================================${NC}"
