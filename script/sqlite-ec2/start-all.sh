#!/bin/bash
# Avvio completo stack Gestione annotazioni AWS con profilo sqlite (EC2 Docker)
# Richiede AWS CLI configurato ma non usa RDS nè DynamoDB, solo EC2 con Docker e SQLite

set -e
# Parametri
REGION="eu-central-1"
PARAM_KEY_NAME="gestioneannotazioni-sqlite-ec2-key"
EC2_INSTANCE_TYPE="t3.medium"
EC2_COUNT=1
VPC_ID=$(aws ec2 describe-vpcs --region $REGION --filters Name=isDefault,Values=true --query 'Vpcs[0].VpcId' --output text)

echo "Avvio stack gestione annotazioni AWS con profilo sqlite (EC2 Docker) nella regione $REGION"

# 1. Crea Security Group
SG_NAME="gestioneannotazioni-sqlite-ec2-sg"
SG_ID=$(aws ec2 create-security-group --group-name $SG_NAME --description "gestioneannotazioni-sqlite-ec2 SG" --vpc-id $VPC_ID --region $REGION --output text)
aws ec2 create-tags --resources $SG_ID --tags Key=Name,Value=gestioneannotazioni-sqlite-ec2-app Key=gestioneannotazioni-sqlite-ec2-app,Value=true --region $REGION
# Apre porte per app (8082), adminer (8084)
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 8082 --cidr 0.0.0.0/0 --region $REGION
# SSH solo per IP chiamante
MY_IP=$(curl -s https://checkip.amazonaws.com | tr -d '\n')
#aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 8084 --cidr $MY_IP/32 --region $REGION
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 22 --cidr $MY_IP/32 --region $REGION

# 2. Crea key pair
aws ec2 create-key-pair --key-name $PARAM_KEY_NAME --region $REGION --query 'KeyMaterial' --output text > $PARAM_KEY_NAME.pem
chmod 400 $PARAM_KEY_NAME.pem

# 3. Avvio EC2 con user_data per SQLite
AMI_ID=$(aws ec2 describe-images --owners amazon --filters "Name=name,Values=amzn2-ami-hvm-2.0.*-x86_64-gp2" --region $REGION --query 'Images | sort_by(@, &CreationDate)[-1].ImageId' --output text)
USER_DATA=$(cat <<'EOF'
#!/bin/bash
sudo yum update -y
sudo amazon-linux-extras install docker -y
sudo service docker start
sudo usermod -a -G docker ec2-user

# Avvio container con profilo sqlite

# Crea rete Docker dedicata
sudo docker network create sqlite-net || true
# Crea volume condiviso per SQLite
sudo docker volume create sqlite-data || true

# Fix permessi volume Docker (sqlite-data)
sudo docker run --rm -v sqlite-data:/data busybox sh -c 'chmod 777 /data'

# Crea file vuoto database.sqlite nel volume condiviso
sudo docker run --rm -v sqlite-data:/data busybox sh -c 'touch /data/database.sqlite && chmod 666 /data/database.sqlite'

# Avvio container Spring Boot con volume condiviso e rete
sudo docker run -d --name gestioneannotazioni \
  --network sqlite-net \
  -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=sqlite \
  -e SERVER_PORT=8082 \
  -e SPRING_DATASOURCE_URL=jdbc:sqlite:/data/database.sqlite \
  -e APP_CREATE_USERS_ENABLED=true \
  -v sqlite-data:/data \
  alnao/gestioneannotazioni:latest

echo "Container avviato: gestioneannotazioni (http://$PUBLIC_IP:8082)"

EOF
)

INSTANCE_ID=$(aws ec2 run-instances \
  --image-id $AMI_ID \
  --count $EC2_COUNT \
  --instance-type $EC2_INSTANCE_TYPE \
  --key-name $PARAM_KEY_NAME \
  --security-group-ids $SG_ID \
  --region $REGION \
  --user-data "$USER_DATA" \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=gestioneannotazioni-sqlite-ec2-app},{Key=gestioneannotazioni-sqlite-ec2-app,Value=true}]' \
  --query 'Instances[0].InstanceId' --output text)

aws ec2 wait instance-running --instance-ids $INSTANCE_ID --region $REGION
PUBLIC_IP=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --region $REGION --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)

echo "Stack avviato! EC2 IP: $PUBLIC_IP"
echo "Gestione annotazioni SQLite disponibile su http://$PUBLIC_IP:8082"
echo "Per accedere via SSH: ssh -i $PARAM_KEY_NAME.pem ec2-user@$PUBLIC_IP"
