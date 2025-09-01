#!/bin/bash
set -e

echo "[INFO] Rimuovo Ingress..."
kubectl delete -f script/minikube-onprem/ingress.yaml || true

echo "[INFO] Rimuovo Adminer e Mongo Express..."
kubectl delete -f script/minikube-onprem/adminer-deployment.yaml || true
kubectl delete -f script/minikube-onprem/mongo-express-deployment.yaml || true

echo "[INFO] Rimuovo backend annotazioni..."
kubectl delete -f script/minikube-onprem/annotazioni-deployment.yaml || true

echo "[INFO] Rimuovo MongoDB e PostgreSQL..."
kubectl delete -f script/minikube-onprem/mongo-deployment.yaml || true
kubectl delete -f script/minikube-onprem/postgres-deployment.yaml || true

echo "[INFO] Rimuovo PersistentVolumeClaim (PVC) per MongoDB e PostgreSQL..."
kubectl delete -f script/minikube-onprem/mongo-pvc.yaml || true
kubectl delete -f script/minikube-onprem/postgres-pvc.yaml || true

echo "[INFO] Tutti i servizi e le risorse sono stati rimossi."
