#!/bin/bash
# Ferma e rimuove lo stack AWS-kube

#cd "$(dirname "$0")"

docker-compose -f script/aws-onprem/docker-compose.yml down
docker volume rm $(docker volume ls -q)
docker rmi $(docker images -q)

echo -e "\nStack fermato e pulizia completata!"