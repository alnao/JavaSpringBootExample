#!/bin/bash
# Script per chiamare tutti i test inclusi quelli per SQLite
#
# Non usa "set -e": i profili vengono eseguiti tutti anche se uno fallisce, così
# il riepilogo finale dice quali sono andati male invece di fermarsi al primo.
# L'esito complessivo è nell'exit code.

# --- Logging: scrive su automatic-test-YYYYMMDD.log
if [ -z "$LOG_FILE" ]; then
  LOG_FILE="./automatic-test-$(date +%Y%m%d).log"
  exec > >(tee -a "$LOG_FILE") 2>&1
fi
export LOG_FILE

BLUE='\033[0;34m'
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# Conteggio dei test e riepilogo finale: questo script possiede il registro,
# quindi è lui a stampare il riepilogo di tutte le esecuzioni.
source "$(dirname "$0")/lib-report.sh"
trap 'report_chiusura $?' EXIT

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}[$(date '+%Y-%m-%d %H:%M:%S')] Test All Gestione Annotazioni - LOG: $LOG_FILE${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Posizione script: $(dirname "$0")${NC}"
echo -e "${BLUE}Directory di lavoro: $(pwd)${NC}"
echo -e "${BLUE}========================================${NC}"

PROFILI_FALLITI=0

# Esegue uno script di profilo tenendo traccia del suo esito
esegui_profilo() {
    local descrizione="$1"
    local script="$2"
    shift 2
    echo -e "${BLUE}>>> $descrizione${NC}"
    "$script" "$@"
    local rc=$?
    if [ $rc -eq 0 ]; then
        echo -e "${GREEN}>>> $descrizione: OK${NC}"
    else
        echo -e "${RED}>>> $descrizione: FALLITO (exit $rc)${NC}"
        PROFILI_FALLITI=$((PROFILI_FALLITI + 1))
    fi
    echo -e "${BLUE}========================================${NC}"
}

# Profilo `kube` eseguito in locale con docker compose
esegui_profilo "Profilo kube con docker-compose" ./script/automatic-test/test-kube-onprem-docker-compose.sh

# Profilo `sqlite` eseguito in locale (con solo sqlite) senza docker
esegui_profilo "Profilo sqlite in locale" ./script/automatic-test/test-sqlite-onprem.sh

# Profilo `aws` eseguito in locale (MySQL, DynamoDB e SQS su LocalStack)
esegui_profilo "Profilo aws in locale" ./script/automatic-test/test-aws-onprem.sh

# Profilo `kube` eseguito in locale con **minikube** e **kubernetes**
esegui_profilo "Profilo kube su minikube" ./script/automatic-test/test-minikube.sh

# Pulizia delle sole risorse dei compose del progetto: i singoli script
# ripuliscono già le proprie, qui restano solo eventuali reti orfane.
docker network prune -f > /dev/null 2>&1

echo -e "${BLUE}========================================${NC}"
if [ "$PROFILI_FALLITI" -gt 0 ]; then
    if [ "$PROFILI_FALLITI" -eq 1 ]; then
        echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ✗ 1 profilo su 4 ha riportato errori${NC}"
    else
        echo -e "${RED}[$(date '+%Y-%m-%d %H:%M:%S')] ✗ $PROFILI_FALLITI profili su 4 hanno riportato errori${NC}"
    fi
else
    echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] ✓ Test completati con successo!${NC} ✓"
fi
echo -e "${BLUE}========================================${NC}"

# Il riepilogo dettagliato viene stampato dal trap, subito dopo questa riga.
if [ "$PROFILI_FALLITI" -gt 0 ]; then
    exit 1
fi
exit 0
