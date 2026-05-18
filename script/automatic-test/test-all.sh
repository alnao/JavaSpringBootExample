# Script per chiamare tutti i test inclusi quelli per SQLite
#!/bin/bash
# set -e 

# --- Logging: scrive su automatic-test-YYYYMMDD.log
if [ -z "$LOG_FILE" ]; then
  LOG_FILE="./automatic-test-$(date +%Y%m%d).log"
  exec > >(tee -a "$LOG_FILE") 2>&1
fi
export LOG_FILE

BLUE='\033[0;34m'
NC='\033[0m' # No Color


echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')] Test All Gestione Annotazioni - LOG: $LOG_FILE${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Posizione script: $(dirname "$0")${NC}"
echo -e "${BLUE}Directory di lavoro: $(pwd)${NC}"
echo -e "${BLUE}========================================${NC}"

# Script per eseguire il profilo `kube` eseguito in locale con docker compose
./script/automatic-test/test-kube-onprem-docker-compose.sh
echo -e "${BLUE}========================================${NC}"

# Script per eseguire il profilo `sqlite` eseguito in locale (con solo sqlite) senza docker
./script/automatic-test/test-sqlite-onprem.sh
echo -e "${BLUE}========================================${NC}"

# Script per eseguire il profilo `kube` eseguito in locale (con Postgresql e MongoDB) con docker compose
./script/automatic-test/test-aws-onprem.sh
echo -e "${BLUE}========================================${NC}"

# Script per eseguire il profilo `kube` eseguito in locale con **minikube** e **kubernetes**
./script/automatic-test/test-minikube.sh
echo -e "${BLUE}========================================${NC}"

# Pulisce docker da eventuali risorse residue
# docker compose up -d --build
# docker-compose down --remove-orphans
docker network prune -f  > /dev/null 2>&1
docker volume rm $(docker volume ls -q)  > /dev/null 2>&1
docker rmi $(docker images -q)  > /dev/null 2>&1

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')] ✓ Test completati con successo! ${NC} ✓"
echo -e "${BLUE}========================================${NC}"