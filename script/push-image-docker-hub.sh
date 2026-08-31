#!/bin/bash
# Semplice script per fare il push dell'immagine Docker su Docker Hub.
# Il login viene fatto qui esplicitamente sul namespace di destinazione: senza -u
# il CLI riusa le credenziali gia' salvate (magari di un altro account) e il push
# fallisce piu' avanti con "denied: requested access to the resource is denied".
set -e

NAMESPACE=alnao
IMAGE="$NAMESPACE/gestioneannotazioni:latest"

# Con -u l'autenticazione avviene per forza come $NAMESPACE: se le credenziali
# non sono valide docker login esce non-zero e set -e ferma lo script.
docker login -u "$NAMESPACE"

docker build -t "$IMAGE" .
docker push "$IMAGE"

echo "---------------------------------------------------------"
echo "✅ Immagine Docker '$IMAGE' pushata su Docker Hub con successo."
