# Stack AWS EC2 con Terraform

Versione **Terraform** dello stack creato da `script/aws-ec2/start-all.sh`: le
stesse risorse (IAM, security group, Aurora MySQL, DynamoDB, SQS, ElastiCache
Redis, key pair, EC2 con Docker) descritte in modo dichiarativo, con gli stessi
nomi e gli stessi tag (`ManagedBy=Terraform`, `Project=Annotazioni.aws-terraform-ec2`).

⚠️ Genera costi reali su AWS (vedi le tabelle in `PlatformAws.md`).

## Uso rapido (dalla root del progetto)

```bash
./script/aws-terraform-ec2/start-all.sh                 # init + apply, stampa gli output
./script/aws-terraform-ec2/test-aws-terraform-ec2.sh    # health, login, export/import SQS, prenotazione
./script/aws-terraform-ec2/stop-all.sh                  # destroy + rimozione del .pem
```

Prerequisiti: Terraform ≥ 1.10, AWS CLI configurata, `curl` e `jq` per il test,
bucket S3 dello state esistente (oppure state locale, vedi sotto).

## Variabili di shell (tutte opzionali)

| Variabile | Default | Effetto |
|-----------|---------|---------|
| `ENVIRONMENT` | `dev` | tag `Environment` (`dev`, `test`, `production`; altro → errore prima di creare risorse) |
| `DB_PASS` | password dimostrativa dello stack bash | password master di Aurora (`TF_VAR_db_password`, mai nei file versionati) |
| `AWS_REGION` | `eu-central-1` | region dello stack |
| `TF_STATE_BUCKET` | `alnao-dev-terraform` | bucket S3 dello state; **vuoto** (`TF_STATE_BUCKET=`) = state locale |
| `TF_STATE_REGION` | `eu-central-1` | region del bucket |
| `TF_STATE_KEY` | `annotazioni/aws-terraform-ec2/terraform.tfstate` | chiave dell'oggetto |

Le altre variabili Terraform (`instance_type`, `db_instance_class`,
`ssh_allowed_cidr`, `key_name`, ...) hanno i default in `variables.tf` e si
cambiano con `TF_VAR_<nome>` o con un `terraform.tfvars` (ignorato da git,
vedi `terraform.tfvars.example`).

Esempi:

```bash
ENVIRONMENT=test ./script/aws-terraform-ec2/start-all.sh          # ambiente di test
TF_STATE_BUCKET=mio-bucket ./script/aws-terraform-ec2/start-all.sh # altro bucket
TF_STATE_BUCKET= ./script/aws-terraform-ec2/start-all.sh           # state locale
```

## Come funziona lo state

`versions.tf` dichiara `backend "s3" {}` **vuoto**: bucket, chiave, region e
`use_lockfile=true` (lock nativo S3, niente tabella DynamoDB) vengono passati
da `script/aws-tf-init.sh` (condiviso dagli stack Terraform) con `terraform init -backend-config=...`. Con
`TF_STATE_BUCKET` vuoto `aws-tf-init.sh` scrive `backend_override.tf` (ignorato da
git) che sostituisce il backend con uno locale: nessun file versionato cambia.

`init` usa `-reconfigure`: cambiando bucket o chiave lo state precedente
**non** viene migrato. Per spostarlo: `terraform init -migrate-state
-backend-config=...` a mano. `stop-all.sh` usa lo stesso backend di
`start-all.sh`: con lo state su S3 il destroy funziona anche da un'altra
macchina con le stesse credenziali.

Per lanciare `terraform` a mano dopo un `start-all.sh`, restare nella cartella:
`terraform plan`, `terraform output`, `terraform state list`.

## Key pair

La key pair `gestioneannotazioni-terraform-key` è generata da Terraform
(`tls_private_key`): la chiave privata viene salvata in
`script/aws-terraform-ec2/gestioneannotazioni-terraform-key.pem` (ignorato da
git, permessi 0400, rimosso da `stop-all.sh`) **e nello state**. Per uno stack
demo con bucket privato è accettabile; in alternativa, per usare una key pair
già esistente su AWS, sostituire in `compute.tf` le tre risorse
`tls_private_key`/`aws_key_pair`/`local_sensitive_file` con
`key_name = "<nome esistente>"` sull'istanza.

## Differenze dallo stack bash

- Stessi nomi per tutto ciò che l'applicazione o gli script cercano per nome
  (security group, Aurora, tabelle DynamoDB, code SQS, Redis, ruolo e profile).
  Diversi: key pair (`…-terraform-key`), `Name` della EC2 e dei volumi
  (`gestioneannotazioni-terraform-ec2[-volume]`) e il **tag marcatore**
  `gestioneannotazioni-terraform-app=true` al posto di `gestioneannotazioni-app=true`.
- Gli `sleep`/retry dello script sono sostituiti dalle attese di Terraform
  (Aurora e Redis `available`); lo user data è lo stesso (`user_data.sh.tftpl`).
- Se lo user data cambia, l'istanza viene **ricreata** (`user_data_replace_on_change`):
  i dati stanno su Aurora/DynamoDB, non sull'istanza.

## Errori da evitare

- **I due stack non coesistono**: con lo stack bash attivo l'apply fallisce
  sulla prima risorsa già esistente (il ruolo IAM) senza toccare le altre.
  Prima `./script/aws-ec2/stop-all.sh`.
- **Non lanciare `script/aws-ec2/stop-all.sh` con lo stack Terraform attivo**:
  cancella per nome Aurora, DynamoDB, SQS, Redis, security group e IAM fuori
  dallo state (la EC2 no, ha un marcatore diverso). Rimedio: un nuovo
  `start-all.sh` rileva ciò che manca e lo ricrea; oppure `stop-all.sh` di
  questa cartella per ripartire da zero.
- **`already exists` su SG, tabelle, code o subnet group ma non sul ruolo IAM**:
  lo stack bash `aws-ec2` non c'è, ma quelle risorse sono condivise con gli
  stack **ECS/EKS** (stessi nomi) oppure sono resti di uno `stop-all.sh`
  incompleto. Diagnosi con i tag:
  `aws resourcegroupstaggingapi get-resources --tag-filters Key=CostCenter,Values=Annotazioni --query 'ResourceTagMappingList[].[ResourceARN, Tags[?Key==`Project`].Value|[0]]' --output table`.
  Rimedio: fermare lo stack che le possiede (`script/aws-ecs/stop-all.sh`,
  oppure `script/aws-ec2/stop-all.sh` per i resti) e rilanciare
  `start-all.sh`; in alternativa adottarle con `terraform import`
  (`aws_security_group.app <sg-id>`, `aws_dynamodb_table.<x> <nome>`,
  `aws_sqs_queue.<x> <url>`, `aws_elasticache_subnet_group.redis <nome>`),
  sapendo che da quel momento le governa Terraform.
- Lo state remoto orfano (dopo aver cancellato risorse a mano) si elimina con
  `aws s3 rm s3://alnao-dev-terraform/annotazioni/aws-terraform-ec2/terraform.tfstate`.

## Verifica senza credenziali

```bash
cd script/aws-terraform-ec2
terraform fmt -check -recursive
terraform init -backend=false
terraform validate
```
