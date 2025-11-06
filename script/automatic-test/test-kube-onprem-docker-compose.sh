#!/bin/bash
# Script per eseguire tutti i test dell'applicazione con il profilo "kube"
# set -e 

#cd ..
#./script/push-image-docker-hub.sh 

echo "Posizione script: $(dirname "$0")"
#cd "$(dirname "$0")/.."
echo "Directory di lavoro: $(pwd)"

echo "Costruzione immagine Docker..."
./script/docker-build.sh 

echo  "Avvio stack Docker necessario per i test..."
docker-compose up -d



# Funzione per terminare l'applicazione in caso di errore
cleanup() {
    echo "Terminazione applicazione"
    docker-compose down --remove-orphans
    docker network prune -f  > /dev/null 2>&1
    docker volume rm $(docker volume ls -q)  > /dev/null 2>&1
    docker rmi $(docker images -q)  > /dev/null 2>&1
}
trap cleanup EXIT

echo "Attesa avvio applicazione (max 60 secondi)..."
for i in {1..30}; do
    if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
        echo "Applicazione pronta dopo $((i*2)) secondi"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "ERRORE: Timeout - L'applicazione non si è avviata in 60 secondi"
        exit 1
    fi
    sleep 2
done

# Prendo il campo status e verifico se è UP
status=$(curl -s http://localhost:8082/actuator/health | jq -r .status)
if [ "$status" == "UP" ]; then
    echo "L'applicazione è in esecuzione correttamente."
else
    echo "L'applicazione non è in esecuzione."
    exit 1
fi


# Login e ottenimento token
echo "Esecuzione login..."
token_response=$(curl -s -X POST http://localhost:8082/api/auth/login -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}')
token=$(echo $token_response | jq -r .token)
echo "Token ottenuto: $token"

if [ -z "$token" ] || [ "$token" == "null" ]; then
    echo "ERRORE: Login fallito. Risposta: $token_response"
    exit 1
else
    echo "Login eseguito correttamente."
fi

curl -s http://localhost:8082/api/annotazioni -H "Authorization: Bearer $token" | jq .  > /dev/null
if [ $? -eq 0 ]; then
    echo "Chiamata API /api/annotazioni eseguita correttamente."
else
    echo "Chiamata API /api/annotazioni fallita."
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
    echo "✅ Creazione annotazione eseguita correttamente. ID: $id_creato"
else
    echo "❌ Creazione annotazione fallita."
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
    kafka_messages=$(docker exec -it gestioneannotazioni-kafka kafka-console-consumer \
        --bootstrap-server localhost:29092 \
        --topic annotazioni-export \
        --from-beginning \
        --timeout-ms 10000 \
        --property print.timestamp=true \
        --property print.key=true \
        --property print.value=true 2>/dev/null)

    if echo "$kafka_messages" | grep -q "\"id\":\"$id_creato\""; then
        echo "✅ Annotazione trovata nei messaggi Kafka."
        found_in_kafka=true
        break
    else
        echo "Annotazione non trovata nei messaggi Kafka. Attesa prima del prossimo tentativo..."
    fi

    attempt=$((attempt + 1))
    sleep 15
done
if [ "$found_in_kafka" = false ]; then
    echo "❌ Annotazione non trovata nei messaggi Kafka dopo $max_attempts tentativi."
    exit 1
fi

# Terminazione applicazione (gestita da trap cleanup)
echo "Terminazione applicazione"
docker-compose down --remove-orphans
docker network prune -f > /dev/null 2>&1
docker volume rm $(docker volume ls -q) > /dev/null 2>&1
docker rmi $(docker images -q) > /dev/null 2>&1

echo "✅ Test con profilo 'KUBE' superati!"

echo "✅ Tutti i test sono stati eseguiti con successo!"
exit 0