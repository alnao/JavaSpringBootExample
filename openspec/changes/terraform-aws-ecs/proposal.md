## Why

Lo stack ECS Fargate del profilo `aws` (`script/aws-ecs/start-all.sh`, 543
righe) è il più complesso del repository ed è solo imperativo: build e push
dell'immagine, due ruoli IAM, Aurora privata, cluster/task/service ECS, log
group, e un secondo script (`run-ecs-mysql-insert.sh`) per inizializzare il
database, che scarica l'SQL da GitHub `master` invece che dal file locale.
Dopo `terraform-aws-ec2` la voce Roadmap "Script terraform per gli stack ECS
ed EKS" è ancora 🚧: questa change copre ECS con lo stesso approccio (stessi
nomi, stessi tag, stesso state remoto parametrizzabile).

## What Changes

- Nuova cartella `script/aws-terraform-ecs/` con una configurazione Terraform
  che crea **lo stesso stack** di `script/aws-ecs/start-all.sh`: repository
  ECR `gestioneannotazioni`, ruoli IAM `gestioneannotazioni-ecs-task-role` e
  `gestioneannotazioni-ecs-execution-role` con le stesse policy, security
  group `gestioneannotazioni-sg` con le stesse regole (8080/443/22 da tutti,
  3306 e 6379 dal security group stesso), Aurora MySQL **privata**
  `gestioneannotazioni-aurora-cluster` + istanza con `database_name`, le tre
  tabelle DynamoDB, le due code SQS, ElastiCache Redis con subnet group,
  cluster ECS `gestioneannotazioni-cluster`, log group
  `/ecs/gestioneannotazioni-app`, task definition Fargate
  `gestioneannotazioni-task` (512 CPU / 1024 MiB, le stesse 16 variabili
  d'ambiente, log `awslogs`), service `gestioneannotazioni-service` con 1 task
  in `awsvpc` con IP pubblico e propagazione dei tag ai task.
- **Immagine**: il wrapper `start-all.sh` fa `terraform apply -target` del
  solo repository ECR, poi `docker build` (Maven dentro al Dockerfile) e
  `docker push`, poi l'apply completo: il service nasce con l'immagine già
  disponibile.
- **Inizializzazione del database**: task definition Fargate
  `gestioneannotazioni-mysql-init` (immagine `mysql:8.0`) dichiarata in
  Terraform con `script/init-database/init-mysql.sql` incorporato dal file
  locale (base64) ed eseguito con `mysql --force` (rilanciabile: gli indici
  duplicati non fermano lo script); il wrapper la lancia con `run-task` dopo
  l'apply e aspetta che termini. Sostituisce, per lo stack Terraform, il passo
  manuale `run-ecs-mysql-insert.sh` e la dipendenza da GitHub.
- **Versione Aurora**: variabile `db_engine_version` con default `null`
  (versione corrente di AWS per `aurora-mysql`, oggi 8.0); il bash pinna
  `5.7.mysql_aurora.2.11.4` (Aurora MySQL 2, fuori dal supporto standard da
  fine 2024): resta possibile pinnarla con la variabile.
- **Stessi nomi del bash** per tutte le risorse, ECS comprese: lo stack è
  mutuamente esclusivo con lo stack ECS bash e, per le risorse condivise
  (SG, tabelle, code, Redis), anche con gli stack EC2 bash e Terraform.
- Tag via `default_tags`: `Environment` (validata), `Project=Annotazioni.aws-terraform-ecs`,
  `Owner=AlNao`, `CostCenter=Annotazioni`, `ManagedBy=Terraform`, più `Name`
  per risorsa; i task ereditano i tag del service.
- **State remoto** con lo stesso meccanismo di `aws-terraform-ec2`: il file
  `tf-init.sh` viene spostato in `script/aws-tf-init.sh` (condiviso, come
  `aws-tags.sh`) e riceve il nome dello stack come argomento per la chiave di
  default (`annotazioni/<stack>/terraform.tfstate`); i wrapper di
  `aws-terraform-ec2` vengono aggiornati al nuovo percorso senza cambiare
  comportamento.
- Wrapper: `start-all.sh` (init, apply ECR, build/push, apply, init DB, attesa
  del task `RUNNING` e risoluzione dell'IP pubblico tramite la ENI del task,
  come fa `test-aws-ecs.sh`), `stop-all.sh` (destroy; l'ECR è dichiarato con
  `force_delete` per cancellare anche le immagini), `test-aws-terraform-ecs.sh`
  derivato da `test-aws-ecs.sh` con esiti registrati in `lib-report.sh`.
- Documentazione: README della cartella, sezione in `PlatformAws.md`, voce
  Roadmap (ECS fatto, EKS resta 🚧), elenco cartelle in `openspec/project.md`.

## Capabilities

### New Capabilities
- `provisioning-aws-terraform-ecs`: lo stack ECS Fargate del profilo `aws` è
  creabile e distruggibile con Terraform, equivalente allo stack bash, con
  immagine su ECR costruita in locale, database inizializzato da un task
  Fargate dichiarato, state remoto parametrizzabile e test dedicato.

### Modified Capabilities
- `provisioning-aws-tag`: il requisito "Tag standard su ogni risorsa creata"
  aggiunge lo scenario dello stack ECS via Terraform (task compresi); il testo
  del requisito non cambia.

## Impact

- **Profili coinvolti**: solo il deploy del profilo `aws` su ECS Fargate.
  Nessuna modifica al runtime; il container è lo stesso e riceve le stesse
  variabili dello stack bash.
- **Core e adapter**: non toccati. Nessun file Java, nessun `pom.xml`.
- **File nuovi**: `script/aws-terraform-ecs/{versions.tf, variables.tf,
  locals.tf, data.tf, ecr.tf, iam.tf, network.tf, database.tf, dynamodb.tf,
  sqs.tf, cache.tf, ecs.tf, init-db.tf, outputs.tf, terraform.tfvars.example,
  .terraform.lock.hcl, start-all.sh, stop-all.sh, test-aws-terraform-ecs.sh,
  README.md}`; `script/aws-tf-init.sh`.
- **File modificati**: `script/aws-terraform-ec2/{start-all.sh, stop-all.sh,
  README.md}` (solo il percorso del file sorgente; `tf-init.sh` rimosso),
  `PlatformAws.md`, `Roadmap.md`, `openspec/project.md`, spec
  `provisioning-aws-tag`.
- **Compatibilità**: gli script di `script/aws-ecs` non cambiano;
  `test-aws-ecs.sh` e `run-ecs-mysql-insert.sh` funzionerebbero anche sullo
  stack Terraform (stessi nomi) ma non servono. Lanciare
  `script/aws-ecs/stop-all.sh` con lo stack Terraform attivo lo distrugge
  fuori dallo state: documentato come errore da evitare. Lo stack Terraform
  EC2 continua a funzionare identico dopo il refactor di `tf-init.sh`
  (stessa chiave di default).
- **Prerequisiti**: Terraform ≥ 1.10, Docker (build locale: alcuni minuti),
  AWS CLI, `jq`, bucket S3 dello state esistente o state locale.
- **Costi**: gli stessi dello stack ECS bash (Aurora, Redis, Fargate); nessun
  costo aggiuntivo per ECR oltre lo storage dell'immagine. Verifica reale a
  carico dell'utente; l'agente esegue solo `fmt`/`init -backend=false`/
  `validate` e dry-run dei wrapper.

## Non-goals

- Terraform per EKS: change successiva; la voce Roadmap resta 🚧 per EKS.
- Application Load Balancer, auto-scaling o più task: si replica lo stack
  bash (1 task con IP pubblico dinamico).
- Creazione del bucket dello state; hardening delle porte aperte a tutti
  (22 e 443 comprese, come nel bash); password via Secrets Manager (voce
  Roadmap separata).
- Rendere `run-ecs-mysql-insert.sh` indipendente da GitHub: lo stack bash
  resta com'è.
- Inserire il test nella regressione `test-all.sh` (stack cloud a pagamento).
