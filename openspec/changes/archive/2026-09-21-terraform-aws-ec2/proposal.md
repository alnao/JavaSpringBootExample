## Why

Lo stack EC2 del profilo `aws` esiste solo come script bash imperativo
(`script/aws-ec2/start-all.sh` + `stop-all.sh`): l'idempotenza è simulata a
mano con `describe-*` prima di ogni `create-*`, il cleanup "a volte non
cancella tutto" (lo dice `PlatformAws.md`) e non esiste una descrizione
dichiarativa dell'infrastruttura da cui leggere cosa c'è davvero su AWS. La
voce "Script terraform" della Roadmap è aperta (🚧) e la spec
`provisioning-aws-tag` ha già riservato il valore `ManagedBy=Terraform`.

## What Changes

- Nuova cartella `script/aws-terraform-ec2/` con una configurazione Terraform
  (Terraform ≥ 1.10, provider AWS 5.x) che crea **lo stesso stack** di
  `script/aws-ec2/start-all.sh`: ruolo IAM e instance profile, security group
  nella VPC di default con le stesse porte (SSH solo dall'IP del chiamante),
  Aurora MySQL (cluster + istanza pubblica), le tre tabelle DynamoDB con il GSI
  di `annotazioni_storicoStati`, le due code SQS, ElastiCache Redis con il suo
  subnet group, key pair generata da Terraform con `.pem` salvato in locale,
  AMI Amazon Linux 2 più recente, EC2 `t3.medium` con user data che inizializza
  Aurora con `script/init-database/init-mysql.sql` e avvia il container
  `alnao/gestioneannotazioni:latest` con profilo `aws`.
- **Stessi nomi di risorsa** dello stack bash (`gestioneannotazioni-sg`,
  `gestioneannotazioni-cluster`, tabelle `annotazioni*`, code
  `gestioneannotazioni-annotazioni-*`, `gestioneannotazioni-redis`, ruolo e
  profile): i due stack sono quindi **mutuamente esclusivi** nella stessa
  region. Fanno eccezione le risorse che hanno un file o un tag locale da non
  sovrapporre: key pair `gestioneannotazioni-terraform-key` e `Name`
  `gestioneannotazioni-terraform-ec2` per istanza e volumi.
- Tag: i sei tag standard tramite `default_tags` del provider
  (`Environment` da variabile validata `dev|test|production`, default `dev`;
  `Project=Annotazioni.aws-terraform-ec2`; `Owner=AlNao`;
  `CostCenter=Annotazioni`; `ManagedBy=Terraform`) più `Name` per risorsa.
  La EC2 porta il marcatore **distinto** `gestioneannotazioni-terraform-app=true`,
  così gli script `stop-all.sh`/`test-aws-ec2.sh` dello stack bash non la
  vedono mai; il test è un nuovo `test-aws-terraform-ec2.sh` derivato da
  `test-aws-ec2.sh`.
- **State remoto parametrizzabile**: backend S3 con configurazione parziale;
  i wrapper `start-all.sh`/`stop-all.sh` fanno `terraform init` leggendo
  `TF_STATE_BUCKET` (default `alnao-dev-terraform`), `TF_STATE_REGION`
  (default `eu-central-1`), `TF_STATE_KEY` (default
  `annotazioni/aws-terraform-ec2/terraform.tfstate`) con lock nativo S3
  (`use_lockfile`); con `TF_STATE_BUCKET` vuoto si usa lo state locale
  tramite un `backend_override.tf` generato e ignorato da git.
- Wrapper bash coerenti con le altre cartelle: `start-all.sh` (init + apply),
  `stop-all.sh` (init + destroy), `test-aws-terraform-ec2.sh`. La password di
  Aurora è una variabile `sensitive` senza default, passata dal wrapper via
  `TF_VAR_db_password` (default demo identico allo stack bash, sovrascrivibile
  con `DB_PASS`).
- `.gitignore`: `.terraform/`, `*.tfstate*`, `*_override.tf`, `backend.hcl`,
  `*.tfvars` (resta versionato `terraform.tfvars.example`); il lock file
  `.terraform.lock.hcl` viene versionato.
- Documentazione: sezione "Esecuzione su AWS EC2 con Terraform" in
  `PlatformAws.md`, voce Roadmap "Script terraform" spuntata, elenco cartelle
  in `openspec/project.md`.

## Capabilities

### New Capabilities
- `provisioning-aws-terraform`: lo stack EC2 del profilo `aws` è creabile e
  distruggibile in modo dichiarativo con Terraform, equivalente allo stack
  bash, con state remoto parametrizzabile, marcatore distinto e test dedicato.

### Modified Capabilities
- `provisioning-aws-tag`: il requisito "Tag standard su ogni risorsa creata"
  ammette `ManagedBy=Terraform` per le risorse create da Terraform (oggi il
  testo prevede solo `Sh` per gli script di shell) e aggiunge lo scenario dello
  stack Terraform.

## Impact

- **Profili coinvolti**: solo il deploy del profilo `aws` su EC2. Nessuna
  modifica al runtime: `sqlite`, `kube`, `azure` e lo stesso profilo `aws`
  si comportano come prima; il container avviato è lo stesso e riceve le
  stesse variabili d'ambiente dello stack bash.
- **Core e adapter**: non toccati. Nessun file Java, nessun `pom.xml`.
- **File nuovi**: `script/aws-terraform-ec2/{versions.tf, variables.tf,
  locals.tf, data.tf, iam.tf, network.tf, database.tf, dynamodb.tf, sqs.tf,
  cache.tf, compute.tf, outputs.tf, user_data.sh.tftpl,
  terraform.tfvars.example, .terraform.lock.hcl, start-all.sh, stop-all.sh,
  test-aws-terraform-ec2.sh, README.md}`.
- **File modificati**: `.gitignore`, `PlatformAws.md`, `Roadmap.md`,
  `openspec/project.md`, spec `provisioning-aws-tag`.
- **Compatibilità**: gli script di `script/aws-ec2` non cambiano. Lo stack
  Terraform non può essere applicato mentre esiste lo stack bash (stessi nomi):
  `terraform apply` fallisce alla prima risorsa già esistente senza toccare
  quelle create dal bash. Viceversa, lanciare `script/aws-ec2/stop-all.sh` con
  lo stack Terraform attivo cancellerebbe Aurora, DynamoDB, SQS, Redis, SG e
  IAM (ritrovati per nome) fuori dallo state — non la EC2, grazie al marcatore
  distinto; documentato come errore da evitare.
- **Prerequisiti**: Terraform ≥ 1.10 installato (sulla macchina di sviluppo
  c'è 1.16); bucket S3 `alnao-dev-terraform` già esistente in `eu-central-1`
  se si usa lo state remoto (non viene creato dalla change).
- **Costi**: gli stessi dello stack bash (Aurora, Redis, EC2 sempre accesi);
  nessun costo per lo state su S3 (pochi KB). La verifica reale richiede
  `terraform apply` su AWS: a carico dell'utente. L'agente esegue soltanto
  `terraform fmt`, `init -backend=false` e `validate` in locale.
- **Chiave privata nello state**: `tls_private_key` salva la chiave anche
  nello state (locale o S3). Accettabile per uno stack demo con bucket
  privato; documentato, con l'alternativa di una key pair esistente.

## Non-goals

- Terraform per gli stack ECS ed EKS: cartelle separate in change future; la
  voce Roadmap resta 🚧 per quelle.
- Creazione del bucket dello state e della sua policy: si assume esistente.
- Riduzione delle porte aperte a `0.0.0.0/0` (3306, 6379, 8080, 8086, 8087):
  si replica lo stack bash com'è; l'hardening è un lavoro a parte.
- Gestione della password via AWS Secrets Manager: è già una voce dedicata
  della Roadmap ("Gestione password via secret").
- Rendere configurabile il nome della tabella `annotazioni_storicoStati`
  nel codice per far coesistere i due stack: richiederebbe una change sul core.
- Inserire il test Terraform in `test-all.sh`: la regressione automatica non
  esegue stack cloud a pagamento.
