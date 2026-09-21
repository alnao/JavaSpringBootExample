#!/bin/bash
# Tag comuni per tutte le risorse AWS create dagli script di provisioning.
# Non va eseguito direttamente: ogni script lo carica con
#   source "$(dirname "$0")/../aws-tags.sh" <cartella>
# dove <cartella> e' la cartella dello script (aws-ec2, aws-ecs, aws-eks, sqlite-ec2)
# e diventa il valore del tag Project=Annotazioni.<cartella>.
#
# Tag applicati a ogni risorsa (vedi PlatformAws.md, sezione "Tag delle risorse"):
#   Name         nome proprio della risorsa, oppure gestioneannotazioni-<servizio>
#   Environment  dev (default) | test | production, dalla variabile di shell ENVIRONMENT
#   Project      Annotazioni.<cartella>
#   Owner        AlNao
#   CostCenter   Annotazioni
#   ManagedBy    Sh
#
# Variabili sovrascrivibili PRIMA del source:
#   ENVIRONMENT     ambiente di riferimento (es. ENVIRONMENT=test ./script/aws-ec2/start-all.sh)
#   TAG_MARKER_KEY  tag marcatore usato dai filtri degli script (default gestioneannotazioni-app)
#
# Vincolo: i valori non devono contenere virgole, "=" o spazi, perche' i formati
# shorthand di aws-cli li userebbero come separatori.

# --- Validazione ambiente: ci si ferma prima di creare qualsiasi risorsa ---
TAG_ENVIRONMENT="${ENVIRONMENT:-dev}"
case "$TAG_ENVIRONMENT" in
  dev|test|production) ;;
  *)
    echo "ERRORE: ENVIRONMENT='$TAG_ENVIRONMENT' non ammesso. Valori ammessi: dev, test, production"
    exit 1
    ;;
esac

# --- Valori dei tag ---
if [ -z "${1:-}" ]; then
  echo "ERRORE: aws-tags.sh richiede la cartella dello script come argomento (es. source aws-tags.sh aws-ec2)"
  exit 1
fi
TAG_PROJECT="Annotazioni.$1"
TAG_OWNER="AlNao"
TAG_COSTCENTER="Annotazioni"
TAG_MANAGEDBY="Sh"
TAG_MARKER_KEY="${TAG_MARKER_KEY:-gestioneannotazioni-app}"

# --- Funzioni: una per ogni formato richiesto da aws-cli ---

# Formato lista "Key=k,Value=v Key=k,Value=v": rds, dynamodb, elasticache, ecr, iam, ec2 create-tags
# Uso: --tags $(aws_tags_kv NOME)   <- SENZA virgolette: ogni tag deve restare un argomento separato
aws_tags_kv() {
  echo "Key=Name,Value=$1 Key=Environment,Value=$TAG_ENVIRONMENT Key=Project,Value=$TAG_PROJECT Key=Owner,Value=$TAG_OWNER Key=CostCenter,Value=$TAG_COSTCENTER Key=ManagedBy,Value=$TAG_MANAGEDBY"
}

# Formato mappa "k=v,k=v": sqs tag-queue, logs create-log-group, eksctl --tags, annotation del load balancer
# Uso: --tags "$(aws_tags_map NOME)"
aws_tags_map() {
  echo "Name=$1,Environment=$TAG_ENVIRONMENT,Project=$TAG_PROJECT,Owner=$TAG_OWNER,CostCenter=$TAG_COSTCENTER,ManagedBy=$TAG_MANAGEDBY"
}

# Formato JSON [{"key":..,"value":..}]: ecs create-cluster, create-service, register-task-definition, run-task
# Uso: --tags "$(aws_tags_json NOME)"   oppure in un file JSON:   "tags": $(aws_tags_json NOME),
aws_tags_json() {
  echo "[{\"key\":\"Name\",\"value\":\"$1\"},{\"key\":\"Environment\",\"value\":\"$TAG_ENVIRONMENT\"},{\"key\":\"Project\",\"value\":\"$TAG_PROJECT\"},{\"key\":\"Owner\",\"value\":\"$TAG_OWNER\"},{\"key\":\"CostCenter\",\"value\":\"$TAG_COSTCENTER\"},{\"key\":\"ManagedBy\",\"value\":\"$TAG_MANAGEDBY\"}]"
}

# Formato "ResourceType=t,Tags=[{Key=k,Value=v},...]": ec2 run-instances e create-key-pair (--tag-specifications)
# Uso: --tag-specifications "$(aws_tags_spec instance NOME "$TAG_MARKER_KEY")" "$(aws_tags_spec volume NOME-volume)"
# Il terzo argomento, se presente, aggiunge il tag marcatore <chiave>=true: va usato solo per le istanze EC2,
# perche' gli script stop-all.sh e di test ritrovano le istanze proprio con quel tag.
aws_tags_spec() {
  local tags="{Key=Name,Value=$2},{Key=Environment,Value=$TAG_ENVIRONMENT},{Key=Project,Value=$TAG_PROJECT},{Key=Owner,Value=$TAG_OWNER},{Key=CostCenter,Value=$TAG_COSTCENTER},{Key=ManagedBy,Value=$TAG_MANAGEDBY}"
  if [ -n "${3:-}" ]; then
    tags="$tags,{Key=$3,Value=true}"
  fi
  echo "ResourceType=$1,Tags=[$tags]"
}
