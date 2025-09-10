#!/bin/bash
# Script: run-ecs-mysql-insert.sh
# Avvia un ECS Task temporaneo nella VPC per eseguire una query di inserimento su Aurora MySQL
# Richiede: AWS CLI configurata, permessi su ECS, ECR, RDS, IAM, VPC
# Richiede che il cluster ECS, il ruolo di esecuzione e il gruppo di log esistano già tramite script start-all.sh
# Necessita il file init-mysql.sql con le query SQL da eseguire
# https://raw.githubusercontent.com/alnao/JavaSpringBootExample/master/script/init-database/init-mysql.sql

set -euo pipefail
export AWS_PAGER=""

# === CONFIGURAZIONE ===
AWS_REGION="eu-central-1"
CLUSTER_NAME="gestioneannotazioni-cluster"
SECURITY_GROUP_ID="" # Da popolare
SUBNETS="" # Da popolare
AURORA_ENDPOINT="" # Da popolare
AURORA_DB_NAME="gestioneannotazioni"
AURORA_MASTER_USER="gestioneannotazioni_user"
AURORA_MASTER_PASS="gestioneannotazioni_pass"
LOG_GROUP_NAME="/ecs/gestioneannotazioni-app"
TASK_ROLE_NAME="gestioneannotazioni-ecs-task-role"
EXEC_ROLE_NAME="gestioneannotazioni-ecs-execution-role"

# === Recupera VPC, Subnet, Security Group, Aurora endpoint ===
VPC_ID=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --region $AWS_REGION --query 'Vpcs[0].VpcId' --output text)
SUBNETS=$(aws ec2 describe-subnets --filters Name=vpc-id,Values=$VPC_ID --region $AWS_REGION --query 'Subnets[*].SubnetId' --output text | tr '\t' ',')
SECURITY_GROUP_ID=$(aws ec2 describe-security-groups --filters Name=group-name,Values=gestioneannotazioni-sg Name=vpc-id,Values=$VPC_ID --region $AWS_REGION --query 'SecurityGroups[0].GroupId' --output text)
AURORA_ENDPOINT=$(aws rds describe-db-clusters --db-cluster-identifier gestioneannotazioni-aurora-cluster --region $AWS_REGION --query 'DBClusters[0].Endpoint' --output text)

# === Definisci comando MySQL da eseguire ===
# MYSQL_COMMAND="mysql -h $AURORA_ENDPOINT -u$AURORA_MASTER_USER -p$AURORA_MASTER_PASS $AURORA_DB_NAME -e \\\"INSERT INTO users (id, username, password, enabled) VALUES ('2b3c4d5e-6f7g-8h9i-0j1k-2l3m4n5o6p7q', 'admin', '\$2b\$12\$TUQyZEAT4R.5nsyGJYm6Z.HQMiD.Z8dRs8nc6k1fHZf31sKt4lUOa', true) ON DUPLICATE KEY UPDATE username='admin';\\\" "
MYSQL_COMMAND="curl -o /tmp/init-mysql.sql https://raw.githubusercontent.com/alnao/JavaSpringBootExample/master/script/init-database/init-mysql.sql && cat /tmp/init-mysql.sql && mysql -h $AURORA_ENDPOINT -u$AURORA_MASTER_USER -p$AURORA_MASTER_PASS < /tmp/init-mysql.sql && echo 'Script SQL eseguito con successo.' "

# === Definisci container image (usa una pubblica con mysql client) ===
MYSQL_IMAGE="mysql:8.0"

TASK_ROLE_ARN=$(aws iam get-role --role-name $TASK_ROLE_NAME --region $AWS_REGION --query 'Role.Arn' --output text)
EXEC_ROLE_ARN=$(aws iam get-role --role-name $EXEC_ROLE_NAME --region $AWS_REGION --query 'Role.Arn' --output text)


# === Definisci task definition temporanea ===
TASK_DEF_NAME="gestioneannotazioni-mysql-client-task"
cat > ./script/aws-ecs/mysql-task-def.json <<EOF
{
  "family": "$TASK_DEF_NAME",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "taskRoleArn": "$TASK_ROLE_ARN",
  "executionRoleArn": "$EXEC_ROLE_ARN",
  "containerDefinitions": [
    {
      "name": "mysql-client",
      "image": "$MYSQL_IMAGE",
      "command": ["/bin/sh", "-c", "$MYSQL_COMMAND"],
      "environment": [],
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

# Registra la task definition e recupera la revision
TASK_DEF_ARN=$(aws ecs register-task-definition --cli-input-json file://script/aws-ecs/mysql-task-def.json --region $AWS_REGION --query 'taskDefinition.taskDefinitionArn' --output text)
REVISION=$(echo "$TASK_DEF_ARN" | awk -F":" '{print $NF}')

# Avvia il task ECS Fargate
TASK_ARN=$(aws ecs run-task \
  --cluster $CLUSTER_NAME \
  --launch-type FARGATE \
  --task-definition $TASK_DEF_NAME:$REVISION \
  --network-configuration "awsvpcConfiguration={subnets=[$SUBNETS],securityGroups=[$SECURITY_GROUP_ID],assignPublicIp=ENABLED}" \
  --region $AWS_REGION \
  --query 'tasks[0].taskArn' --output text)

echo "Task ECS avviato: $TASK_ARN"

# Attendi che il task sia completato
while true; do
  STATUS=$(aws ecs describe-tasks --cluster $CLUSTER_NAME --tasks $TASK_ARN --region $AWS_REGION --query 'tasks[0].lastStatus' --output text)
  echo "Stato task: $STATUS"
  if [[ "$STATUS" == "STOPPED" ]]; then break; fi
  sleep 10
done

echo "Task completato. Log disponibili su CloudWatch."

# Deregistra la task definition temporanea usando family:revision
aws ecs deregister-task-definition --task-definition $TASK_DEF_NAME:$REVISION --region $AWS_REGION
echo "Task definition deregistrata."

rm -f ./script/aws-ecs/mysql-task-def.json

echo "Finito!"
