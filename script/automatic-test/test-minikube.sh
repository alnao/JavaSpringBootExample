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
report_run_inizio "kube (minikube)"
echo "[$(date '+%Y-%m-%d %H:%M:%S')] === INIZIO: test-minikube.sh ==="

#cd ..
#./script/push-image-docker-hub.sh 

echo "Posizione script: $(dirname "$0")"
#cd "$(dirname "$0")/.."
echo "Directory di lavoro: $(pwd)"

echo "Avvio Minikube..."
minikube start --memory=6096 --cpus=3


echo "Avvio stack Minikube necessario per i test..."
./script/minikube/start-all.sh

echo "Usando Freelens/Openlens si può verificare lo stato dei servizi/pod..."

# Funzione per terminare l'applicazione in caso di errore
cleanup() {
    ./script/minikube/stop-all.sh > /dev/null 2>&1
    minikube delete > /dev/null 2>&1
    echo "Applicazione terminata e minikube eliminato."
}
trap 'RC=$?; cleanup; report_chiusura $RC' EXIT

sleep 10
URL=$(minikube service gestioneannotazioni-app -n gestioneannotazioni --url)

echo "Attesa avvio applicazione (max 300 secondi)..."
for i in {1..10}; do
    if curl -s $URL/actuator/health > /dev/null 2>&1; then
        echo "Applicazione pronta dopo $((i*30)) secondi"
        break
    fi
    if [ $i -eq 10 ]; then
        test_ko "Avvio applicazione entro il tempo massimo"
        exit 1
    fi
    echo "Attesa... ($((i*30)) secondi trascorsi)"
    sleep 30
done

# Prendo il campo status e verifico se è UP
status=$(curl -s $URL/actuator/health | jq -r .status)
if [ "$status" == "UP" ]; then
    test_ok "Health actuator UP"
else
    test_ko "Health actuator UP (stato: $status)"
    exit 1
fi


# Login e ottenimento token
echo "Esecuzione login..."
token_response=$(curl -s -X POST $URL/api/auth/login -H "Content-Type: application/json" \
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

curl -s $URL/api/annotazioni -H "Authorization: Bearer $token" | jq .  > /dev/null
if [ $? -eq 0 ]; then
    test_ok "GET /api/annotazioni"
else
    test_ko "GET /api/annotazioni"
    exit 1
fi

echo "Creazione annotazione..."
RISPOSTA=$(curl -s -X POST $URL/api/annotazioni \
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

RISPOSTA_INVIO1=$(curl -s -X PATCH $URL/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"vecchioStato":"INSERITA","utente":"admin","nuovoStato":"CONFERMATA"}')
echo "Risposta conferma annotazione: $RISPOSTA_INVIO1"

RISPOSTA_INVIO2=$(curl -s -X PATCH $URL/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"vecchioStato":"CONFERMATA","utente":"admin","nuovoStato":"DAINVIARE"}')
echo "Risposta invio annotazione: $RISPOSTA_INVIO2"

# Verifica che l'annotazione sia stata inviata a Kafka
POD=$(kubectl get pods -n gestioneannotazioni --no-headers | grep ^kafka-service | awk '{print $1}')
echo "POD di kafka: $POD"

echo "Verifica messaggi Kafka (max 10 minuti)..."
max_attempts=40  # 40 tentativi x 15 secondi = 600 secondi (10 minuti)
attempt=0
found_in_kafka=false

while [ $attempt -lt $max_attempts ]; do
    attempt=$((attempt + 1))
    
    # Usa timeout per limitare il tempo di lettura a 10 secondi
    kafka_messages=$(timeout 10s kubectl exec $POD -n gestioneannotazioni -- bash -c \
        "kafka-console-consumer --bootstrap-server localhost:9092 --topic annotazioni-export --from-beginning --timeout-ms 8000" 2>&1 || true)
    
    echo "Tentativo $attempt/$max_attempts - Messaggi ricevuti: ${#kafka_messages} caratteri"
    
    # Verifica se il messaggio contiene l'ID dell'annotazione creata
    if echo "$kafka_messages" | grep -q "$id_creato"; then
        test_ok "Export annotazione sul topic Kafka (tentativo $attempt)"
        echo "Contenuto messaggio: $kafka_messages"
        found_in_kafka=true
        break
    else
        elapsed=$((attempt * 15))
        echo "⏳ Annotazione non ancora presente in Kafka (attesa ${elapsed}s/600s)"
        
        if [ $attempt -lt $max_attempts ]; then
            sleep 15
        fi
    fi
done

if [ "$found_in_kafka" = false ]; then
    test_ko "Export annotazione sul topic Kafka"
    echo "Ultimi messaggi ricevuti: $kafka_messages"
    exit 1
fi


#Avvio lo script dedicato per il test di prenotazione annotazione
echo ""
echo ""
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Esecuzione test di prenotazione annotazione..."
if ! ./script/automatic-test/test-prenotazione-annotazione.sh $URL; then
    echo "Test di prenotazione annotazione falliti: vedi il riepilogo finale"
    ESITO_FIGLI=1
fi

echo ""
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Esecuzione test import/export Kafka..."
./script/automatic-test/test-import-kafka.sh $URL kube

# Terminazione applicazione (gestita da trap cleanup)
echo "Terminazione applicazione"
./script/minikube/stop-all.sh
minikube delete

echo "✅ Test con profilo 'KUBE' su minikube superati!"
echo "[$(date '+%Y-%m-%d %H:%M:%S')] === FINE: test-minikube.sh ==="

if [ "$(report_ko_totali)" -gt 0 ] || [ "${ESITO_FIGLI:-0}" -ne 0 ]; then
    echo "❌ Alcuni test su minikube sono falliti"
    exit 1
fi

echo "✅ Tutti i test sono stati eseguiti con successo!"
exit 0