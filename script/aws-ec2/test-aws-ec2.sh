#!/bin/bash

# Avvio completo stack Gestione annotazioni AWS (Aurora MySQL, DynamoDB, EC2 Docker)
# Richiede AWS CLI configurato e permessi admin

set -e
# Disabilita paginazione aws cli
export AWS_PAGER=""

REGION="eu-central-1"
INSTANCE_ID=$(aws ec2 describe-instances --region $REGION --filters Name=tag:gestioneannotazioni-app,Values=true Name=instance-state-name,Values=running,stopped --query 'Reservations[].Instances[].InstanceId' --output text)
PUBLIC_IP=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --region $REGION --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
IMPORT_QUEUE_NAME="gestioneannotazioni-annotazioni-import"


echo "Instance ID: $INSTANCE_IDS con PUBLIC_IP $PUBLIC_IP"

URL="http://$PUBLIC_IP:8080"


#### SAVE AND EXPORT


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



#### IMPORT

IMPORT_QUEUE_URL=$(aws sqs list-queues \
    --region=eu-central-1 \
    --queue-name-prefix=$IMPORT_QUEUE_NAME \
    | jq -r '.QueueUrls[0]')


IMPORT_UUID=$(cat /proc/sys/kernel/random/uuid)
echo "UUID annotazione da importare: $IMPORT_UUID"

# Costruisce il payload JSON nel formato AnnotazioneCompleta atteso dal SqsAnnotazioneImportService
IMPORT_PAYLOAD=$(cat <<PAYLOAD
{
  "annotazione": {
    "id": "$IMPORT_UUID",
    "titolo": "Annotazione Importata da SQS",
    "descrizione": "Test import automatico da coda SQS",
    "valoreNota": "Valore importato",
    "dataCreazione": "2024-06-01T10:00:00Z"
  },
  "metadata": {
    "id": "$IMPORT_UUID",
    "stato": "DAINVIARE",
    "utente": "admin",
    "descrizione": "Test import automatico",
    "pubblica": false,
    "priorita": 1
  }
}
PAYLOAD
)

echo "Invio messaggio nella coda SQS import..."
SEND_RESULT=$(aws sqs send-message \
    --region=eu-central-1 \
    --queue-url="$IMPORT_QUEUE_URL" \
    --message-body "$IMPORT_PAYLOAD" 2>&1)

SEND_MSG_ID=$(echo "$SEND_RESULT" | jq -r '.MessageId' 2>/dev/null)
if [ -z "$SEND_MSG_ID" ] || [ "$SEND_MSG_ID" == "null" ]; then
    echo "❌ ERRORE: Invio messaggio SQS import fallito."
    echo "   Risposta: $SEND_RESULT"
    exit 1
fi
echo "✅ Messaggio inviato nella coda import. MessageId: $SEND_MSG_ID"

# Attende che lo scheduler processi il messaggio (cron ogni 30 secondi, max 3 minuti)
echo "Attesa elaborazione import da parte dello scheduler (max 3 minuti)..."
max_import_attempts=36  # 36 tentativi x 10 secondi = 6 minuti
import_attempt=0
import_found=false

while [ $import_attempt -lt $max_import_attempts ]; do
    import_attempt=$((import_attempt + 1))
    elapsed_import=$((import_attempt * 10))
    echo "⏳ Tentativo $import_attempt/$max_import_attempts - Verifica annotazione importata (${elapsed_import}s/180s)..."

    RISPOSTA_IMPORT=$(curl -s $URL/api/annotazioni/$IMPORT_UUID \
        -H "Authorization: Bearer $token")

    stato_importata=$(echo "$RISPOSTA_IMPORT" | jq -r '.stato' 2>/dev/null)

    if [ "$stato_importata" == "IMPORTATA" ]; then
        echo "✅ Annotazione $IMPORT_UUID trovata con stato IMPORTATA al tentativo $import_attempt"
        echo "Dettagli annotazione importata:"
        echo "$RISPOSTA_IMPORT" | jq .
        import_found=true
        break
    elif [ "$stato_importata" != "null" ] && [ -n "$stato_importata" ]; then
        echo "⚠️  Annotazione trovata ma stato non ancora IMPORTATA: $stato_importata. Continuo..."
    fi

    sleep 10
done

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Esecuzione test di prenotazione annotazione..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
"$PROJECT_ROOT/script/automatic-test/test-prenotazione-annotazione.sh" "$URL"




echo "Script complete"