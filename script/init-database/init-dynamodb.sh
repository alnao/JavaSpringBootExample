#!/bin/bash
# Script per creare la tabella DynamoDB locale per Annotazioni

set -e

# Endpoint DynamoDB Local
DYNAMO_ENDPOINT=${DYNAMO_ENDPOINT:-http://dynamodb:8000}
DYNAMODB_ANNOTAZIONI_TABLE_NAME=${DYNAMODB_ANNOTAZIONI_TABLE_NAME:-annotazioni}
DYNAMODB_STORICO_ANNOTAZIONI_TABLE_NAME=${DYNAMODB_STORICO_ANNOTAZIONI_TABLE_NAME:-annotazioni_storico}
REGION=${AWS_REGION:-eu-central-1}
AWS_ACCESS_KEY_ID=${AWS_ACCESS_KEY_ID:-dummy}
AWS_SECRET_ACCESS_KEY=${AWS_SECRET_ACCESS_KEY:-dummy}

export AWS_ACCESS_KEY_ID
export AWS_SECRET_ACCESS_KEY

# Attendi che DynamoDB sia pronto
until curl -s "$DYNAMO_ENDPOINT" > /dev/null; do
  echo "Aspetto che DynamoDB sia pronto su $DYNAMO_ENDPOINT..."
  sleep 2
done

echo "Creo tabella $DYNAMODB_ANNOTAZIONI_TABLE_NAME su $DYNAMO_ENDPOINT nella regione $REGION..."

aws dynamodb create-table \
  --table-name "$DYNAMODB_ANNOTAZIONI_TABLE_NAME" \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint-url "$DYNAMO_ENDPOINT" \
  --region "$REGION" || echo "Tabella già esistente o errore ignorato."

echo "Creo tabella $DYNAMODB_STORICO_ANNOTAZIONI_TABLE_NAME su $DYNAMO_ENDPOINT nella regione $REGION..."

aws dynamodb create-table \
  --table-name "$DYNAMODB_STORICO_ANNOTAZIONI_TABLE_NAME" \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --endpoint-url "$DYNAMO_ENDPOINT" \
  --region "$REGION" || echo "Tabella già esistente o errore ignorato."


echo "Tabella DynamoDB pronta."

###
#export DYNAMO_ENDPOINT=http://localhost:8420
#export TABLE_NAME=annotazioni
#export REGION=eu-central-1
#aws dynamodb delete-table \
#  --table-name "$TABLE_NAME" \
#  --endpoint-url "$DYNAMO_ENDPOINT" \
#  --region "$REGION"
