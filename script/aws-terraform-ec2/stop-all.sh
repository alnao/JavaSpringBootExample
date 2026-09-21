#!/bin/bash
# Rimozione completa dello stack creato da start-all.sh (terraform destroy):
# EC2, key pair, Redis, code SQS, tabelle DynamoDB, Aurora (senza snapshot
# finale), security group, instance profile e ruolo IAM.
# ATTENZIONE: cancella tutti i dati nei database, operazione irreversibile.
#
# Usa lo stesso backend di start-all.sh (variabili TF_STATE_*, vedi script/aws-tf-init.sh).
# Senza uno stack applicato termina senza errori ("No changes").

set -e
export AWS_PAGER=""
cd "$(dirname "$0")"

# Le variabili sono richieste da Terraform anche in destroy (la password non viene usata)
export TF_VAR_environment="${ENVIRONMENT:-dev}"
export TF_VAR_region="${AWS_REGION:-eu-central-1}"
export TF_VAR_db_password="${DB_PASS:-gestioneannotazioni_pass}"

source ../aws-tf-init.sh aws-terraform-ec2   # cwd = cartella dello stack (cd sopra)
terraform_init_backend

echo "Destroy dello stack (Aurora richiede alcuni minuti)..."
terraform destroy -auto-approve -input=false

# Il file .pem della key pair generata non serve piu' (la key pair e' stata cancellata)
rm -f ./*.pem
echo "Stack AWS gestioneannotazioni (Terraform) rimosso completamente."
