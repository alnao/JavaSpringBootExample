#!/bin/bash
# Avvio completo dello stack Gestione annotazioni su AWS ECS Fargate con TERRAFORM:
# stesse risorse di script/aws-ecs/start-all.sh (ECR, Aurora MySQL, DynamoDB, SQS,
# ElastiCache Redis, cluster/task/service ECS), piu' l'inizializzazione del
# database che nello stack bash e' il passo separato run-ecs-mysql-insert.sh.
#
# Sequenza (l'apply e' in due passi per avere immagine e DB pronti PRIMA del service):
#   1. terraform init (backend, vedi script/aws-tf-init.sh)
#   2. terraform apply mirato: repository ECR + task di init (trascina Aurora, SG, ruoli, cluster, log group)
#   3. docker build dalla root del progetto (Maven dentro al Dockerfile) + push su ECR
#   4. ecs run-task del task di inizializzazione (init-mysql.sql) e attesa del suo esito
#   5. terraform apply completo: Redis, DynamoDB, SQS, task definition dell'app, service
#   6. attesa del task RUNNING e stampa dell'IP pubblico (dalla ENI del task, come test-aws-ecs.sh)
#
# Richiede: Terraform >= 1.10, Docker, AWS CLI configurata, jq, bucket S3 dello state
# esistente (oppure TF_STATE_BUCKET= per lo state locale).
#
# Variabili di shell (tutte opzionali, stesse degli script bash):
#   ENVIRONMENT   dev (default) | test | production  -> tag Environment
#   DB_PASS       password master di Aurora (default: stessa dimostrativa dello stack bash)
#   AWS_REGION    region (default eu-central-1)
#   IMAGE_TAG     tag dell'immagine su ECR (default latest)
#   SKIP_BUILD    1 = salta build e push (rilanci con immagine gia' su ECR)
#   TF_STATE_*    backend dello state, vedi script/aws-tf-init.sh
#
# ATTENZIONE: nomi di risorsa IDENTICI allo stack ECS bash e (per SG, DynamoDB,
# SQS, Redis) agli stack EC2: se uno di questi e' attivo l'apply fallisce sulla
# prima risorsa gia' esistente. Fermarli prima con il rispettivo stop-all.sh.

set -e
export AWS_PAGER=""

# --- Prerequisiti: prima di qualsiasi comando terraform/aws ---
for cmd in terraform docker aws jq; do
  if ! command -v "$cmd" > /dev/null 2>&1; then
    echo "ERRORE: comando '$cmd' non trovato. Serve per costruire l'immagine e gestire lo stack."
    exit 1
  fi
done

cd "$(dirname "$0")"
PROJECT_ROOT="$(cd ../.. && pwd)"
REGION="${AWS_REGION:-eu-central-1}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

# Variabili Terraform dalle variabili di shell (la validazione di environment e' in variables.tf)
export TF_VAR_environment="${ENVIRONMENT:-dev}"
export TF_VAR_region="$REGION"
export TF_VAR_image_tag="$IMAGE_TAG"
export TF_VAR_db_password="${DB_PASS:-gestioneannotazioni_pass}"

source ../aws-tf-init.sh aws-terraform-ecs # cwd = cartella dello stack (cd sopra)
terraform_init_backend

# --- 2. Apply mirato: ECR e task di init (con Aurora, SG, ruoli, cluster ECS, log group) ---
echo ""
echo "[2/6] Apply mirato: repository ECR e prerequisiti dell'inizializzazione DB (Aurora richiede diversi minuti)..."
# Nota: un apply mirato scrive nello state solo gli output che dipendono dai target;
# le subnet servono al run-task del passo 4, quindi il data source va mirato esplicitamente.
terraform apply -auto-approve -input=false \
  -target=aws_ecr_repository.app \
  -target=aws_ecs_task_definition.init \
  -target=data.aws_subnets.default

# --- 3. Build e push dell'immagine ---
echo ""
echo "[3/6] Build e push dell'immagine Docker su ECR..."
ECR_URL=$(terraform output -raw ecr_repository_url)
if [ "${SKIP_BUILD:-}" == "1" ]; then
  echo "SKIP_BUILD=1: salto build e push, uso l'immagine gia' presente su $ECR_URL:$IMAGE_TAG"
else
  aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$ECR_URL"
  docker build -t "gestioneannotazioni:$IMAGE_TAG" "$PROJECT_ROOT"
  docker tag "gestioneannotazioni:$IMAGE_TAG" "$ECR_URL:$IMAGE_TAG"
  docker push "$ECR_URL:$IMAGE_TAG"
  echo "✓ Immagine pubblicata: $ECR_URL:$IMAGE_TAG"
fi

# --- 4. Inizializzazione del database con il task Fargate dichiarato in init-db.tf ---
echo ""
echo "[4/6] Inizializzazione del database Aurora (task Fargate mysql:8.0)..."
CLUSTER_NAME=$(terraform output -raw ecs_cluster_name)
INIT_TASK_DEF=$(terraform output -raw init_task_definition)
SG_ID=$(terraform output -raw security_group_id)
SUBNETS=$(terraform output -raw subnet_ids)
LOG_GROUP=$(terraform output -raw log_group_name)
INIT_TASK_ARN=$(aws ecs run-task \
  --cluster "$CLUSTER_NAME" \
  --launch-type FARGATE \
  --task-definition "$INIT_TASK_DEF" \
  --network-configuration "awsvpcConfiguration={subnets=[$SUBNETS],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
  --tags "$(terraform output -raw init_task_tags_json)" \
  --region "$REGION" \
  --query 'tasks[0].taskArn' --output text)
echo "Task di init avviato: $INIT_TASK_ARN"
for i in $(seq 1 60); do
  STATUS=$(aws ecs describe-tasks --cluster "$CLUSTER_NAME" --tasks "$INIT_TASK_ARN" --region "$REGION" --query 'tasks[0].lastStatus' --output text)
  [ "$STATUS" == "STOPPED" ] && break
  echo "  stato task di init: $STATUS (tentativo $i/60)"
  sleep 10
done
INIT_EXIT=$(aws ecs describe-tasks --cluster "$CLUSTER_NAME" --tasks "$INIT_TASK_ARN" --region "$REGION" --query 'tasks[0].containers[0].exitCode' --output text)
if [ "$STATUS" != "STOPPED" ] || [ "$INIT_EXIT" != "0" ]; then
  echo "ERRORE: inizializzazione del database fallita (stato $STATUS, exit code $INIT_EXIT)."
  echo "Log: aws logs tail $LOG_GROUP --log-stream-name-prefix init --region $REGION"
  echo "Lo stack e' parziale ma coerente: correggere e rilanciare start-all.sh."
  exit 1
fi
echo "✓ Database inizializzato"

# --- 5. Apply completo ---
echo ""
echo "[5/6] Apply completo dello stack (Redis richiede alcuni minuti)..."
terraform apply -auto-approve -input=false

# --- 6. Attesa del task dell'applicazione e IP pubblico (dalla ENI, come test-aws-ecs.sh) ---
echo ""
echo "[6/6] Attesa del task ECS in stato RUNNING..."
SERVICE_NAME=$(terraform output -raw ecs_service_name)
TASK_ARN="None"
for i in $(seq 1 20); do
  TASK_ARN=$(aws ecs list-tasks --cluster "$CLUSTER_NAME" --service-name "$SERVICE_NAME" --desired-status RUNNING --region "$REGION" --query 'taskArns[0]' --output text 2>/dev/null)
  [ -n "$TASK_ARN" ] && [ "$TASK_ARN" != "None" ] && break
  echo "  tentativo $i/20: nessun task RUNNING, attendo 30 secondi..."
  sleep 30
done
PUBLIC_IP=""
if [ -n "$TASK_ARN" ] && [ "$TASK_ARN" != "None" ]; then
  ENI_ID=$(aws ecs describe-tasks --cluster "$CLUSTER_NAME" --tasks "$TASK_ARN" --region "$REGION" --query 'tasks[0].attachments[0].details[?name==`networkInterfaceId`].value' --output text)
  [ -n "$ENI_ID" ] && [ "$ENI_ID" != "None" ] && PUBLIC_IP=$(aws ec2 describe-network-interfaces --network-interface-ids "$ENI_ID" --region "$REGION" --query 'NetworkInterfaces[0].Association.PublicIp' --output text 2>/dev/null)
fi

echo ""
echo "=== STACK AVVIATO (Terraform) ==="
terraform output
echo ""
echo "Tutte le risorse sono taggate con Project=Annotazioni.aws-terraform-ecs, Environment=$TF_VAR_environment, ManagedBy=Terraform."
echo "Console ECS: https://$REGION.console.aws.amazon.com/ecs/v2/clusters/$CLUSTER_NAME/services/$SERVICE_NAME"
echo "Log app:     aws logs tail $LOG_GROUP --log-stream-name-prefix ecs --follow --region $REGION"
if [ -n "$PUBLIC_IP" ] && [ "$PUBLIC_IP" != "None" ]; then
  echo ""
  echo "🎉 MICROSERVIZIO DISPONIBILE SU:"
  echo "   Frontend: http://$PUBLIC_IP:8080"
  echo "   API:      http://$PUBLIC_IP:8080/api/annotazioni"
  echo "   Swagger:  http://$PUBLIC_IP:8080/swagger-ui.html"
  echo "   Health:   http://$PUBLIC_IP:8080/actuator/health"
else
  echo "⚠️  Nessun task RUNNING o IP pubblico non ancora assegnato: controlla la console ECS e i log."
fi
echo ""
echo "Test:      ./script/aws-terraform-ecs/test-aws-terraform-ecs.sh"
echo "Rimozione: ./script/aws-terraform-ecs/stop-all.sh"
