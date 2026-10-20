#!/bin/bash
# Rotazione di una credenziale dello stack minikube, profilo kube (change openspec secret-kube).
#
# Uso (dalla root del progetto, con il contesto kubectl di minikube attivo):
#   ./script/minikube/rotate-secret.sh JWT_SECRET
#   ./script/minikube/rotate-secret.sh POSTGRES_PASSWORD
#   ./script/minikube/rotate-secret.sh MONGO_ROOT_PASSWORD
#
# Passi: legge i valori correnti dal Secret; genera il nuovo valore; per le password dei database lo
# imposta prima sul database (il valore passa a psql/mongo da stdin, mai come argomento); aggiorna il
# Secret con un patch da file temporaneo; riavvia i Deployment che usano la credenziale e attende il
# rollout; verifica /actuator/health con un port-forward temporaneo (i Deployment non hanno probe).
# Il nuovo valore non viene mai stampato.
#
# JWT_SECRET: gli access token gia' emessi vengono rifiutati (401), gli utenti rifanno il login.
# APP_USER_*_PASSWORD non sono ruotabili: gli utenti iniziali si creano solo su database vuoto.
set -euo pipefail

NAMESPACE=gestioneannotazioni
SECRET_NAME=gestioneannotazioni-credenziali
PORTA_LOCALE="${PORTA_LOCALE:-18090}"
TENTATIVI=60
ATTESA_SECONDI=2

VARIABILE="${1:-}"
case "$VARIABILE" in
    JWT_SECRET|POSTGRES_PASSWORD|MONGO_ROOT_PASSWORD)
        ;;
    APP_USER_*_PASSWORD)
        echo "❌ $VARIABILE non si puo' ruotare con questo script."
        echo "   Gli utenti iniziali vengono creati solo su un database vuoto: cambiare la variabile"
        echo "   non cambia la password di un utente gia' creato. Cambiala dall'applicazione."
        exit 1
        ;;
    *)
        echo "Uso: $0 JWT_SECRET|POSTGRES_PASSWORD|MONGO_ROOT_PASSWORD"
        exit 1
        ;;
esac

valore() {
    kubectl get secret "$SECRET_NAME" --namespace="$NAMESPACE" -o jsonpath="{.data.$1}" | base64 -d
}
pod() {
    kubectl get pod -l "app=$1" --namespace="$NAMESPACE" -o jsonpath="{.items[0].metadata.name}"
}

if ! kubectl get secret "$SECRET_NAME" --namespace="$NAMESPACE" > /dev/null 2>&1; then
    echo "❌ Secret $SECRET_NAME non trovato: avvia lo stack con ./script/minikube/start-all.sh"
    exit 1
fi

if [ "$VARIABILE" = "JWT_SECRET" ]; then
    NUOVO_VALORE="$(openssl rand -base64 64 | tr -d '\n')"
else
    # esadecimale: nessun carattere da codificare in MONGODB_URI o da proteggere nell'SQL
    NUOVO_VALORE="$(openssl rand -hex 24)"
fi

TMP_FILE="$(mktemp)"
chmod 600 "$TMP_FILE"
PF_PID=""
trap 'rm -f "$TMP_FILE"; [ -n "$PF_PID" ] && kill "$PF_PID" 2>/dev/null || true' EXIT

echo "🔐 Rotazione di $VARIABILE..."
case "$VARIABILE" in
    POSTGRES_PASSWORD)
        UTENTE="$(valore POSTGRES_USERNAME)"
        printf "ALTER USER \"%s\" WITH PASSWORD '%s';\n" "$UTENTE" "$NUOVO_VALORE" \
            | kubectl exec -i "$(pod postgres)" --namespace="$NAMESPACE" -- \
                psql -q -v ON_ERROR_STOP=1 -U "$UTENTE" -d gestioneannotazioni > /dev/null
        echo "✅ Password cambiata su PostgreSQL"
        DEPLOYMENT=(gestioneannotazioni-app)
        ;;
    MONGO_ROOT_PASSWORD)
        UTENTE="$(valore MONGO_ROOT_USERNAME)"
        printf 'var a = db.getSiblingDB("admin"); a.auth("%s", "%s"); a.changeUserPassword("%s", "%s");\n' \
            "$UTENTE" "$(valore MONGO_ROOT_PASSWORD)" "$UTENTE" "$NUOVO_VALORE" \
            | kubectl exec -i "$(pod mongo)" --namespace="$NAMESPACE" -- mongo --quiet admin > /dev/null
        echo "✅ Password cambiata su MongoDB"
        DEPLOYMENT=(gestioneannotazioni-app mongo-express)
        ;;
    JWT_SECRET)
        DEPLOYMENT=(gestioneannotazioni-app)
        ;;
esac

# Patch del Secret da file: il valore (in base64) non compare negli argomenti di kubectl
printf '{"data":{"%s":"%s"}}' "$VARIABILE" "$(printf '%s' "$NUOVO_VALORE" | base64 -w0)" > "$TMP_FILE"
unset NUOVO_VALORE
kubectl patch secret "$SECRET_NAME" --namespace="$NAMESPACE" --type=merge --patch-file="$TMP_FILE" > /dev/null
rm -f "$TMP_FILE"
echo "✅ Secret $SECRET_NAME aggiornato"

for d in "${DEPLOYMENT[@]}"; do
    echo "🔄 Riavvio del Deployment $d..."
    kubectl rollout restart "deployment/$d" --namespace="$NAMESPACE" > /dev/null
    kubectl rollout status "deployment/$d" --namespace="$NAMESPACE" --timeout=300s > /dev/null
done

echo "⏳ Verifica di /actuator/health tramite port-forward su localhost:$PORTA_LOCALE..."
kubectl port-forward "svc/gestioneannotazioni-app" "$PORTA_LOCALE:8080" --namespace="$NAMESPACE" > /dev/null 2>&1 &
PF_PID=$!
for i in $(seq 1 "$TENTATIVI"); do
    if curl -s "http://localhost:$PORTA_LOCALE/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
        echo "✅ Applicazione UP dopo $((i * ATTESA_SECONDI)) secondi: rotazione di $VARIABILE completata"
        exit 0
    fi
    sleep "$ATTESA_SECONDI"
done
echo "❌ L'applicazione non e' tornata UP entro $((TENTATIVI * ATTESA_SECONDI)) secondi"
echo "   Controlla i log: kubectl logs deployment/gestioneannotazioni-app -n $NAMESPACE"
exit 1
