#!/bin/bash
# Configurazione del backend Terraform e "terraform init", condivisa dagli
# stack Terraform (script/aws-terraform-*). Non va eseguito: ogni wrapper lo
# carica con
#   cd "$(dirname "$0")" && source ../aws-tf-init.sh <nome-stack>
# dove <nome-stack> e' la cartella dello stack (aws-terraform-ec2, aws-terraform-ecs)
# e determina la chiave di default dello state: annotazioni/<nome-stack>/terraform.tfstate.
#
# Lo state sta di default su S3; tutto e' sovrascrivibile da variabili di shell:
#   TF_STATE_BUCKET   bucket dello state (default alnao-dev-terraform).
#                     VUOTO (TF_STATE_BUCKET= ) = state LOCALE nella cartella dello stack
#   TF_STATE_REGION   region del bucket (default eu-central-1)
#   TF_STATE_KEY      chiave dell'oggetto (default annotazioni/<nome-stack>/terraform.tfstate)
#
# Il backend "s3" in versions.tf e' dichiarato vuoto (configurazione parziale) e
# viene completato qui con -backend-config. Per lo state locale si genera
# backend_override.tf (ignorato da git): un file *_override.tf sostituisce il
# blocco backend senza toccare i file versionati.
#
# -reconfigure: se cambia bucket/chiave NON si migra lo state vecchio; per
# spostarlo davvero usare a mano "terraform init -migrate-state".

if [ -z "${1:-}" ]; then
  echo "ERRORE: aws-tf-init.sh richiede il nome dello stack come argomento (es. source aws-tf-init.sh aws-terraform-ec2)"
  exit 1
fi
TF_STACK_NAME="$1"

terraform_init_backend() {
  local bucket="${TF_STATE_BUCKET-alnao-dev-terraform}" # "-" e non ":-": vuoto vale "locale"
  local region="${TF_STATE_REGION:-eu-central-1}"
  local key="${TF_STATE_KEY:-annotazioni/$TF_STACK_NAME/terraform.tfstate}"

  if [ -z "$bucket" ]; then
    echo "State Terraform LOCALE: $(pwd)/terraform.tfstate"
    cat > backend_override.tf <<'OVERRIDE'
# Generato da aws-tf-init.sh perche' TF_STATE_BUCKET e' vuoto: state locale.
# File ignorato da git; viene rimosso al prossimo init con bucket S3.
terraform {
  backend "local" {
    path = "terraform.tfstate"
  }
}
OVERRIDE
    terraform init -input=false -reconfigure
  else
    rm -f backend_override.tf
    echo "State Terraform su s3://$bucket/$key ($region), lock nativo S3"
    terraform init -input=false -reconfigure \
      -backend-config="bucket=$bucket" \
      -backend-config="key=$key" \
      -backend-config="region=$region" \
      -backend-config="use_lockfile=true"
  fi
}
