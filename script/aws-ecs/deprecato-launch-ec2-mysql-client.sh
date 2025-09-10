# questo script è deprecato, usare script/aws-ecs/run-mysql-client-aurora.sh


#!/bin/bash
# Script: launch-ec2-mysql-client.sh
# Avvia una EC2 nella stessa VPC e Security Group dei task ECS per eseguire operazioni MySQL su Aurora
# Richiede: AWS CLI configurata, permessi su EC2, IAM, VPC

set -euo pipefail
export AWS_PAGER=""

AWS_REGION="eu-central-1"
#AMI_ID="ami-0c55b159cbfafe1f0" # Amazon Linux 2, aggiorna se necessario
AMI_ID=$(aws ec2 describe-images --owners amazon --filters "Name=name,Values=amzn2-ami-hvm-2.0.*-x86_64-gp2" --region $AWS_REGION --query 'Images | sort_by(@, &CreationDate)[-1].ImageId' --output text)
INSTANCE_TYPE="t3.micro"
KEY_NAME="alberto-nao-francoforte" # Aggiorna se necessario
VPC_ID=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --region $AWS_REGION --query 'Vpcs[0].VpcId' --output text)
SUBNET_ID=$(aws ec2 describe-subnets --filters Name=vpc-id,Values=$VPC_ID --region $AWS_REGION --query 'Subnets[0].SubnetId' --output text)
SECURITY_GROUP_ID=$(aws ec2 describe-security-groups --filters Name=group-name,Values=gestioneannotazioni-sg Name=vpc-id,Values=$VPC_ID --region $AWS_REGION --query 'SecurityGroups[0].GroupId' --output text)

# Avvia EC2
INSTANCE_ID=$(aws ec2 run-instances \
  --image-id $AMI_ID \
  --count 1 \
  --instance-type $INSTANCE_TYPE \
  --key-name $KEY_NAME \
  --subnet-id $SUBNET_ID \
  --security-group-ids $SECURITY_GROUP_ID \
  --region $AWS_REGION \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=gestioneannotazioni-mysql-client}]' \
  --query 'Instances[0].InstanceId' --output text)

echo "EC2 avviata: $INSTANCE_ID"

# Attendi che sia running
aws ec2 wait instance-running --instance-ids $INSTANCE_ID --region $AWS_REGION

# Recupera IP pubblico
PUBLIC_IP=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --region $AWS_REGION --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)
echo "EC2 Public IP: $PUBLIC_IP"

echo "Puoi collegarti con: ssh -i $KEY_NAME ec2-user@$PUBLIC_IP"

echo "Ricorda di installare il client MySQL e di terminare la EC2 quando hai finito."

# sudo yum install mysql-server
# mysql -h <aurora-endpoint> -u<user> -p<password> <database>

# aws ec2 terminate-instances --instance-ids $INSTANCE_ID --region $AWS_REGION