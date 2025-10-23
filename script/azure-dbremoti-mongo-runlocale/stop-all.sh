#!/bin/bash
echo "🚀 Avvio rimozione risorse Azure Cosmos-MongoDb + SQL Server..."
#./script/azure-dbremoti-mongo-runlocale/stop-all.sh  

# ATTENZIONE: Questo comando elimina tutto!
RESOURCE_GROUP="gestioneannotazioni-rg-mongo-postgres"

docker stop azure-dbremoti-mongo-runlocale

echo "📊 Riepilogo risorse da cancellare:"
az resource list \
  --resource-group $RESOURCE_GROUP \
  --output table
echo "🚀 Avvio rimozione di tutto il $RESOURCE_GROUP. Potrebbero volerci alcuni minuti per completare la eliminazione di tutte le risorse"

az group delete --resource-group $RESOURCE_GROUP --yes #--no-wait

rm .env-azure-dbremoti-mongo-runlocale
echo "✅ File .env-azure-dbremoti-mongo-runlocale rimosso"

echo "✅ Rimozione Finita!"

