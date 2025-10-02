#!/bin/bash
# Rimozione completa stack gestioneannotazioni AWS (Aurora MySQL, DynamoDB, EC2, Security Group)
set -e

# Disabilita paginazione aws cli
export AWS_PAGER=""

REGION="eu-central-1"
DB_CLUSTER_ID="gestioneannotazioni-cluster"
DB_INSTANCE_ID="gestioneannotazioni-instance"
SG_NAME="gestioneannotazioni-sg"
KEY_NAME="${KEY_NAME:-gestioneannotazioni-key}"
SQS_QUEUE_NAME="gestioneannotazioni-annotazioni"


# 1. Termina e rimuovi tutte le EC2 con tag gestioneannotazioni-app
INSTANCE_IDS=$(aws ec2 describe-instances --region $REGION --filters Name=tag:gestioneannotazioni-app,Values=true Name=instance-state-name,Values=running,stopped --query 'Reservations[].Instances[].InstanceId' --output text)
if [ -n "$INSTANCE_IDS" ]; then
  echo "Terminazione istanze EC2: $INSTANCE_IDS"
  aws ec2 terminate-instances --instance-ids $INSTANCE_IDS --region $REGION
  aws ec2 wait instance-terminated --instance-ids $INSTANCE_IDS --region $REGION
fi

# 2. Rimuovi Aurora MySQL
aws rds delete-db-instance --db-instance-identifier $DB_INSTANCE_ID --skip-final-snapshot --region $REGION || echo "Istanza Aurora non esistente"
aws rds wait db-instance-deleted --db-instance-identifier $DB_INSTANCE_ID --region $REGION || true
aws rds delete-db-cluster --db-cluster-identifier $DB_CLUSTER_ID --skip-final-snapshot --region $REGION || echo "Cluster Aurora non esistente"
aws rds wait db-cluster-deleted --db-cluster-identifier $DB_CLUSTER_ID --region $REGION || true

# 3. Rimuovi tabella DynamoDB
aws dynamodb delete-table --table-name annotazioni --region $REGION || echo "Tabella annotazioni non esistente"
aws dynamodb delete-table --table-name annotazioni_storico --region $REGION || echo "Tabella annotazioni_storico non esistente"
aws dynamodb delete-table --table-name annotazioni_storicoStati --region $REGION || echo "Tabella annotazioni_storicoStati non esistente"

# 4. Rimuovi coda SQS
echo "Rimozione coda SQS..."
SQS_QUEUE_URL=$(aws sqs get-queue-url --queue-name $SQS_QUEUE_NAME --region $REGION --query 'QueueUrl' --output text 2>/dev/null) || echo "Coda SQS non trovata"
if [ -n "$SQS_QUEUE_URL" ] && [ "$SQS_QUEUE_URL" != "None" ]; then
  echo "Eliminazione coda SQS: $SQS_QUEUE_URL"
  aws sqs delete-queue --queue-url "$SQS_QUEUE_URL" --region $REGION || echo "Errore nella rimozione coda SQS"
else
  echo "Coda SQS $SQS_QUEUE_NAME non esistente"
fi

# 5. Rimuovi Security Group
echo "Rimozione Security Group..."
SG_ID=$(aws ec2 describe-security-groups --region $REGION --filters Name=group-name,Values=$SG_NAME --query 'SecurityGroups[0].GroupId' --output text 2>/dev/null)
if [ "$SG_ID" != "None" ] && [ -n "$SG_ID" ]; then
  echo "Eliminazione Security Group: $SG_ID"
  aws ec2 delete-security-group --group-id $SG_ID --region $REGION || echo "Errore nella rimozione Security Group"
else
  echo "Security Group $SG_NAME non esistente"
fi

# 6. Rimuovi chiave EC2
echo "Rimozione key pair..."
aws ec2 delete-key-pair --key-name $KEY_NAME --region $REGION || echo "Key pair non esistente"
rm -f $KEY_NAME.pem

# 7. Rimuovi IAM Role e Instance Profile
echo "Rimozione IAM Role e Instance Profile..."
ROLE_NAME="gestioneannotazioni-ec2-role"
INSTANCE_PROFILE_NAME="gestioneannotazioni-ec2-profile"
POLICY_ARN="arn:aws:iam::aws:policy/AmazonRDSFullAccess"
POLICY_ARN2="arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
POLICY_ARN3="arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
POLICY_ARN4="arn:aws:iam::aws:policy/AmazonSQSFullAccess"

if aws iam get-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --region $REGION &>/dev/null; then
  aws iam remove-role-from-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --role-name $ROLE_NAME --region $REGION || true
  aws iam delete-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --region $REGION || true
fi
if aws iam get-role --role-name $ROLE_NAME --region $REGION &>/dev/null; then
  aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN --region $REGION || true
  aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN2 --region $REGION || true
  aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN3 --region $REGION || true
  aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN4 --region $REGION || true
  aws iam delete-role --role-name $ROLE_NAME --region $REGION || true
fi

echo "Stack AWS gestioneannotazioni rimosso completamente!"
echo "Risorse rimosse:"
echo "- EC2 instances con tag gestioneannotazioni-app"
echo "- Aurora MySQL cluster e instance"
echo "- Tabelle DynamoDB (annotazioni, annotazioni_storico, annotazioni_storicoStati)"
echo "- Coda SQS ($SQS_QUEUE_NAME)"
echo "- Security Group ($SG_NAME)"
echo "- Key pair ($KEY_NAME)"
echo "- IAM Role e Instance Profile ($INSTANCE_PROFILE_NAME)"