#!/bin/bash
set -e

echo "[INFO] Applico PersistentVolumeClaim per MongoDB e PostgreSQL..."
kubectl apply -f script/minikube-onprem/mongo-pvc.yaml
kubectl apply -f script/minikube-onprem/postgres-pvc.yaml

echo "[INFO] Avvio MongoDB e PostgreSQL (con storage persistente)..."
kubectl apply -f script/minikube-onprem/mongo-deployment.yaml
kubectl apply -f script/minikube-onprem/postgres-deployment.yaml

echo "[INFO] Avvio backend annotazioni (2 repliche)..."
kubectl apply -f script/minikube-onprem/annotazioni-deployment.yaml

echo "[INFO] Avvio Adminer e Mongo Express (tool di gestione DB)..."
kubectl apply -f script/minikube-onprem/adminer-deployment.yaml
kubectl apply -f script/minikube-onprem/mongo-express-deployment.yaml

echo "[INFO] Applico Ingress per esporre l'applicazione su annotazioni.local..."
kubectl apply -f script/minikube-onprem/ingress.yaml

echo "[INFO] Tutti i servizi sono stati avviati."
echo "[INFO] Ricorda di aggiungere '127.0.0.1 annotazioni.local' al file /etc/hosts se vuoi usare l'Ingress."