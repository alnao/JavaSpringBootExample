#!/bin/bash
# Avvia lo stack AWS-kube (MySQL, DynamoDB Local, Spring Boot, Adminer, DynamoDB Admin)
cd "$(dirname "$0")"


#echo "🔄 Pull ultima immagine dell'applicazione..."
#docker-compose pull app

echo "🗑️  Rimozione immagine locale precedente..."
docker rmi alnao/gestioneannotazioni:latest 2>/dev/null || true

echo "🚀 Avvio stack..."
docker-compose up -d

echo ""
echo "✅ Stack avviato!"
echo "- Frontend:        http://localhost:8082"
echo "- Backend API:     http://localhost:8082/api/annotazioni"
echo "- Adminer (MySQL): http://localhost:8086"
echo "- DynamoDB Admin:  http://localhost:8087"
echo "- Redis:           localhost:6379"
"""

## COMANDI DI ESEMPIO PER IMPORTARE una annotazione su SQS AWS!

curl -s http://localhost:4566/_localstack/health | jq .
AWS_ACCESS_KEY_ID=dummy AWS_SECRET_ACCESS_KEY=dummy
aws sqs list-queues --endpoint-url=http://localhost:4566 --region=eu-central-1

   "QueueUrls": [
       "http://sqs.eu-central-1.localhost.localstack.cloud:4566/000000000000/annotazioni-import",
       "http://sqs.eu-central-1.localhost.localstack.cloud:4566/000000000000/annotazioni-export"
   ]

aws sqs receive-message \
  --endpoint-url=http://localhost:4566 \
  --region=eu-central-1 \
  --queue-url=http://sqs.eu-central-1.localhost.localstack.cloud:4566/000000000000/annotazioni-export \
  --max-number-of-messages=10 \
  --no-sign-request

IMPORT_UUID=$(cat /proc/sys/kernel/random/uuid)
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
echo $IMPORT_PAYLOAD | jq .

IMPORT_QUEUE_URL="http://sqs.eu-central-1.localhost.localstack.cloud:4566/000000000000/annotazioni-import"
aws sqs send-message \
    --endpoint-url=http://localhost:4566 \
    --region=eu-central-1 \
    --queue-url="$IMPORT_QUEUE_URL" \
    --message-body "$IMPORT_PAYLOAD"
aws sqs receive-message \
  --endpoint-url=http://localhost:4566 \
  --region=eu-central-1 \
  --queue-url=$IMPORT_QUEUE_URL \
  --max-number-of-messages=10 \
  --no-sign-request

"""
#FINE