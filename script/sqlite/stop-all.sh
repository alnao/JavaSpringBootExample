#!/bin/bash

# Script per fermare l'ambiente SQLite locale
set -e

echo "🛑 Fermando ambiente SQLite locale..."

# Directory del script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Vai alla directory dello script
cd "$SCRIPT_DIR"

# Ferma i servizi
echo "🔽 Fermando servizi Docker Compose..."
docker-compose down

echo ""
echo "✅ Ambiente SQLite locale fermato!"
echo ""
echo "💡 Per rimuovere anche i volumi (dati persistenti):"
echo "   docker-compose down -v"
echo ""
echo "🧹 Per pulizia completa (immagini, volumi, network):"
echo "   docker-compose down -v --rmi all"
