#!/bin/bash
# Script di provisioning e deploy completo su AWS ECS Fargate per il microservizio gestionepersonale
# Richiede: AWS CLI configurata, permessi su ECS, ECR, RDS, DynamoDB, IAM, VPC
# Esegue: build/push immagine, creazione risorse, deploy ECS, attese, init DB

AWS_REGION="eu-central-1"
ECR_REPO_NAME="gestionepersonale"
IMAGE_TAG="latest"
CLUSTER_NAME="gestionepersonale-cluster"
SERVICE_NAME="gestionepersonale-service"
TASK_FAMILY="gestionepersonale-task"
CONTAINER_NAME="gestionepersonale"
RDS_DB_ID="gestionepersonale-db"
DYNAMODB_TABLE="annotazioni"
DYNAMODB_TABLE2="annotazioni_storico"

AURORA_CLUSTER_ID="gestionepersonale-aurora-cluster"
AURORA_DB_NAME="gestionepersonale"
AURORA_MASTER_USER="gestionepersonale_user"
AURORA_MASTER_PASS="gestionepersonale_pass"
AURORA_INSTANCE_ID="gestionepersonale-aurora-instance"
AURORA_ENGINE="aurora-mysql"
AURORA_ENGINE_VER="5.7.mysql_aurora.2.11.4"
AURORA_INSTANCE_CLASS="db.t3.medium"

# Attendi che almeno un task sia running
echo "Attendo che il task ECS sia in stato RUNNING..."
for i in {1..20}; do
  TASK_ARN=$(aws ecs list-tasks --cluster $CLUSTER_NAME --service-name $SERVICE_NAME --desired-status RUNNING --region $AWS_REGION --query 'taskArns[0]' --output text 2>/dev/null)
  if [ "$TASK_ARN" != "None" ] && [ -n "$TASK_ARN" ]; then
    echo "Task trovato: $TASK_ARN"
    break
  fi
  echo "Tentativo $i/20: attendo task running..."
  sleep 15
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
  echo "Eseguire lo script  ./script/aws-ecs/check-fargate.sh "
fi