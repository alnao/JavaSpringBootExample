#!/bin/bash
# Avvio completo dello stack Gestione annotazioni su AWS EC2 con TERRAFORM:
# stesse risorse di script/aws-ec2/start-all.sh (Aurora MySQL, DynamoDB, SQS,
# ElastiCache Redis, EC2 con Docker), create in modo dichiarativo.
# Richiede: Terraform >= 1.10, AWS CLI configurata (credenziali), bucket S3
# dello state esistente (oppure TF_STATE_BUCKET= per lo state locale).
#
# Variabili di shell (tutte opzionali, stesse degli script bash):
#   ENVIRONMENT   dev (default) | test | production  -> tag Environment
#   DB_PASS       password master di Aurora (default: stessa dimostrativa dello stack bash)
#   AWS_REGION    region (default eu-central-1)
#   TF_STATE_*    backend dello state, vedi script/aws-tf-init.sh
#
# ATTENZIONE: lo stack bash (script/aws-ec2) usa gli STESSI NOMI di risorsa:
# se e' attivo, l'apply fallisce sulla prima risorsa gia' esistente. Fermarlo
# prima con ./script/aws-ec2/stop-all.sh.

set -e
export AWS_PAGER=""
cd "$(dirname "$0")"

# Variabili Terraform dalle variabili di shell (la validazione di environment e' in variables.tf)
export TF_VAR_environment="${ENVIRONMENT:-dev}"
export TF_VAR_region="${AWS_REGION:-eu-central-1}"
export TF_VAR_db_password="${DB_PASS:-gestioneannotazioni_pass}"

source ../aws-tf-init.sh aws-terraform-ec2   # cwd = cartella dello stack (cd sopra)
terraform_init_backend

echo "Apply dello stack (Aurora e Redis richiedono diversi minuti)..."
terraform apply -auto-approve -input=false

echo ""
echo "Stack avviato! Output:"
terraform output
echo ""
echo "Tutte le risorse sono taggate con Project=Annotazioni.aws-terraform-ec2, Environment=$TF_VAR_environment, ManagedBy=Terraform."
echo "Test:    ./script/aws-terraform-ec2/test-aws-terraform-ec2.sh"
echo "Rimozione: ./script/aws-terraform-ec2/stop-all.sh"
