#!/bin/bash
# Script di provisioning e deploy completo su AWS ECS Fargate per il microservizio gestioneannotazioni
# Richiede: AWS CLI configurata, permessi su ECS, ECR, RDS, DynamoDB, IAM, VPC
# Esegue: build/push immagine, creazione risorse, deploy ECS, attese, init DB

export AWS_PAGER=""

# Funzione helper per gestire errori di risorse già esistenti
safe_run() {
  local description=$1
  shift
  if eval "$@" &>/dev/null; then
    echo "✓ $description"
    return 0
  else
    local exit_code=$?
    echo "⊘ $description (ignorato)"
    return 0
  fi
}


# === CONFIGURAZIONE ===
AWS_REGION="eu-central-1"
# Tag comuni a tutte le risorse (Name, Environment, Project, Owner, CostCenter, ManagedBy): vedi script/aws-tags.sh
source "$(dirname "$0")/../aws-tags.sh" aws-ecs
ECR_REPO_NAME="gestioneannotazioni"
IMAGE_TAG="latest"
CLUSTER_NAME="gestioneannotazioni-cluster"
SERVICE_NAME="gestioneannotazioni-service"
TASK_FAMILY="gestioneannotazioni-task"
CONTAINER_NAME="gestioneannotazioni"
RDS_DB_ID="gestioneannotazioni-db"
DYNAMODB_TABLE="annotazioni"
DYNAMODB_TABLE2="annotazioni_storico"

AURORA_CLUSTER_ID="gestioneannotazioni-aurora-cluster"
AURORA_DB_NAME="gestioneannotazioni"
AURORA_MASTER_USER="gestioneannotazioni_user"
AURORA_MASTER_PASS="gestioneannotazioni_pass"
AURORA_INSTANCE_ID="gestioneannotazioni-aurora-instance"
AURORA_ENGINE="aurora-mysql"
AURORA_ENGINE_VER="5.7.mysql_aurora.2.11.4"
AURORA_INSTANCE_CLASS="db.t3.medium"

SQS_EXPORT_QUEUE_NAME="gestioneannotazioni-annotazioni-export"
SQS_EXPORT_QUEUE_URL="https://sqs.$AWS_REGION.amazonaws.com/000000000000/$SQS_EXPORT_QUEUE_NAME"
SQS_IMPORT_QUEUE_NAME="gestioneannotazioni-annotazioni-import"
SQS_IMPORT_QUEUE_URL="https://sqs.$AWS_REGION.amazonaws.com/000000000000/$SQS_IMPORT_QUEUE_NAME"

VPC_ID="" # lasciato vuoto per usare la default VPC
SUBNETS="" # verrà popolato dallo script
SECURITY_GROUP_ID="" # verrà creato


# === 1. Build e push immagine su ECR ===
echo "[1/7] Build e push immagine Docker su ECR..."
if ! aws ecr describe-repositories --repository-names "$ECR_REPO_NAME" --region $AWS_REGION > /dev/null 2>&1; then
  safe_run "Creazione ECR repository" "aws ecr create-repository --repository-name \"$ECR_REPO_NAME\" --region $AWS_REGION --tags $(aws_tags_kv $ECR_REPO_NAME)"
else
  echo "✓ ECR repository già esistente."
fi
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_URL="$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_REPO_NAME"
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_URL
docker build -t $ECR_REPO_NAME:$IMAGE_TAG .
docker tag $ECR_REPO_NAME:$IMAGE_TAG $ECR_URL:$IMAGE_TAG
docker push $ECR_URL:$IMAGE_TAG

# === 1b. Creazione IAM Role per ECS Task ===
echo "[1b/7] Creazione IAM Role per ECS Task..."
TASK_ROLE_NAME="gestioneannotazioni-ecs-task-role"
TASK_ROLE_ARN=""
POLICY_ARN="arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
RDS_POLICY_ARN="arn:aws:iam::aws:policy/AmazonRDSFullAccess"
SQS_POLICY_ARN="arn:aws:iam::aws:policy/AmazonSQSFullAccess"

if ! aws iam get-role --role-name $TASK_ROLE_NAME --region $AWS_REGION > /dev/null 2>&1; then
  TASK_ROLE_ARN=$(aws iam create-role \
    --role-name $TASK_ROLE_NAME \
    --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ecs-tasks.amazonaws.com"},"Action":"sts:AssumeRole"}]}' \
    --region $AWS_REGION \
    --query 'Role.Arn' --output text)
  safe_run "Attach policy AmazonDynamoDBFullAccess" "aws iam attach-role-policy --role-name $TASK_ROLE_NAME --policy-arn $POLICY_ARN --region $AWS_REGION"
  safe_run "Attach policy AmazonRDSFullAccess" "aws iam attach-role-policy --role-name $TASK_ROLE_NAME --policy-arn $RDS_POLICY_ARN --region $AWS_REGION"
  safe_run "Attach policy AmazonSQSFullAccess" "aws iam attach-role-policy --role-name $TASK_ROLE_NAME --policy-arn $SQS_POLICY_ARN --region $AWS_REGION"
  echo "✓ IAM Task Role creato: $TASK_ROLE_ARN"
else
  TASK_ROLE_ARN=$(aws iam get-role --role-name $TASK_ROLE_NAME --region $AWS_REGION --query 'Role.Arn' --output text)
  echo "✓ IAM Task Role già esistente: $TASK_ROLE_ARN"
fi

# === 1c. Creazione ECS Execution Role ===
echo "[1c/7] Creazione ECS Execution Role..."
EXEC_ROLE_NAME="gestioneannotazioni-ecs-execution-role"
EXEC_ROLE_ARN=""
EXEC_POLICY_ARN="arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
LOGS_POLICY_ARN="arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
SQS_POLICY_ARN="arn:aws:iam::aws:policy/AmazonSQSFullAccess"

if ! aws iam get-role --role-name $EXEC_ROLE_NAME --region $AWS_REGION > /dev/null 2>&1; then
  EXEC_ROLE_ARN=$(aws iam create-role \
    --role-name $EXEC_ROLE_NAME \
    --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ecs-tasks.amazonaws.com"},"Action":"sts:AssumeRole"}]}' \
    --region $AWS_REGION \
    --query 'Role.Arn' --output text)
  safe_run "Attach policy AmazonECSTaskExecutionRolePolicy" "aws iam attach-role-policy --role-name $EXEC_ROLE_NAME --policy-arn $EXEC_POLICY_ARN --region $AWS_REGION"
  safe_run "Attach policy CloudWatchLogsFullAccess" "aws iam attach-role-policy --role-name $EXEC_ROLE_NAME --policy-arn $LOGS_POLICY_ARN --region $AWS_REGION"
  safe_run "Attach policy AmazonSQSFullAccess" "aws iam attach-role-policy --role-name $EXEC_ROLE_NAME --policy-arn $SQS_POLICY_ARN --region $AWS_REGION"
  echo "✓ IAM Execution Role creato: $EXEC_ROLE_ARN"
else
  EXEC_ROLE_ARN=$(aws iam get-role --role-name $EXEC_ROLE_NAME --region $AWS_REGION --query 'Role.Arn' --output text)
  echo "✓ IAM Execution Role già esistente: $EXEC_ROLE_ARN"
  # Assicura che la policy CloudWatchLogs sia attaccata
  if ! aws iam list-attached-role-policies --role-name $EXEC_ROLE_NAME --region $AWS_REGION --query 'AttachedPolicies[*].PolicyArn' --output text | grep -q $LOGS_POLICY_ARN; then
    safe_run "Attach policy CloudWatchLogsFullAccess" "aws iam attach-role-policy --role-name $EXEC_ROLE_NAME --policy-arn $LOGS_POLICY_ARN --region $AWS_REGION"
  fi
fi

# Tag IAM Role (Task e Execution)
safe_run "Tag IAM Task Role" "aws iam tag-role --role-name $TASK_ROLE_NAME --tags $(aws_tags_kv $TASK_ROLE_NAME) --region $AWS_REGION"
safe_run "Tag IAM Execution Role" "aws iam tag-role --role-name $EXEC_ROLE_NAME --tags $(aws_tags_kv $EXEC_ROLE_NAME) --region $AWS_REGION"

# === 2. Preparazione networking (deve essere prima di Aurora) ===
echo "[2/7] Preparazione networking..."
VPC_ID=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --region $AWS_REGION --query 'Vpcs[0].VpcId' --output text)
SUBNETS=$(aws ec2 describe-subnets --filters Name=vpc-id,Values=$VPC_ID --region $AWS_REGION --query 'Subnets[*].SubnetId' --output text | tr '\t' ',')

# Verifica se il Security Group esiste già
SG_ID=$(aws ec2 describe-security-groups --filters Name=group-name,Values=gestioneannotazioni-sg Name=vpc-id,Values=$VPC_ID --region $AWS_REGION --query 'SecurityGroups[0].GroupId' --output text 2>/dev/null)

if [ "$SG_ID" == "None" ] || [ -z "$SG_ID" ]; then
  echo "Creazione Security Group..."
  SECURITY_GROUP_ID=$(aws ec2 create-security-group --group-name gestioneannotazioni-sg --description "gestioneannotazioni ECS SG" --vpc-id $VPC_ID --region $AWS_REGION --query 'GroupId' --output text)
  safe_run "Tag Security Group" "aws ec2 create-tags --resources $SECURITY_GROUP_ID --tags $(aws_tags_kv gestioneannotazioni-sg) Key=$TAG_MARKER_KEY,Value=true --region $AWS_REGION"

  # Regole di sicurezza: apri solo le porte necessarie
  safe_run "Regola porta 8080" "aws ec2 authorize-security-group-ingress --group-id $SECURITY_GROUP_ID --protocol tcp --port 8080 --cidr 0.0.0.0/0 --region $AWS_REGION"
  safe_run "Regola porta 3306" "aws ec2 authorize-security-group-ingress --group-id $SECURITY_GROUP_ID --protocol tcp --port 3306 --source-group $SECURITY_GROUP_ID --region $AWS_REGION"
  safe_run "Regola porta 443" "aws ec2 authorize-security-group-ingress --group-id $SECURITY_GROUP_ID --protocol tcp --port 443 --cidr 0.0.0.0/0 --region $AWS_REGION"
  safe_run "Regola porta 22" "aws ec2 authorize-security-group-ingress --group-id $SECURITY_GROUP_ID --protocol tcp --port 22 --cidr 0.0.0.0/0 --region $AWS_REGION"
else
  SECURITY_GROUP_ID=$SG_ID
  echo "✓ Security Group già esistente: $SECURITY_GROUP_ID"
fi

# === 3. Provisioning RDS MySQL (Aurora) ===
echo "[3/7] Provisioning RDS MySQL (Aurora)..."

# Crea Aurora cluster se non esiste
echo "Verifica esistenza Aurora cluster..."
if ! aws rds describe-db-clusters --db-cluster-identifier $AURORA_CLUSTER_ID --region $AWS_REGION > /dev/null 2>&1; then
  aws rds create-db-cluster \
    --db-cluster-identifier $AURORA_CLUSTER_ID \
    --engine $AURORA_ENGINE \
    --engine-version $AURORA_ENGINE_VER \
    --master-username $AURORA_MASTER_USER \
    --master-user-password $AURORA_MASTER_PASS \
    --database-name $AURORA_DB_NAME \
    --vpc-security-group-ids $SECURITY_GROUP_ID \
    --region $AWS_REGION \
    --tags $(aws_tags_kv $AURORA_CLUSTER_ID)
  echo "✓ Aurora cluster creato"
else
  echo "✓ Aurora cluster già esistente."
fi

# Crea Aurora instance se non esiste
echo "Verifica esistenza Aurora instance..."
if ! aws rds describe-db-instances --db-instance-identifier $AURORA_INSTANCE_ID --region $AWS_REGION > /dev/null 2>&1; then
  aws rds create-db-instance \
    --db-instance-identifier $AURORA_INSTANCE_ID \
    --db-cluster-identifier $AURORA_CLUSTER_ID \
    --engine $AURORA_ENGINE \
    --db-instance-class $AURORA_INSTANCE_CLASS \
    --region $AWS_REGION \
    --tags $(aws_tags_kv $AURORA_INSTANCE_ID)
  echo "✓ Aurora instance creata"
else
  echo "✓ Aurora instance già esistente."
fi
# Attendi che il cluster sia disponibile
echo "Attendo che Aurora sia disponibile..."
max_attempts=60
attempt=0
while [ $attempt -lt $max_attempts ]; do
  STATUS=$(aws rds describe-db-clusters --db-cluster-identifier $AURORA_CLUSTER_ID --region $AWS_REGION --query 'DBClusters[0].Status' --output text 2>/dev/null)
  echo "Stato Aurora: $STATUS (tentativo $((attempt+1))/$max_attempts)"
  if [[ "$STATUS" == "available" ]]; then 
    echo "✓ Aurora disponibile"
    break
  fi
  sleep 20
  attempt=$((attempt+1))
done

# Attendi anche che l'instance sia disponibile
echo "Attendo che Aurora instance sia disponibile..."
attempt=0
while [ $attempt -lt $max_attempts ]; do
  INSTANCE_STATUS=$(aws rds describe-db-instances --db-instance-identifier $AURORA_INSTANCE_ID --region $AWS_REGION --query 'DBInstances[0].DBInstanceStatus' --output text 2>/dev/null)
  echo "Stato Aurora instance: $INSTANCE_STATUS (tentativo $((attempt+1))/$max_attempts)"
  if [[ "$INSTANCE_STATUS" == "available" ]]; then 
    echo "✓ Aurora instance disponibile"
    break
  fi
  sleep 20
  attempt=$((attempt+1))
done

# Recupera endpoint
aurora_endpoint=$(aws rds describe-db-clusters --db-cluster-identifier $AURORA_CLUSTER_ID --region $AWS_REGION --query 'DBClusters[0].Endpoint' --output text)
echo "Aurora endpoint: $aurora_endpoint"
echo "Aurora user: $AURORA_MASTER_USER"
echo "Aurora password: $AURORA_MASTER_PASS"

# Verifica che Aurora sia raggiungibile (debug)
echo "=== DEBUG: Verifica configurazione Aurora ==="
aws rds describe-db-clusters --db-cluster-identifier $AURORA_CLUSTER_ID --region $AWS_REGION --query 'DBClusters[0].{Status:Status,Endpoint:Endpoint,Port:Port,VpcSecurityGroups:VpcSecurityGroups}' --output table
aws rds describe-db-instances --db-instance-identifier $AURORA_INSTANCE_ID --region $AWS_REGION --query 'DBInstances[0].{Status:DBInstanceStatus,Endpoint:Endpoint.Address}' --output table

# Verifica Security Group
echo "=== DEBUG: Verifica Security Group ==="
aws ec2 describe-security-groups --group-ids $SECURITY_GROUP_ID --region $AWS_REGION --query 'SecurityGroups[0].{GroupId:GroupId,IpPermissions:IpPermissions}' --output table

# Test connettività network se possibile
echo "=== DEBUG: Test rete ==="
echo "Aurora endpoint: $aurora_endpoint"
echo "Aurora port: 3306"
echo "Security Group ID: $SECURITY_GROUP_ID"
echo "VPC ID: $VPC_ID"

# Verifica che il Security Group di Aurora sia corretto
AURORA_SG=$(aws rds describe-db-clusters --db-cluster-identifier $AURORA_CLUSTER_ID --region $AWS_REGION --query 'DBClusters[0].VpcSecurityGroups[0].VpcSecurityGroupId' --output text)
echo "Aurora Security Group: $AURORA_SG"
if [ "$AURORA_SG" != "$SECURITY_GROUP_ID" ]; then
  echo "ATTENZIONE: Aurora ha Security Group diverso da quello atteso!"
  echo "Modifico Security Group di Aurora..."
  aws rds modify-db-cluster --db-cluster-identifier $AURORA_CLUSTER_ID --vpc-security-group-ids $SECURITY_GROUP_ID --region $AWS_REGION
  echo "Attendo modifica Security Group..."
  sleep 30
fi

# === 4. Provisioning DynamoDB ===
echo "[4/7] Provisioning DynamoDB..."
if ! aws dynamodb describe-table --table-name $DYNAMODB_TABLE --region $AWS_REGION > /dev/null 2>&1; then
  aws dynamodb create-table \
    --table-name $DYNAMODB_TABLE \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region $AWS_REGION \
    --tags $(aws_tags_kv $DYNAMODB_TABLE)
  echo "✓ Tabella DynamoDB $DYNAMODB_TABLE creata"
else
  echo "✓ Tabella DynamoDB $DYNAMODB_TABLE già esistente."
fi

if ! aws dynamodb describe-table --table-name $DYNAMODB_TABLE2 --region $AWS_REGION > /dev/null 2>&1; then
  aws dynamodb create-table \
    --table-name $DYNAMODB_TABLE2 \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region $AWS_REGION \
    --tags $(aws_tags_kv $DYNAMODB_TABLE2)
  echo "✓ Tabella DynamoDB $DYNAMODB_TABLE2 creata"
else
  echo "✓ Tabella DynamoDB $DYNAMODB_TABLE2 già esistente."
fi

# Crea tabella annotazioni_storicoStati con GSI
if ! aws dynamodb describe-table --table-name annotazioni_storicoStati --region $AWS_REGION > /dev/null 2>&1; then
  aws dynamodb create-table \
    --table-name annotazioni_storicoStati \
    --attribute-definitions \
      AttributeName=idOperazione,AttributeType=S \
      AttributeName=idAnnotazione,AttributeType=S \
      AttributeName=dataModifica,AttributeType=S \
    --key-schema AttributeName=idOperazione,KeyType=HASH \
    --global-secondary-indexes '[
      {
        "IndexName": "idAnnotazione-index",
        "KeySchema": [
          {"AttributeName":"idAnnotazione","KeyType":"HASH"},
          {"AttributeName":"dataModifica","KeyType":"RANGE"}
        ],
        "Projection": {"ProjectionType":"ALL"},
        "ProvisionedThroughput": {"ReadCapacityUnits":5,"WriteCapacityUnits":5}
      }
    ]' \
    --billing-mode PROVISIONED \
    --provisioned-throughput ReadCapacityUnits=5,WriteCapacityUnits=5 \
    --tags $(aws_tags_kv annotazioni_storicoStati) \
    --region "$AWS_REGION"
  echo "✓ Tabella DynamoDB annotazioni_storicoStati creata"
else
  echo "✓ Tabella DynamoDB annotazioni_storicoStati già esistente."
fi

# 4. Crea coda SQS con tag
echo "Creazione coda SQS: $SQS_EXPORT_QUEUE_NAME"
SQS_EXPORT_QUEUE_URL=$(aws sqs create-queue \
  --queue-name $SQS_EXPORT_QUEUE_NAME \
  --region $AWS_REGION \
  --query 'QueueUrl' --output text 2>/dev/null) || {
  SQS_EXPORT_QUEUE_URL=$(aws sqs get-queue-url --queue-name $SQS_EXPORT_QUEUE_NAME --region $AWS_REGION --query 'QueueUrl' --output text 2>/dev/null)
}

if [ -n "$SQS_EXPORT_QUEUE_URL" ] && [ "$SQS_EXPORT_QUEUE_URL" != "None" ]; then
  safe_run "Tag coda SQS export" "aws sqs tag-queue --queue-url \"$SQS_EXPORT_QUEUE_URL\" --tags \"$(aws_tags_map $SQS_EXPORT_QUEUE_NAME)\" --region $AWS_REGION"
  echo "✓ SQS Queue URL: $SQS_EXPORT_QUEUE_URL"
else
  echo "⊘ Impossibile creare o trovare la coda SQS export"
fi

# 4b. Crea coda SQS di import con tag
echo "Creazione coda SQS di import: $SQS_IMPORT_QUEUE_NAME"
SQS_IMPORT_QUEUE_URL=$(aws sqs create-queue \
  --queue-name $SQS_IMPORT_QUEUE_NAME \
  --region $AWS_REGION \
  --query 'QueueUrl' --output text 2>/dev/null) || {
  SQS_IMPORT_QUEUE_URL=$(aws sqs get-queue-url --queue-name $SQS_IMPORT_QUEUE_NAME --region $AWS_REGION --query 'QueueUrl' --output text 2>/dev/null)
}

if [ -n "$SQS_IMPORT_QUEUE_URL" ] && [ "$SQS_IMPORT_QUEUE_URL" != "None" ]; then
  safe_run "Tag coda SQS import" "aws sqs tag-queue --queue-url \"$SQS_IMPORT_QUEUE_URL\" --tags \"$(aws_tags_map $SQS_IMPORT_QUEUE_NAME)\" --region $AWS_REGION"
  echo "✓ SQS Import Queue URL: $SQS_IMPORT_QUEUE_URL"
else
  echo "⊘ Impossibile creare o trovare la coda SQS import"
fi

# 4c. Crea subnet group per ElastiCache (richiesto per creare il cluster)
CACHE_SUBNET_GROUP_NAME="gestioneannotazioni-redis-subnet-group"
SUBNET_IDS=$(aws ec2 describe-subnets --region $AWS_REGION --filters "Name=vpc-id,Values=$VPC_ID" --query 'Subnets[*].SubnetId' --output text)

if ! aws elasticache describe-cache-subnet-groups --cache-subnet-group-name $CACHE_SUBNET_GROUP_NAME --region $AWS_REGION > /dev/null 2>&1; then
  safe_run "Creazione ElastiCache subnet group" "aws elasticache create-cache-subnet-group --cache-subnet-group-name $CACHE_SUBNET_GROUP_NAME --cache-subnet-group-description \"Subnet group for gestioneannotazioni Redis\" --subnet-ids $SUBNET_IDS --region $AWS_REGION --tags $(aws_tags_kv $CACHE_SUBNET_GROUP_NAME)"
  echo "✓ Cache subnet group creato"
else
  echo "✓ Cache subnet group già esistente"
fi

# 4c. Crea ElastiCache Redis cluster
REDIS_CLUSTER_ID="gestioneannotazioni-redis"
if ! aws elasticache describe-cache-clusters --cache-cluster-id $REDIS_CLUSTER_ID --region $AWS_REGION > /dev/null 2>&1; then
  safe_run "Creazione ElastiCache Redis cluster" "aws elasticache create-cache-cluster --cache-cluster-id $REDIS_CLUSTER_ID --engine redis --cache-node-type cache.t3.micro --num-cache-nodes 1 --cache-subnet-group-name $CACHE_SUBNET_GROUP_NAME --security-group-ids $SECURITY_GROUP_ID --region $AWS_REGION --tags $(aws_tags_kv $REDIS_CLUSTER_ID)"
  echo "✓ Redis cluster creato"
else
  echo "✓ Redis cluster già esistente"
fi

# Aggiungi regola porta Redis (6379) al security group
safe_run "Autorizzazione porta 6379" "aws ec2 authorize-security-group-ingress --group-id $SECURITY_GROUP_ID --protocol tcp --port 6379 --source-group $SECURITY_GROUP_ID --region $AWS_REGION"

# Attendi che Redis sia disponibile
echo "Attendo che ElastiCache Redis sia disponibile (può richiedere 5-10 minuti)..."
max_attempts=40
attempt=0
while [ $attempt -lt $max_attempts ]; do
  if aws elasticache describe-cache-clusters --cache-cluster-id $REDIS_CLUSTER_ID --region $AWS_REGION --query 'CacheClusters[0].CacheClusterStatus' --output text 2>/dev/null | grep -q "available"; then
    echo "✓ ElastiCache Redis disponibile"
    break
  fi
  sleep 15
  attempt=$((attempt+1))
done

# Recupera endpoint Redis
REDIS_ENDPOINT=$(aws elasticache describe-cache-clusters \
  --cache-cluster-id $REDIS_CLUSTER_ID \
  --show-cache-node-info \
  --region $AWS_REGION \
  --query 'CacheClusters[0].CacheNodes[0].Endpoint.Address' \
  --output text 2>/dev/null)
REDIS_PORT=$(aws elasticache describe-cache-clusters \
  --cache-cluster-id $REDIS_CLUSTER_ID \
  --show-cache-node-info \
  --region $AWS_REGION \
  --query 'CacheClusters[0].CacheNodes[0].Endpoint.Port' \
  --output text 2>/dev/null)

echo "Redis endpoint: $REDIS_ENDPOINT:$REDIS_PORT"

# === 5. Creazione cluster ECS ===
echo "[5/7] Creazione ECS Cluster..."
CLUSTER_STATUS=$(aws ecs describe-clusters --clusters $CLUSTER_NAME --region $AWS_REGION --query 'clusters[0].status' --output text 2>/dev/null)

if [ "$CLUSTER_STATUS" != "ACTIVE" ]; then
  if [ "$CLUSTER_STATUS" == "None" ] || [ -z "$CLUSTER_STATUS" ]; then
    echo "Creazione del cluster ECS: $CLUSTER_NAME"
    aws ecs create-cluster --cluster-name $CLUSTER_NAME --region $AWS_REGION \
      --tags "$(aws_tags_json $CLUSTER_NAME)"
    echo "✓ Cluster ECS creato"
  fi
else
  echo "✓ ECS Cluster $CLUSTER_NAME già esistente e attivo."
fi

# === 6. Definizione task ECS Fargate ===
echo "[6/7] Definizione task ECS Fargate..."
LOG_GROUP_NAME="/ecs/gestioneannotazioni-app"
safe_run "Creazione CloudWatch Log Group" "aws logs create-log-group --log-group-name $LOG_GROUP_NAME --tags $(aws_tags_map $LOG_GROUP_NAME) --region $AWS_REGION"

rm -f ./script/aws-ecs/task-def.json
cat > ./script/aws-ecs/task-def.json <<EOF
{
  "family": "$TASK_FAMILY",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "taskRoleArn": "$TASK_ROLE_ARN",
  "executionRoleArn": "$EXEC_ROLE_ARN",
  "tags": $(aws_tags_json $TASK_FAMILY),
  "containerDefinitions": [
    {
      "name": "$CONTAINER_NAME",
      "image": "$ECR_URL:$IMAGE_TAG",
      "portMappings": [
        { "containerPort": 8080, "protocol": "tcp" }
      ],
      "environment": [
        { "name": "AWS_ACCESS_KEY_ID", "value": "" },
        { "name": "AWS_SECRET_ACCESS_KEY", "value": "" },
        { "name": "SPRING_PROFILES_ACTIVE", "value": "aws" },
        { "name": "AWS_REGION", "value": "$AWS_REGION" },
        { "name": "AWS_RDS_URL", "value": "jdbc:mysql://$aurora_endpoint:3306/$AURORA_DB_NAME" },
        { "name": "AWS_RDS_USERNAME", "value": "$AURORA_MASTER_USER" },
        { "name": "AWS_RDS_PASSWORD", "value": "$AURORA_MASTER_PASS" },
        { "name": "RDS_HOST", "value": "$aurora_endpoint" },
        { "name": "RDS_PORT", "value": "3306" },
        { "name": "RDS_DATABASE", "value": "$AURORA_DB_NAME" },
        { "name": "RDS_USERNAME", "value": "$AURORA_MASTER_USER" },
        { "name": "RDS_PASSWORD", "value": "$AURORA_MASTER_PASS" },
        { "name": "SQS_EXPORT_QUEUE_URL", "value": "$SQS_EXPORT_QUEUE_URL" },
        { "name": "SQS_IMPORT_QUEUE_URL", "value": "$SQS_IMPORT_QUEUE_URL" },
        { "name": "REDIS_HOST", "value": "$REDIS_ENDPOINT" },
        { "name": "REDIS_PORT", "value": "$REDIS_PORT" }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "$LOG_GROUP_NAME",
          "awslogs-region": "$AWS_REGION",
          "awslogs-stream-prefix": "ecs"
        }
      }
    }
  ]
}
EOF

aws ecs register-task-definition --cli-input-json file://script/aws-ecs/task-def.json --region $AWS_REGION > /dev/null
echo "✓ Task Definition registrata"

# === 7. Deploy servizio ECS Fargate ===
echo "[7/7] Deploy servizio ECS Fargate..."
TASK_DEF_ARN=$(aws ecs describe-task-definition --task-definition $TASK_FAMILY --region $AWS_REGION --query 'taskDefinition.taskDefinitionArn' --output text 2>/dev/null)

if [ -z "$TASK_DEF_ARN" ] || [ "$TASK_DEF_ARN" == "None" ]; then
  echo "⊘ Task definition $TASK_FAMILY non trovata (continuo comunque)"
else
  SERVICE_STATUS=$(aws ecs describe-services --cluster $CLUSTER_NAME --services $SERVICE_NAME --region $AWS_REGION --query 'services[0].status' --output text 2>/dev/null)
  
  if [ "$SERVICE_STATUS" != "ACTIVE" ]; then
    if [ "$SERVICE_STATUS" == "None" ] || [ -z "$SERVICE_STATUS" ]; then
      echo "Creazione del servizio ECS: $SERVICE_NAME"
      aws ecs create-service \
        --cluster $CLUSTER_NAME \
        --service-name $SERVICE_NAME \
        --task-definition $TASK_DEF_ARN \
        --desired-count 1 \
        --launch-type FARGATE \
        --network-configuration "awsvpcConfiguration={subnets=[$SUBNETS],securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=ENABLED}" \
        --region $AWS_REGION \
        --tags "$(aws_tags_json $SERVICE_NAME)" \
        --propagate-tags SERVICE > /dev/null
      echo "✓ Servizio ECS creato"
    fi
  else
    echo "✓ ECS Service $SERVICE_NAME già esistente e attivo."
  fi
fi


# === 8. Output accesso ===
echo "[8/8] Servizio avviato. Recupero endpoint pubblico..."

# Attendi che almeno un task sia running
echo "Attendo che il task ECS sia in stato RUNNING..."
for i in {1..20}; do
  TASK_ARN=$(aws ecs list-tasks --cluster $CLUSTER_NAME --service-name $SERVICE_NAME --desired-status RUNNING --region $AWS_REGION --query 'taskArns[0]' --output text 2>/dev/null)
  if [ "$TASK_ARN" != "None" ] && [ -n "$TASK_ARN" ]; then
    echo "Task trovato: $TASK_ARN"
    break
  fi
  echo "Tentativo $i/20: attendo task running..."
  sleep 30
done

# Recupera l'IP pubblico del task
if [ "$TASK_ARN" != "None" ] && [ -n "$TASK_ARN" ]; then
  PUBLIC_IP=$(aws ecs describe-tasks --cluster $CLUSTER_NAME --tasks $TASK_ARN --region $AWS_REGION --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' --output text)
  if [ -n "$PUBLIC_IP" ] && [ "$PUBLIC_IP" != "None" ]; then
    # Ottieni l'IP dalla network interface
    PUBLIC_IP=$(aws ec2 describe-network-interfaces --network-interface-ids $PUBLIC_IP --region $AWS_REGION --query 'NetworkInterfaces[0].Association.PublicIp' --output text 2>/dev/null)
  fi
else
  echo "⚠️  Nessun task running trovato. Verifica nella console ECS."
fi


echo "=== INFO DEPLOY ==="
echo "Aurora endpoint: $aurora_endpoint"
echo "ElastiCache Redis: $REDIS_ENDPOINT:$REDIS_PORT"
echo "SQS Queue: $SQS_EXPORT_QUEUE_URL"
echo "SQS Import Queue: $SQS_IMPORT_QUEUE_URL"
echo "Security Group ID: $SECURITY_GROUP_ID"
echo "VPC ID: $VPC_ID"
echo "Subnets: $SUBNETS"
echo ""
echo "Verifica lo stato su AWS ECS Console: https://$AWS_REGION.console.aws.amazon.com/ecs/v2/clusters/$CLUSTER_NAME/services/$SERVICE_NAME"
echo "CloudWatch Logs: https://$AWS_REGION.console.aws.amazon.com/cloudwatch/home?region=$AWS_REGION#logsV2:log-groups/log-group/%2Fecs%2Fgestioneannotazioni-app"
echo ""
echo "Comandi debug utili:"
echo "aws ecs describe-tasks --cluster $CLUSTER_NAME --tasks \$(aws ecs list-tasks --cluster $CLUSTER_NAME --region $AWS_REGION --query 'taskArns[0]' --output text) --region $AWS_REGION"
echo "aws logs get-log-events --log-group-name $LOG_GROUP_NAME --log-stream-name ecs/$CONTAINER_NAME/\$(date +%Y/%m/%d) --region $AWS_REGION"

if [ -n "$PUBLIC_IP" ] && [ "$PUBLIC_IP" != "None" ] && [ "$PUBLIC_IP" != "null" ]; then
  echo ""
  echo "🎉 MICROSERVIZIO DISPONIBILE SU:"
  echo "   Frontend: http://$PUBLIC_IP:8080"
  echo "   API: http://$PUBLIC_IP:8080/api/annotazioni"
  echo "   Swagger: http://$PUBLIC_IP:8080/swagger-ui.html"
  echo "   Health: http://$PUBLIC_IP:8080/actuator/health"
  echo ""
else
  echo "⚠️  Impossibile recuperare l'IP pubblico del task. Verifica nella console ECS."
fi
