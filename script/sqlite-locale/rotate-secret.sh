#!/bin/bash
# Rotazione di una credenziale dell'ambiente sqlite-locale (change openspec secret-sqlite).
#
# Uso: ./rotate-secret.sh JWT_SECRET
#
# Passi: genera un nuovo valore casuale, aggiorna la riga nel file .env, ricrea il container
# dell'applicazione (le variabili d'ambiente di un container si fissano alla creazione, un
# "docker restart" non basta) e attende che /actuator/health risponda UP.
# Il nuovo valore non viene mai stampato.
#
# Effetto della rotazione di JWT_SECRET: gli access token gia' emessi vengono rifiutati (401) e gli
# utenti devono rifare il login.
#
# Le password APP_USER_*_PASSWORD non sono ruotabili da qui: gli utenti iniziali vengono creati
# solo su un database vuoto, quindi cambiare la variabile non cambia la password gia' salvata.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ENV_FILE="$SCRIPT_DIR/.env"
HEALTH_URL="${HEALTH_URL:-http://localhost:8082/actuator/health}"
TENTATIVI=30
ATTESA_SECONDI=2

VARIABILE="${1:-}"
case "$VARIABILE" in
    JWT_SECRET)
        ;;
    APP_USER_*_PASSWORD)
        echo "❌ $VARIABILE non si puo' ruotare con questo script."
        echo "   Gli utenti iniziali vengono creati solo su un database vuoto: cambiare la variabile"
        echo "   non cambia la password di un utente gia' creato. Cambiala dall'applicazione oppure"
        echo "   ricrea il database (docker-compose down -v) dopo aver aggiornato il file .env."
        exit 1
        ;;
    *)
        echo "Uso: $0 JWT_SECRET"
        exit 1
        ;;
esac

if [ ! -f "$ENV_FILE" ]; then
    echo "❌ File $ENV_FILE non trovato: crealo da .env.example e avvia l'ambiente con start-all.sh"
    exit 1
fi
if ! grep -q "^${VARIABILE}=" "$ENV_FILE"; then
    echo "❌ Nel file $ENV_FILE manca la riga ${VARIABILE}="
    exit 1
fi

echo "🔐 Rotazione di $VARIABILE..."

# Il nuovo valore passa ad awk come variabile d'ambiente: non compare tra gli argomenti dei processi
# e non serve fare escape dei caratteri del base64 (/ + =).
TMP_FILE="$(mktemp "$SCRIPT_DIR/.env.rotate.XXXXXX")"
trap 'rm -f "$TMP_FILE"' EXIT
NUOVO_VALORE="$(openssl rand -base64 64 | tr -d '\n')" \
    awk -v variabile="$VARIABILE" \
        'index($0, variabile "=") == 1 { print variabile "=" ENVIRON["NUOVO_VALORE"]; next } { print }' \
        "$ENV_FILE" > "$TMP_FILE"
chmod --reference="$ENV_FILE" "$TMP_FILE"
mv "$TMP_FILE" "$ENV_FILE"
echo "✅ File .env aggiornato"

echo "🔄 Ricreazione del container dell'applicazione..."
cd "$SCRIPT_DIR"
docker-compose up -d --force-recreate spring-app

echo "⏳ Attesa di $HEALTH_URL (max $((TENTATIVI * ATTESA_SECONDI)) secondi)..."
for i in $(seq 1 "$TENTATIVI"); do
    if curl -s "$HEALTH_URL" 2>/dev/null | grep -q '"status":"UP"'; then
        echo "✅ Applicazione UP dopo $((i * ATTESA_SECONDI)) secondi: rotazione di $VARIABILE completata"
        exit 0
    fi
    sleep "$ATTESA_SECONDI"
done

echo "❌ L'applicazione non e' tornata UP entro $((TENTATIVI * ATTESA_SECONDI)) secondi"
echo "   Controlla i log: docker-compose logs spring-app"
exit 1
