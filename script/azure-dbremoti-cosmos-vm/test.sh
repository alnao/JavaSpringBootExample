#!/bin/bash
# Script per eseguire tutti i test dell'applicazione
# set -e 


echo "Posizione script: $(dirname "$0")"


# Funzione per terminare l'applicazione in caso di errore
cleanup() {
    echo "Terminato script con cleanup"
}
trap cleanup EXIT


SERVICE_URL=$(az vm show -d -g gestioneannotazioni-rg-cosmos-mssql -n gestioneannotazioni-vm --query publicIps --output tsv)
SERVICE_URL="$SERVICE_URL:8082"
echo "Service URL: $SERVICE_URL"

echo "Attesa avvio applicazione (max 60 secondi)..."
for i in {1..30}; do
    if curl -s http://$SERVICE_URL/actuator/health > /dev/null 2>&1; then
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
status=$(curl -s http://$SERVICE_URL/actuator/health | jq -r .status)
if [ "$status" == "UP" ]; then
    echo "L'applicazione è in esecuzione correttamente."
else
    echo "L'applicazione non è in esecuzione."
    exit 1
fi


# Login e ottenimento token
echo "Esecuzione login..."
token_response=$(curl -s -X POST http://$SERVICE_URL/api/auth/login -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}')
token=$(echo $token_response | jq -r .token)
echo "Token ottenuto: $token"

if [ -z "$token" ] || [ "$token" == "null" ]; then
    echo "ERRORE: Login fallito. Risposta: $token_response"
    exit 1
else
    echo "Login eseguito correttamente."
fi

curl -s http://$SERVICE_URL/api/annotazioni -H "Authorization: Bearer $token" | jq .  > /dev/null
if [ $? -eq 0 ]; then
    echo "Chiamata API /api/annotazioni eseguita correttamente."
else
    echo "Chiamata API /api/annotazioni fallita."
    exit 1
fi

echo "Creazione annotazione..."
RISPOSTA=$(curl -s -X POST http://$SERVICE_URL/api/annotazioni \
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

RISPOSTA_INVIO1=$(curl -s -X PATCH http://$SERVICE_URL/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"vecchioStato":"INSERITA","utente":"admin","nuovoStato":"CONFERMATA"}')
echo "Risposta conferma annotazione: $RISPOSTA_INVIO1"

RISPOSTA_INVIO2=$(curl -s -X PATCH http://$SERVICE_URL/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"vecchioStato":"CONFERMATA","utente":"admin","nuovoStato":"DAINVIARE"}')
echo "Risposta invio annotazione: $RISPOSTA_INVIO2"




# Verifica che l'annotazione sia stata inviata a Service Bus
echo "Verifica invio annotazione a Service Bus (max 10 minuti)..."
max_attempts=40  # 40 tentativi x 15 secondi = 600 secondi (10 minuti)
attempt=0
found_in_servicebus=false

RESOURCE_GROUP="gestioneannotazioni-rg-cosmos-mssql"
SERVICEBUS_NAMESPACE="gestioneannotazioni-servicebus"
QUEUE_NAME="eventbus-annotazioni"

while [ $attempt -lt $max_attempts ]; do
    echo "Controllo messaggi Service Bus, tentativo $((attempt + 1))..."
    
    # Conta messaggi attivi nella coda
    message_count=$(az servicebus queue show \
        --resource-group $RESOURCE_GROUP \
        --namespace-name $SERVICEBUS_NAMESPACE \
        --name $QUEUE_NAME \
        --query "countDetails.activeMessageCount" \
        --output tsv 2>/dev/null)
    
    if [ "$message_count" -gt 0 ]; then
        echo "✅ Trovati $message_count messaggi nella coda Service Bus."
        found_in_servicebus=true
        break
    else
        echo "Nessun messaggio trovato in Service Bus. Attesa prima del prossimo tentativo..."
    fi
    
    attempt=$((attempt + 1))
    sleep 15
done

if [ "$found_in_servicebus" = false ]; then
    echo "❌ Nessun messaggio trovato in Service Bus dopo $max_attempts tentativi."
    exit 1
fi


echo "✅ Tutti i test sono stati eseguiti con successo!"
exit 0