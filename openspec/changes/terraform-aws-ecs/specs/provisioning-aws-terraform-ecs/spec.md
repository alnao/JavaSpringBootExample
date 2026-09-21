## Purpose

Definisce come lo stack ECS Fargate del profilo `aws` viene creato e distrutto
con Terraform: equivalenza con lo stack bash, immagine costruita in locale e
pubblicata su ECR, database inizializzato da un task dichiarato, stato remoto
configurabile e verifica automatica.

## ADDED Requirements

### Requirement: Stack ECS equivalente a quello bash

La configurazione Terraform in `script/aws-terraform-ecs` SHALL creare lo
stesso insieme di risorse dello script `script/aws-ecs/start-all.sh`, con gli
stessi nomi: repository ECR `gestioneannotazioni`; ruoli IAM
`gestioneannotazioni-ecs-task-role` (DynamoDB, RDS, SQS) e
`gestioneannotazioni-ecs-execution-role` (esecuzione task ECS, CloudWatch
Logs, SQS); security group `gestioneannotazioni-sg` nella VPC di default con
le porte 8080, 443 e 22 aperte a tutti e 3306 e 6379 aperte solo al security
group stesso; cluster Aurora MySQL `gestioneannotazioni-aurora-cluster` con
database `gestioneannotazioni` e istanza `gestioneannotazioni-aurora-instance`
non raggiungibile pubblicamente; tabelle DynamoDB `annotazioni`,
`annotazioni_storico` e `annotazioni_storicoStati` (con l'indice
`idAnnotazione-index`); code SQS `gestioneannotazioni-annotazioni-export` e
`-import`; subnet group `gestioneannotazioni-redis-subnet-group` e cluster
Redis `gestioneannotazioni-redis`; cluster ECS `gestioneannotazioni-cluster`;
log group `/ecs/gestioneannotazioni-app`; task definition Fargate
`gestioneannotazioni-task` (512 CPU, 1024 MiB); service
`gestioneannotazioni-service` con un task in rete `awsvpc` con IP pubblico.

Al termine di `start-all.sh` l'applicazione SHALL rispondere su
`http://<ip del task>:8080` con lo stesso comportamento dello stack bash:
stessa immagine (costruita dal Dockerfile del repository), stesso profilo
`aws`, stesse variabili d'ambiente del task, database inizializzato con
`script/init-database/init-mysql.sql`.

#### Scenario: Apply su account vuoto

- **WHEN** viene eseguito `script/aws-terraform-ecs/start-all.sh` su un
  account privo delle risorse
- **THEN** al termine tutte le risorse elencate esistono con i nomi indicati
- **AND** il service ha un task in stato `RUNNING`
- **AND** `http://<ip pubblico del task>:8080/actuator/health` risponde `UP`
- **AND** lo script stampa l'IP pubblico del task, l'URL dell'applicazione e
  gli endpoint di Aurora, Redis e delle code

#### Scenario: Apply con uno stack bash già presente

- **WHEN** lo stack ECS bash, oppure lo stack EC2 (bash o Terraform), è attivo
  e viene eseguito `script/aws-terraform-ecs/start-all.sh`
- **THEN** l'apply fallisce sulla prima risorsa con nome già esistente
- **AND** nessuna risorsa dello stack esistente viene modificata o eliminata

#### Scenario: Stessa configurazione del container

- **WHEN** il task è avviato dallo stack Terraform
- **THEN** il container riceve le stesse 16 variabili d'ambiente della task
  definition dello stack bash (`SPRING_PROFILES_ACTIVE=aws`, `AWS_REGION`,
  `AWS_RDS_URL`, `AWS_RDS_USERNAME`, `AWS_RDS_PASSWORD`, `RDS_HOST`,
  `RDS_PORT`, `RDS_DATABASE`, `RDS_USERNAME`, `RDS_PASSWORD`,
  `SQS_EXPORT_QUEUE_URL`, `SQS_IMPORT_QUEUE_URL`, `REDIS_HOST`, `REDIS_PORT`,
  `AWS_ACCESS_KEY_ID` e `AWS_SECRET_ACCESS_KEY` vuote) con gli stessi valori
- **AND** i log del container finiscono nel log group `/ecs/gestioneannotazioni-app`

### Requirement: Immagine costruita in locale e pubblicata su ECR

`start-all.sh` SHALL costruire l'immagine dal `Dockerfile` alla radice del
repository e pubblicarla nel repository ECR creato da Terraform con il tag
`latest` **prima** che il service ECS venga creato, così che il primo task
trovi l'immagine disponibile. La task definition SHALL referenziare
l'immagine del repository ECR dello stesso account e della stessa region.

#### Scenario: Ordine build e service

- **WHEN** viene eseguito `start-all.sh` su un account vuoto
- **THEN** il repository ECR esiste e contiene l'immagine `latest` prima della
  creazione del service
- **AND** il task del service parte senza errori di `CannotPullContainerError`

#### Scenario: Docker assente

- **WHEN** `start-all.sh` viene lanciato su una macchina senza il comando
  `docker`
- **THEN** lo script si ferma con un messaggio esplicito prima di creare
  qualsiasi risorsa

### Requirement: Inizializzazione del database da task dichiarato

Terraform SHALL dichiarare una task definition Fargate
`gestioneannotazioni-mysql-init` che esegue il contenuto di
`script/init-database/init-mysql.sql` (letto dal file locale al momento
dell'apply, non da una URL esterna) contro il cluster Aurora dello stack,
tollerando gli errori di oggetti già esistenti così che possa essere
rilanciata. `start-all.sh` SHALL eseguirla dopo l'apply e attendere che il
task termini; un'uscita del task diversa da zero SHALL essere segnalata come
errore.

#### Scenario: Database pronto dopo start-all

- **WHEN** `start-all.sh` termina con successo
- **THEN** il database `gestioneannotazioni` contiene le tabelle e gli utenti
  di `init-mysql.sql`
- **AND** l'utente `admin` può autenticarsi sull'applicazione

#### Scenario: Rilancio di start-all

- **WHEN** `start-all.sh` viene rilanciato su uno stack già applicato
- **THEN** Terraform non modifica risorse, il task di inizializzazione termina
  senza errore e i dati esistenti restano invariati

#### Scenario: SQL modificato in locale

- **WHEN** `script/init-database/init-mysql.sql` viene modificato e si
  rilancia `start-all.sh`
- **THEN** la task definition viene aggiornata (nuova revisione) e il task
  esegue il nuovo contenuto senza bisogno di pubblicare il file su GitHub

### Requirement: Distruzione completa

`script/aws-terraform-ecs/stop-all.sh` SHALL distruggere tutte le risorse
create dall'apply, comprese le immagini nel repository ECR, senza snapshot
finali di Aurora. Dopo la distruzione nessuna risorsa con
`Project=Annotazioni.aws-terraform-ecs` SHALL restare nell'account, salvo lo
state remoto.

#### Scenario: Destroy dopo apply

- **WHEN** viene eseguito `stop-all.sh` su uno stack applicato
- **THEN** service, task definition, cluster ECS, log group, repository ECR
  con le sue immagini, Redis e subnet group, code SQS, tabelle DynamoDB,
  istanza e cluster Aurora, security group e ruoli IAM non esistono più
- **AND** una ricerca per tag `Project=Annotazioni.aws-terraform-ecs` non
  restituisce risorse

#### Scenario: Destroy senza stack

- **WHEN** viene eseguito `stop-all.sh` senza che esista uno stack applicato
- **THEN** lo script termina senza errori e senza toccare risorse

### Requirement: Stato remoto parametrizzabile

Lo state SHALL essere salvato di default nel bucket S3 `alnao-dev-terraform`
nella region `eu-central-1` con chiave
`annotazioni/aws-terraform-ecs/terraform.tfstate` e lock nativo su S3, con le
stesse variabili di shell `TF_STATE_BUCKET`, `TF_STATE_REGION` e
`TF_STATE_KEY` dello stack EC2 e lo stesso comportamento con
`TF_STATE_BUCKET` vuoto (state locale, ignorato da git). Lo stack EC2 SHALL
conservare la propria chiave di default
`annotazioni/aws-terraform-ec2/terraform.tfstate`: i due state non SHALL mai
sovrapporsi.

#### Scenario: Chiavi distinte per i due stack

- **WHEN** `start-all.sh` di `aws-terraform-ec2` e di `aws-terraform-ecs`
  vengono lanciati senza variabili `TF_STATE_*`
- **THEN** gli state vengono scritti in due oggetti diversi dello stesso
  bucket, `annotazioni/aws-terraform-ec2/terraform.tfstate` e
  `annotazioni/aws-terraform-ecs/terraform.tfstate`

#### Scenario: State locale

- **WHEN** `start-all.sh` viene lanciato con `TF_STATE_BUCKET=` (vuoto)
- **THEN** lo state viene scritto in `script/aws-terraform-ecs/terraform.tfstate`
- **AND** `git status` non mostra file di state né di override del backend

### Requirement: Ambiente, versione Aurora e credenziali da variabili

Il tag `Environment` SHALL derivare da una variabile con default `dev` e
valori ammessi `dev`, `test` e `production`, rifiutata prima di creare
risorse se diversa; il wrapper SHALL accettare la variabile di shell
`ENVIRONMENT`. La versione del motore Aurora SHALL essere una variabile con
default "versione corrente di AWS per `aurora-mysql`", impostabile a
`5.7.mysql_aurora.2.11.4` per replicare lo stack bash. La password master di
Aurora SHALL essere una variabile sensibile senza default nei file
versionati, passata dal wrapper dalla variabile di shell `DB_PASS` (in sua
assenza lo stesso valore dimostrativo dello stack bash) e mai mostrata negli
output di plan e apply.

#### Scenario: Ambiente non ammesso

- **WHEN** `start-all.sh` viene lanciato con `ENVIRONMENT=collaudo`
- **THEN** Terraform rifiuta la variabile e nessuna risorsa viene creata

#### Scenario: Versione Aurora pinnata

- **WHEN** l'apply viene eseguito con `TF_VAR_db_engine_version=5.7.mysql_aurora.2.11.4`
- **THEN** il cluster Aurora viene creato con quella versione, come farebbe
  lo script bash

#### Scenario: Password non in chiaro

- **WHEN** viene eseguito `terraform plan` o `apply`
- **THEN** la password compare come `(sensitive value)`
- **AND** nessun file versionato in `script/aws-terraform-ecs` contiene la
  password

### Requirement: Test dedicato

`script/aws-terraform-ecs/test-aws-terraform-ecs.sh` SHALL eseguire sullo
stack Terraform gli stessi controlli di `script/aws-ecs/test-aws-ecs.sh`
(individuazione del task e del suo IP pubblico, attesa disponibilità, login,
lettura e creazione annotazione, transizioni di stato, import da SQS,
prenotazione), registrando gli esiti con la libreria di report condivisa e
terminando con errore se non trova un task `RUNNING`.

#### Scenario: Test dopo apply

- **WHEN** lo stack Terraform è applicato e viene eseguito
  `test-aws-terraform-ecs.sh`
- **THEN** tutti i controlli risultano OK nel riepilogo finale

#### Scenario: Test senza stack

- **WHEN** viene eseguito `test-aws-terraform-ecs.sh` senza un task `RUNNING`
- **THEN** lo script segnala che non trova il task e termina con errore

### Requirement: Validazione senza credenziali

La configurazione SHALL superare `terraform fmt -check` e
`terraform validate` senza credenziali AWS, senza Docker e senza accesso al
bucket dello state.

#### Scenario: Validate in locale

- **WHEN** in `script/aws-terraform-ecs` si eseguono
  `terraform init -backend=false` e `terraform validate`
- **THEN** entrambi terminano con successo senza variabili d'ambiente AWS
