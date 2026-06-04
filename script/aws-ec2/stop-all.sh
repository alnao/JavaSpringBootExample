#!/bin/bash
# Rimozione completa stack gestioneannotazioni AWS (Aurora MySQL, DynamoDB, EC2, Security Group)

# Disabilita paginazione aws cli
export AWS_PAGER=""

REGION="eu-central-1"
DB_CLUSTER_ID="gestioneannotazioni-cluster"
DB_INSTANCE_ID="gestioneannotazioni-instance"
SG_NAME="gestioneannotazioni-sg"
KEY_NAME="${KEY_NAME:-gestioneannotazioni-key}"
SQS_QUEUE_NAME_IMPORT="gestioneannotazioni-annotazioni-import"
SQS_QUEUE_NAME_EXPORT="gestioneannotazioni-annotazioni-export"
REDIS_CLUSTER_ID="gestioneannotazioni-redis"

# Funzione helper per ignorare errori di risorse non esistenti
safe_delete() {
  local command=$1
  local resource_name=$2
  
  if eval "$command" &>/dev/null; then
    echo "✓ $resource_name rimosso"
    return 0
  else
    local exit_code=$?
    # Non-zero significa che la risorsa non esiste o c'è stato un errore
    # In entrambi i casi, continuiamo
    echo "⊘ $resource_name non trovato o già rimosso"
    return 0
  fi
}

# Funzione helper per attese sicure
safe_wait() {
  local command=$1
  local resource_name=$2
  
  if eval "$command" &>/dev/null; then
    echo "✓ Attesa $resource_name completata"
    return 0
  else
    echo "⊘ Timeout o risorsa non trovata per $resource_name (continuo comunque)"
    return 0
  fi
}

# 1. Termina e rimuovi tutte le EC2 con tag gestioneannotazioni-app
INSTANCE_IDS=$(aws ec2 describe-instances --region $REGION --filters Name=tag:gestioneannotazioni-app,Values=true Name=instance-state-name,Values=running,stopped --query 'Reservations[].Instances[].InstanceId' --output text)
if [ -n "$INSTANCE_IDS" ]; then
  echo "Terminazione istanze EC2: $INSTANCE_IDS"
  aws ec2 terminate-instances --instance-ids $INSTANCE_IDS --region $REGION
  aws ec2 wait instance-terminated --instance-ids $INSTANCE_IDS --region $REGION
fi

# 2. Rimuovi Aurora MySQL
echo "Rimozione Aurora MySQL..."
safe_delete "aws rds delete-db-instance --db-instance-identifier $DB_INSTANCE_ID --skip-final-snapshot --region $REGION" "Istanza Aurora $DB_INSTANCE_ID"
safe_wait "aws rds wait db-instance-deleted --db-instance-identifier $DB_INSTANCE_ID --region $REGION" "eliminazione istanza Aurora"
safe_delete "aws rds delete-db-cluster --db-cluster-identifier $DB_CLUSTER_ID --skip-final-snapshot --region $REGION" "Cluster Aurora $DB_CLUSTER_ID"
safe_wait "aws rds wait db-cluster-deleted --db-cluster-identifier $DB_CLUSTER_ID --region $REGION" "eliminazione cluster Aurora"

# 3. Rimuovi tabelle DynamoDB
echo "Rimozione tabelle DynamoDB..."
safe_delete "aws dynamodb delete-table --table-name annotazioni --region $REGION" "Tabella DynamoDB 'annotazioni'"
safe_delete "aws dynamodb delete-table --table-name annotazioni_storico --region $REGION" "Tabella DynamoDB 'annotazioni_storico'"
safe_delete "aws dynamodb delete-table --table-name annotazioni_storicoStati --region $REGION" "Tabella DynamoDB 'annotazioni_storicoStati'"

# 4. Rimuovi coda SQS
echo "Rimozione code SQS..."

# SQS Import
SQS_QUEUE_URL=$(aws sqs get-queue-url --queue-name $SQS_QUEUE_NAME_IMPORT --region $REGION --query 'QueueUrl' --output text 2>/dev/null)
if [ -n "$SQS_QUEUE_URL" ] && [ "$SQS_QUEUE_URL" != "None" ]; then
  safe_delete "aws sqs delete-queue --queue-url '$SQS_QUEUE_URL' --region $REGION" "Coda SQS import"
else
  echo "⊘ Coda SQS import non trovata"
fi

# SQS Export
SQS_QUEUE_URL=$(aws sqs get-queue-url --queue-name $SQS_QUEUE_NAME_EXPORT --region $REGION --query 'QueueUrl' --output text 2>/dev/null)
if [ -n "$SQS_QUEUE_URL" ] && [ "$SQS_QUEUE_URL" != "None" ]; then
  safe_delete "aws sqs delete-queue --queue-url '$SQS_QUEUE_URL' --region $REGION" "Coda SQS export"
else
  echo "⊘ Coda SQS export non trovata"
fi

# 4b. Rimuovi ElastiCache Redis cluster
echo "Rimozione ElastiCache Redis cluster..."
safe_delete "aws elasticache delete-cache-cluster --cache-cluster-id $REDIS_CLUSTER_ID --region $REGION" "Redis cluster $REDIS_CLUSTER_ID"
safe_wait "aws elasticache wait cache-cluster-deleted --cache-cluster-id $REDIS_CLUSTER_ID --region $REGION" "eliminazione Redis cluster"

# 4c. Rimuovi subnet group ElastiCache
CACHE_SUBNET_GROUP_NAME="gestioneannotazioni-redis-subnet-group"
echo "Rimozione subnet group ElastiCache..."
safe_delete "aws elasticache delete-cache-subnet-group --cache-subnet-group-name $CACHE_SUBNET_GROUP_NAME --region $REGION" "Subnet group ElastiCache"

# 5. Rimuovi Security Group
echo "Rimozione Security Group..."
SG_ID=$(aws ec2 describe-security-groups --region $REGION --filters Name=group-name,Values=$SG_NAME --query 'SecurityGroups[0].GroupId' --output text 2>/dev/null)
if [ "$SG_ID" != "None" ] && [ -n "$SG_ID" ]; then
  safe_delete "aws ec2 delete-security-group --group-id $SG_ID --region $REGION" "Security Group $SG_NAME"
else
  echo "⊘ Security Group $SG_NAME non trovato"
fi

# 6. Rimuovi chiave EC2
echo "Rimozione key pair..."
safe_delete "aws ec2 delete-key-pair --key-name $KEY_NAME --region $REGION" "Key pair $KEY_NAME"
rm -f $KEY_NAME.pem

# 7. Rimuovi IAM Role e Instance Profile
echo "Rimozione IAM Role e Instance Profile..."
ROLE_NAME="gestioneannotazioni-ec2-role"
INSTANCE_PROFILE_NAME="gestioneannotazioni-ec2-profile"
POLICY_ARN="arn:aws:iam::aws:policy/AmazonRDSFullAccess"
POLICY_ARN2="arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
POLICY_ARN3="arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
POLICY_ARN4="arn:aws:iam::aws:policy/AmazonSQSFullAccess"

# Rimuovi instance profile
if aws iam get-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --region $REGION &>/dev/null; then
  safe_delete "aws iam remove-role-from-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --role-name $ROLE_NAME --region $REGION" "Rimozione ruolo da instance profile"
  safe_delete "aws iam delete-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --region $REGION" "Instance profile $INSTANCE_PROFILE_NAME"
else
  echo "⊘ Instance profile $INSTANCE_PROFILE_NAME non trovato"
fi

# Rimuovi role
if aws iam get-role --role-name $ROLE_NAME --region $REGION &>/dev/null; then
  safe_delete "aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN --region $REGION" "Policy AmazonRDSFullAccess da $ROLE_NAME"
  safe_delete "aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN2 --region $REGION" "Policy AmazonDynamoDBFullAccess da $ROLE_NAME"
  safe_delete "aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN3 --region $REGION" "Policy AmazonEC2ContainerRegistryReadOnly da $ROLE_NAME"
  safe_delete "aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN4 --region $REGION" "Policy AmazonSQSFullAccess da $ROLE_NAME"
  safe_delete "aws iam delete-role --role-name $ROLE_NAME --region $REGION" "IAM Role $ROLE_NAME"
else
  echo "⊘ IAM Role $ROLE_NAME non trovato"
fi

echo "Stack AWS gestioneannotazioni rimosso completamente!"
echo "Risorse rimosse:"
echo "- EC2 instances con tag gestioneannotazioni-app"
echo "- Aurora MySQL cluster e instance"
echo "- Tabelle DynamoDB (annotazioni, annotazioni_storico, annotazioni_storicoStati)"
echo "- Coda SQS ($SQS_QUEUE_NAME)"
echo "- ElastiCache Redis cluster ($REDIS_CLUSTER_ID)"
echo "- ElastiCache subnet group ($CACHE_SUBNET_GROUP_NAME)"
echo "- Security Group ($SG_NAME)"
echo "- Key pair ($KEY_NAME)"
echo "- IAM Role e Instance Profile ($INSTANCE_PROFILE_NAME)"