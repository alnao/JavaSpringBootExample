#!/bin/bash
# Avvia lo stack AWS-onprem (MySQL, DynamoDB Local, Spring Boot, Adminer, DynamoDB Admin)
cd "$(dirname "$0")"
docker-compose up -d --build

echo "\nStack avviato!"
echo "- Frontend:        http://localhost:8080"
echo "- Backend API:     http://localhost:8085/api/annotazioni"
echo "- Adminer (MySQL): http://localhost:8086"
echo "- DynamoDB Admin:  http://localhost:8087"
