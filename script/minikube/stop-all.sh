#!/bin/bash
set -e

echo "[INFO] Rimuovo Ingress..."
kubectl delete -f script/minikube/ingress.yaml || true

echo "[INFO] Rimuovo Adminer e Mongo Express..."
kubectl delete -f script/minikube/adminer-deployment.yaml || true
kubectl delete -f script/minikube/mongo-express-deployment.yaml || true

echo "[INFO] Rimuovo backend gestioneannotazioni..."
kubectl delete -f script/minikube/gestioneannotazioni-deployment.yaml || true

echo "[INFO] Rimuovo MongoDB e PostgreSQL..."
kubectl delete -f script/minikube/mongo-deployment.yaml || true
kubectl delete -f script/minikube/postgres-deployment.yaml || true

echo "[INFO] Rimuovo PersistentVolumeClaim (PVC) per MongoDB e PostgreSQL..."
kubectl delete -f script/minikube/mongo-pvc.yaml || true
kubectl delete -f script/minikube/postgres-pvc.yaml || true

echo "[INFO] Rimuovo Kafka UI, Kafka e Zookeeper..."
kubectl delete -f script/minikube/kafka-ui-deployment.yaml || true
kubectl delete -f script/minikube/kafka-deployment.yaml || true
kubectl delete -f script/minikube/zookeeper-deployment.yaml || true

echo "[INFO] Distruggo il namespace gestioneannotazioni..."
kubectl delete -f script/minikube/namespace.yaml

echo "[INFO] Tutti i servizi e le risorse sono stati rimossi."
