#!/bin/bash
set -e
# Disabilita paginazione aws cli
export AWS_PAGER=""

# Parametri
REGION="eu-central-1"
SG_NAME="gestioneannotazioni-sqlite-ec2-sg"
PARAM_KEY_NAME="gestioneannotazioni-sqlite-ec2-key"

# Trova e termina le istanze EC2 con tag sqlite-ec2-app
INSTANCE_IDS=$(aws ec2 describe-instances --region $REGION --filters "Name=tag:gestioneannotazioni-sqlite-ec2-app,Values=true" "Name=instance-state-name,Values=running" --query 'Reservations[*].Instances[*].InstanceId' --output text)
if [ -n "$INSTANCE_IDS" ]; then
  echo "Termino le istanze EC2: $INSTANCE_IDS"
  aws ec2 terminate-instances --instance-ids $INSTANCE_IDS --region $REGION
  aws ec2 wait instance-terminated --instance-ids $INSTANCE_IDS --region $REGION
else
  echo "Nessuna istanza EC2 da terminare."
fi

# Rimuovi security group
SG_ID=$(aws ec2 describe-security-groups --region $REGION --filters Name=group-name,Values=$SG_NAME --query 'SecurityGroups[0].GroupId' --output text)
if [ "$SG_ID" != "None" ]; then
  echo "Rimuovo security group: $SG_ID"
  aws ec2 delete-security-group --group-id $SG_ID --region $REGION || true
else
  echo "Security group non trovato."
fi

# Rimuovi key pair
if aws ec2 describe-key-pairs --key-names $PARAM_KEY_NAME --region $REGION &>/dev/null; then
  echo "Rimuovo key pair: $PARAM_KEY_NAME"
  aws ec2 delete-key-pair --key-name $PARAM_KEY_NAME --region $REGION
  rm -f $PARAM_KEY_NAME.pem
else
  echo "Key pair non trovato."
fi

echo "Stack SQLite EC2 fermato e risorse rimosse."
