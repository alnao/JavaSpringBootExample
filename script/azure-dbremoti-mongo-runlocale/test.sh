#!/bin/bash
# Script per eseguire tutti i test dell'applicazione
# set -e 


echo "Posizione script: $(dirname "$0")"


# Funzione per terminare l'applicazione in caso di errore
cleanup() {
    echo "Terminato script con cleanup"
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



# Verifica che l'annotazione sia stata inviata a Event Hub
echo "Verifica invio annotazione a Event Hub (max 10 minuti)..."
max_attempts=40  # 40 tentativi x 15 secondi = 600 secondi (10 minuti)
attempt=0
found_in_eventhub=false  # Inizializza la variabile

RESOURCE_GROUP="gestioneannotazioni-rg-mongo-postgres"
EVENT_HUBS_NAMESPACE="gestioneannotazioni-eventhubs"
EVENT_HUB_NAME="annotazioni-export"

echo "🔑 Recupero connection string Event Hub..."
EVENTHUBS_CONNECTION_STRING=$(az eventhubs namespace authorization-rule keys list \
  --resource-group $RESOURCE_GROUP \
  --namespace-name $EVENT_HUBS_NAMESPACE \
  --name RootManageSharedAccessKey \
  --query primaryConnectionString -o tsv)

# Rimuovi "Endpoint=..." se presente
#if [[ $EVENTHUBS_CONNECTION_STRING == Endpoint=* ]]; then
#    EVENTHUBS_CONNECTION_STRING=$(echo "$EVENTHUBS_CONNECTION_STRING" | sed 's/^Endpoint=[^;]*;//')
#fi
KAFKA_URL="${EVENT_HUBS_NAMESPACE}.servicebus.windows.net:9093"
echo "  Kafka URL: $KAFKA_URL"

echo "📦 Verifica installazione kcat..."
if ! command -v kcat &> /dev/null; then
    echo "⚠️  kcat non installato. Deve essere installato con apt-get install -y kcat"
    exit 1
fi

while [ $attempt -lt $max_attempts ]; do
    echo "🔍 Controllo messaggi Event Hub con kcat, tentativo $((attempt + 1))/$max_attempts..."
    
    # Leggi ultimi messaggi dal topic
    messages=$(kcat -b $KAFKA_URL \
        -X security.protocol=SASL_SSL \
        -X sasl.mechanisms=PLAIN \
        -X sasl.username='$ConnectionString' \
        -X sasl.password="$EVENTHUBS_CONNECTION_STRING" \
        -C -t "$EVENT_HUB_NAME" \
        -o beginning \
        -c 1 \
        2>/dev/null)

    echo "$messages"
    
    if [ -n "$messages" ]; then
        message_count=$(echo "$messages" | wc -l)
        echo "✅ Trovati $message_count messaggi in Event Hub (Kafka)."
        echo "📄 Esempio primo messaggio:"
        echo "$messages" | head -1 | cut -c1-200
        found_in_eventhub=true
        break
    else
        echo "   Nessun messaggio trovato. Attesa 15 secondi..."
    fi
    
    attempt=$((attempt + 1))
    sleep 15
done

if [ "$found_in_eventhub" = false ]; then
    echo "❌ Nessun messaggio trovato in Event Hub dopo $max_attempts tentativi."
    echo "ℹ️  Verifica manualmente i log del container:"
    echo "   docker logs azure-dbremoti-mongo-runlocale | grep -i kafka"
    exit 1
fi

echo "✅ Tutti i test sono stati eseguiti con successo!"
exit 0
