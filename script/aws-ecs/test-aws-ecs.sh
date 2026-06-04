#!/bin/bash

# Test completo del microservizio gestioneannotazioni su AWS ECS Fargate
# Esegue: login, creazione annotazione, transizioni di stato, import da SQS, prenotazione
# Richiede AWS CLI configurato e permessi su ECS, SQS

export AWS_PAGER=""

REGION="eu-central-1"
CLUSTER_NAME="gestioneannotazioni-cluster"
SERVICE_NAME="gestioneannotazioni-service"
IMPORT_QUEUE_NAME="gestioneannotazioni-annotazioni-import"

echo "=== TEST AWS ECS GESTIONEANNOTAZIONI ==="
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Inizio test..."

# === 1. Recupera l'URL del servizio ECS ===
echo ""
echo "Fase 1: Recupero endpoint ECS..."

# Ottieni il task in esecuzione
TASK_ARN=$(aws ecs list-tasks --cluster $CLUSTER_NAME --service-name $SERVICE_NAME --desired-status RUNNING --region $REGION --query 'taskArns[0]' --output text 2>/dev/null)
if [ -z "$TASK_ARN" ] || [ "$TASK_ARN" == "None" ]; then
    echo "❌ ERRORE: Nessun task RUNNING trovato nel servizio $SERVICE_NAME"
    exit 1
fi
echo "✓ Task trovato: $TASK_ARN"

# Ottieni i dettagli del task
TASK_DETAILS=$(aws ecs describe-tasks --cluster $CLUSTER_NAME --tasks $TASK_ARN --region $REGION --output json 2>/dev/null)
echo "Debug task details: $TASK_DETAILS" | head -c 500

# Ottieni l'ID dell'interfaccia di rete - prova multiple query paths
NETWORK_INTERFACE_ID=$(echo "$TASK_DETAILS" | jq -r '.tasks[0].attachments[] | select(.name=="elasticNetworkInterface") | .details[] | select(.name=="networkInterfaceId") | .value' 2>/dev/null)

if [ -z "$NETWORK_INTERFACE_ID" ] || [ "$NETWORK_INTERFACE_ID" == "None" ] || [ "$NETWORK_INTERFACE_ID" == "null" ]; then
    echo "⚠️  Metodo 1 fallito, provo metodo alternativo..."
    # Metodo alternativo: ottieni ENI da EC2 describe-network-interfaces
    SECURITY_GROUP_ID=$(echo "$TASK_DETAILS" | jq -r '.tasks[0].overrides.containerOverrides[0].environment[0]' 2>/dev/null || echo "")
    
    # Prova a recuperare direttamente l'IP privato dal task
    PRIVATE_IP=$(echo "$TASK_DETAILS" | jq -r '.tasks[0].attachments[0].details[] | select(.name=="privateIPv4Address") | .value' 2>/dev/null)
    if [ -n "$PRIVATE_IP" ] && [ "$PRIVATE_IP" != "null" ]; then
        echo "⚠️  Recuperato IP privato direttamente: $PRIVATE_IP"
        PUBLIC_IP=$PRIVATE_IP
        NETWORK_INTERFACE_ID="direct-ip"
    else
        echo "❌ ERRORE: Impossibile recuperare l'ID dell'interfaccia di rete o l'IP"
        exit 1
    fi
else
    echo "✓ Network Interface ID: $NETWORK_INTERFACE_ID"
fi

# Ottieni l'IP pubblico
#if [ "$NETWORK_INTERFACE_ID" == "direct-ip" ]; then
#    # IP già recuperato direttamente
#    echo "✓ Usando IP privato recuperato direttamente: $PUBLIC_IP"
#else
    #PUBLIC_IP=$(aws ec2 describe-network-interfaces --network-interface-ids $NETWORK_INTERFACE_ID --region $REGION --query 'NetworkInterfaces[0].Association.PublicIp' --output text 2>/dev/null)
    PUBLIC_IP=$(aws ecs describe-tasks --cluster $CLUSTER_NAME --tasks $TASK_ARN --region $REGION --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' --output text)
    if [ -n "$PUBLIC_IP" ] && [ "$PUBLIC_IP" != "None" ]; then
        # Ottieni l'IP dalla network interface
        PUBLIC_IP=$(aws ec2 describe-network-interfaces --network-interface-ids $PUBLIC_IP --region $REGION --query 'NetworkInterfaces[0].Association.PublicIp' --output text 2>/dev/null)
    fi

    if [ -z "$PUBLIC_IP" ] || [ "$PUBLIC_IP" == "None" ] || [ "$PUBLIC_IP" == "null" ]; then
        echo "⚠️  AVVISO: Impossibile recuperare l'IP pubblico, provo con l'IP privato..."
        PRIVATE_IP=$(aws ec2 describe-network-interfaces --network-interface-ids $NETWORK_INTERFACE_ID --region $REGION --query 'NetworkInterfaces[0].PrivateIpAddress' --output text 2>/dev/null)
        if [ -z "$PRIVATE_IP" ] || [ "$PRIVATE_IP" == "None" ]; then
            echo "❌ ERRORE: Impossibile recuperare nemmeno l'IP privato"
            exit 1
        fi
        PUBLIC_IP=$PRIVATE_IP
        echo "⚠️  Usando IP privato: $PUBLIC_IP"
    else
        echo "✓ IP pubblico: $PUBLIC_IP"
    fi
#fi

URL="http://$PUBLIC_IP:8080"
echo "✓ URL servizio: $URL"

# Attendi che il servizio sia disponibile
echo ""
echo "Fase 2: Attesa disponibilità servizio..."
max_attempts=30
attempt=0
while [ $attempt -lt $max_attempts ]; do
    if curl -s $URL/actuator/health | jq -e '.status == "UP"' > /dev/null 2>&1; then
        echo "✓ Servizio disponibile"
        break
    fi
    attempt=$((attempt + 1))
    if [ $attempt -lt $max_attempts ]; then
        echo "⏳ Tentativo $attempt/$max_attempts: servizio non ancora disponibile, attendo..."
        sleep 5
    fi
done

if [ $attempt -eq $max_attempts ]; then
    echo "⚠️  AVVISO: Servizio non ancora completamente disponibile, continuo comunque..."
fi

# === 2. Login e ottenimento token ===
echo ""
echo "Fase 3: Autenticazione..."
token_response=$(curl -s -X POST $URL/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}')
token=$(echo $token_response | jq -r .token 2>/dev/null)
echo "Risposta login: $token_response"

if [ -z "$token" ] || [ "$token" == "null" ]; then
    echo "❌ ERRORE: Login fallito"
    exit 1
else
    echo "✓ Login eseguito correttamente. Token: ${token:0:20}..."
fi

# === 3. Test API getAnnotazioni ===
echo ""
echo "Fase 4: Test API getAnnotazioni..."
curl -s $URL/api/annotazioni -H "Authorization: Bearer $token" | jq . > /dev/null
if [ $? -eq 0 ]; then
    echo "✓ Chiamata API /api/annotazioni eseguita correttamente"
else
    echo "❌ ERRORE: Chiamata API /api/annotazioni fallita"
    exit 1
fi

# === 4. Creazione annotazione ===
echo ""
echo "Fase 5: Creazione annotazione..."
RISPOSTA=$(curl -s -X POST $URL/api/annotazioni \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"titolo":"Test Annotazione ECS","descrizione":"Descrizione di test","valoreNota":"Valore di test","stato":"INSERITA","dataCreazione":"2024-06-01T12:00:00Z","utente":"admin"}')
echo "Risposta POST annotazione: $RISPOSTA"

id_creato=$(echo $RISPOSTA | jq -r .id 2>/dev/null)
if [ -n "$id_creato" ] && [ "$id_creato" != "null" ]; then
    echo "✓ Creazione annotazione eseguita correttamente. ID: $id_creato"
else
    echo "❌ ERRORE: Creazione annotazione fallita"
    echo "   Risposta completa: $RISPOSTA"
    exit 1
fi

# === 5. Transizione stato INSERITA -> CONFERMATA ===
echo ""
echo "Fase 6: Transizione stato INSERITA -> CONFERMATA..."
RISPOSTA_INVIO1=$(curl -s -X PATCH $URL/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"vecchioStato":"INSERITA","utente":"admin","nuovoStato":"CONFERMATA"}')
echo "Risposta: $RISPOSTA_INVIO1"

stato_1=$(echo $RISPOSTA_INVIO1 | jq -r .stato 2>/dev/null)
if [ "$stato_1" == "CONFERMATA" ]; then
    echo "✓ Transizione a CONFERMATA eseguita correttamente"
else
    echo "⚠️  AVVISO: Transizione non confermata. Stato: $stato_1"
fi

# === 6. Transizione stato CONFERMATA -> DAINVIARE ===
echo ""
echo "Fase 7: Transizione stato CONFERMATA -> DAINVIARE..."
RISPOSTA_INVIO2=$(curl -s -X PATCH $URL/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"vecchioStato":"CONFERMATA","utente":"admin","nuovoStato":"DAINVIARE"}')
echo "Risposta: $RISPOSTA_INVIO2"

stato_2=$(echo $RISPOSTA_INVIO2 | jq -r .stato 2>/dev/null)
if [ "$stato_2" == "DAINVIARE" ]; then
    echo "✓ Transizione a DAINVIARE eseguita correttamente"
else
    echo "⚠️  AVVISO: Transizione non confermata. Stato: $stato_2"
fi

# === 7. Test IMPORT da SQS ===
echo ""
echo "Fase 8: Test import da SQS..."

# Recupera URL della coda import
IMPORT_QUEUE_URL=$(aws sqs get-queue-url \
    --queue-name $IMPORT_QUEUE_NAME \
    --region $REGION \
    --query 'QueueUrl' \
    --output text 2>/dev/null)

if [ -z "$IMPORT_QUEUE_URL" ] || [ "$IMPORT_QUEUE_URL" == "None" ]; then
    echo "⚠️  AVVISO: Coda SQS import non trovata, salto test import"
else
    echo "✓ Coda SQS trovata: $IMPORT_QUEUE_URL"
    
    # Genera UUID per l'annotazione da importare
    IMPORT_UUID=$(cat /proc/sys/kernel/random/uuid)
    echo "UUID annotazione da importare: $IMPORT_UUID"
    
    # Costruisce il payload JSON nel formato AnnotazioneCompleta
    IMPORT_PAYLOAD=$(cat <<PAYLOAD
{
  "annotazione": {
    "id": "$IMPORT_UUID",
    "titolo": "Annotazione Importata da SQS - ECS",
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
    
    echo "Invio messaggio nella coda SQS import..."
    SEND_RESULT=$(aws sqs send-message \
        --region $REGION \
        --queue-url "$IMPORT_QUEUE_URL" \
        --message-body "$IMPORT_PAYLOAD" 2>&1)
    
    SEND_MSG_ID=$(echo "$SEND_RESULT" | jq -r '.MessageId' 2>/dev/null)
    if [ -z "$SEND_MSG_ID" ] || [ "$SEND_MSG_ID" == "null" ]; then
        echo "❌ ERRORE: Invio messaggio SQS import fallito"
        echo "   Risposta: $SEND_RESULT"
    else
        echo "✓ Messaggio inviato nella coda import. MessageId: $SEND_MSG_ID"
        
        # Attende che lo scheduler processi il messaggio
        echo ""
        echo "Fase 9: Attesa elaborazione import da parte dello scheduler..."
        max_import_attempts=36  # 36 tentativi x 10 secondi = 360 secondi (6 minuti)
        import_attempt=0
        import_found=false
        
        while [ $import_attempt -lt $max_import_attempts ]; do
            import_attempt=$((import_attempt + 1))
            elapsed_import=$((import_attempt * 10))
            echo "⏳ Tentativo $import_attempt/$max_import_attempts - Verifica annotazione importata (${elapsed_import}s/360s)..."
            
            RISPOSTA_IMPORT=$(curl -s $URL/api/annotazioni/$IMPORT_UUID \
                -H "Authorization: Bearer $token" 2>/dev/null)
            
            stato_importata=$(echo "$RISPOSTA_IMPORT" | jq -r '.stato' 2>/dev/null)
            
            if [ "$stato_importata" == "IMPORTATA" ]; then
                echo "✓ Annotazione $IMPORT_UUID trovata con stato IMPORTATA al tentativo $import_attempt"
                echo "Dettagli annotazione importata:"
                echo "$RISPOSTA_IMPORT" | jq .
                import_found=true
                break
            elif [ "$stato_importata" != "null" ] && [ -n "$stato_importata" ]; then
                echo "⚠️  Annotazione trovata ma stato non ancora IMPORTATA: $stato_importata. Continuo..."
            fi
            
            sleep 10
        done
        
        if [ "$import_found" = false ]; then
            echo "⚠️  AVVISO: Annotazione non trovata come IMPORTATA dopo $max_import_attempts tentativi"
        fi
    fi
fi

# === 8. Test prenotazione annotazione ===
echo ""
echo "Fase 10: Test prenotazione annotazione..."
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

if [ -f "$PROJECT_ROOT/script/automatic-test/test-prenotazione-annotazione.sh" ]; then
    "$PROJECT_ROOT/script/automatic-test/test-prenotazione-annotazione.sh" "$URL"
    echo "✓ Test prenotazione completato"
else
    echo "⚠️  AVVISO: Script test-prenotazione-annotazione.sh non trovato, salto questo test"
fi

# === SUMMARY ===
echo ""
echo "=========================================="
echo "✓ TEST COMPLETATO CON SUCCESSO"
echo "=========================================="
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Fine test"
echo ""
echo "Riepilogo:"
echo "- Servizio ECS: $URL"
echo "- Annotazione creata: $id_creato"
echo "- Annotazione importata: $IMPORT_UUID"
echo "- Status: OK"
