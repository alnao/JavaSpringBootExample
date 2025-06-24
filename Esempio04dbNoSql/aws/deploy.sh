#!/bin/bash

# Script per il deploy del microservizio su AWS
# Utilizzo: ./deploy-aws.sh [REGION]

set -e

# Parametri configurabili
REGION=${1:-"eu-central-1"}
PROJECT_NAME="esempio04"
CLUSTER_NAME="${PROJECT_NAME}-cluster"
SERVICE_NAME="${PROJECT_NAME}-service"
TASK_DEFINITION_NAME="${PROJECT_NAME}-task"
REPOSITORY_NAME="${PROJECT_NAME}-repo"

# Colori per output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Inizio deploy del microservizio ${PROJECT_NAME} nella regione ${REGION}${NC}"
echo -e "${GREEN}> Start [$(date +'%Y-%m-%d %H:%M:%S')] ${NC}"

# Funzione per logging
log() {
    #echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
    echo -e "${GREEN}> $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}"
    exit 1
}

warn() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

# Verifica che AWS CLI sia installato e configurato
if ! command -v aws &> /dev/null; then
    error "AWS CLI non è installato. Installalo prima di continuare."
fi

if ! aws sts get-caller-identity &> /dev/null; then
    error "AWS CLI non è configurato correttamente. Esegui 'aws configure' prima di continuare."
fi

log "Configurazione AWS verificata ✓"

# Ottieni informazioni sulla VPC e subnet di default
log "Recupero informazioni VPC e subnet di default..."
DEFAULT_VPC=$(aws ec2 describe-vpcs --region $REGION --filters "Name=isDefault,Values=true" --query 'Vpcs[0].VpcId' --output text)
if [ "$DEFAULT_VPC" = "None" ] || [ -z "$DEFAULT_VPC" ]; then
    error "Nessuna VPC di default trovata nella regione $REGION"
fi

SUBNETS=$(aws ec2 describe-subnets --region $REGION --filters "Name=vpc-id,Values=$DEFAULT_VPC" --query 'Subnets[*].SubnetId' --output text)
if [ -z "$SUBNETS" ]; then
    error "Nessuna subnet trovata nella VPC di default"
fi

SUBNET_IDS=(${SUBNETS})
log "VPC di default: $DEFAULT_VPC"
log "Subnet trovate: ${SUBNET_IDS[*]}"

# 1. Crea ECR Repository
log "Creazione ECR Repository..."
aws ecr describe-repositories --region $REGION --repository-names $REPOSITORY_NAME &> /dev/null || {
    aws ecr create-repository --region $REGION --repository-name $REPOSITORY_NAME
    log "ECR Repository $REPOSITORY_NAME creato ✓"
}

# Ottieni l'URI del repository
REPOSITORY_URI=$(aws ecr describe-repositories --region $REGION --repository-names $REPOSITORY_NAME --query 'repositories[0].repositoryUri' --output text)
log "Repository URI: $REPOSITORY_URI"

# 2. Build e push dell'immagine Docker
log "Build e push dell'immagine Docker..."
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $(echo $REPOSITORY_URI | cut -d'/' -f1)
docker build -t $REPOSITORY_NAME .
docker tag $REPOSITORY_NAME:latest $REPOSITORY_URI:latest
docker push $REPOSITORY_URI:latest
log "Immagine Docker pushata ✓"

# 3. Crea Security Group
log "Creazione Security Group..."
SECURITY_GROUP_ID=$(aws ec2 create-security-group \
    --region $REGION \
    --group-name "${PROJECT_NAME}-sg" \
    --description "Security group for ${PROJECT_NAME} microservice" \
    --vpc-id $DEFAULT_VPC \
    --query 'GroupId' --output text 2>/dev/null || \
    aws ec2 describe-security-groups \
    --region $REGION \
    --group-names "${PROJECT_NAME}-sg" \
    --query 'SecurityGroups[0].GroupId' --output text)

# Aggiungi regole al Security Group
aws ec2 authorize-security-group-ingress \
    --region $REGION \
    --group-id $SECURITY_GROUP_ID \
    --protocol tcp \
    --port 8080 \
    --cidr 0.0.0.0/0 2>/dev/null || warn "Regola per porta 8080 già esistente"

aws ec2 authorize-security-group-ingress \
    --region $REGION \
    --group-id $SECURITY_GROUP_ID \
    --protocol tcp \
    --port 27017 \
    --source-group $SECURITY_GROUP_ID 2>/dev/null || warn "Regola per porta 27017 già esistente"

log "Security Group creato: $SECURITY_GROUP_ID ✓"

# 4. Crea DynamoDB Table
log "Creazione tabella DynamoDB..."
aws dynamodb create-table \
    --region $REGION \
    --table-name "${PROJECT_NAME}-table" \
    --attribute-definitions \
        AttributeName=id,AttributeType=S \
    --key-schema \
        AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST 2>/dev/null || warn "Tabella DynamoDB già esistente"

# Attendi che la tabella sia attiva
log "Attendo che la tabella DynamoDB sia attiva..."
aws dynamodb wait table-exists --region $REGION --table-name "${PROJECT_NAME}-table"
log "Tabella DynamoDB attiva ✓"

# 5. Crea DocumentDB Subnet Group
log "Creazione DocumentDB Subnet Group..."
aws docdb create-db-subnet-group \
    --region $REGION \
    --db-subnet-group-name "${PROJECT_NAME}-subnet-group" \
    --db-subnet-group-description "Subnet group for ${PROJECT_NAME} DocumentDB" \
    --subnet-ids ${SUBNET_IDS[*]} 2>/dev/null || warn "Subnet group già esistente"

# 6. Crea DocumentDB Cluster
log "Creazione DocumentDB Cluster..."
DOCDB_PASSWORD="Password123!"
aws docdb create-db-cluster \
    --region $REGION \
    --db-cluster-identifier "${PROJECT_NAME}-docdb-cluster" \
    --engine docdb \
    --master-username root \
    --master-user-password $DOCDB_PASSWORD \
    --db-subnet-group-name "${PROJECT_NAME}-subnet-group" \
    --vpc-security-group-ids $SECURITY_GROUP_ID \
    --backup-retention-period 7 \
    --preferred-backup-window "03:00-04:00" \
    --preferred-maintenance-window "sun:04:00-sun:05:00" \
    --engine-version "4.0.0" 2>/dev/null || warn "Cluster DocumentDB già esistente"

# 7. Crea DocumentDB Instance
log "Creazione DocumentDB Instance..."
aws docdb create-db-instance \
    --region $REGION \
    --db-instance-identifier "${PROJECT_NAME}-docdb-instance" \
    --db-instance-class db.t3.medium \
    --engine docdb \
    --db-cluster-identifier "${PROJECT_NAME}-docdb-cluster" 2>/dev/null || warn "Istanza DocumentDB già esistente"

# Attendi che il cluster sia disponibile
log "Attendo che DocumentDB sia disponibile..."
#aws docdb wait db-cluster-available --region $REGION --db-cluster-identifier "${PROJECT_NAME}-docdb-cluster"
aws docdb wait db-instance-available --region $REGION --db-instance-identifier "${PROJECT_NAME}-docdb-instance"

# Ottieni l'endpoint DocumentDB
DOCDB_ENDPOINT=$(aws docdb describe-db-clusters \
    --region $REGION \
    --db-cluster-identifier "${PROJECT_NAME}-docdb-cluster" \
    --query 'DBClusters[0].Endpoint' --output text)
log "DocumentDB Endpoint: $DOCDB_ENDPOINT"

# 8. Crea ECS Cluster
log "Creazione ECS Cluster..."
aws ecs create-cluster \
    --region $REGION \
    --cluster-name $CLUSTER_NAME \
    --capacity-providers FARGATE \
    --default-capacity-provider-strategy capacityProvider=FARGATE,weight=1 2>/dev/null || warn "Cluster ECS già esistente"

# 9. Crea IAM Role per ECS Task
log "Creazione IAM Role per ECS Task..."
TASK_ROLE_NAME="${PROJECT_NAME}-task-role"
EXECUTION_ROLE_NAME="${PROJECT_NAME}-execution-role"

# Task Role
aws iam create-role \
    --role-name $TASK_ROLE_NAME \
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Principal": {
                    "Service": "ecs-tasks.amazonaws.com"
                },
                "Action": "sts:AssumeRole"
            }
        ]
    }' 2>/dev/null || warn "Task role già esistente"

# Execution Role
aws iam create-role \
    --role-name $EXECUTION_ROLE_NAME \
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [
            {
                "Effect": "Allow",
                "Principal": {
                    "Service": "ecs-tasks.amazonaws.com"
                },
                "Action": "sts:AssumeRole"
            }
        ]
    }' 2>/dev/null || warn "Execution role già esistente"

# Attach policy per DynamoDB
aws iam attach-role-policy \
    --role-name $TASK_ROLE_NAME \
    --policy-arn arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess 2>/dev/null || warn "Policy DynamoDB già associata"

# Attach policy per ECS Task Execution
aws iam attach-role-policy \
    --role-name $EXECUTION_ROLE_NAME \
    --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy 2>/dev/null || warn "Policy ECS già associata"

aws iam attach-role-policy \
    --role-name $EXECUTION_ROLE_NAME \
    --policy-arn arn:aws:iam::aws:policy/CloudWatchFullAccess 2>/dev/null || warn "Policy CloudWatch-ECS già associata"


# Ottieni ARN dei ruoli
TASK_ROLE_ARN=$(aws iam get-role --role-name $TASK_ROLE_NAME --query 'Role.Arn' --output text)
EXECUTION_ROLE_ARN=$(aws iam get-role --role-name $EXECUTION_ROLE_NAME --query 'Role.Arn' --output text)

log "Task Role ARN: $TASK_ROLE_ARN"
log "Execution Role ARN: $EXECUTION_ROLE_ARN"

# 10. Crea Task Definition
log "Creazione ECS Task Definition..."
cat > task-definition.json << EOF
{
    "family": "$TASK_DEFINITION_NAME",
    "networkMode": "awsvpc",
    "requiresCompatibilities": ["FARGATE"],
    "cpu": "256",
    "memory": "512",
    "executionRoleArn": "$EXECUTION_ROLE_ARN",
    "taskRoleArn": "$TASK_ROLE_ARN",
    "containerDefinitions": [
        {
            "name": "$PROJECT_NAME",
            "image": "$REPOSITORY_URI:latest",
            "portMappings": [
                {
                    "containerPort": 8080,
                    "protocol": "tcp"
                }
            ],
            "environment": [
                {
                    "name": "SPRING_PROFILES_ACTIVE",
                    "value": "aws"
                },
                {
                    "name": "AWS_REGION",
                    "value": "$REGION"
                },
                {
                    "name": "DYNAMODB_TABLE_NAME",
                    "value": "${PROJECT_NAME}-table"
                },
                {
                    "name": "MONGO_HOST",
                    "value": "$DOCDB_ENDPOINT"
                },
                {
                    "name": "MONGO_PORT",
                    "value": "27017"
                },
                {
                    "name": "MONGO_USERNAME",
                    "value": "root"
                },
                {
                    "name": "MONGO_PASSWORD",
                    "value": "$DOCDB_PASSWORD"
                },
                {
                    "name": "MONGO_DATABASE",
                    "value": "microservice_db"
                }
            ],
            "logConfiguration": {
                "logDriver": "awslogs",
                "options": {
                    "awslogs-group": "/ecs/${PROJECT_NAME}",
                    "awslogs-region": "$REGION",
                    "awslogs-stream-prefix": "ecs",
                    "awslogs-create-group": "true"
                }
            },
            "healthCheck": {
                "command": [
                    "CMD-SHELL",
                    "curl http://localhost:8080/actuator/health || exit 1"
                ],
                "interval": 30,
                "timeout": 10,
                "retries": 5
            },
            "essential": true
        }
    ]
}
EOF

aws ecs register-task-definition \
    --region $REGION \
    --cli-input-json file://task-definition.json

log "Task Definition registrata ✓"

# 11. Crea ECS Service
log "Creazione ECS Service..."
aws ecs create-service \
    --region $REGION \
    --cluster $CLUSTER_NAME \
    --service-name $SERVICE_NAME \
    --task-definition $TASK_DEFINITION_NAME \
    --desired-count 1 \
    --launch-type FARGATE \
    --network-configuration "awsvpcConfiguration={subnets=[${SUBNET_IDS[0]}],securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=ENABLED}" 2>/dev/null || {
        log "Aggiornamento del servizio esistente..."
        aws ecs update-service \
            --region $REGION \
            --cluster $CLUSTER_NAME \
            --service $SERVICE_NAME \
            --task-definition $TASK_DEFINITION_NAME \
            --desired-count 1
    }

# 12. Attendi che il servizio sia stabile
log "Attendo che il servizio ECS sia stabile..."
aws ecs wait services-stable --region $REGION --cluster $CLUSTER_NAME --services $SERVICE_NAME

# 13. Ottieni informazioni sul servizio
log "Recupero informazioni sul servizio..."
TASK_ARN=$(aws ecs list-tasks \
    --region $REGION \
    --cluster $CLUSTER_NAME \
    --service-name $SERVICE_NAME \
    --query 'taskArns[0]' --output text)

if [ "$TASK_ARN" != "None" ] && [ -n "$TASK_ARN" ]; then
    PUBLIC_IP=$(aws ecs describe-tasks \
        --region $REGION \
        --cluster $CLUSTER_NAME \
        --tasks $TASK_ARN \
        --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' --output text | xargs -I {} aws ec2 describe-network-interfaces --region $REGION --network-interface-ids {} --query 'NetworkInterfaces[0].Association.PublicIp' --output text)
    
    if [ "$PUBLIC_IP" != "None" ] && [ -n "$PUBLIC_IP" ]; then
        log "IP pubblico del servizio: $PUBLIC_IP"
        log "URL del servizio: http://$PUBLIC_IP:8080"
    fi
fi

# Cleanup
rm -f task-definition.json

echo -e "${GREEN}🎉 Deploy completato con successo!${NC}"
echo
echo -e "${YELLOW}Riepilogo risorse create:${NC}"
echo "- ECR Repository: $REPOSITORY_URI"
echo "- Security Group: $SECURITY_GROUP_ID"
echo "- DynamoDB Table: ${PROJECT_NAME}-table"
echo "- DocumentDB Cluster: ${PROJECT_NAME}-docdb-cluster"
echo "- DocumentDB Endpoint: $DOCDB_ENDPOINT"
echo "- ECS Cluster: $CLUSTER_NAME"
echo "- ECS Service: $SERVICE_NAME"
echo
echo -e "${YELLOW}Credenziali DocumentDB:${NC}"
echo "- Username: root"
echo "- Password: $DOCDB_PASSWORD"
echo
echo -e "${YELLOW}Per verificare lo stato del servizio:${NC}"
echo "aws ecs describe-services --region $REGION --cluster $CLUSTER_NAME --services $SERVICE_NAME"
echo
echo -e "${YELLOW}Per vedere i log:${NC}"
echo "aws logs tail /ecs/${PROJECT_NAME} --region $REGION --follow"
echo "--------------------------"xc 