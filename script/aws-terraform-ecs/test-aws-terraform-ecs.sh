#!/bin/bash
# Test automatici sullo stack creato da script/aws-terraform-ecs/start-all.sh.
# Derivato da script/aws-ecs/test-aws-ecs.sh (stesse 10 fasi: endpoint del task,
# disponibilita', login, lettura e creazione annotazione, transizioni di stato,
# import da SQS, prenotazione); gli esiti vengono registrati con lib-report.sh,
# come per gli altri test del progetto. Cluster e service hanno gli stessi nomi
# dello stack bash: il test vale per lo stack Terraform attivo.
# Richiede AWS CLI configurata, curl e jq.

export AWS_PAGER=""
REGION="${AWS_REGION:-eu-central-1}"
CLUSTER_NAME="gestioneannotazioni-cluster"
SERVICE_NAME="gestioneannotazioni-service"
IMPORT_QUEUE_NAME="gestioneannotazioni-annotazioni-import"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

# Conteggio dei test e riepilogo finale (registro condiviso con gli script figli)
source "$PROJECT_ROOT/script/automatic-test/lib-report.sh"
report_run_inizio "aws (terraform-ecs)"
trap 'RC=$?; [ "$(report_ko_totali)" -gt 0 ] && RC=1; report_chiusura $RC; exit $RC' EXIT

echo "=== TEST AWS ECS (TERRAFORM) GESTIONEANNOTAZIONI ==="
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Inizio test..."

# === Fase 1: task RUNNING del service e suo IP pubblico (dalla ENI) ===
echo ""
echo "Fase 1: Recupero endpoint ECS..."
TASK_ARN=$(aws ecs list-tasks --cluster $CLUSTER_NAME --service-name $SERVICE_NAME --desired-status RUNNING --region $REGION --query 'taskArns[0]' --output text 2>/dev/null)
if [ -z "$TASK_ARN" ] || [ "$TASK_ARN" == "None" ]; then
    test_ko "Nessun task RUNNING nel service $SERVICE_NAME del cluster $CLUSTER_NAME: lo stack Terraform non e' attivo? (./script/aws-terraform-ecs/start-all.sh)"
    exit 1
fi
test_ok "Task RUNNING trovato: $TASK_ARN"

ENI_ID=$(aws ecs describe-tasks --cluster $CLUSTER_NAME --tasks $TASK_ARN --region $REGION --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' --output text 2>/dev/null)
PUBLIC_IP=""
if [ -n "$ENI_ID" ] && [ "$ENI_ID" != "None" ]; then
    PUBLIC_IP=$(aws ec2 describe-network-interfaces --network-interface-ids $ENI_ID --region $REGION --query 'NetworkInterfaces[0].Association.PublicIp' --output text 2>/dev/null)
fi
if [ -z "$PUBLIC_IP" ] || [ "$PUBLIC_IP" == "None" ]; then
    test_ko "IP pubblico del task non recuperabile (ENI: $ENI_ID)"
    exit 1
fi
test_ok "IP pubblico del task: $PUBLIC_IP"
URL="http://$PUBLIC_IP:8080"

# === Fase 2: disponibilita' del servizio ===
echo ""
echo "Fase 2: Attesa disponibilita' servizio..."
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -s --max-time 10 $URL/actuator/health | jq -e '.status == "UP"' > /dev/null 2>&1; then
        break
    fi
    attempt=$((attempt + 1))
    [ $attempt -lt $max_attempts ] && echo "⏳ Tentativo $attempt/$max_attempts: servizio non ancora disponibile, attendo..." && sleep 5
done
if [ $attempt -lt $max_attempts ]; then
    test_ok "Health actuator UP su $URL"
else
    test_ko "Health actuator non UP dopo $max_attempts tentativi su $URL"
    exit 1
fi

# === Fase 3: login ===
echo ""
echo "Fase 3: Autenticazione..."
token_response=$(curl -s -X POST $URL/api/auth/login -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}')
token=$(echo "$token_response" | jq -r .token 2>/dev/null)
if [ -z "$token" ] || [ "$token" == "null" ]; then
    test_ko "Login utente admin (risposta: $token_response)"
    exit 1
fi
test_ok "Login utente admin"

# === Fase 4: GET annotazioni ===
echo ""
echo "Fase 4: Test API getAnnotazioni..."
if curl -s $URL/api/annotazioni -H "Authorization: Bearer $token" | jq . > /dev/null 2>&1; then
    test_ok "GET /api/annotazioni"
else
    test_ko "GET /api/annotazioni"
    exit 1
fi

# === Fase 5: creazione annotazione ===
echo ""
echo "Fase 5: Creazione annotazione..."
RISPOSTA=$(curl -s -X POST $URL/api/annotazioni \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"titolo":"Test Annotazione ECS Terraform","descrizione":"Descrizione di test","valoreNota":"Valore di test","stato":"INSERITA","dataCreazione":"2024-06-01T12:00:00Z","utente":"admin"}')
id_creato=$(echo "$RISPOSTA" | jq -r .id 2>/dev/null)
if [ -n "$id_creato" ] && [ "$id_creato" != "null" ]; then
    test_ok "Creazione annotazione (ID: $id_creato)"
else
    test_ko "Creazione annotazione (risposta: $RISPOSTA)"
    exit 1
fi

# === Fase 6 e 7: transizioni di stato INSERITA -> CONFERMATA -> DAINVIARE ===
echo ""
echo "Fase 6: Transizione stato INSERITA -> CONFERMATA..."
RISPOSTA_INVIO1=$(curl -s -X PATCH $URL/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" -H "Content-Type: application/json" \
    -d '{"vecchioStato":"INSERITA","utente":"admin","nuovoStato":"CONFERMATA"}')
if [ "$(echo "$RISPOSTA_INVIO1" | jq -r .stato 2>/dev/null)" == "CONFERMATA" ]; then
    test_ok "Transizione a CONFERMATA"
else
    test_ko "Transizione a CONFERMATA (risposta: $RISPOSTA_INVIO1)"
fi

echo ""
echo "Fase 7: Transizione stato CONFERMATA -> DAINVIARE..."
RISPOSTA_INVIO2=$(curl -s -X PATCH $URL/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" -H "Content-Type: application/json" \
    -d '{"vecchioStato":"CONFERMATA","utente":"admin","nuovoStato":"DAINVIARE"}')
if [ "$(echo "$RISPOSTA_INVIO2" | jq -r .stato 2>/dev/null)" == "DAINVIARE" ]; then
    test_ok "Transizione a DAINVIARE (lo scheduler la esportera' sulla coda SQS)"
else
    test_ko "Transizione a DAINVIARE (risposta: $RISPOSTA_INVIO2)"
fi

# === Fase 8 e 9: import da SQS ===
echo ""
echo "Fase 8: Test import da SQS..."
IMPORT_QUEUE_URL=$(aws sqs get-queue-url --queue-name $IMPORT_QUEUE_NAME --region $REGION --query 'QueueUrl' --output text 2>/dev/null)
if [ -z "$IMPORT_QUEUE_URL" ] || [ "$IMPORT_QUEUE_URL" == "None" ]; then
    test_ko "Coda SQS di import $IMPORT_QUEUE_NAME non trovata"
else
    IMPORT_UUID=$(cat /proc/sys/kernel/random/uuid)
    echo "UUID annotazione da importare: $IMPORT_UUID"
    # Payload nel formato AnnotazioneCompleta atteso dal SqsAnnotazioneImportService
    IMPORT_PAYLOAD=$(cat <<PAYLOAD
{
  "annotazione": {
    "id": "$IMPORT_UUID",
    "titolo": "Annotazione Importata da SQS - ECS Terraform",
    "descrizione": "Test import automatico da coda SQS su ECS",
    "valoreNota": "Valore importato da ECS",
    "dataCreazione": "2024-06-01T10:00:00Z"
  },
  "metadata": {
    "id": "$IMPORT_UUID",
    "stato": "DAINVIARE",
    "utente": "admin",
    "descrizione": "Test import automatico ECS",
    "pubblica": false,
    "priorita": 1
  }
}
PAYLOAD
)
    SEND_RESULT=$(aws sqs send-message --region $REGION --queue-url "$IMPORT_QUEUE_URL" --message-body "$IMPORT_PAYLOAD" 2>&1)
    SEND_MSG_ID=$(echo "$SEND_RESULT" | jq -r '.MessageId' 2>/dev/null)
    if [ -z "$SEND_MSG_ID" ] || [ "$SEND_MSG_ID" == "null" ]; then
        test_ko "Invio messaggio sulla coda SQS di import (risposta: $SEND_RESULT)"
    else
        test_ok "Invio messaggio sulla coda SQS di import (MessageId: $SEND_MSG_ID)"
        echo ""
        echo "Fase 9: Attesa elaborazione import da parte dello scheduler (max 6 minuti)..."
        max_import_attempts=36
        import_attempt=0
        import_found=false
        while [ $import_attempt -lt $max_import_attempts ]; do
            import_attempt=$((import_attempt + 1))
            echo "⏳ Tentativo $import_attempt/$max_import_attempts - verifica annotazione importata ($((import_attempt * 10))s/360s)..."
            RISPOSTA_IMPORT=$(curl -s $URL/api/annotazioni/$IMPORT_UUID -H "Authorization: Bearer $token" 2>/dev/null)
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
            echo "$RISPOSTA_IMPORT" | jq .
        else
            test_ko "Import annotazione da SQS: $IMPORT_UUID non risulta IMPORTATA dopo $max_import_attempts tentativi"
        fi
    fi
fi

# === Fase 10: prenotazione (script condiviso, registra i propri test nello stesso riepilogo) ===
echo ""
echo "Fase 10: Test prenotazione annotazione..."
"$PROJECT_ROOT/script/automatic-test/test-prenotazione-annotazione.sh" "$URL"

echo ""
echo "[$(date '+%Y-%m-%d %H:%M:%S')] === FINE: test-aws-terraform-ecs.sh (servizio $URL, annotazione creata $id_creato) ==="
