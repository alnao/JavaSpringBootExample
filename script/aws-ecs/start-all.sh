#!/bin/bash
# Script di provisioning e deploy completo su AWS ECS Fargate per il microservizio gestioneannotazioni
# Richiede: AWS CLI configurata, permessi su ECS, ECR, RDS, DynamoDB, IAM, VPC
# Esegue: build/push immagine, creazione risorse, deploy ECS, attese, init DB

set -euo pipefail
export AWS_PAGER=""


# === CONFIGURAZIONE ===
AWS_REGION="eu-central-1"
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

VPC_ID="" # lasciato vuoto per usare la default VPC
SUBNETS="" # verrà popolato dallo script
SECURITY_GROUP_ID="" # verrà creato


# === 1. Build e push immagine su ECR ===
echo "[1/7] Build e push immagine Docker su ECR..."
if ! aws ecr describe-repositories --repository-names "$ECR_REPO_NAME" --region $AWS_REGION > /dev/null 2>&1; then
  aws ecr create-repository --repository-name "$ECR_REPO_NAME" --region $AWS_REGION --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true
else
  echo "ECR repository già esistente."
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
if ! aws iam get-role --role-name $TASK_ROLE_NAME --region $AWS_REGION > /dev/null 2>&1; then
  TASK_ROLE_ARN=$(aws iam create-role \
    --role-name $TASK_ROLE_NAME \
    --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ecs-tasks.amazonaws.com"},"Action":"sts:AssumeRole"}]}' \
    --region $AWS_REGION \
    --query 'Role.Arn' --output text)
  aws iam attach-role-policy --role-name $TASK_ROLE_NAME --policy-arn $POLICY_ARN --region $AWS_REGION
else
  TASK_ROLE_ARN=$(aws iam get-role --role-name $TASK_ROLE_NAME --region $AWS_REGION --query 'Role.Arn' --output text)
  echo "IAM Role già esistente: $TASK_ROLE_ARN"
fi

# === 1c. Creazione ECS Execution Role ===
echo "[1c/7] Creazione ECS Execution Role..."
EXEC_ROLE_NAME="gestioneannotazioni-ecs-execution-role"
EXEC_ROLE_ARN=""
EXEC_POLICY_ARN="arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
LOGS_POLICY_ARN="arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
if ! aws iam get-role --role-name $EXEC_ROLE_NAME --region $AWS_REGION > /dev/null 2>&1; then
  EXEC_ROLE_ARN=$(aws iam create-role \
    --role-name $EXEC_ROLE_NAME \
    --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ecs-tasks.amazonaws.com"},"Action":"sts:AssumeRole"}]}' \
    --region $AWS_REGION \
    --query 'Role.Arn' --output text)
  aws iam attach-role-policy --role-name $EXEC_ROLE_NAME --policy-arn $EXEC_POLICY_ARN --region $AWS_REGION
  aws iam attach-role-policy --role-name $EXEC_ROLE_NAME --policy-arn $LOGS_POLICY_ARN --region $AWS_REGION
else
  EXEC_ROLE_ARN=$(aws iam get-role --role-name $EXEC_ROLE_NAME --region $AWS_REGION --query 'Role.Arn' --output text)
  echo "IAM Execution Role già esistente: $EXEC_ROLE_ARN"
  # Assicura che la policy CloudWatchLogs sia attaccata
  if ! aws iam list-attached-role-policies --role-name $EXEC_ROLE_NAME --region $AWS_REGION --query 'AttachedPolicies[*].PolicyArn' --output text | grep -q $LOGS_POLICY_ARN; then
    aws iam attach-role-policy --role-name $EXEC_ROLE_NAME --policy-arn $LOGS_POLICY_ARN --region $AWS_REGION
  fi
fi

# Tag IAM Role (Task e Execution)
aws iam tag-role --role-name $TASK_ROLE_NAME --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true --region $AWS_REGION || true
aws iam tag-role --role-name $EXEC_ROLE_NAME --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true --region $AWS_REGION || true

# === 2. Preparazione networking (deve essere prima di Aurora) ===
echo "[2/7] Preparazione networking..."
VPC_ID=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --region $AWS_REGION --query 'Vpcs[0].VpcId' --output text)
SUBNETS=$(aws ec2 describe-subnets --filters Name=vpc-id,Values=$VPC_ID --region $AWS_REGION --query 'Subnets[*].SubnetId' --output text | tr '\t' ',')
if ! aws ec2 describe-security-groups --filters Name=group-name,Values=gestioneannotazioni-sg Name=vpc-id,Values=$VPC_ID --region $AWS_REGION --query 'SecurityGroups[0].GroupId' --output text | grep -v None > /dev/null 2>&1; then
  SECURITY_GROUP_ID=$(aws ec2 create-security-group --group-name gestioneannotazioni-sg --description "gestioneannotazioni ECS SG" --vpc-id $VPC_ID --region $AWS_REGION --query 'GroupId' --output text)
  aws ec2 create-tags --resources $SECURITY_GROUP_ID --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true --region $AWS_REGION

  # Regole di sicurezza: apri solo le porte necessarie
  aws ec2 authorize-security-group-ingress --group-id $SECURITY_GROUP_ID --protocol tcp --port 8080 --cidr 0.0.0.0/0 --region $AWS_REGION # HTTP app
  aws ec2 authorize-security-group-ingress --group-id $SECURITY_GROUP_ID --protocol tcp --port 3306 --source-group $SECURITY_GROUP_ID --region $AWS_REGION # MySQL/Aurora solo da ECS
  aws ec2 authorize-security-group-ingress --group-id $SECURITY_GROUP_ID --protocol tcp --port 443 --cidr 0.0.0.0/0 --region $AWS_REGION # HTTPS (opzionale)
  aws ec2 authorize-security-group-ingress --group-id $SECURITY_GROUP_ID --protocol tcp --port 22 --cidr 0.0.0.0/0 --region $AWS_REGION # SSH (solo se necessario, meglio restringere)
else
  SECURITY_GROUP_ID=$(aws ec2 describe-security-groups --filters Name=group-name,Values=gestioneannotazioni-sg Name=vpc-id,Values=$VPC_ID --region $AWS_REGION --query 'SecurityGroups[0].GroupId' --output text)
  echo "Security Group già esistente: $SECURITY_GROUP_ID"
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
    --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true
else
  echo "Aurora cluster già esistente."
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
    --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true
else
  echo "Aurora instance già esistente."
fi
# Attendi che il cluster sia disponibile
echo "Attendo che Aurora sia disponibile..."
while true; do
  STATUS=$(aws rds describe-db-clusters --db-cluster-identifier $AURORA_CLUSTER_ID --region $AWS_REGION --query 'DBClusters[0].Status' --output text)
  echo "Stato Aurora: $STATUS"
  if [[ "$STATUS" == "available" ]]; then break; fi
  sleep 20
done

# Attendi anche che l'instance sia disponibile
echo "Attendo che Aurora instance sia disponibile..."
while true; do
  INSTANCE_STATUS=$(aws rds describe-db-instances --db-instance-identifier $AURORA_INSTANCE_ID --region $AWS_REGION --query 'DBInstances[0].DBInstanceStatus' --output text)
  echo "Stato Aurora instance: $INSTANCE_STATUS"
  if [[ "$INSTANCE_STATUS" == "available" ]]; then break; fi
  sleep 20
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
    --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true
else
  echo "Tabella DynamoDB $DYNAMODB_TABLE già esistente."
fi
if ! aws dynamodb describe-table --table-name $DYNAMODB_TABLE2 --region $AWS_REGION > /dev/null 2>&1; then
  aws dynamodb create-table \
    --table-name $DYNAMODB_TABLE2 \
    --attribute-definitions AttributeName=id,AttributeType=S \
    --key-schema AttributeName=id,KeyType=HASH \
    --billing-mode PAY_PER_REQUEST \
    --region $AWS_REGION \
    --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true
else
  echo "Tabella DynamoDB $DYNAMODB_TABLE2 già esistente."
fi

# === 5. Creazione cluster ECS ===
echo "[5/7] Creazione ECS Cluster..."
CLUSTER_STATUS=$(aws ecs describe-clusters --clusters $CLUSTER_NAME --region $AWS_REGION --query 'clusters[0].status' --output text 2>/dev/null)
if [ "$CLUSTER_STATUS" != "ACTIVE" ]; then
    if [ "$CLUSTER_STATUS" == "None" ] || [ -z "$CLUSTER_STATUS" ]; then
        echo "Creazione del cluster ECS: $CLUSTER_NAME"
        aws ecs create-cluster --cluster-name $CLUSTER_NAME --region $AWS_REGION \
            --tags '[
                {"key": "Name", "value": "gestioneannotazioni-app"},
                {"key": "Project", "value": "gestioneannotazioni-app"}
            ]' && echo "Cluster ECS creato con successo."
    else
        echo "Cluster $CLUSTER_NAME esiste ma non è attivo (stato: $CLUSTER_STATUS)"
        aws ecs create-cluster --cluster-name $CLUSTER_NAME --region $AWS_REGION \
            --tags '[
                {"key": "Name", "value": "gestioneannotazioni-app"},
                {"key": "Project", "value": "gestioneannotazioni-app"}
            ]' && echo "Cluster ECS creato con successo."
    fi
else
    echo "ECS Cluster $CLUSTER_NAME già esistente e attivo."
fi

# === 6. Definizione task ECS Fargate ===
echo "[5/7] Definizione task ECS Fargate..."
LOG_GROUP_NAME="/ecs/gestioneannotazioni-app"
aws logs create-log-group --log-group-name $LOG_GROUP_NAME --region $AWS_REGION 2>/dev/null || true
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
        { "name": "RDS_PASSWORD", "value": "$AURORA_MASTER_PASS" }
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
aws ecs register-task-definition --cli-input-json file://script/aws-ecs/task-def.json --region $AWS_REGION

# === 7. Deploy servizio ECS Fargate ===
echo "[7/7] Deploy servizio ECS Fargate..."
# Ottieni ARN della task definition
TASK_DEF_ARN=$(aws ecs describe-task-definition --task-definition $TASK_FAMILY --region $AWS_REGION --query 'taskDefinition.taskDefinitionArn' --output text 2>/dev/null)
if [ -z "$TASK_DEF_ARN" ] || [ "$TASK_DEF_ARN" == "None" ]; then
    echo "Errore: Task definition $TASK_FAMILY non trovata!"
    exit 1
fi
# Controlla se il servizio esiste e è attivo
SERVICE_STATUS=$(aws ecs describe-services --cluster $CLUSTER_NAME --services $SERVICE_NAME --region $AWS_REGION --query 'services[0].status' --output text 2>/dev/null)
if [ "$SERVICE_STATUS" != "ACTIVE" ]; then
    echo "Creazione del servizio ECS: $SERVICE_NAME"
    aws ecs create-service \
        --cluster $CLUSTER_NAME \
        --service-name $SERVICE_NAME \
        --task-definition $TASK_DEF_ARN \
        --desired-count 1 \
        --launch-type FARGATE \
        --network-configuration "awsvpcConfiguration={subnets=[$SUBNETS],securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=ENABLED}" \
        --region $AWS_REGION \
        --tags '[
            {"key":"Name","value":"gestioneannotazioni-app"},
            {"key":"Project","value":"gestioneannotazioni-app"},
            {"key":"Environment","value":"production"}
        ]' && echo "Servizio ECS creato con successo."
else
    echo "ECS Service $SERVICE_NAME già esistente e attivo."
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
