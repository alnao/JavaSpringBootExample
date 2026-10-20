#!/bin/bash
# Rotazione di una credenziale del docker-compose.yml di root, profilo kube (change openspec secret-kube).
#
# Uso (dalla root del progetto o da qualsiasi cartella):
#   ./script/docker-compose-rotate-secret.sh JWT_SECRET
#   ./script/docker-compose-rotate-secret.sh POSTGRES_PASSWORD
#   ./script/docker-compose-rotate-secret.sh MONGO_ROOT_PASSWORD
#   ./script/docker-compose-rotate-secret.sh --env-file <file> <VARIABILE>   (default: .env della root)
#
# Passi: genera un nuovo valore; per le password dei database lo imposta prima sul database (il valore
# passa a psql/mongosh da stdin, mai come argomento); aggiorna la riga nel file .env; ricrea i container
# che usano la credenziale (le variabili d'ambiente si fissano alla creazione); attende che
# /actuator/health risponda UP. Il nuovo valore non viene mai stampato.
#
# JWT_SECRET: gli access token gia' emessi vengono rifiutati (401), gli utenti rifanno il login.
# APP_USER_*_PASSWORD non sono ruotabili: gli utenti iniziali si creano solo su database vuoto.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="$ROOT_DIR/.env"
HEALTH_URL="${HEALTH_URL:-http://localhost:8082/actuator/health}"
TENTATIVI=60
ATTESA_SECONDI=2

if [ "${1:-}" = "--env-file" ]; then
    ENV_FILE="${2:?manca il percorso dopo --env-file}"
    shift 2
fi
VARIABILE="${1:-}"
case "$VARIABILE" in
    JWT_SECRET|POSTGRES_PASSWORD|MONGO_ROOT_PASSWORD)
        ;;
    APP_USER_*_PASSWORD)
        echo "❌ $VARIABILE non si puo' ruotare con questo script."
        echo "   Gli utenti iniziali vengono creati solo su un database vuoto: cambiare la variabile"
        echo "   non cambia la password di un utente gia' creato. Cambiala dall'applicazione oppure"
        echo "   ricrea il database (docker-compose down -v) dopo aver aggiornato il file .env."
        exit 1
        ;;
    *)
        echo "Uso: $0 [--env-file <file>] JWT_SECRET|POSTGRES_PASSWORD|MONGO_ROOT_PASSWORD"
        exit 1
        ;;
esac

if [ ! -f "$ENV_FILE" ]; then
    echo "❌ File $ENV_FILE non trovato: crealo da .env.example e avvia lo stack con docker-compose"
    exit 1
fi
valore() { grep "^$1=" "$ENV_FILE" | head -1 | cut -d= -f2-; }
if [ -z "$(valore "$VARIABILE")" ]; then
    echo "❌ Nel file $ENV_FILE manca la riga ${VARIABILE}="
    exit 1
fi

COMPOSE=(docker-compose -f "$ROOT_DIR/docker-compose.yml" --env-file "$ENV_FILE")
if [ "$VARIABILE" = "JWT_SECRET" ]; then
    NUOVO_VALORE="$(openssl rand -base64 64 | tr -d '\n')"
else
    # esadecimale: nessun carattere da codificare in MONGODB_URI o da proteggere nell'SQL
    NUOVO_VALORE="$(openssl rand -hex 24)"
fi

echo "🔐 Rotazione di $VARIABILE..."
case "$VARIABILE" in
    POSTGRES_PASSWORD)
        UTENTE="$(valore POSTGRES_USERNAME)"
        printf "ALTER USER \"%s\" WITH PASSWORD '%s';\n" "$UTENTE" "$NUOVO_VALORE" \
            | docker exec -i gestioneannotazioni-postgres psql -q -v ON_ERROR_STOP=1 -U "$UTENTE" -d gestioneannotazioni > /dev/null
        echo "✅ Password cambiata su PostgreSQL"
        DA_RICREARE=(app)
        ;;
    MONGO_ROOT_PASSWORD)
        UTENTE="$(valore MONGO_ROOT_USERNAME)"
        printf 'const a = db.getSiblingDB("admin"); a.auth("%s", "%s"); a.changeUserPassword("%s", "%s");\n' \
            "$UTENTE" "$(valore MONGO_ROOT_PASSWORD)" "$UTENTE" "$NUOVO_VALORE" \
            | docker exec -i gestioneannotazioni-mongodb mongosh --quiet mongodb://localhost:27017/admin > /dev/null
        echo "✅ Password cambiata su MongoDB"
        DA_RICREARE=(app mongo-express)
        ;;
    JWT_SECRET)
        DA_RICREARE=(app)
        ;;
esac

# Aggiornamento del file: il valore passa ad awk come variabile d'ambiente, non come argomento
TMP_FILE="$(mktemp "$ENV_FILE.rotate.XXXXXX")"
trap 'rm -f "$TMP_FILE"' EXIT
NUOVO_VALORE="$NUOVO_VALORE" awk -v variabile="$VARIABILE" \
    'index($0, variabile "=") == 1 { print variabile "=" ENVIRON["NUOVO_VALORE"]; next } { print }' \
    "$ENV_FILE" > "$TMP_FILE"
chmod --reference="$ENV_FILE" "$TMP_FILE"
mv "$TMP_FILE" "$ENV_FILE"
unset NUOVO_VALORE
echo "✅ File $(basename "$ENV_FILE") aggiornato"

echo "🔄 Ricreazione di: ${DA_RICREARE[*]}"
"${COMPOSE[@]}" up -d --no-deps --force-recreate "${DA_RICREARE[@]}" > /dev/null 2>&1

echo "⏳ Attesa di $HEALTH_URL (max $((TENTATIVI * ATTESA_SECONDI)) secondi)..."
for i in $(seq 1 "$TENTATIVI"); do
    if curl -s "$HEALTH_URL" 2>/dev/null | grep -q '"status":"UP"'; then
        echo "✅ Applicazione UP dopo $((i * ATTESA_SECONDI)) secondi: rotazione di $VARIABILE completata"
        exit 0
    fi
    sleep "$ATTESA_SECONDI"
done
echo "❌ L'applicazione non e' tornata UP entro $((TENTATIVI * ATTESA_SECONDI)) secondi"
echo "   Controlla i log: docker logs gestioneannotazioni-app"
exit 1
