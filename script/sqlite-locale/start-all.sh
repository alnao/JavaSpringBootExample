#!/bin/bash

# Script per avviare l'ambiente SQLite locale
set -e

echo "🔧 Avvio ambiente SQLite locale..."

# Directory del script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"

echo "📁 Directory progetto: $PROJECT_ROOT"
echo "📁 Directory script: $SCRIPT_DIR"

# Vai alla directory root del progetto
cd "$PROJECT_ROOT"

# Build del progetto se necessario
echo "🔨 Build del progetto..."
#if [ ! -f "application/target/application-1.0.0.jar" ]; then
#    echo "   JAR non trovato, eseguo build..."
    #mvn clean package -DskipTests
    mvn clean package -P sqlite -DskipTests
#else
#    echo "   JAR trovato, salto il build"
#fi

# Vai alla directory dello script
cd "$SCRIPT_DIR"

# nota: commentato perchè non serve più!
# Installa dipendenze Node.js per il mock server
#echo "📦 Installazione dipendenze Node.js..."
#cd replit-db-mock
#if [ ! -d "node_modules" ]; then
#    npm install
#else
#    echo "   Dipendenze già installate"
#fi
#cd ..

# Avvia i servizi
echo "🚀 Avvio servizi Docker Compose..."
docker-compose up --build -d

# Attendi che i servizi siano pronti
echo "⏳ Attendo che i servizi siano pronti..."
sleep 10

# Controllo stato servizi
echo "📊 Stato servizi:"
docker-compose ps

# Controllo health del mock server
#echo "🏥 Test health check ReplitDB Mock..."
#if curl -f http://localhost:3000/health > /dev/null 2>&1; then
#    echo "✅ ReplitDB Mock: OK"
#else
#    echo "❌ ReplitDB Mock: ERRORE"
#fi

# Controllo applicazione Spring Boot
echo "🌱 Test Spring Boot App..."
sleep 5
if curl -f http://localhost:8082/actuator/health > /dev/null 2>&1; then
    echo "✅ Spring Boot App: OK"
else
    echo "⚠️  Spring Boot App: Non ancora pronta (normale durante l'avvio)"
fi

echo ""
echo "🎉 Ambiente Replit locale avviato!"
echo ""
echo "📋 Servizi disponibili:"
echo "   🌱 Spring Boot App:     http://localhost:8082"
#echo "   🔧 ReplitDB Mock:       http://localhost:3000"
#echo "   📊 ReplitDB Mock Health: http://localhost:3000/health"
#echo "   📂 ReplitDB Mock Keys:   http://localhost:3000/"
echo "   🗄️  Adminer (SQLite):    http://localhost:8084"
echo ""
echo "📝 Comandi utili:"
echo "   📊 Stato:     docker-compose ps"
echo "   📜 Log:       docker-compose logs -f"
echo "   🛑 Stop:      docker-compose down"
echo "   🧹 Cleanup:   docker-compose down -v"
echo ""
echo "🔍 Test delle API:"
echo "   curl http://localhost:8082/actuator/health"
echo ""
echo "💡 Per vedere i log in tempo reale:"
echo "   docker-compose logs -f sqlite-spring-app"
echo ""

#FINE