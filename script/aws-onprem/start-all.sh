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