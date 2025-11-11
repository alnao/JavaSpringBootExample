#!/bin/bash

# filepath: script/azure-dbremoti-cosmos-aci/stop-all.sh

set -e

RESOURCE_GROUP="gestioneannotazioni-aci-rg"

echo "🧹 Inizio rimozione risorse Azure ACI..."
echo ""


echo "📊 Riepilogo risorse da cancellare:"
az resource list \
  --resource-group $RESOURCE_GROUP \
  --output table

echo ""
echo "🚀 Avvio rimozione di tutto il $RESOURCE_GROUP. Potrebbero volerci alcuni minuti per completare la eliminazione di tutte le risorse"

az group delete --resource-group $RESOURCE_GROUP --yes


# 6. Eliminazione file .env
echo "💾 Eliminazione file .env-azure-aci..."
rm -f .env-azure-aci
echo ""
echo "✅ Pulizia completata!"
echo ""

