#!/bin/bash
# Rimozione completa stack Annotazioni AWS (Aurora MySQL, DynamoDB, EC2, Security Group)
set -e

# Disabilita paginazione aws cli
export AWS_PAGER=""

REGION="eu-central-1"
DB_CLUSTER_ID="annotazioni-cluster"
DB_INSTANCE_ID="annotazioni-instance"
SG_NAME="annotazioni-sg"
KEY_NAME="${KEY_NAME:-annotazioni-key}"

# 1. Termina e rimuovi tutte le EC2 con tag annotazioni-app
INSTANCE_IDS=$(aws ec2 describe-instances --region $REGION --filters Name=tag:annotazioni-app,Values=true Name=instance-state-name,Values=running,stopped --query 'Reservations[].Instances[].InstanceId' --output text)
if [ -n "$INSTANCE_IDS" ]; then
  aws ec2 terminate-instances --instance-ids $INSTANCE_IDS --region $REGION
  aws ec2 wait instance-terminated --instance-ids $INSTANCE_IDS --region $REGION
fi

# 2. Rimuovi Aurora MySQL
aws rds delete-db-instance --db-instance-identifier $DB_INSTANCE_ID --skip-final-snapshot --region $REGION || true
aws rds wait db-instance-deleted --db-instance-identifier $DB_INSTANCE_ID --region $REGION || true
aws rds delete-db-cluster --db-cluster-identifier $DB_CLUSTER_ID --skip-final-snapshot --region $REGION || true
aws rds wait db-cluster-deleted --db-cluster-identifier $DB_CLUSTER_ID --region $REGION || true

# 3. Rimuovi tabella DynamoDB
aws dynamodb delete-table --table-name annotazioni --region $REGION || true
aws dynamodb delete-table --table-name annotazioni_storico --region $REGION || true

# 4. Rimuovi Security Group
SG_ID=$(aws ec2 describe-security-groups --region $REGION --filters Name=group-name,Values=$SG_NAME --query 'SecurityGroups[0].GroupId' --output text)
if [ "$SG_ID" != "None" ]; then
  aws ec2 delete-security-group --group-id $SG_ID --region $REGION || true
fi

# 5. Rimuovi chiave EC2
aws ec2 delete-key-pair --key-name $KEY_NAME --region $REGION || true
rm -f $KEY_NAME.pem

# 6. Rimuovi IAM Role e Instance Profile
ROLE_NAME="annotazioni-ec2-role"
INSTANCE_PROFILE_NAME="annotazioni-ec2-profile"
POLICY_ARN="arn:aws:iam::aws:policy/AmazonRDSFullAccess"
POLICY_ARN2="arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
POLICY_ARN3="arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"

if aws iam get-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --region $REGION &>/dev/null; then
  aws iam remove-role-from-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --role-name $ROLE_NAME --region $REGION || true
  aws iam delete-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --region $REGION || true
fi
if aws iam get-role --role-name $ROLE_NAME --region $REGION &>/dev/null; then
  aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN --region $REGION || true
  aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN2 --region $REGION || true
  aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN3 --region $REGION || true
  aws iam delete-role --role-name $ROLE_NAME --region $REGION || true
fi

echo "Stack AWS Annotazioni rimosso. Tutte le EC2 con tag annotazioni-app terminate."
