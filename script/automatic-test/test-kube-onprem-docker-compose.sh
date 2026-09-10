#!/bin/bash
# Script per eseguire tutti i test dell'applicazione con il profilo "kube"
# set -e 

# --- Logging: scrive su automatic-test-YYYYMMDD.log
if [ -z "$LOG_FILE" ]; then
  LOG_FILE="./automatic-test-$(date +%Y%m%d).log"
  exec > >(tee -a "$LOG_FILE") 2>&1
fi
export LOG_FILE

# Conteggio dei test e riepilogo finale
source "$(dirname "$0")/lib-report.sh"
report_run_inizio "kube (docker-compose)"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] === INIZIO: test-kube-onprem-docker-compose.sh ==="

#cd ..
#./script/push-image-docker-hub.sh 

echo "Posizione script: $(dirname "$0")"
#cd "$(dirname "$0")/.."
echo "Directory di lavoro: $(pwd)"

#echo "Costruzione immagine Docker..."
#./script/docker-build.sh 

echo  "Avvio stack Docker necessario per i test..."
docker-compose up -d --build



# Funzione per terminare l'applicazione in caso di errore
cleanup() {
    # Limitato alle risorse dichiarate in questo docker-compose: container,
    # rete, volumi e immagini costruite localmente. Non tocca immagini e volumi
    # di altri progetti presenti sulla macchina.
    docker-compose down --volumes --remove-orphans --rmi local
    echo "Script test-kube-onprem-docker-compose concluso"
}
trap 'RC=$?; cleanup; report_chiusura $RC' EXIT

echo "Attesa avvio applicazione (max 60 secondi)..."
for i in {1..30}; do
    if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
        echo "Applicazione pronta dopo $((i*2)) secondi"
        break
    fi
    if [ $i -eq 30 ]; then
        test_ko "Avvio applicazione entro 60 secondi"
        exit 1
    fi
    sleep 2
done

# Prendo il campo status e verifico se è UP
status=$(curl -s http://localhost:8082/actuator/health | jq -r .status)
if [ "$status" == "UP" ]; then
    test_ok "Health actuator UP"
else
    test_ko "Health actuator UP (stato: $status)"
    exit 1
fi


# Login e ottenimento token
echo "Esecuzione login..."
token_response=$(curl -s -X POST http://localhost:8082/api/auth/login -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}')
token=$(echo $token_response | jq -r .token)
echo "Token ottenuto: $token"

if [ -z "$token" ] || [ "$token" == "null" ]; then
    test_ko "Login utente admin"
    echo "   Risposta: $token_response"
    exit 1
else
    test_ok "Login utente admin"
fi

curl -s http://localhost:8082/api/annotazioni -H "Authorization: Bearer $token" | jq .  > /dev/null
if [ $? -eq 0 ]; then
    test_ok "GET /api/annotazioni"
else
    test_ko "GET /api/annotazioni"
    exit 1
fi

echo "Creazione annotazione..."
RISPOSTA=$(curl -s -X POST http://localhost:8082/api/annotazioni \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"titolo":"Test Annotazione","descrizione":"Descrizione di test","valoreNota":"Valore di test","stato":"INSERITA","dataCreazione":"2024-06-01T12:00:00Z","utente":"admin"}' \
         | jq .)
echo "Risposta POST annotazione: $RISPOSTA"
# Verifica che la risposta contenga un ID (segno di successo)
id_creato=$(echo $RISPOSTA | jq -r .id 2>/dev/null)
if [ -n "$id_creato" ] && [ "$id_creato" != "null" ]; then
    test_ok "Creazione annotazione (ID: $id_creato)"
else
    test_ko "Creazione annotazione"
    echo "   Risposta completa: $RISPOSTA"
    exit 1
fi

# da risposta prendo l'id e provo a inviare l'annotazione
echo "Invio annotazione creata (ID: $id_creato)..."

RISPOSTA_INVIO1=$(curl -s -X PATCH http://localhost:8082/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"vecchioStato":"INSERITA","utente":"admin","nuovoStato":"CONFERMATA"}')
echo "Risposta conferma annotazione: $RISPOSTA_INVIO1"

RISPOSTA_INVIO2=$(curl -s -X PATCH http://localhost:8082/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"vecchioStato":"CONFERMATA","utente":"admin","nuovoStato":"DAINVIARE"}')
echo "Risposta invio annotazione: $RISPOSTA_INVIO2"

# Verifica che l'annotazione sia stata inviata a Kafka
echo "Verifica invio annotazione a Kafka (max 10 minuti)..."
max_attempts=40  # 40 tentativi x 15 secondi = 600 secondi (10 minuti)
attempt=0
found_in_kafka=false    

while [ $attempt -lt $max_attempts ]; do
    echo "Controllo messaggi Kafka, tentativo $((attempt + 1))..."
    # Niente -it: richiede un terminale e farebbe fallire lo script in CI o in background
    kafka_messages=$(docker exec gestioneannotazioni-kafka kafka-console-consumer \
        --bootstrap-server localhost:29092 \
        --topic annotazioni-export \
        --from-beginning \
        --timeout-ms 10000 \
        --property print.timestamp=true \
        --property print.key=true \
        --property print.value=true 2>/dev/null)

    if echo "$kafka_messages" | grep -q "\"id\":\"$id_creato\""; then
        echo "Annotazione trovata nei messaggi Kafka al tentativo $((attempt + 1))."
        found_in_kafka=true
        break
    else
        echo "Annotazione non trovata nei messaggi Kafka. Attesa prima del prossimo tentativo..."
    fi

    attempt=$((attempt + 1))
    sleep 15
done
if [ "$found_in_kafka" = false ]; then
    test_ko "Export annotazione sul topic Kafka annotazioni-export"
    exit 1
fi

test_ok "Export annotazione sul topic Kafka annotazioni-export"

#Avvio lo script dedicato per il test di prenotazione annotazione
echo ""
echo ""
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Esecuzione test di prenotazione annotazione..."
if ! ./script/automatic-test/test-prenotazione-annotazione.sh; then
    echo "Test di prenotazione annotazione falliti: vedi il riepilogo finale"
    ESITO_FIGLI=1
fi

echo ""
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Esecuzione test import/export Kafka..."
if ! ./script/automatic-test/test-import-kafka.sh; then
    echo "Test di import Kafka falliti: vedi il riepilogo finale"
    ESITO_FIGLI=1
fi

# Terminazione dello stack: gestita dal trap cleanup

echo "✅ Test con profilo 'KUBE' superati!"
echo "[$(date '+%Y-%m-%d %H:%M:%S')] === FINE: test-kube-onprem-docker-compose.sh ==="

if [ "$(report_ko_totali)" -gt 0 ] || [ "${ESITO_FIGLI:-0}" -ne 0 ]; then
    echo "❌ Alcuni test del profilo 'kube' sono falliti"
    exit 1
fi

echo "✅ Tutti i test sono stati eseguiti con successo!"
exit 0