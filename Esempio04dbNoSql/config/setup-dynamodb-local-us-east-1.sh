#!/bin/bash
# scripts/setup-dynamodb.sh

echo "Waiting for DynamoDB Local to be ready..."
until curl -s http://localhost:8000 > /dev/null; do
    echo "DynamoDB Local is not ready yet. Waiting..."
    sleep 5
done

echo "Creating DynamoDB table..."
aws dynamodb create-table \
    --table-name users \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --endpoint-url http://localhost:8000 \
    --region us-east-1

echo "DynamoDB table created successfully!"

# Inserisci alcuni dati di esempio
echo "Inserting sample data..."
aws dynamodb put-item \
    --table-name users \
    --item '{"id":{"S":"1"},"name":{"S":"Mario Rossi"},"email":{"S":"mario.rossi@example.com"},"createdAt":{"S":"2024-01-01T10:00:00Z"},"updatedAt":{"S":"2024-01-01T10:00:00Z"}}' \
    --endpoint-url http://localhost:8000 \
    --region us-east-1

aws dynamodb put-item \
    --table-name users \
    --item '{"id":{"S":"2"},"name":{"S":"Giulia Verdi"},"email":{"S":"giulia.verdi@example.com"},"createdAt":{"S":"2024-01-01T10:00:00Z"},"updatedAt":{"S":"2024-01-01T10:00:00Z"}}' \
    --endpoint-url http://localhost:8000 \
    --region us-east-1

echo "Sample data inserted!"