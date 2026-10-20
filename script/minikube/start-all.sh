#!/bin/bash
set -e

echo "[INFO] Creo il namespace gestioneannotazioni..."
kubectl apply -f script/minikube/namespace.yaml

# Credenziali (change openspec secret-kube): Secret creato con valori casuali solo se non esiste gia'.
# I valori passano da un file temporaneo leggibile solo dall'utente, cancellato subito dopo: non compaiono
# negli argomenti dei processi ne' nell'output. Per leggerli vedi il comando stampato in fondo.
SECRET_NAME=gestioneannotazioni-credenziali
if ! kubectl get secret "$SECRET_NAME" --namespace=gestioneannotazioni > /dev/null 2>&1; then
  echo "[INFO] Creo il Secret $SECRET_NAME con valori generati..."
  SECRET_ENV_FILE="$(mktemp)"
  chmod 600 "$SECRET_ENV_FILE"
  trap 'rm -f "$SECRET_ENV_FILE"' EXIT
  {
    echo "POSTGRES_USERNAME=gestioneannotazioni_user"
    echo "POSTGRES_PASSWORD=$(openssl rand -hex 24)"
    echo "MONGO_ROOT_USERNAME=admin"
    echo "MONGO_ROOT_PASSWORD=$(openssl rand -hex 24)"
    echo "MONGO_EXPRESS_USERNAME=admin"
    echo "MONGO_EXPRESS_PASSWORD=$(openssl rand -hex 16)"
    echo "JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')"
    echo "APP_USER_ADMIN_PASSWORD=$(openssl rand -hex 12)"
    echo "APP_USER_MODERATORE_PASSWORD=$(openssl rand -hex 12)"
    echo "APP_USER_ALNAO_PASSWORD=$(openssl rand -hex 12)"
  } > "$SECRET_ENV_FILE"
  kubectl create secret generic "$SECRET_NAME" --namespace=gestioneannotazioni --from-env-file="$SECRET_ENV_FILE" > /dev/null
  rm -f "$SECRET_ENV_FILE"
else
  echo "[INFO] Secret $SECRET_NAME gia' presente: non viene rigenerato"
fi

# Avvio i PersistentVolumeClaim
echo "[INFO] Applico PersistentVolumeClaim per MongoDB e PostgreSQL..."
kubectl apply -f script/minikube/mongo-pvc.yaml
kubectl apply -f script/minikube/postgres-pvc.yaml

# Avvio i database
echo "[INFO] Avvio MongoDB e PostgreSQL (con storage persistente)..."
kubectl apply -f script/minikube/mongo-deployment.yaml
kubectl apply -f script/minikube/postgres-deployment.yaml

# Attendi che i pod dei database siano pronti
echo "[INFO] Attendo che i DB siano partiti correttamente per eseguire gli script di inizializzazione..."
echo "[INFO] Attesa avvio database (max 120 secondi)..."
sleep 60 # Attendo ulteriori 60 secondi per essere sicuro che i DB siano pronti
kubectl wait --for=condition=Ready pod -l app=mongo --timeout=120s --namespace=gestioneannotazioni
kubectl wait --for=condition=Ready pod -l app=postgres --timeout=120s --namespace=gestioneannotazioni
sleep 60 # Attendo ulteriori 60 secondi per essere sicuro che i DB siano pronti

# Copia ed esegui lo script su MongoDB
MONGO_DB=$(kubectl get pod -l app=mongo -o jsonpath="{.items[0].metadata.name}"  --namespace=gestioneannotazioni)
kubectl cp script/init-database/init-mongodb.js $MONGO_DB:/init-mongodb.js  --namespace=gestioneannotazioni
# credenziali lette dalle variabili del pod (dal Secret): non passano dall'host
kubectl exec $MONGO_DB --namespace=gestioneannotazioni -- sh -c 'mongo -u "$MONGO_INITDB_ROOT_USERNAME" -p "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin /init-mongodb.js'

# Copia ed esegui lo script su PostgreSQL
POSTGRES_DB=$(kubectl get pod -l app=postgres -o jsonpath="{.items[0].metadata.name}"  --namespace=gestioneannotazioni)
kubectl cp script/init-database/init-postgres.sql $POSTGRES_DB:/init-postgres.sql  --namespace=gestioneannotazioni
kubectl exec $POSTGRES_DB --namespace=gestioneannotazioni -- sh -c 'psql -U "$POSTGRES_USER" -d gestioneannotazioni -f /init-postgres.sql'

# Avvio Zookeeper, Kafka e Kafka UI
echo "[INFO] Avvio Zookeeper, Kafka e Kafka UI..."
kubectl apply -f script/minikube/zookeeper-deployment.yaml
kubectl wait --for=condition=Ready pod -l app=zookeeper --timeout=300s --namespace=gestioneannotazioni
kubectl apply -f script/minikube/kafka-deployment.yaml
kubectl wait --for=condition=Ready pod -l app=kafka-service --timeout=300s --namespace=gestioneannotazioni
kubectl apply -f script/minikube/kafka-ui-deployment.yaml
kubectl wait --for=condition=Ready pod -l app=kafka-ui --timeout=300s --namespace=gestioneannotazioni

# Attendi che i pod del backend siano pronti e avvio adminer e mongo-express
echo "[INFO] Avvio Adminer e Mongo Express (tool di gestione DB)..."
kubectl apply -f script/minikube/adminer-deployment.yaml
kubectl apply -f script/minikube/mongo-express-deployment.yaml

# Avvio Redis
echo "[INFO] Avvio Redis..."
kubectl apply -f script/minikube/redis-deployment.yaml
kubectl wait --for=condition=Ready pod -l app=redis --timeout=300s --namespace=gestioneannotazioni

# Avvio il backend
echo "[INFO] Avvio backend gestioneannotazioni (2 repliche)..."
kubectl apply -f script/minikube/gestioneannotazioni-deployment.yaml

# Attendi che i pod del backend siano pronti
echo "[INFO] Attendo che i pod del backend siano pronti..."
kubectl wait --for=condition=Ready pod -l app=gestioneannotazioni-app --timeout=300s --namespace=gestioneannotazioni

# Espongo i servizi con ingress
echo "[INFO] Applico Ingress per esporre l'applicazione su gestioneannotazioni.local..."
kubectl apply -f script/minikube/ingress.yaml

# Aggiungi gestioneannotazioni.local a /etc/hosts se non presente
#MINIKUBE_IP=$(minikube ip)
#if ! grep -q "gestioneannotazioni.local" /etc/hosts; then
#  echo "[INFO] Aggiungo $(minikube ip) gestioneannotazioni.local a /etc/hosts (richiede sudo)..."
#  echo "$MINIKUBE_IP gestioneannotazioni.local" | sudo tee -a /etc/hosts
#else
#  echo "[INFO] gestioneannotazioni.local già presente in /etc/hosts"
#fi
kubectl wait --for=condition=Ready pod -l app=gestioneannotazioni-app --timeout=300s  --namespace=gestioneannotazioni
URL=$(minikube service gestioneannotazioni-app -n gestioneannotazioni --url)

KAFKA_UI_URL=$(minikube service kafka-ui -n gestioneannotazioni --url)
#FINE!
echo "[INFO] Tutti i servizi sono stati avviati."
echo "[INFO] Se vuoi puoi aggiungere '127.0.0.1 gestioneannotazioni.local' al file /etc/hosts se vuoi usare l'Ingress."
echo "[INFO] L'applicazione è raggiungibile all'URL: $URL"
echo "[INFO] Kafka UI è raggiungibile all'URL: $KAFKA_UI_URL"
echo "[INFO] Credenziali nel Secret $SECRET_NAME, per esempio la password di admin:"
echo "       kubectl get secret $SECRET_NAME -n gestioneannotazioni -o jsonpath='{.data.APP_USER_ADMIN_PASSWORD}' | base64 -d"
echo "[INFO] Rotazione: ./script/minikube/rotate-secret.sh JWT_SECRET|POSTGRES_PASSWORD|MONGO_ROOT_PASSWORD"
#oppure eseguire il comando
#minikube service gestioneannotazioni-app -n gestioneannotazioni

