#!/bin/bash
# Semplice script per fare il push dell'immagine Docker su Docker Hub
# Assicurati di aver effettuato il login con `docker login` prima di eseguire questo script
set -e

docker login
docker build -t alnao/gestionepersonale:latest .
docker push alnao/gestionepersonale:latest

echo "Immagine Docker 'alnao/gestionepersonale:latest' pushata su Docker Hub con successo."
