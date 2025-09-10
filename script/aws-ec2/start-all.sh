#!/bin/bash

# Avvio completo stack Gestione annotazioni AWS (Aurora MySQL, DynamoDB, EC2 Docker)
# Richiede AWS CLI configurato e permessi admin

set -e

# Disabilita paginazione aws cli
export AWS_PAGER=""

REGION="eu-central-1"
PARAM_KEY_NAME="${KEY_NAME:-gestioneannotazioni-key}"
DB_INSTANCE_CLASS="${DB_INSTANCE_CLASS:-db.t3.medium}"
EC2_INSTANCE_TYPE="${EC2_INSTANCE_TYPE:-t3.medium}"
EC2_COUNT="${EC2_COUNT:-1}"
VPC_ID=$(aws ec2 describe-vpcs --region $REGION --filters Name=isDefault,Values=true --query 'Vpcs[0].VpcId' --output text)

# 0. Crea IAM Role e Instance Profile se non esistono
ROLE_NAME="gestioneannotazioni-ec2-role"
INSTANCE_PROFILE_NAME="gestioneannotazioni-ec2-profile"
POLICY_ARN="arn:aws:iam::aws:policy/AmazonRDSFullAccess" 
#ex AmazonRDSReadOnlyAccess
POLICY_ARN2="arn:aws:iam::aws:policy/AmazonDynamoDBFullAccess"
POLICY_ARN3="arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"

if ! aws iam get-role --role-name $ROLE_NAME --region $REGION &>/dev/null; then
  aws iam create-role --role-name $ROLE_NAME --assume-role-policy-document '{"Version":"2012-10-17","Statement":[{"Effect":"Allow","Principal":{"Service":"ec2.amazonaws.com"},"Action":"sts:AssumeRole"}]}' --region $REGION
  aws iam attach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN --region $REGION
  aws iam attach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN2 --region $REGION
  aws iam attach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN3 --region $REGION
fi
if ! aws iam get-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --region $REGION &>/dev/null; then
  aws iam create-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --region $REGION
  sleep 5
  aws iam add-role-to-instance-profile --instance-profile-name $INSTANCE_PROFILE_NAME --role-name $ROLE_NAME --region $REGION
fi

# 1. Crea Security Group con tag
SG_NAME="gestioneannotazioni-sg"
SG_ID=$(aws ec2 create-security-group --group-name $SG_NAME --description "gestioneannotazioni SG" --vpc-id $VPC_ID --region $REGION --output text)
aws ec2 create-tags --resources $SG_ID --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true --region $REGION
# Apre porte per MySQL (3306), app (8080), adminer (8086), dynamodb-admin (8087)
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 3306 --cidr 0.0.0.0/0 --region $REGION
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 8080 --cidr 0.0.0.0/0 --region $REGION
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 8086 --cidr 0.0.0.0/0 --region $REGION
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 8087 --cidr 0.0.0.0/0 --region $REGION

# Ottieni IP pubblico chiamante per SSH
MY_IP=$(curl -s https://checkip.amazonaws.com | tr -d '\n')
# Apre porta SSH solo per l'IP chiamante
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 22 --cidr $MY_IP/32 --region $REGION

# 2. Crea Aurora MySQL con tag
DB_CLUSTER_ID="gestioneannotazioni-cluster"
DB_INSTANCE_ID="gestioneannotazioni-instance"
DB_NAME="gestioneannotazioni"
DB_USER="gestioneannotazioni_user"
DB_PASS="gestioneannotazioni_pass"
aws rds create-db-cluster \
  --db-cluster-identifier $DB_CLUSTER_ID \
  --engine aurora-mysql \
  --master-username $DB_USER \
  --master-user-password $DB_PASS \
  --vpc-security-group-ids $SG_ID \
  --region $REGION \
  --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true
aws rds create-db-instance \
  --db-instance-identifier $DB_INSTANCE_ID \
  --db-cluster-identifier $DB_CLUSTER_ID \
  --engine aurora-mysql \
  --db-instance-class $DB_INSTANCE_CLASS \
  --region $REGION \
  --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true \
  --publicly-accessible

# Attendi che Aurora sia disponibile prima di recuperare l'endpoint
echo "Attendo che Aurora cluster sia disponibile..."
aws rds wait db-cluster-available --db-cluster-identifier $DB_CLUSTER_ID --region $REGION

# Recupera endpoint Aurora (ora dovrebbe essere pronto)
AURORA_ENDPOINT=""
for i in {1..10}; do
  AURORA_ENDPOINT=$(aws rds describe-db-clusters --db-cluster-identifier $DB_CLUSTER_ID --region $REGION --query 'DBClusters[0].Endpoint' --output text 2>/dev/null)
  STATUS=$(aws rds describe-db-clusters --db-cluster-identifier $DB_CLUSTER_ID --region $REGION --query 'DBClusters[0].Status' --output text 2>/dev/null)
  echo "Tentativo $i: endpoint=$AURORA_ENDPOINT, status=$STATUS"
  if [ -n "$AURORA_ENDPOINT" ] && [ "$AURORA_ENDPOINT" != "None" ] && [ "$AURORA_ENDPOINT" != "null" ]; then
    echo "Aurora endpoint trovato: $AURORA_ENDPOINT"
    break
  fi
  echo "Attendo endpoint Aurora... ($i)"
  sleep 15
done

if [ -z "$AURORA_ENDPOINT" ] || [ "$AURORA_ENDPOINT" = "None" ] || [ "$AURORA_ENDPOINT" = "null" ]; then
  echo "ERRORE: Impossibile ottenere l'endpoint Aurora dopo $i tentativi"
  exit 1
fi

echo "Aurora endpoint finale: $AURORA_ENDPOINT"

# 3. Crea tabella DynamoDB con tag
aws dynamodb create-table \
  --table-name annotazioni \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region $REGION \
  --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true || echo "Tabella DynamoDB già esistente o errore ignorato."

# 3b. Crea tabella DynamoDB per lo storico
aws dynamodb create-table \
  --table-name annotazioni_storico \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region $REGION \
  --tags Key=Name,Value=gestioneannotazioni-app Key=gestioneannotazioni-app,Value=true || echo "Tabella DynamoDB storico già esistente o errore ignorato."

# 4. Crea key pair parametrica
aws ec2 create-key-pair --key-name $PARAM_KEY_NAME --region $REGION --query 'KeyMaterial' --output text > $PARAM_KEY_NAME.pem
chmod 400 $PARAM_KEY_NAME.pem

#  AURORA_ENDPOINT=$(aws rds describe-db-clusters --db-cluster-identifier $DB_CLUSTER_ID --region $REGION --query 'DBClusters[0].Endpoint' --output text)
echo "Eseguo EC2 con endpoint AURORA_ENDPOINT=$AURORA_ENDPOINT"

# 5. Crea EC2 con tag e user_data per avvio Docker
AMI_ID=$(aws ec2 describe-images --owners amazon --filters "Name=name,Values=amzn2-ami-hvm-2.0.*-x86_64-gp2" --region $REGION --query 'Images | sort_by(@, &CreationDate)[-1].ImageId' --output text)
USER_DATA=$(base64 -w0 <<EOF
#!/bin/bash
sudo yum update -y
sudo amazon-linux-extras install docker -y
sudo yum install -y mysql
sudo service docker start
sudo usermod -a -G docker ec2-user

AURORA_HOST="${AURORA_ENDPOINT}"
DB_USER="${DB_USER}"
DB_PASS="${DB_PASS}"
DB_NAME="${DB_NAME}"
REGION="${REGION}"
echo "Host=\$AURORA_HOST"
echo "User=\$DB_USER"
echo "Pass=\$DB_PASS"
echo "Dbname=\$DB_NAME"
echo "Region=\$REGION"

# Test connessione diretta (Aurora dovrebbe essere gia pronto)
for i in {1..3}; do
  echo "[EC2 user_data] Tentativo \$i: controllo se Aurora risponde su \$AURORA_HOST:3306..."
  if mysql -h "\$AURORA_HOST" -u"\$DB_USER" -p"\$DB_PASS" -e "SELECT 1" 2>/dev/null; then
    echo "Aurora risponde."
    break
  fi
  echo "Aurora non ancora pronta, attendo 30 secondi..."
  sleep 30
done

cat <<'EOSQL' > /tmp/init-mysql.sql
$(cat ./script/init-database/init-mysql.sql)
EOSQL

mysql -h "\$AURORA_HOST" -u"\$DB_USER" -p"\$DB_PASS" < /tmp/init-mysql.sql

for i in {1..3}; do
  echo "[EC2 user_data] Avvio microservizio (tentativo \${i})..."
  sudo docker run -d -p 8080:8080 \
    -e SPRING_PROFILES_ACTIVE=aws \
    -e AWS_RDS_URL="jdbc:mysql://\$AURORA_HOST:3306/\$DB_NAME" \
    -e AWS_RDS_USERNAME=\$DB_USER \
    -e AWS_RDS_PASSWORD=\$DB_PASS \
    -e AWS_REGION=\$REGION \
    -e DYNAMODB_ANNOTAZIONI_TABLE_NAME=annotazioni \
    -e AWS_ACCESS_KEY_ID= \
    -e AWS_SECRET_ACCESS_KEY= \
    alnao/gestioneannotazioni:latest
  sleep 10
  if sudo docker ps | grep alnao/gestioneannotazioni; then
    echo "Microservizio avviato con successo."
    break
  fi
  echo "Microservizio non avviato, attendo 120 secondi e riprovo..."
  sleep 30
done
EOF
)

echo "Usando AMI_ID=$AMI_ID"
echo "-----------"
echo $USER_DATA
echo "-----------"
echo "Avvio EC2..."

INSTANCE_ID=$(aws ec2 run-instances \
  --image-id $AMI_ID \
  --count $EC2_COUNT \
  --instance-type $EC2_INSTANCE_TYPE \
  --key-name $PARAM_KEY_NAME \
  --security-group-ids $SG_ID \
  --region $REGION \
  --user-data $USER_DATA \
  --iam-instance-profile Name=$INSTANCE_PROFILE_NAME \
  --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=gestioneannotazioni-app},{Key=gestioneannotazioni-app,Value=true}]' \
  --query 'Instances[0].InstanceId' --output text)

# Attendi che EC2 sia running
aws ec2 wait instance-running --instance-ids $INSTANCE_ID --region $REGION
PUBLIC_IP=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --region $REGION --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)

echo "Stack avviato! EC2 IP: $PUBLIC_IP"
echo "Aurora MySQL, DynamoDB e Security Group creati. Tutte le risorse taggate gestioneannotazioni-app."

# Vecchia versione con copia file SQL via SCP e SSH per esecuzione sostituito dallo script in user_data
# Copia il file init-mysql.sql sulla EC2
#scp -i $PARAM_KEY_NAME.pem -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ../init-database/init-mysql.sql ec2-user@$PUBLIC_IP:/tmp/init-mysql.sql
# Esegui lo script SQL su Aurora tramite SSH sulla EC2
#ssh -i $PARAM_KEY_NAME.pem -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ec2-user@$PUBLIC_IP "mysql -h $AURORA_ENDPOINT -u$DB_USER -p$DB_PASS < /tmp/init-mysql.sql"
