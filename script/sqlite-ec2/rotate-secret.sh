#!/bin/bash
# Rotazione di una credenziale dello stack sqlite-ec2 (change openspec secret-sqlite).
#
# Uso (dalla root del progetto, dove start-all.sh ha creato la chiave .pem):
#   ./script/sqlite-ec2/rotate-secret.sh JWT_SECRET
#
# Passi: trova l'istanza con il tag marcatore, verifica la raggiungibilita' ssh, genera il nuovo
# valore SULL'ISTANZA (non transita in rete e non viene mai stampato), aggiorna la riga nel file
# di credenziali, ricrea il container con run-app.sh e attende che /actuator/health risponda UP.
#
# Effetto della rotazione di JWT_SECRET: gli access token gia' emessi vengono rifiutati (401) e gli
# utenti devono rifare il login.
#
# Le password APP_USER_*_PASSWORD non sono ruotabili da qui: gli utenti iniziali vengono creati
# solo su un database vuoto, quindi cambiare la variabile non cambia la password gia' salvata.
set -euo pipefail
export AWS_PAGER=""

REGION="${REGION:-eu-central-1}"
TAG_MARKER_KEY="gestioneannotazioni-sqlite-ec2-app"
SG_NAME="gestioneannotazioni-sqlite-ec2-sg"
PEM_FILE="${PEM_FILE:-gestioneannotazioni-sqlite-ec2-key.pem}"
CONF_DIR="/etc/gestioneannotazioni"
TENTATIVI=30
ATTESA_SECONDI=2

VARIABILE="${1:-}"
case "$VARIABILE" in
    JWT_SECRET)
        ;;
    APP_USER_*_PASSWORD)
        echo "❌ $VARIABILE non si puo' ruotare con questo script."
        echo "   Gli utenti iniziali vengono creati solo su un database vuoto: cambiare la variabile"
        echo "   non cambia la password di un utente gia' creato. Cambiala dall'applicazione."
        exit 1
        ;;
    *)
        echo "Uso: $0 JWT_SECRET"
        exit 1
        ;;
esac

if [ ! -f "$PEM_FILE" ]; then
    echo "❌ Chiave $PEM_FILE non trovata: lancia lo script dalla root del progetto (dove start-all.sh"
    echo "   l'ha creata) oppure indica il percorso con PEM_FILE=..."
    exit 1
fi

echo "🔎 Ricerca dell'istanza con tag $TAG_MARKER_KEY=true..."
PUBLIC_IP=$(aws ec2 describe-instances --region "$REGION" \
    --filters "Name=tag:$TAG_MARKER_KEY,Values=true" "Name=instance-state-name,Values=running" \
    --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
if [ -z "$PUBLIC_IP" ] || [ "$PUBLIC_IP" = "None" ]; then
    echo "❌ Nessuna istanza sqlite-ec2 in esecuzione: avvia lo stack con start-all.sh"
    exit 1
fi
echo "   Istanza: $PUBLIC_IP"

SSH=(ssh -i "$PEM_FILE" -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null
     -o ConnectTimeout=10 -o BatchMode=yes "ec2-user@$PUBLIC_IP")

if ! "${SSH[@]}" true; then
    echo "❌ Istanza non raggiungibile via ssh: nessuna modifica eseguita."
    echo "   La porta 22 del security group $SG_NAME e' aperta solo all'IP che ha lanciato start-all.sh."
    echo "   Se il tuo IP e' cambiato aggiungilo alla regola di ingresso della porta 22, per esempio:"
    echo "   aws ec2 authorize-security-group-ingress --group-name $SG_NAME --protocol tcp --port 22 --cidr <tuo-ip>/32 --region $REGION"
    exit 1
fi

echo "🔐 Rotazione di $VARIABILE sull'istanza..."
"${SSH[@]}" sudo bash -s -- "$VARIABILE" "$CONF_DIR" <<'REMOTO'
set -euo pipefail
VARIABILE="$1"
FILE="$2/app.env"
grep -q "^${VARIABILE}=" "$FILE" || { echo "Nel file $FILE manca la riga ${VARIABILE}="; exit 1; }
TMP_FILE="$(mktemp "$FILE.XXXXXX")"
trap 'rm -f "$TMP_FILE"' EXIT
NUOVO_VALORE="$(openssl rand -base64 64 | tr -d '\n')" \
    awk -v variabile="$VARIABILE" \
        'index($0, variabile "=") == 1 { print variabile "=" ENVIRON["NUOVO_VALORE"]; next } { print }' \
        "$FILE" > "$TMP_FILE"
chmod 600 "$TMP_FILE"
mv "$TMP_FILE" "$FILE"
REMOTO
echo "✅ File di credenziali aggiornato"

echo "🔄 Ricreazione del container dell'applicazione..."
"${SSH[@]}" sudo "$CONF_DIR/run-app.sh" > /dev/null

HEALTH_URL="http://$PUBLIC_IP:8082/actuator/health"
echo "⏳ Attesa di $HEALTH_URL (max $((TENTATIVI * ATTESA_SECONDI)) secondi)..."
for i in $(seq 1 "$TENTATIVI"); do
    if curl -s "$HEALTH_URL" 2>/dev/null | grep -q '"status":"UP"'; then
        echo "✅ Applicazione UP dopo $((i * ATTESA_SECONDI)) secondi: rotazione di $VARIABILE completata"
        exit 0
    fi
    sleep "$ATTESA_SECONDI"
done

echo "❌ L'applicazione non e' tornata UP entro $((TENTATIVI * ATTESA_SECONDI)) secondi"
echo "   Controlla i log: ${SSH[*]} sudo docker logs gestioneannotazioni"
exit 1
