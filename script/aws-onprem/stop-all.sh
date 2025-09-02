#!/bin/bash
# Ferma e rimuove lo stack AWS-onprem
cd "$(dirname "$0")"
docker-compose down
