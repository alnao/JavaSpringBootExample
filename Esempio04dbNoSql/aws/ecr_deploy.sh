#!/bin/bash

# Script per build e push di immagini Docker su AWS ECR
# Utilizzo: ./ecr-push.sh [ACCOUNT_ID] [REGION] [PROJECT_NAME] [TAG]

set -e

# Parametri configurabili
REGION=${1:-"eu-central-1"}
PROJECT_NAME=${2:-"esempio04"}
TAG=${3:-"latest"}

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Funzioni per logging
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
    exit 1
}

warn() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

# Banner
echo -e "${BLUE}"
echo "==============================================="
echo "    AWS ECR Docker Build & Push Script"
echo "==============================================="
echo -e "${NC}"


# Ottieni l'URI del repository
REPOSITORY_NAME="${PROJECT_NAME}-repo"
ECR_URL=$(aws ecr describe-repositories --region $REGION --repository-names $REPOSITORY_NAME --query 'repositories[0].repositoryUri' --output text)
log "Repository URI: $REPOSITORY_URI"

# Costruisci URL del repository ECR
#FULL_IMAGE_URI="${ECR_URL}/${REPOSITORY_NAME}:${TAG}"
FULL_IMAGE_URI="${ECR_URL}:${TAG}"

info "Configurazione:"
echo "  Regione: $REGION"
echo "  Nome progetto: $PROJECT_NAME"
echo "  Tag: $TAG"
echo "  Repository URI: $FULL_IMAGE_URI"
echo

# Verifica che AWS CLI sia installato e configurato
if ! command -v aws &> /dev/null; then
    error "AWS CLI non è installato. Installalo prima di continuare."
fi

if ! aws sts get-caller-identity &> /dev/null; then
    error "AWS CLI non è configurato correttamente. Esegui 'aws configure' prima di continuare."
fi

# Verifica che Docker sia installato e in esecuzione
if ! command -v docker &> /dev/null; then
    error "Docker non è installato. Installalo prima di continuare."
fi

if ! docker info &> /dev/null; then
    error "Docker non è in esecuzione. Avvia Docker prima di continuare."
fi

log "Prerequisiti verificati ✓"

# Verifica che esista un Dockerfile nella directory corrente
if [ ! -f "Dockerfile" ]; then
    error "Dockerfile non trovato nella directory corrente"
fi

log "Dockerfile trovato ✓"

# 1. Verifica/Crea il repository ECR
log "Verifica esistenza repository ECR..."
if ! aws ecr describe-repositories --region $REGION --repository-names $REPOSITORY_NAME &> /dev/null; then
    warn "Repository ECR non esistente, creazione in corso..."
    aws ecr create-repository --region $REGION --repository-name $REPOSITORY_NAME
    log "Repository ECR '$REPOSITORY_NAME' creato ✓"
else
    log "Repository ECR '$REPOSITORY_NAME' già esistente ✓"
fi

# 2. Login a ECR
log "Login a ECR in corso..."
if aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_URL; then
    log "Login a ECR completato ✓"
else
    error "Login a ECR fallito"
fi

# 3. Build dell'immagine Docker
log "Build dell'immagine Docker in corso..."
BUILD_START_TIME=$(date +%s)

if docker build -t $PROJECT_NAME .; then
    BUILD_END_TIME=$(date +%s)
    BUILD_DURATION=$((BUILD_END_TIME - BUILD_START_TIME))
    log "Build completata in ${BUILD_DURATION}s ✓"
else
    error "Build dell'immagine Docker fallita"
fi

# 4. Tag dell'immagine
log "Tag dell'immagine in corso..."
if docker tag $PROJECT_NAME:latest $FULL_IMAGE_URI; then
    log "Tag completato ✓"
else
    error "Tag dell'immagine fallito"
fi

# 5. Push dell'immagine su ECR
log "Push dell'immagine su ECR in corso..."
PUSH_START_TIME=$(date +%s)

if docker push $FULL_IMAGE_URI; then
    PUSH_END_TIME=$(date +%s)
    PUSH_DURATION=$((PUSH_END_TIME - PUSH_START_TIME))
    log "Push completato in ${PUSH_DURATION}s ✓"
else
    error "Push dell'immagine su ECR fallito"
fi

# 6. Verifica che l'immagine sia stata pushata correttamente
log "Verifica dell'immagine pushata..."
if aws ecr describe-images --region $REGION --repository-name $REPOSITORY_NAME --image-ids imageTag=$TAG &> /dev/null; then
    log "Immagine verificata su ECR ✓"
else
    error "Immagine non trovata su ECR dopo il push"
fi

# 7. Ottieni informazioni sull'immagine
IMAGE_INFO=$(aws ecr describe-images --region $REGION --repository-name $REPOSITORY_NAME --image-ids imageTag=$TAG --query 'imageDetails[0]')
IMAGE_SIZE=$(echo $IMAGE_INFO | jq -r '.imageSizeInBytes')
PUSH_DATE=$(echo $IMAGE_INFO | jq -r '.imagePushedAt')

# Converti dimensione in MB
if [ "$IMAGE_SIZE" != "null" ] && [ -n "$IMAGE_SIZE" ]; then
    IMAGE_SIZE_MB=$((IMAGE_SIZE / 1024 / 1024))
    info "Dimensione immagine: ${IMAGE_SIZE_MB} MB"
fi

if [ "$PUSH_DATE" != "null" ] && [ -n "$PUSH_DATE" ]; then
    info "Data push: $PUSH_DATE"
fi

# 8. Cleanup immagini locali (opzionale)
read -p "Vuoi rimuovere le immagini Docker locali per liberare spazio? (y/n): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    log "Rimozione immagini locali..."
    docker rmi $PROJECT_NAME:latest 2>/dev/null || warn "Immagine locale già rimossa"
    docker rmi $FULL_IMAGE_URI 2>/dev/null || warn "Immagine taggata già rimossa"
    log "Cleanup completato ✓"
fi

# Riepilogo finale
echo
echo -e "${GREEN}🎉 Build e push completati con successo!${NC}"
echo
echo -e "${BLUE}Riepilogo:${NC}"
echo "  ✓ Repository: $REPOSITORY_NAME"
echo "  ✓ Immagine URI: $FULL_IMAGE_URI"
echo "  ✓ Regione: $REGION"
echo "  ✓ Tag: $TAG"
echo
echo -e "${YELLOW}Per utilizzare questa immagine in ECS o altri servizi AWS:${NC}"
echo "  $FULL_IMAGE_URI"
echo
echo -e "${YELLOW}Per fare il pull dell'immagine:${NC}"
echo "  aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_URL"
echo "  docker pull $FULL_IMAGE_URI"
echo
echo -e "${YELLOW}Per vedere tutte le immagini nel repository:${NC}"
echo "  aws ecr list-images --region $REGION --repository-name $REPOSITORY_NAME"