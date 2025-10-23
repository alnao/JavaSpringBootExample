#!/bin/bash
set -e

echo "[INFO] Creo il namespace gestioneannotazioni..."
kubectl apply -f script/minikube-onprem/namespace.yaml

# Avvio i PersistentVolumeClaim
echo "[INFO] Applico PersistentVolumeClaim per MongoDB e PostgreSQL..."
kubectl apply -f script/minikube-onprem/mongo-pvc.yaml
kubectl apply -f script/minikube-onprem/postgres-pvc.yaml

# Avvio i database
echo "[INFO] Avvio MongoDB e PostgreSQL (con storage persistente)..."
kubectl apply -f script/minikube-onprem/mongo-deployment.yaml
kubectl apply -f script/minikube-onprem/postgres-deployment.yaml

# Attendi che i pod dei database siano pronti
echo "[INFO] Attendo che i DB siano partiti correttamente per eseguire gli script di inizializzazione..."
kubectl wait --for=condition=Ready pod -l app=mongo --timeout=120s --namespace=gestioneannotazioni
kubectl wait --for=condition=Ready pod -l app=postgres --timeout=120s --namespace=gestioneannotazioni
sleep 60 # Attendo ulteriori 60 secondi per essere sicuro che i DB siano pronti

# Copia ed esegui lo script su MongoDB
MONGO_DB=$(kubectl get pod -l app=mongo -o jsonpath="{.items[0].metadata.name}"  --namespace=gestioneannotazioni)
kubectl cp script/init-database/init-mongodb.js $MONGO_DB:/init-mongodb.js  --namespace=gestioneannotazioni
kubectl exec -it $MONGO_DB --namespace=gestioneannotazioni -- mongo -u demo -p demo --authenticationDatabase admin /init-mongodb.js 

# Copia ed esegui lo script su PostgreSQL
POSTGRES_DB=$(kubectl get pod -l app=postgres -o jsonpath="{.items[0].metadata.name}"  --namespace=gestioneannotazioni)
kubectl cp script/init-database/init-postgres.sql $POSTGRES_DB:/init-postgres.sql  --namespace=gestioneannotazioni
kubectl exec -it $POSTGRES_DB --namespace=gestioneannotazioni -- psql -U demo -d gestioneannotazioni -f /init-postgres.sql

# Avvio Zookeeper, Kafka e Kafka UI
echo "[INFO] Avvio Zookeeper, Kafka e Kafka UI..."
kubectl apply -f script/minikube-onprem/zookeeper-deployment.yaml
kubectl wait --for=condition=Ready pod -l app=zookeeper --timeout=300s  --namespace=gestioneannotazioni
kubectl apply -f script/minikube-onprem/kafka-deployment.yaml  
kubectl wait --for=condition=Ready pod -l app=kafka-service --timeout=300s  --namespace=gestioneannotazioni
kubectl apply -f script/minikube-onprem/kafka-ui-deployment.yaml  

# Avvio il backend
echo "[INFO] Avvio backend gestioneannotazioni (2 repliche)..."
kubectl apply -f script/minikube-onprem/gestioneannotazioni-deployment.yaml

# Attendi che i pod del backend siano pronti e avvio adminer e mongo-express
echo "[INFO] Avvio Adminer e Mongo Express (tool di gestione DB)..."
kubectl apply -f script/minikube-onprem/adminer-deployment.yaml
kubectl apply -f script/minikube-onprem/mongo-express-deployment.yaml

# Espongo i servizi con ingress
echo "[INFO] Applico Ingress per esporre l'applicazione su gestioneannotazioni.local..."
kubectl apply -f script/minikube-onprem/ingress.yaml

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
#oppure eseguire il comando
#minikube service gestioneannotazioni-app -n gestioneannotazioni

