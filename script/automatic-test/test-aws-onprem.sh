#!/bin/bash
# Script per eseguire tutti i test dell'applicazione con il profilo "kube"
# set -e 

#cd ..
#./script/push-image-docker-hub.sh 

echo "Posizione script: $(dirname "$0")"
#cd "$(dirname "$0")/.."
echo "Directory di lavoro: $(pwd)"

echo "Avvio docker-compose per AWS OnPrem necessari per i test..."
docker-compose -f script/aws-onprem/docker-compose.yml up -d --build

echo "L'applicazione web sarà disponibile su [http://localhost:8082](http://localhost:8082)"
echo "Adminer (MySQL): [http://localhost:8086](http://localhost:8086)"
echo "DynamoDB Admin: [http://localhost:8087](http://localhost:8087)"


# Funzione per terminare l'applicazione in caso di errore
cleanup() {
    docker-compose -f script/aws-onprem/docker-compose.yml down
    docker volume rm $(docker volume ls -q) > /dev/null 2>&1
    docker rmi $(docker images -q) > /dev/null 2>&1
}
trap cleanup EXIT

URL="http://localhost:8082"

echo "Attesa avvio applicazione (max 60 secondi)..."
for i in {1..30}; do
    if curl -s $URL/actuator/health > /dev/null 2>&1; then
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
status=$(curl -s $URL/actuator/health | jq -r .status)
if [ "$status" == "UP" ]; then
    echo "L'applicazione è in esecuzione correttamente."
else
    echo "L'applicazione non è in esecuzione."
    exit 1
fi


# Login e ottenimento token
echo "Esecuzione login..."
token_response=$(curl -s -X POST $URL/api/auth/login -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}')
token=$(echo $token_response | jq -r .token)
echo "Token ottenuto: $token"

if [ -z "$token" ] || [ "$token" == "null" ]; then
    echo "ERRORE: Login fallito. Risposta: $token_response"
    exit 1
else
    echo "Login eseguito correttamente."
fi

curl -s $URL/api/annotazioni -H "Authorization: Bearer $token" | jq .  > /dev/null
if [ $? -eq 0 ]; then
    echo "Chiamata API /api/annotazioni eseguita correttamente."
else
    echo "Chiamata API /api/annotazioni fallita."
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
    echo "✅ Creazione annotazione eseguita correttamente. ID: $id_creato"
else
    echo "❌ Creazione annotazione fallita."
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


SQS=$(aws sqs list-queues --endpoint-url=http://localhost:4566 --region=eu-central-1)
QUEUE_URL=$(echo $SQS | jq -r '.QueueUrls[]')
if [ -z "$QUEUE_URL" ]; then
    echo "❌ ERRORE: Coda SQS 'GestioneAnnotazioniQueue' non trovata."
    exit 1
fi
echo "Coda SQS trovata: $QUEUE_URL"

echo "Verifica messaggi SQS (max 10 minuti)..."
max_attempts=40  # 40 tentativi x 15 secondi = 600 secondi (10 minuti)
attempt=0
found_in_sqs=false

while [ $attempt -lt $max_attempts ]; do
    attempt=$((attempt + 1))
    
    # Ricevi messaggio da SQS
    RISPOSTA=$(aws sqs receive-message \
        --endpoint-url=http://localhost:4566 \
        --queue-url=$QUEUE_URL \
        --region=eu-central-1 \
        --wait-time-seconds=10 \
        --max-number-of-messages=10 2>&1)
    
    echo "Tentativo $attempt/$max_attempts - Verifica presenza messaggi..."
    
    # Verifica se ci sono messaggi
    message_count=$(echo "$RISPOSTA" | jq -r '.Messages | length' 2>/dev/null || echo "0")

    # Gestione valori null o vuoti
    if [ -z "$message_count" ] || [ "$message_count" = "null" ]; then
        message_count=0
    fi

    if [ "$message_count" -gt 0 ]; then
        # Verifica se il messaggio contiene l'ID dell'annotazione creata
        if echo "$RISPOSTA" | jq -r '.Messages[].Body' | grep -q "$id_creato"; then
            echo "✅ Annotazione trovata in SQS al tentativo $attempt"
            echo "Numero messaggi ricevuti: $message_count"
            
            # Mostra il corpo del messaggio
            message_body=$(echo "$RISPOSTA" | jq -r '.Messages[0].Body')
            echo "Corpo messaggio:"
            echo "$message_body" | jq .
            
            found_in_sqs=true
            
            # Opzionale: Cancella il messaggio dalla coda
            #receipt_handle=$(echo "$RISPOSTA" | jq -r '.Messages[0].ReceiptHandle')
            #aws sqs delete-message \
            #    --endpoint-url=http://localhost:4566 \
            #    --queue-url=$QUEUE_URL \
            #    --region=eu-central-1 \
            #    --receipt-handle="$receipt_handle"
            #echo "Messaggio cancellato dalla coda SQS"
            
            break
        else
            echo "⚠️  Messaggi presenti ma ID annotazione non trovato. Continuo..."
        fi
    else
        elapsed=$((attempt * 15))
        echo "⏳ Nessun messaggio in SQS (attesa ${elapsed}s/600s)"
    fi
    
    if [ $attempt -lt $max_attempts ]; then
        sleep 15
    fi
done

if [ "$found_in_sqs" = false ]; then
    echo "❌ Annotazione non trovata nei messaggi SQS dopo 10 minuti."
    echo "Ultima risposta SQS: $RISPOSTA"
    exit 1
fi


#Avvio lo script dedicato per il test di prenotazione annotazione
echo ""
echo ""
echo "Esecuzione test di prenotazione annotazione..."
./script/automatic-test/test-prenotazione-annotazione.sh



# Terminazione applicazione (gestita da trap cleanup)
docker-compose -f script/aws-onprem/docker-compose.yml down

echo "✅ Test con profilo 'KUBE' superati!"

echo "✅ Tutti i test sono stati eseguiti con successo!"
exit 0