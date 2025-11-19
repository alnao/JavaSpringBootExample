#!/bin/bash

echo "Costruzione immagine Docker..."

mvn clean package -DskipTests
docker build -t alnao/gestioneannotazioni:latest .

echo "---------------------------------------------------------"
echo "✅ Immagine Docker costruita con successo: alnao/gestioneannotazioni:latest"