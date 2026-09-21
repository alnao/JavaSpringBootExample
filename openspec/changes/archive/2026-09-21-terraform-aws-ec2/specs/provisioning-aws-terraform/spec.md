## Purpose

Definisce come lo stack EC2 del profilo `aws` viene creato e distrutto in modo
dichiarativo con Terraform: equivalenza con lo stack bash, stato remoto
configurabile, isolamento dagli script bash e verifica automatica.

## ADDED Requirements

### Requirement: Stack equivalente a quello bash

La configurazione Terraform in `script/aws-terraform-ec2` SHALL creare lo
stesso insieme di risorse dello script `script/aws-ec2/start-all.sh`, con gli
stessi nomi: ruolo IAM `gestioneannotazioni-ec2-role` con le stesse policy e
instance profile `gestioneannotazioni-ec2-profile`; security group
`gestioneannotazioni-sg` nella VPC di default con le porte 3306, 6379, 8080,
8086 e 8087 aperte a tutti e la 22 aperta solo all'IP pubblico di chi applica;
cluster Aurora MySQL `gestioneannotazioni-cluster` con istanza
`gestioneannotazioni-instance` raggiungibile pubblicamente; tabelle DynamoDB
`annotazioni`, `annotazioni_storico` e `annotazioni_storicoStati` (quest'ultima
con l'indice `idAnnotazione-index`); code SQS
`gestioneannotazioni-annotazioni-export` e `-import`; subnet group
`gestioneannotazioni-redis-subnet-group` e cluster Redis
`gestioneannotazioni-redis`; una key pair e un'istanza EC2 Amazon Linux 2.

Al termine dell'apply l'applicazione SHALL rispondere su `http://<ip>:8080`
con lo stesso comportamento dello stack bash: stesso container, stesso profilo
`aws`, stesse variabili d'ambiente, database inizializzato con
`script/init-database/init-mysql.sql`.

#### Scenario: Apply su account vuoto

- **WHEN** viene eseguito `script/aws-terraform-ec2/start-all.sh` su un
  account privo delle risorse
- **THEN** al termine tutte le risorse elencate esistono con i nomi indicati
- **AND** `http://<ip pubblico>:8080/actuator/health` risponde `UP`
- **AND** gli output riportano IP pubblico, endpoint Aurora, endpoint Redis,
  URL delle due code e comando SSH

#### Scenario: Apply con stack bash già presente

- **WHEN** lo stack di `script/aws-ec2` è attivo e viene eseguito
  `script/aws-terraform-ec2/start-all.sh`
- **THEN** l'apply fallisce sulla prima risorsa con nome già esistente
- **AND** nessuna risorsa dello stack bash viene modificata o eliminata

#### Scenario: Stessa configurazione dell'applicazione

- **WHEN** l'istanza è avviata dallo stack Terraform
- **THEN** il container riceve `SPRING_PROFILES_ACTIVE=aws`, `AWS_RDS_URL`,
  `AWS_RDS_USERNAME`, `AWS_RDS_PASSWORD`, `AWS_REGION`,
  `DYNAMODB_ANNOTAZIONI_TABLE_NAME`, `SQS_EXPORT_QUEUE_URL`,
  `SQS_IMPORT_QUEUE_URL`, `REDIS_HOST` e `REDIS_PORT` con gli stessi valori
  che riceverebbe dallo stack bash

### Requirement: Distruzione completa

`script/aws-terraform-ec2/stop-all.sh` SHALL distruggere tutte le risorse
create dall'apply, senza snapshot finali di Aurora, e SHALL rimuovere il file
`.pem` locale della key pair. Dopo la distruzione nessuna risorsa con
`Project=Annotazioni.aws-terraform-ec2` SHALL restare nell'account, salvo lo
state remoto.

#### Scenario: Destroy dopo apply

- **WHEN** viene eseguito `stop-all.sh` su uno stack applicato
- **THEN** EC2, key pair, Redis e subnet group, code SQS, tabelle DynamoDB,
  istanza e cluster Aurora, security group, instance profile e ruolo IAM non
  esistono più
- **AND** una ricerca per tag `Project=Annotazioni.aws-terraform-ec2` non
  restituisce risorse

#### Scenario: Destroy senza stack

- **WHEN** viene eseguito `stop-all.sh` senza che esista uno stack applicato
- **THEN** lo script termina senza errori e senza toccare risorse

### Requirement: Stato remoto parametrizzabile

Lo state Terraform SHALL essere salvato di default nel bucket S3
`alnao-dev-terraform` nella region `eu-central-1` con chiave
`annotazioni/aws-terraform-ec2/terraform.tfstate` e lock nativo su S3. Bucket,
region e chiave SHALL essere sovrascrivibili con le variabili di shell
`TF_STATE_BUCKET`, `TF_STATE_REGION` e `TF_STATE_KEY` senza modificare i file
versionati. Con `TF_STATE_BUCKET` vuoto lo state SHALL essere locale, nella
cartella della configurazione, e il file di state SHALL essere ignorato da
git. I wrapper SHALL usare la stessa configurazione di backend sia in
`start-all.sh` sia in `stop-all.sh`.

#### Scenario: Default remoto

- **WHEN** `start-all.sh` viene lanciato senza variabili `TF_STATE_*`
- **THEN** lo state viene scritto in
  `s3://alnao-dev-terraform/annotazioni/aws-terraform-ec2/terraform.tfstate`
- **AND** `stop-all.sh` lanciato in seguito, anche da un'altra macchina con le
  stesse credenziali, trova lo stack e lo distrugge

#### Scenario: Bucket diverso

- **WHEN** `start-all.sh` viene lanciato con `TF_STATE_BUCKET=altro-bucket`
- **THEN** lo state viene scritto in `altro-bucket`, con chiave e region di
  default

#### Scenario: State locale

- **WHEN** `start-all.sh` viene lanciato con `TF_STATE_BUCKET=` (vuoto)
- **THEN** lo state viene scritto in `script/aws-terraform-ec2/terraform.tfstate`
- **AND** `git status` non mostra file di state né file di override del backend

### Requirement: Isolamento dallo stack bash

L'istanza EC2 creata da Terraform SHALL portare il tag marcatore
`gestioneannotazioni-terraform-app=true` e NON SHALL portare
`gestioneannotazioni-app=true`. Gli script `stop-all.sh` e `test-aws-ec2.sh`
di `script/aws-ec2` NON SHALL individuare l'istanza Terraform. La key pair e
il `Name` di istanza e volumi SHALL essere distinti da quelli dello stack bash
(`gestioneannotazioni-terraform-key`, `gestioneannotazioni-terraform-ec2`,
`gestioneannotazioni-terraform-ec2-volume`), così che i file `.pem` locali dei
due stack non si sovrascrivano.

#### Scenario: Script bash non vedono la EC2 Terraform

- **WHEN** lo stack Terraform è attivo e viene eseguito
  `script/aws-ec2/test-aws-ec2.sh`
- **THEN** lo script non trova alcuna istanza con tag
  `gestioneannotazioni-app=true`

#### Scenario: Marcatore presente

- **WHEN** lo stack Terraform è applicato
- **THEN** l'istanza EC2 riporta `gestioneannotazioni-terraform-app=true`
  oltre ai sei tag standard

### Requirement: Ambiente e credenziali da variabili

Il tag `Environment` SHALL derivare da una variabile Terraform con default
`dev` e valori ammessi `dev`, `test` e `production`; un valore diverso SHALL
essere rifiutato prima di creare risorse. Il wrapper SHALL accettare la
variabile di shell `ENVIRONMENT` con lo stesso significato degli script bash.
La password dell'utente master di Aurora SHALL essere una variabile sensibile
senza default nei file versionati; il wrapper SHALL passarla dalla variabile
di shell `DB_PASS`, usando in sua assenza lo stesso valore dimostrativo dello
stack bash, e Terraform NON SHALL mostrarla nell'output di plan e apply.

#### Scenario: Ambiente esplicito

- **WHEN** `start-all.sh` viene lanciato con `ENVIRONMENT=test`
- **THEN** tutte le risorse riportano `Environment=test`

#### Scenario: Ambiente non ammesso

- **WHEN** `start-all.sh` viene lanciato con `ENVIRONMENT=collaudo`
- **THEN** Terraform rifiuta la variabile e nessuna risorsa viene creata

#### Scenario: Password non in chiaro

- **WHEN** viene eseguito `terraform plan` o `apply`
- **THEN** il valore della password compare come `(sensitive value)`
- **AND** nessun file versionato in `script/aws-terraform-ec2` contiene la
  password

### Requirement: Test dedicato

`script/aws-terraform-ec2/test-aws-terraform-ec2.sh` SHALL eseguire sullo
stack Terraform gli stessi controlli di `script/aws-ec2/test-aws-ec2.sh`
(health, login, creazione annotazione, export su SQS, import da SQS,
prenotazione), individuando l'istanza tramite il marcatore
`gestioneannotazioni-terraform-app=true` e registrando gli esiti con la
libreria di report condivisa.

#### Scenario: Test dopo apply

- **WHEN** lo stack Terraform è applicato e viene eseguito
  `test-aws-terraform-ec2.sh`
- **THEN** tutti i controlli risultano OK nel riepilogo finale

#### Scenario: Test senza stack

- **WHEN** viene eseguito `test-aws-terraform-ec2.sh` senza uno stack attivo
- **THEN** lo script segnala che non trova l'istanza e termina con errore

### Requirement: Validazione senza credenziali

La configurazione SHALL superare `terraform fmt -check` e
`terraform validate` senza credenziali AWS e senza accesso al bucket dello
state, così che la verifica statica sia ripetibile in locale e in CI.

#### Scenario: Validate in locale

- **WHEN** in `script/aws-terraform-ec2` si eseguono
  `terraform init -backend=false` e `terraform validate`
- **THEN** entrambi terminano con successo senza variabili d'ambiente AWS
