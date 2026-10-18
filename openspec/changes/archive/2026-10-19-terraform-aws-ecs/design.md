## Context

Vedi `proposal.md` - Why. Stato attuale rilevante:

- `script/aws-ecs/start-all.sh` (letto per intero): ECR + `docker build .`
  dalla root (il Dockerfile compila con Maven in uno stage builder) + push →
  ruoli IAM task/execution (taggati a ogni giro) → SG → Aurora **pinnata a
  5.7.mysql_aurora.2.11.4** con `--database-name`, istanza non pubblica →
  DynamoDB → SQS → ElastiCache → cluster ECS → log group → task definition
  scritta in `task-def.json` via heredoc (16 variabili, `awslogs`) → service
  (desired 1, `awsvpc`, IP pubblico, propagate tags) → attesa task RUNNING e
  IP pubblico tramite ENI. **Nessuna inizializzazione del DB**: è il passo
  manuale `run-ecs-mysql-insert.sh` (task Fargate `mysql:8.0` che scarica
  l'SQL da GitHub `master`, deregistrato alla fine).
- L'applicazione gira con `ddl-auto=validate`: senza tabelle il container
  esce e ECS lo riavvia in loop finché il DB non è inizializzato. Nel bash
  questo è tollerato; nel Terraform l'ordine giusto è: DB inizializzato
  **prima** del service.
- `init-mysql.sql` (7,4 KB) non è idempotente per gli indici (`CREATE INDEX`
  senza `IF NOT EXISTS`, che MySQL non supporta): un secondo run fallisce alla
  riga 60 se lanciato con `mysql` senza `--force`.
- `test-aws-ecs.sh` (297 righe) trova il task per nome cluster/service,
  risolve l'IP pubblico via `describe-tasks` → ENI → `describe-network-interfaces`,
  attende la disponibilità, poi esegue gli stessi controlli funzionali del
  test EC2. Non usa `lib-report.sh`.
- `aws-terraform-ec2` ha già: `tf-init.sh` (backend parziale + override
  locale), pattern `default_tags` + `Name`, wrapper con `TF_VAR_*` dalle
  variabili di shell, `.gitignore` per Terraform (copre `**/.terraform/`,
  `*.tfstate*`, `*_override.tf`, `*.tfvars`).
- Vincoli: l'agente non lancia `aws`, `docker build/push` né gli script di
  provisioning; può fare `terraform init -backend=false`/`validate`, render
  locali e dry-run dei wrapper con stub.

## Goals / Non-Goals

**Goals:**
- Stessa struttura e stesse convenzioni di `aws-terraform-ec2`: chi ha letto
  quello legge questo senza sorprese.
- Un solo `start-all.sh` che porta dallo zero all'applicazione raggiungibile,
  DB inizializzato compreso (oggi per ECS servono due script).
- Il codice condiviso fra i due stack Terraform vive in un posto solo
  (`script/aws-tf-init.sh`).

**Non-Goals:**
- Moduli Terraform condivisi fra EC2 ed ECS per Aurora/DynamoDB/SQS/Redis:
  la duplicazione è voluta, ogni cartella resta leggibile da sola.
- Provider `docker` di Terraform o `local-exec` per build/push: la build resta
  nel wrapper, dove si vede e si ripete a mano.
- Migrare lo stack bash ECS nello state (`terraform import`).

## Decisions

### D1. Layout dei file

```
script/aws-tf-init.sh          (spostato da aws-terraform-ec2/tf-init.sh) terraform_init_backend, chiave
                               di default annotazioni/<stack>/terraform.tfstate con <stack> = argomento del source
script/aws-terraform-ecs/
  versions.tf                  required_version >= 1.10, provider aws ~> 5.0, backend "s3" {}, default_tags
  variables.tf                 region, environment (validation), db_engine_version (default null),
                               db_instance_class, db_username, db_password (sensitive), db_name,
                               image_tag ("latest"), task_cpu ("512"), task_memory ("1024"), desired_count (1)
  locals.tf                    nomi identici al bash, common_tags, url immagine, porte
  data.tf                      aws_vpc default, aws_subnets, aws_caller_identity, aws_region
  ecr.tf                       aws_ecr_repository (force_delete = true)
  iam.tf                       2 ruoli + attachment (task: DynamoDB/RDS/SQS; execution: ECSTaskExecution/Logs/SQS)
  network.tf                   SG + regole: 8080/443/22 da 0.0.0.0/0, 3306/6379 dal SG stesso, egress
  database.tf                  aws_rds_cluster (database_name, engine_version = var) + istanza privata
  dynamodb.tf / sqs.tf / cache.tf   identici a aws-terraform-ec2
  ecs.tf                       aws_ecs_cluster, aws_cloudwatch_log_group, aws_ecs_task_definition "app",
                               aws_ecs_service
  init-db.tf                   aws_ecs_task_definition "init" (mysql:8.0, SQL incorporato)
  outputs.tf                   ecr_repository_url, cluster/service/task def, sg e subnet per run-task,
                               endpoint, code, log group
  terraform.tfvars.example, .terraform.lock.hcl, README.md
  start-all.sh / stop-all.sh / test-aws-terraform-ecs.sh
```

### D2. Immagine: apply mirato dell'ECR, build e push nel wrapper

```bash
terraform apply -auto-approve -input=false -target=aws_ecr_repository.app -target=aws_ecs_task_definition.init -target=data.aws_subnets.default
ECR_URL=$(terraform output -raw ecr_repository_url)
aws ecr get-login-password --region "$REGION" | docker login --username AWS --password-stdin "$ECR_URL"
docker build -t "gestioneannotazioni:$IMAGE_TAG" "$PROJECT_ROOT"      # stesso comando del bash, dalla root
docker tag "gestioneannotazioni:$IMAGE_TAG" "$ECR_URL:$IMAGE_TAG" && docker push "$ECR_URL:$IMAGE_TAG"
```

- La task definition dell'app referenzia
  `${aws_ecr_repository.app.repository_url}:${var.image_tag}`: l'URL include
  account e region, nessun `sts get-caller-identity` nel wrapper.
- Un apply mirato scrive nello state solo gli output che dipendono dai target:
  per questo il data source delle subnet è mirato (serve al `run-task`) e
  `init_task_tags_json` è costruito dai `tags_all` della task definition di
  init, non dai locals.
- `-target` è accettato qui perché il wrapper è l'unico punto d'ingresso e
  l'apply completo segue subito; Terraform stampa l'avviso sul targeting, che
  il README spiega.
- Il wrapper verifica `command -v docker` e `jq` **prima** di `terraform init`
  (scenario "Docker assente"). `SKIP_BUILD=1` salta build e push nei rilanci
  con immagine già pubblicata.
- `force_delete = true` sul repository: `destroy` cancella anche le immagini,
  come `ecr delete-repository --force` in `stop-all.sh` bash.

*Alternative scartate*: provider `kreuzwerker/docker` (dipendenza in più,
build dentro Terraform difficile da diagnosticare); `terraform_data` +
`local-exec` (stessa opacità, non idempotente); immagine da Docker Hub (non
sarebbe "la stessa cosa" del bash, scelta esplicita dell'utente).

### D3. Ordine: DB inizializzato prima del service

Lo stesso apply mirato di D2 include `aws_ecs_task_definition.init`, che
dichiara `depends_on` su istanza Aurora, cluster ECS e **regole del security
group** (egress e 3306 interna): il targeting trascina Aurora (cluster **e**
istanza, quindi l'attesa `available`), SG con le sue regole, ruoli, log group
e cluster ECS, più gli `aws_iam_role_policy_attachment` del ruolo di
execution (senza, il task non può creare il log stream: `AccessDenied` visto
al secondo giro reale). La dipendenza dalle regole è indispensabile: il provider toglie
l'egress di default quando crea il SG, e senza `aws_vpc_security_group_egress_rule.all`
il task di init non raggiunge CloudWatch Logs né Docker Hub
(`ResourceInitializationError`, visto al primo giro reale). Stessa dipendenza
esplicita sul service ECS e sull'istanza dello stack EC2. Poi il wrapper:

```bash
aws ecs run-task --cluster "$CLUSTER" --launch-type FARGATE --task-definition "$INIT_TASK_DEF" \
  --network-configuration "awsvpcConfiguration={subnets=[$SUBNETS],securityGroups=[$SG_ID],assignPublicIp=ENABLED}" \
  --tags "$(terraform output -raw init_task_tags_json)"      # stessi 6 tag + Name del task
# attesa STOPPED (10 s × 60), poi exitCode del container: 0 = ok, altro = errore con rimando ai log
terraform apply -auto-approve -input=false                    # Redis, DynamoDB, SQS, task def app, service
```

`assignPublicIp=ENABLED` serve al task per scaricare `mysql:8.0` da Docker Hub
(VPC di default senza NAT), come nel bash. Il service nasce con immagine
presente e DB pronto: niente crash-loop iniziale.

*Alternativa scartata*: `wait_for_steady_state` sul service con init dopo
l'apply: il container esce finché mancano le tabelle e il service non
diventa mai "steady".

### D4. Task di inizializzazione con SQL incorporato e rilanciabile

`init-db.tf`: family `gestioneannotazioni-mysql-init`, Fargate 256/512,
ruolo di esecuzione dello stack (pull + log), nessun task role, log nello
stesso log group con prefisso `init`. Container `mysql:8.0` con
`environment` `DB_HOST`, `DB_USER`, `MYSQL_PWD` (il client la legge
dall'ambiente, così non compare nella riga di comando) e `command`:

```sh
mysql -h "$DB_HOST" -u"$DB_USER" -e 'SELECT 1' || exit 1            # connessione
echo '<base64 di init-mysql.sql>' | base64 -d > /tmp/init-mysql.sql  # dal file locale, letto all'apply
mysql --force -h "$DB_HOST" -u"$DB_USER" < /tmp/init-mysql.sql       # continua sugli oggetti già esistenti
mysql -h "$DB_HOST" -u"$DB_USER" -e 'SELECT COUNT(*) FROM gestioneannotazioni.users' || exit 1  # verifica
```

- `base64encode(file("${path.module}/../init-database/init-mysql.sql"))` nel
  `command`: ~10 KB, ben sotto il limite della task definition; se l'SQL
  cambia, l'apply registra una nuova revisione (scenario "SQL modificato").
- `--force` da solo restituisce comunque exit ≠ 0 se una statement fallisce
  (gli indici al secondo giro): per questo l'esito è dato dalla verifica
  finale sulla tabella `users`, non dall'exit di `--force`. Connessione
  fallita → exit 1 (scenario "esito diverso da zero segnalato").
- La password nella task definition è visibile in console come nel bash
  (`RDS_PASSWORD` nella task def dell'app e `-p` nel comando di
  `run-ecs-mysql-insert.sh`): debito già tracciato dalla voce Roadmap
  "Gestione password via secret".

### D5. Task definition dell'app e service

`aws_ecs_task_definition.app`: `family`, `network_mode = "awsvpc"`,
`requires_compatibilities = ["FARGATE"]`, `cpu`/`memory` da variabili,
`task_role_arn`, `execution_role_arn`, `container_definitions = jsonencode([...])`
con le 16 variabili nello stesso ordine del heredoc bash (`AWS_ACCESS_KEY_ID`
e `AWS_SECRET_ACCESS_KEY` vuote comprese) e `logConfiguration awslogs`
(`awslogs-stream-prefix = "ecs"`). `aws_ecs_service`: `desired_count`,
`launch_type = "FARGATE"`, `network_configuration` su tutte le subnet della
VPC di default con `assign_public_ip = true`, `propagate_tags = "SERVICE"`,
`task_definition = aws_ecs_task_definition.app.arn` (una nuova revisione
aggiorna il service con rolling deploy). Nessun `wait_for_steady_state`:
l'attesa e la risoluzione dell'IP restano nel wrapper, come nel bash e nel
test (`list-tasks` RUNNING → `describe-tasks` → ENI → `Association.PublicIp`,
20 tentativi × 30 s).

### D6. Aurora privata con versione parametrica

`aws_rds_cluster`: `engine = "aurora-mysql"`, `engine_version = var.db_engine_version`
(default `null` → versione corrente di AWS), `database_name = var.db_name`,
SG, `skip_final_snapshot`, `apply_immediately`. Istanza `db.t3.medium`,
`publicly_accessible = false` (il bash non passa `--publicly-accessible`).
Il pin `5.7.mysql_aurora.2.11.4` è riproducibile con
`TF_VAR_db_engine_version`; il README avverte che Aurora MySQL 2 è fuori
supporto standard e può non essere più creabile o costare di più.

### D7. Security group

Regole con `aws_vpc_security_group_ingress_rule`: mappa `{ app = 8080,
https = 443, ssh = 22 }` da `0.0.0.0/0` (come il bash, 22 compresa) e mappa
`{ mysql = 3306, redis = 6379 }` con `referenced_security_group_id =
aws_security_group.app.id` (equivalente di `--source-group`); egress totale.

### D8. `aws-tf-init.sh` condiviso

Spostato in `script/aws-tf-init.sh` con la stessa funzione
`terraform_init_backend`; il nome dello stack arriva come argomento del
`source` e determina la chiave di default:

```bash
source "$(dirname "$0")/../aws-tf-init.sh" aws-terraform-ecs   # → annotazioni/aws-terraform-ecs/terraform.tfstate
```

`aws-terraform-ec2/{start-all,stop-all}.sh` passano `aws-terraform-ec2`:
stessa chiave di oggi, nessun cambiamento osservabile (la spec
`provisioning-aws-terraform` resta valida senza delta). Stesso pattern di
`aws-tags.sh`. `aws-terraform-ec2/tf-init.sh` viene rimosso e il README di
quella cartella aggiornato.

### D9. Wrapper

- `start-all.sh`: prerequisiti (`docker`, `jq`, `terraform`) → `TF_VAR_*`
  (`ENVIRONMENT`, `DB_PASS`, `AWS_REGION`, `IMAGE_TAG`) → init backend →
  apply mirato (D2/D3) → build/push → run-task init e attesa → apply completo
  → attesa task RUNNING e IP → stampa URL, endpoint, comandi utili (log,
  console) come il bash.
- `stop-all.sh`: init → `destroy -auto-approve`. Il provider scala il
  service a 0 e attende i task prima di cancellarlo; l'ECR con `force_delete`
  elimina le immagini; senza stack "No changes", exit 0.
- `test-aws-terraform-ecs.sh`: copia di `test-aws-ecs.sh` con le 10 fasi
  convertite in `test_ok`/`test_ko` (`report_run_inizio "aws (terraform-ecs)"`),
  `set -e` rimosso, uscita con errore se nessun task RUNNING; richiama
  `test-prenotazione-annotazione.sh`.
- Password: come in EC2, default dimostrativo nel wrapper (letterale già
  presente nei bash), mai nei `.tf`.

### D10. Validazione senza cloud

`terraform fmt -check`, `init -backend=false`, `validate`; render in una
configurazione temporanea delle due `container_definitions` con valori finti
per validare il JSON e confrontare l'elenco delle variabili con il heredoc
bash (`diff` sui 16 nomi); dry-run dei wrapper con stub di `terraform`,
`aws` e `docker` per verificare la sequenza (apply mirato → login/build/push
→ run-task → apply → attesa) e le uscite anticipate (docker assente, init
task con exitCode 1, task mai RUNNING).

### D11. Spec

Nuova capability `provisioning-aws-terraform-ecs` (i requisiti della
capability EC2 sono scritti sullo stack EC2: estenderli avrebbe reso ogni
requisito bifronte). Delta MODIFIED su `provisioning-aws-tag`: solo lo
scenario "Stack ECS Fargate via Terraform", testo invariato.

## Risks / Trade-offs

- [Build Docker lunga o fallita (Maven nel Dockerfile)] → è il comportamento
  del bash; il wrapper si ferma prima del secondo apply e lo stack resta
  coerente (ECR vuoto, nessun service); si rilancia `start-all.sh`.
- [`-target` lascia risorse "orfane" se il wrapper si interrompe fra i due
  apply] → lo state è comunque consistente; il successivo `start-all.sh`
  completa, `stop-all.sh` distrugge tutto.
- [Il task di init non riesce a scaricare `mysql:8.0`] → serve l'IP pubblico
  (`assignPublicIp=ENABLED`) e la regola 443 in uscita (egress totale);
  l'errore compare nei log del task, il wrapper indica il log group.
- [Aurora 8.0 al posto di 5.7] → driver MySQL 8.2 e Hibernate gestiscono
  entrambe; il pin resta disponibile. Rischio opposto: 5.7 non più creabile
  o a pagamento (extended support).
- [Rilancio dell'init su DB già popolato] → `INSERT IGNORE` e `IF NOT EXISTS`
  sulle tabelle; indici duplicati ignorati con `--force`; esito dalla
  verifica finale.
- [Password visibile nelle task definition] → invariato rispetto al bash,
  tracciato in Roadmap.
- [Stessi nomi degli stack EC2] → apply fallisce sulla prima risorsa
  esistente senza toccarla; README e proposal lo documentano insieme al
  divieto di usare `script/aws-ecs/stop-all.sh` sullo stack Terraform.
- [Refactor di `tf-init.sh` rompe lo stack EC2] → dry-run con stub dei
  wrapper EC2 dopo lo spostamento; chiave di default identica.

## Migration Plan

1. Spostare `tf-init.sh` → `script/aws-tf-init.sh` con argomento; aggiornare
   i wrapper EC2 e verificarli in dry-run.
2. Creare la cartella ECS, `fmt`/`init`/`validate`, render dei JSON.
3. Wrapper e test con dry-run; documentazione; Roadmap; project.md.
4. Utente: stack bash/EC2 spenti → `start-all.sh` → `test-aws-terraform-ecs.sh`
   → controllo tag → `stop-all.sh`.
5. Rollback: ripristinare `tf-init.sh` nella cartella EC2 e rimuovere la
   cartella ECS; nessuna risorsa resta dopo `stop-all.sh`.
