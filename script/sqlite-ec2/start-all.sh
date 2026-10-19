#!/bin/bash
# Avvio completo stack Gestione annotazioni AWS con profilo sqlite (EC2 Docker)
# Richiede AWS CLI configurato ma non usa RDS nè DynamoDB, solo EC2 con Docker e SQLite

set -e
# Disabilita paginazione aws cli
export AWS_PAGER=""

# Parametri
REGION="eu-central-1"
# Tag comuni a tutte le risorse (Name, Environment, Project, Owner, CostCenter, ManagedBy): vedi script/aws-tags.sh
# Il marcatore usato da stop-all.sh per ritrovare la EC2 e' diverso da quello dello stack aws-ec2
TAG_MARKER_KEY="gestioneannotazioni-sqlite-ec2-app"
source "$(dirname "$0")/../aws-tags.sh" sqlite-ec2
PARAM_KEY_NAME="gestioneannotazioni-sqlite-ec2-key"
EC2_INSTANCE_TYPE="t3.medium"
EC2_COUNT=1
VPC_ID=$(aws ec2 describe-vpcs --region $REGION --filters Name=isDefault,Values=true --query 'Vpcs[0].VpcId' --output text)

echo "Avvio stack gestione annotazioni AWS con profilo sqlite (EC2 Docker) nella regione $REGION"

# 1. Crea Security Group
SG_NAME="gestioneannotazioni-sqlite-ec2-sg"
SG_ID=$(aws ec2 create-security-group --group-name $SG_NAME --description "gestioneannotazioni SG" --vpc-id $VPC_ID --region $REGION --query 'GroupId' --output text 2>/dev/null) || {
  echo "Security Group già esistente, recupero l'ID..."
  SG_ID=$(aws ec2 describe-security-groups --group-names $SG_NAME --region $REGION --query 'SecurityGroups[0].GroupId' --output text)
}
aws ec2 create-tags --resources $SG_ID --tags $(aws_tags_kv $SG_NAME) Key=$TAG_MARKER_KEY,Value=true --region $REGION
# Apre porte per app (8082), adminer (8084)
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 8082 --cidr 0.0.0.0/0 --region $REGION
# SSH solo per IP chiamante
MY_IP=$(curl -s https://checkip.amazonaws.com | tr -d '\n')
#aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 8084 --cidr $MY_IP/32 --region $REGION
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 22 --cidr $MY_IP/32 --region $REGION

# 2. Crea key pair
aws ec2 create-key-pair --key-name $PARAM_KEY_NAME --region $REGION --tag-specifications "$(aws_tags_spec key-pair $PARAM_KEY_NAME)" --query 'KeyMaterial' --output text > $PARAM_KEY_NAME.pem
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

# Credenziali dell'applicazione (change openspec secret-sqlite): generate qui sull'istanza, leggibili
# solo da root, mai stampate (l'output dello user-data finisce in /var/log/cloud-init-output.log).
# Il file non viene rigenerato se esiste gia': lo aggiorna solo rotate-secret.sh.
CONF_DIR="${CONF_DIR:-/etc/gestioneannotazioni}"
umask 077
sudo mkdir -p "$CONF_DIR"
if [ ! -f "$CONF_DIR/app.env" ]; then
  {
    echo "JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')"
    echo "APP_USER_ADMIN_PASSWORD=$(openssl rand -base64 18)"
    echo "APP_USER_MODERATORE_PASSWORD=$(openssl rand -base64 18)"
    echo "APP_USER_ALNAO_PASSWORD=$(openssl rand -base64 18)"
  } | sudo tee "$CONF_DIR/app.env" > /dev/null
  sudo chmod 600 "$CONF_DIR/app.env"
fi

# Unico punto con i parametri del container: lo usano questo avvio e rotate-secret.sh per ricrearlo
# (le variabili d'ambiente di un container si fissano alla creazione, un docker restart non basta)
sudo tee "$CONF_DIR/run-app.sh" > /dev/null <<RUNAPP
#!/bin/bash
docker rm -f gestioneannotazioni > /dev/null 2>&1 || true
docker run -d --name gestioneannotazioni \\
  --network sqlite-net \\
  -p 8082:8082 \\
  --env-file $CONF_DIR/app.env \\
  -e SPRING_PROFILES_ACTIVE=sqlite \\
  -e SERVER_PORT=8082 \\
  -e SPRING_DATASOURCE_URL=jdbc:sqlite:/data/database.sqlite \\
  -e APP_CREATE_USERS_ENABLED=true \\
  -v sqlite-data:/data \\
  alnao/gestioneannotazioni:latest
RUNAPP
sudo chmod 700 "$CONF_DIR/run-app.sh"

# Avvio container Spring Boot con volume condiviso, rete e file di credenziali
sudo "$CONF_DIR/run-app.sh"

echo "Container avviato: gestioneannotazioni (porta 8082)"

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
  --tag-specifications "$(aws_tags_spec instance gestioneannotazioni-sqlite-ec2 $TAG_MARKER_KEY)" "$(aws_tags_spec volume gestioneannotazioni-sqlite-ec2-volume)" \
  --query 'Instances[0].InstanceId' --output text)

aws ec2 wait instance-running --instance-ids $INSTANCE_ID --region $REGION

# Elastic IP: lo script non ne alloca; se uno e' gia' associato all'istanza (console o altra
# automazione) riceve i tag standard. Best effort: un errore di tagging non ferma lo script.
EIP_ALLOC_ID=$(aws ec2 describe-addresses --filters Name=instance-id,Values=$INSTANCE_ID --region $REGION --query 'Addresses[0].AllocationId' --output text 2>/dev/null || true)
if [ -n "$EIP_ALLOC_ID" ] && [ "$EIP_ALLOC_ID" != "None" ]; then
  aws ec2 create-tags --resources $EIP_ALLOC_ID --tags $(aws_tags_kv gestioneannotazioni-sqlite-ec2-eip) --region $REGION \
    || echo "Avviso: tag dell'Elastic IP $EIP_ALLOC_ID non applicati"
fi

PUBLIC_IP=$(aws ec2 describe-instances --instance-ids $INSTANCE_ID --region $REGION --query 'Reservations[0].Instances[0].PublicIpAddress' --output text)

echo "Stack avviato! EC2 IP: $PUBLIC_IP"
echo "Gestione annotazioni SQLite disponibile su http://$PUBLIC_IP:8082"
echo "Per accedere via SSH: ssh -i $PARAM_KEY_NAME.pem ec2-user@$PUBLIC_IP"
echo ""
echo "Credenziali (generate sull'istanza al primo avvio, mai trasmesse da qui):"
echo "  ssh -i $PARAM_KEY_NAME.pem ec2-user@$PUBLIC_IP sudo cat /etc/gestioneannotazioni/app.env"
echo "  utente admin = APP_USER_ADMIN_PASSWORD; l'app e' pronta qualche minuto dopo l'avvio dell'istanza"
echo "Rotazione della chiave JWT: ./script/sqlite-ec2/rotate-secret.sh JWT_SECRET (dalla root del progetto)"
