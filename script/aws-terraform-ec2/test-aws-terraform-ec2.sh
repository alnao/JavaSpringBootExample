#!/bin/bash
# Test automatici sullo stack creato da script/aws-terraform-ec2/start-all.sh:
# health, login, creazione e invio annotazione, export su SQS, import da SQS e
# prenotazione. Derivato da script/aws-ec2/test-aws-ec2.sh: cambia il tag con
# cui viene individuata la EC2 (marcatore Terraform) e gli esiti vengono
# registrati con lib-report.sh, come per gli altri test del progetto.
# Richiede AWS CLI configurata, curl e jq.

export AWS_PAGER=""
REGION="${AWS_REGION:-eu-central-1}"
IMPORT_QUEUE_NAME="gestioneannotazioni-annotazioni-import"
MARKER_TAG="gestioneannotazioni-terraform-app"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Conteggio dei test e riepilogo finale (registro condiviso con gli script figli)
source "$PROJECT_ROOT/script/automatic-test/lib-report.sh"
report_run_inizio "aws (terraform-ec2)"
trap 'RC=$?; [ "$(report_ko_totali)" -gt 0 ] && RC=1; report_chiusura $RC; exit $RC' EXIT

echo "[$(date '+%Y-%m-%d %H:%M:%S')] === INIZIO: test-aws-terraform-ec2.sh ==="

# --- Individua la EC2 dello stack Terraform tramite il marcatore ---
INSTANCE_ID=$(aws ec2 describe-instances --region $REGION \
    --filters "Name=tag:$MARKER_TAG,Values=true" "Name=instance-state-name,Values=running" \
    --query 'Reservations[].Instances[].InstanceId' --output text 2>/dev/null)
if [ -z "$INSTANCE_ID" ] || [ "$INSTANCE_ID" == "None" ]; then
    test_ko "Istanza EC2 con tag $MARKER_TAG=true non trovata: lo stack Terraform non e' attivo? (./script/aws-terraform-ec2/start-all.sh)"
    exit 1
fi
PUBLIC_IP=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --region $REGION \
    --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
test_ok "Istanza EC2 trovata: $INSTANCE_ID con IP pubblico $PUBLIC_IP"
URL="http://$PUBLIC_IP:8080"

# --- Health ---
HEALTH=$(curl -s --max-time 10 $URL/actuator/health | jq -r .status 2>/dev/null)
if [ "$HEALTH" == "UP" ]; then
    test_ok "Health actuator UP"
else
    test_ko "Health actuator non UP (risposta: '$HEALTH'): l'applicazione non risponde su $URL"
    exit 1
fi

#### SAVE AND EXPORT

# Login e ottenimento token
token_response=$(curl -s -X POST $URL/api/auth/login -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}')
token=$(echo "$token_response" | jq -r .token 2>/dev/null)
if [ -z "$token" ] || [ "$token" == "null" ]; then
    test_ko "Login utente admin (risposta: $token_response)"
    exit 1
fi
test_ok "Login utente admin"

if curl -s $URL/api/annotazioni -H "Authorization: Bearer $token" | jq . > /dev/null 2>&1; then
    test_ok "GET /api/annotazioni"
else
    test_ko "GET /api/annotazioni"
    exit 1
fi

RISPOSTA=$(curl -s -X POST $URL/api/annotazioni \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"titolo":"Test Annotazione","descrizione":"Descrizione di test","valoreNota":"Valore di test","stato":"INSERITA","dataCreazione":"2024-06-01T12:00:00Z","utente":"admin"}')
id_creato=$(echo "$RISPOSTA" | jq -r .id 2>/dev/null)
if [ -n "$id_creato" ] && [ "$id_creato" != "null" ]; then
    test_ok "Creazione annotazione (ID: $id_creato)"
else
    test_ko "Creazione annotazione (risposta: $RISPOSTA)"
    exit 1
fi

# Conferma e marcatura per l'invio: lo scheduler la esporta sulla coda SQS
RISPOSTA_INVIO1=$(curl -s -X PATCH $URL/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" -H "Content-Type: application/json" \
    -d '{"vecchioStato":"INSERITA","utente":"admin","nuovoStato":"CONFERMATA"}')
RISPOSTA_INVIO2=$(curl -s -X PATCH $URL/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" -H "Content-Type: application/json" \
    -d '{"vecchioStato":"CONFERMATA","utente":"admin","nuovoStato":"DAINVIARE"}')
stato_finale=$(echo "$RISPOSTA_INVIO2" | jq -r .stato 2>/dev/null)
if [ "$stato_finale" == "DAINVIARE" ]; then
    test_ok "Conferma e invio annotazione (stato DAINVIARE)"
else
    test_ko "Conferma e invio annotazione (risposte: $RISPOSTA_INVIO1 / $RISPOSTA_INVIO2)"
fi

#### IMPORT

IMPORT_QUEUE_URL=$(aws sqs list-queues --region=$REGION --queue-name-prefix=$IMPORT_QUEUE_NAME | jq -r '.QueueUrls[0]')
IMPORT_UUID=$(cat /proc/sys/kernel/random/uuid)
echo "UUID annotazione da importare: $IMPORT_UUID"

# Payload nel formato AnnotazioneCompleta atteso dal SqsAnnotazioneImportService
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

SEND_RESULT=$(aws sqs send-message --region=$REGION --queue-url="$IMPORT_QUEUE_URL" --message-body "$IMPORT_PAYLOAD" 2>&1)
SEND_MSG_ID=$(echo "$SEND_RESULT" | jq -r '.MessageId' 2>/dev/null)
if [ -z "$SEND_MSG_ID" ] || [ "$SEND_MSG_ID" == "null" ]; then
    test_ko "Invio messaggio sulla coda SQS di import (risposta: $SEND_RESULT)"
    exit 1
fi
test_ok "Invio messaggio sulla coda SQS di import (MessageId: $SEND_MSG_ID)"

# Attende che lo scheduler processi il messaggio (cron ogni 2 minuti, max 6 minuti)
max_import_attempts=36
import_attempt=0
import_found=false
while [ $import_attempt -lt $max_import_attempts ]; do
    import_attempt=$((import_attempt + 1))
    echo "⏳ Tentativo $import_attempt/$max_import_attempts - verifica annotazione importata ($((import_attempt * 10))s/360s)..."
    RISPOSTA_IMPORT=$(curl -s $URL/api/annotazioni/$IMPORT_UUID -H "Authorization: Bearer $token")
    stato_importata=$(echo "$RISPOSTA_IMPORT" | jq -r '.stato' 2>/dev/null)
    if [ "$stato_importata" == "IMPORTATA" ]; then
        import_found=true
        break
    elif [ "$stato_importata" != "null" ] && [ -n "$stato_importata" ]; then
        echo "⚠️  Annotazione trovata ma stato non ancora IMPORTATA: $stato_importata. Continuo..."
    fi
    sleep 10
done
if [ "$import_found" == "true" ]; then
    test_ok "Import annotazione da SQS con stato IMPORTATA (tentativo $import_attempt)"
    echo "Dettagli annotazione importata:"
    echo "$RISPOSTA_IMPORT" | jq .
else
    test_ko "Import annotazione da SQS: $IMPORT_UUID non risulta IMPORTATA dopo $max_import_attempts tentativi"
fi

# --- Prenotazione (script condiviso, registra i propri test nello stesso riepilogo) ---
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Esecuzione test di prenotazione annotazione..."
"$PROJECT_ROOT/script/automatic-test/test-prenotazione-annotazione.sh" "$URL"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] === FINE: test-aws-terraform-ec2.sh ==="
