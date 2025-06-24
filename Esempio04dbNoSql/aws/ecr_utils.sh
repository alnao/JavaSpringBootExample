#!/bin/bash

# Script di utilità per gestire immagini ECR
# Utilizzo: ./ecr-utils.sh [COMMAND] [ACCOUNT_ID] [REGION] [PROJECT_NAME]

set -e

COMMAND=${1}
ACCOUNT_ID=${2}
REGION=${3:-"eu-central-1"}
PROJECT_NAME=${4:-"microservice"}

# Colori
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
    exit 1
}

info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

show_help() {
    echo -e "${BLUE}ECR Utilities Script${NC}"
    echo
    echo "Utilizzo: $0 [COMMAND] [ACCOUNT_ID] [REGION] [PROJECT_NAME]"
    echo
    echo "Comandi disponibili:"
    echo "  list      - Lista tutte le immagini nel repository"
    echo "  info      - Mostra informazioni dettagliate sul repository"
    echo "  cleanup   - Rimuove immagini vecchie (mantiene le ultime 5)"
    echo "  delete    - Elimina il repository ECR (ATTENZIONE: irreversibile)"
    echo "  pull      - Fa il pull dell'ultima immagine"
    echo "  scan      - Avvia una scansione di sicurezza dell'immagine"
    echo
    echo "Esempi:"
    echo "  $0 list 123456789012 eu-central-1 microservice"
    echo "  $0 info 123456789012"
    echo "  $0 cleanup 123456789012 us-east-1 myapp"
}

if [ -z "$COMMAND" ] || [ -z "$ACCOUNT_ID" ]; then
    show_help
    exit 1
fi

ECR_URL="${ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
REPOSITORY_NAME="$PROJECT_NAME"

case $COMMAND in
    "list")
        log "Lista immagini nel repository $REPOSITORY_NAME..."
        aws ecr list-images --region $REGION --repository-name $REPOSITORY_NAME --query 'imageIds[*].[imageTag,imageDigest]' --output table
        ;;
    
    "info")
        log "Informazioni repository $REPOSITORY_NAME..."
        aws ecr describe-repositories --region $REGION --repository-names $REPOSITORY_NAME --output table
        echo
        log "Dettagli immagini:"
        aws ecr describe-images --region $REGION --repository-name $REPOSITORY_NAME --query 'imageDetails[*].[imageTags[0],imageSizeInBytes,imagePushedAt]' --output table
        ;;
    
    "cleanup")
        log "Cleanup immagini vecchie (mantengo le ultime 5)..."
        OLD_IMAGES=$(aws ecr describe-images --region $REGION --repository-name $REPOSITORY_NAME --query 'sort_by(imageDetails,&imagePushedAt)[:-5].[imageDigest]' --output text)
        
        if [ -n "$OLD_IMAGES" ] && [ "$OLD_IMAGES" != "None" ]; then
            for digest in $OLD_IMAGES; do
                if [ "$digest" != "None" ] && [ -n "$digest" ]; then
                    log "Rimozione immagine: $digest"
                    aws ecr batch-delete-image --region $REGION --repository-name $REPOSITORY_NAME --image-ids imageDigest=$digest
                fi
            done
            log "Cleanup completato ✓"
        else
            info "Nessuna immagine da rimuovere"
        fi
        ;;
    
    "delete")
        echo -e "${RED}ATTENZIONE: Stai per eliminare il repository ECR '$REPOSITORY_NAME'${NC}"
        echo -e "${RED}Questa operazione è IRREVERSIBILE!${NC}"
        read -p "Sei sicuro? Digita 'DELETE' per confermare: " confirm
        
        if [ "$confirm" = "DELETE" ]; then
            log "Eliminazione repository $REPOSITORY_NAME..."
            aws ecr delete-repository --region $REGION --repository-name $REPOSITORY_NAME --force
            log "Repository eliminato ✓"
        else
            info "Operazione annullata"
        fi
        ;;
    
    "pull")
        log "Login e pull dell'ultima immagine..."
        aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_URL
        docker pull ${ECR_URL}/${REPOSITORY_NAME}:latest
        log "Pull completato ✓"
        ;;
    
    "scan")
        log "Avvio scansione di sicurezza..."
        aws ecr start-image-scan --region $REGION --repository-name $REPOSITORY_NAME --image-id imageTag=latest
        log "Scansione avviata. Usa 'aws ecr describe-image-scan-findings' per vedere i risultati"
        ;;
    
    *)
        error "Comando non riconosciuto: $COMMAND"
        show_help
        ;;
esac