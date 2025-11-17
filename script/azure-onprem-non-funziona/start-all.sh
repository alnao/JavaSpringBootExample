#!/bin/bash

echo "🚀 Avvio stack Azure locale..."

# Verifica che Docker sia in esecuzione
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker non è in esecuzione"
    exit 1
fi

# Build e push dell'immagine se necessario
echo "📦 Verifica immagine Docker..."
if ! docker images | grep -q "alnao/gestioneannotazioni"; then
    # docker build -t alnao/gestioneannotazioni:latest .
    echo "🔨 Build immagine Docker..."
    #cd ../../
    ./script/push-image-docker-hub.sh
    #cd script/azure-onprem/
fi

# Avvio stack
echo "🐳 Avvio containers..."
docker-compose -f script/azure-onprem-non-funziona/docker-compose.yml up -d 

# Attesa che i servizi siano pronti
#echo "⏳ Attesa avvio servizi..."
#sleep 30

# Verifica stato servizi
#echo "🔍 Verifica stato servizi..."
#docker-compose ps

echo ""
echo "✅ Stack Azure locale avviato!"
echo ""
echo "🌐 Servizi disponibili:"
echo "  • App (Azure profile):      http://localhost:8082"
echo "  • API Swagger:              http://localhost:8082/swagger-ui.html"
echo "  • SQL Server Adminer:       http://localhost:8086"
echo "  • CosmosDB Data Explorer:   https://localhost:8081/_explorer/index.html"
echo "  • Azurite Storage Explorer: http://localhost:8087"
echo ""
echo "🔑 Credenziali:"
echo "  • SQL Server: sa / GestioneAnnotazioni123!"
echo "  • CosmosDB: Emulator (chiave predefinita)"
echo "  • Azurite: devstoreaccount1 / Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw=="
echo ""
echo "📝 Per i logs: docker-compose logs -f <service-name>"
echo "🛑 Per fermare: ./stop-all.sh"


