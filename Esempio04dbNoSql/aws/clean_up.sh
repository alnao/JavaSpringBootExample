#!/bin/bash

# Script per cleanup delle risorse AWS
# Utilizzo: ./cleanup-aws.sh [REGION]

set -e

REGION=${1:-"eu-central-1"}
PROJECT_NAME="esempio04"
CLUSTER_NAME="${PROJECT_NAME}-cluster"
SERVICE_NAME="${PROJECT_NAME}-service"
REPOSITORY_NAME="${PROJECT_NAME}-repo"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

echo -e "${RED} Inizio cleanup delle risorse AWS per ${PROJECT_NAME}${NC}"

# 1. Elimina ECS Service
log "Eliminazione ECS Service..."
aws ecs update-service --region $REGION --cluster $CLUSTER_NAME --service $SERVICE_NAME --desired-count 0 2>/dev/null || warn "Servizio non trovato"
aws ecs delete-service --region $REGION --cluster $CLUSTER_NAME --service $SERVICE_NAME 2>/dev/null || warn "Servizio non trovato"

# 2. Elimina ECS Cluster
log "Eliminazione ECS Cluster..."
aws ecs delete-cluster --region $REGION --cluster $CLUSTER_NAME 2>/dev/null || warn "Cluster non trovato"
#aws ecs deregister-task-definition --task-definition "${PROJECT_NAME}-task" --region $REGION 

# 3. Elimina DocumentDB
log "Eliminazione DocumentDB..."
aws docdb delete-db-instance --region $REGION --db-instance-identifier "${PROJECT_NAME}-docdb-instance" --skip-final-snapshot 2>/dev/null || warn "Istanza DocumentDB non trovata"
aws docdb delete-db-cluster --region $REGION --db-cluster-identifier "${PROJECT_NAME}-docdb-cluster" --skip-final-snapshot 2>/dev/null || warn "Cluster DocumentDB non trovato"
aws docdb delete-db-subnet-group --region $REGION --db-subnet-group-name "${PROJECT_NAME}-subnet-group" 2>/dev/null || warn "Subnet group non trovato"

# 4. Elimina DynamoDB Table
log "Eliminazione tabella DynamoDB..."
aws dynamodb delete-table --region $REGION --table-name "${PROJECT_NAME}-table" 2>/dev/null || warn "Tabella DynamoDB non trovata"

# 5. Elimina Security Group
log "Eliminazione Security Group..."
SECURITY_GROUP_ID=$(aws ec2 describe-security-groups --region $REGION --group-names "${PROJECT_NAME}-sg" --query 'SecurityGroups[0].GroupId' --output text 2>/dev/null)
if [ "$SECURITY_GROUP_ID" != "None" ] && [ -n "$SECURITY_GROUP_ID" ]; then
    aws ec2 delete-security-group --region $REGION --group-id $SECURITY_GROUP_ID 2>/dev/null || warn "Security group non eliminato"
fi

# 6. Elimina IAM Roles
log "Eliminazione IAM Roles..."
aws iam detach-role-policy --role-name "${PROJECT_NAME}-task-role" --policy-arn arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess 2>/dev/null || warn "Policy già scollegata"
aws iam delete-role --role-name "${PROJECT_NAME}-task-role" 2>/dev/null || warn "Task role non trovato"
aws iam detach-role-policy --role-name "${PROJECT_NAME}-execution-role" --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy 2>/dev/null || warn "Policy già scollegata"
aws iam delete-role --role-name "${PROJECT_NAME}-execution-role" 2>/dev/null || warn "Execution role non trovato"

# 7. Elimina ECR Repository
log "Eliminazione ECR Repository..."
aws ecr delete-repository --region $REGION --repository-name $REPOSITORY_NAME --force 2>/dev/null || warn "Repository ECR non trovato"

echo -e "${GREEN} Cleanup completato!${NC}"