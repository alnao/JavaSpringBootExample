#!/bin/bash

echo "🛑 Arresto stack Azure locale..."

# Ferma e rimuove containers
docker-compose -f script/azure-onprem-non-funziona/docker-compose.yml down

# Opzionale: rimuovi volumi (decommentare se necessario)
echo "🗑️  Rimozione volumi (opzionale)..."
#echo "💡 Per rimuovere anche i volumi persistenti:"
#echo "   docker volume rm \$(docker volume ls -q | grep azure-onprem)"
docker volume rm $(docker volume ls -q)

#echo "🧹 Pulizia network..."
#docker network rm gestioneannotazioni-azure-network
#docker network prune -f

echo "✅ Stack Azure fermato!"
echo ""
