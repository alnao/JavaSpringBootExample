#!/bin/bash

mvn clean package -DskipTests
docker build -t alnao/gestioneannotazioni:latest .
