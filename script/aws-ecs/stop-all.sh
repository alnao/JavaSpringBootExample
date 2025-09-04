#!/bin/bash
# Script di teardown per risorse create da start-all.sh su AWS ECS Fargate
# ATTENZIONE: elimina cluster ECS, servizio, task definition, Aurora, DynamoDB, ECR repo, Security Group, IAM Role
# Richiede: AWS CLI configurata e permessi admin

set -euo pipefail
export AWS_PAGER=""


AWS_REGION="eu-central-1"
ECR_REPO_NAME="gestionepersonale"
CLUSTER_NAME="gestionepersonale-cluster"
SERVICE_NAME="gestionepersonale-service"
TASK_FAMILY="gestionepersonale-task"
CONTAINER_NAME="gestionepersonale"
DYNAMODB_TABLE="annotazioni"
DYNAMODB_TABLE2="annotazioni_storico"
AURORA_CLUSTER_ID="gestionepersonale-aurora-cluster"
AURORA_INSTANCE_ID="gestionepersonale-aurora-instance"
SECURITY_GROUP_NAME="gestionepersonale-sg"
TASK_ROLE_NAME="gestionepersonale-ecs-task-role"
EXEC_ROLE_NAME="gestionepersonale-ecs-execution-role"
LOG_GROUP_NAME="/ecs/gestionepersonale-app"

# 1. Elimina servizio ECS
aws ecs update-service --cluster $CLUSTER_NAME --service $SERVICE_NAME --desired-count 0 --region $AWS_REGION || true
aws ecs delete-service --cluster $CLUSTER_NAME --service $SERVICE_NAME --force --region $AWS_REGION || true

# 2. Deregistra tutte le revisioni della task definition
for rev in $(aws ecs list-task-definitions --family-prefix $TASK_FAMILY --region $AWS_REGION --query 'taskDefinitionArns' --output text); do
  aws ecs deregister-task-definition --task-definition $rev --region $AWS_REGION || true
done

#Aspetto che tutti i task siano terminati, circa un minuto dovrebbe bastare!
sleep 60

# 3. Elimina cluster ECS
aws ecs delete-cluster --cluster $CLUSTER_NAME --region $AWS_REGION || true

# 4. Elimina DynamoDB tables
aws dynamodb delete-table --table-name $DYNAMODB_TABLE --region $AWS_REGION || true
aws dynamodb delete-table --table-name $DYNAMODB_TABLE2 --region $AWS_REGION || true

# 5. Elimina Aurora instance e cluster
aws rds delete-db-instance --db-instance-identifier $AURORA_INSTANCE_ID --skip-final-snapshot --region $AWS_REGION || true
aws rds wait db-instance-deleted --db-instance-identifier $AURORA_INSTANCE_ID --region $AWS_REGION || true
aws rds delete-db-cluster --db-cluster-identifier $AURORA_CLUSTER_ID --skip-final-snapshot --region $AWS_REGION || true
aws rds wait db-cluster-deleted --db-cluster-identifier $AURORA_CLUSTER_ID --region $AWS_REGION || true

# 6. Elimina Security Group
VPC_ID=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --region $AWS_REGION --query 'Vpcs[0].VpcId' --output text)
SG_ID=$(aws ec2 describe-security-groups --filters Name=group-name,Values=$SECURITY_GROUP_NAME Name=vpc-id,Values=$VPC_ID --region $AWS_REGION --query 'SecurityGroups[0].GroupId' --output text)
if [ -n "$SG_ID" ] && [ "$SG_ID" != "None" ]; then
  aws ec2 delete-security-group --group-id $SG_ID --region $AWS_REGION || true
fi

# 7. Elimina ECR repository (con immagini)
aws ecr delete-repository --repository-name $ECR_REPO_NAME --force --region $AWS_REGION || true

# 8. Elimina IAM Role
aws iam detach-role-policy --role-name $TASK_ROLE_NAME --policy-arn arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess --region $AWS_REGION || true
aws iam delete-role --role-name $TASK_ROLE_NAME --region $AWS_REGION || true
aws iam detach-role-policy --role-name $EXEC_ROLE_NAME --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy --region $AWS_REGION || true
aws iam detach-role-policy --role-name $EXEC_ROLE_NAME --policy-arn arn:aws:iam::aws:policy/CloudWatchLogsFullAccess --region $AWS_REGION || true
aws iam delete-role --role-name $EXEC_ROLE_NAME --region $AWS_REGION || true

# 9. Elimina CloudWatch Log Group
aws logs delete-log-group --log-group-name $LOG_GROUP_NAME --region $AWS_REGION || true

rm -f ./script/aws-ecs/task-def.json

echo "Teardown completato. Tutte le risorse sono state eliminate."
