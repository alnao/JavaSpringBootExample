# Stack AWS ECS Fargate con Terraform

Versione **Terraform** dello stack creato da `script/aws-ecs/start-all.sh`:
le stesse risorse (ECR, ruoli IAM, security group, Aurora MySQL privata,
DynamoDB, SQS, ElastiCache Redis, cluster/task/service ECS Fargate, log group)
con gli stessi nomi e gli stessi tag (`ManagedBy=Terraform`,
`Project=Annotazioni.aws-terraform-ecs`), più l'inizializzazione del database
che nello stack bash è il passo separato `run-ecs-mysql-insert.sh`.

⚠️ Genera costi reali su AWS (vedi le tabelle in `PlatformAws.md`).

## Uso rapido (dalla root del progetto)

```bash
./script/aws-terraform-ecs/start-all.sh                 # init + apply in due passi + build/push + init DB
./script/aws-terraform-ecs/test-aws-terraform-ecs.sh    # health, login, transizioni, import SQS, prenotazione
./script/aws-terraform-ecs/stop-all.sh                  # destroy (immagini ECR comprese)
```

Prerequisiti: Terraform ≥ 1.10, **Docker** (la build compila il progetto con
Maven dentro al Dockerfile: alcuni minuti), AWS CLI configurata, `jq`, bucket
S3 dello state esistente (oppure state locale, vedi sotto).

## Cosa fa `start-all.sh`, e perché l'apply è in due passi

1. `terraform init` con il backend (vedi `script/aws-tf-init.sh`).
2. `terraform apply -target=aws_ecr_repository.app -target=aws_ecs_task_definition.init -target=data.aws_subnets.default`:
   crea il repository ECR e, per dipendenza, Aurora (cluster **e** istanza),
   security group, ruoli, cluster ECS e log group; il data source delle subnet
   è mirato perché un apply mirato scrive solo gli output legati ai target e le
   subnet servono al `run-task` del passo 4. Terraform stampa un avviso
   sul targeting: è atteso, l'apply completo segue al passo 5.
3. `docker build` dalla root del progetto, `docker tag` e `push` su ECR.
4. `aws ecs run-task` della task definition `gestioneannotazioni-mysql-init`
   (immagine `mysql:8.0`, `init-mysql.sql` incorporato dal file locale): il
   wrapper aspetta che termini e si ferma se l'exit code non è 0 (log nel
   log group con prefisso `init`).
5. `terraform apply` completo: Redis, DynamoDB, SQS, task definition
   dell'applicazione e service.
6. Attesa del task `RUNNING` e stampa dell'IP pubblico (ricavato dalla ENI del
   task, come fa il test).

Così il service nasce con l'immagine già su ECR e le tabelle già create:
l'applicazione (`ddl-auto=validate`) non parte in crash-loop come farebbe
lanciando `terraform apply` da solo prima dell'init. Se il wrapper si
interrompe fra un passo e l'altro, lo state resta coerente: basta rilanciarlo.

## Variabili di shell (tutte opzionali)

| Variabile | Default | Effetto |
|-----------|---------|---------|
| `ENVIRONMENT` | `dev` | tag `Environment` (`dev`, `test`, `production`; altro → errore prima di creare risorse) |
| `DB_PASS` | password dimostrativa dello stack bash | password master di Aurora (`TF_VAR_db_password`, mai nei file versionati) |
| `AWS_REGION` | `eu-central-1` | region dello stack |
| `IMAGE_TAG` | `latest` | tag dell'immagine su ECR |
| `SKIP_BUILD` | (vuota) | `1` = salta build e push: utile nei rilanci quando l'immagine è già su ECR |
| `TF_VAR_db_engine_version` | (vuota = versione corrente AWS, oggi 8.0) | `5.7.mysql_aurora.2.11.4` per replicare lo script bash — Aurora MySQL 2 è fuori dal supporto standard: può non essere più creabile o costare di più (extended support) |
| `TF_STATE_BUCKET` / `TF_STATE_REGION` / `TF_STATE_KEY` | `alnao-dev-terraform` / `eu-central-1` / `annotazioni/aws-terraform-ecs/terraform.tfstate` | backend dello state; `TF_STATE_BUCKET=` vuoto = state locale |

Le altre variabili (`task_cpu`, `task_memory`, `desired_count`,
`db_instance_class`, ...) hanno i default in `variables.tf` e si cambiano con
`TF_VAR_<nome>` o con un `terraform.tfvars` (ignorato da git, vedi
`terraform.tfvars.example`).

## State

Stesso meccanismo dello stack EC2 (`script/aws-tf-init.sh`): backend S3 con
configurazione parziale completata da `-backend-config`, lock nativo S3,
`backend_override.tf` generato (e ignorato da git) per lo state locale. La
chiave di default è distinta da quella dello stack EC2, i due state non si
sovrappongono. `init` usa `-reconfigure`: cambiando bucket o chiave lo state
precedente non viene migrato (`terraform init -migrate-state` a mano).

## Differenze dallo stack bash

- Nessuna: stessi nomi per tutte le risorse, ECS comprese, quindi
  `script/aws-ecs/test-aws-ecs.sh` funzionerebbe anche qui. Il test dedicato
  registra però gli esiti con `lib-report.sh`.
- Il DB viene inizializzato dal wrapper (task Fargate dichiarato in
  `init-db.tf`, SQL letto dal file locale e rilanciabile grazie a `mysql --force`)
  invece che con `run-ecs-mysql-insert.sh`, che scarica l'SQL da GitHub.
- Aurora: versione parametrica (default corrente AWS) invece del pin 5.7.
- La password di Aurora compare nelle task definition (`RDS_PASSWORD`,
  `MYSQL_PWD`) come nello stack bash: la gestione via secret è una voce della
  Roadmap. Nel `plan` Terraform mostra le `container_definitions` come
  `(sensitive value)` proprio per questo.

## Errori da evitare

- **Questo stack non coesiste** con lo stack ECS bash né, per SG/DynamoDB/SQS/Redis,
  con gli stack EC2 (bash o Terraform): l'apply fallisce sulla prima risorsa
  già esistente. Prima lo `stop-all.sh` della cartella che l'ha creata.
  Diagnosi con i tag:
  `aws resourcegroupstaggingapi get-resources --tag-filters Key=CostCenter,Values=Annotazioni --query 'ResourceTagMappingList[].[ResourceARN, Tags[?Key==`Project`].Value|[0]]' --output table`.
- **Non lanciare `script/aws-ecs/stop-all.sh` con questo stack attivo**:
  cancellerebbe tutto per nome fuori dallo state. Usare sempre
  `script/aws-terraform-ecs/stop-all.sh`.
- `terraform apply` a mano senza il wrapper crea il service prima dell'init e
  con l'ECR vuoto: il task non parte finché non si fanno build/push e init.
- Lo state remoto orfano si elimina con
  `aws s3 rm s3://alnao-dev-terraform/annotazioni/aws-terraform-ecs/terraform.tfstate`.

## Verifica senza credenziali

```bash
cd script/aws-terraform-ecs
terraform fmt -check -recursive
terraform init -backend=false
terraform validate
```
