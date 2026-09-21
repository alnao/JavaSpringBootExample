# Sistema di Gestione annotazioni - AWS

  <p align="center">
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=black"  height=60/>
    <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white"  height=60/>
  </p>

Progetto realizzato da `< AlNao />` come esempio pratico con Java Spring Boot: consente di creare, modificare e visualizzare annotazioni, utenti con privilegi da moderatore possono confermare le annotazioni e utenti con privilegi da amministratori possono confermare e *inviare* annotazioni a sistemi esterni.


## ☁️ Esecuzione del profilo AWS in locale

Per simulare l'ambiente AWS in locale (MySQL come RDS, DynamoDB Local, Adminer, DynamoDB Admin UI, Spring Boot profilo AWS):
- Esiste uno script che esegue la creazione, tutti i test automatici e poi il deprovisioning di tutto
  ```bash
  ./script/automatic-test/test-aws-onprem.sh
  ```
- Prima di eseguire il comando di compose bisogna verficare che la versione dell'immagine su DockerHub sia aggiornata!
    ```bash
    ./script/push-image-docker-hub.sh
    ```
    oppure localmente
    ```
    mvn clean package -DskipTests
    docker build -t alnao/gestioneannotazioni:latest .
    ```
- Comando per la creazione dello stack nel docker locale
  ```bash
  `./script/aws-onprem/start-all.sh`
  ```
  - oppure
    ```bash
    docker-compose -f script/aws-onprem/docker-compose.yml up -d
    ```
  - lo stack crea anche tabelle su Dynamo e database/tabelle su MySql locale
  - lo stack crea anche la coda SQS tramite immagine `localstack`, per everificare lo stato dei messaggi nella coda è possibile eseguire i comandi
    ```bash
    # Lista dalle code disponibili
    docker exec -it gestioneannotazioni-localstack awslocal sqs list-queues --region=eu-central-1
    # Crea la coda di export se non esiste
    docker exec -it gestioneannotazioni-localstack awslocal sqs create-queue --queue-name annotazioni --region=eu-central-1
    # Crea la coda di import se non esiste
    docker exec -it gestioneannotazioni-localstack awslocal sqs create-queue --queue-name annotazioni-import --region=eu-central-1
    # Lista dei messaggi dalla coda di export
    docker exec -it gestioneannotazioni-localstack awslocal sqs receive-message --queue-url http://localhost:4566/000000000000/annotazioni --region=eu-central-1
    # Lista dei messaggi dalla coda di import
    docker exec -it gestioneannotazioni-localstack awslocal sqs receive-message --queue-url http://localhost:4566/000000000000/annotazioni-import --region=eu-central-1
    # Verifica delle variabili di ambiente 
    docker exec -it gestioneannotazioni-app-aws env | grep AWS
    ```
- Servizi disponibili:
  - **Frontend**:        [http://localhost:8082](http://localhost:8082)
  - **Backend API**:     [http://localhost:8082/api/annotazioni](http://localhost:8082/api/annotazioni)
  - **Adminer (MySQL)**: [http://localhost:8086](http://localhost:8086)
  - **DynamoDB Admin**:  [http://localhost:8087](http://localhost:8087)
- Per vedere i log di un servizio:
  ```bash
  docker-compose logs -f <nome-servizio>
  ```
- Per fermare tutto e rimuovere i componenti:
  - presente anche uno script 
    ```bash
    ./script/aws-onprem/stop-all.sh
    ```
  - oppure manualmente con docker
    ```bash
    docker-compose -f script/aws-onprem/docker-compose.yml down
    docker volume rm $(docker volume ls -q)
    docker rmi $(docker images -q)
    ```

### 🚀 Esecuzione su AWS EC2
Questa modalità consente di eseguire l'intero stack annotazioni su AWS EC2, con provisioning completamente automatizzato di tutte le risorse cloud necessarie (Aurora MySQL, DynamoDB, EC2, Security Group, IAM Role, KeyPair, ecc.) tramite script Bash e AWS CLI.
- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- Prerequisiti:
  - AWS CLI installata e configurata (`aws configure`)
  - Credenziali AWS con permessi minimi per EC2, RDS, DynamoDB, IAM, VPC, KeyPair
  - Chiave SSH per accesso sicuro all'istanza EC2 (verrà generata se non presente)
  - Lo script usa la VPC di default di un account e crea il security group necessario
- Provisioning e deploy automatico:
  - Avvio di tutte le risorse e avvio del microservizio con docker nella EC2::
    ```bash
    ./script/aws-ec2/start-all.sh
    ```
    Lo script esegue in sequenza:
    - Creazione VPC, Security Group, KeyPair, IAM Role
    - Provisioning Aurora MySQL (RDS) e DynamoDB
    - Creazione della coda SQS `gestioneannotazioni-annotazioni` utilizzata per l'invio/export delle annotazioni
    - Creazione della coda SQS `gestioneannotazioni-annotazioni-import` utilizzata per l'import delle annotazioni
    - Upload e lancio script di inizializzazione SQL su Aurora (init-mysql.sql)
    - Creazione del Redis con ElasticCache (e di una subnet specifica!). *La creazione potrebbe necessitare diversi minuti*
    - Creazione e configurazione istanza EC2 (Amazon Linux 2)
    - Deploy automatico del jar Spring Boot e avvio con profilo `aws`
    - Configurazione variabili d'ambiente e sicurezza SSH
  - Esecuzione di test automatici (export, import e prenotazione)
    ```bash
    script/aws-ec2/test-aws-ec2.sh
    ```
  - Accesso all'applicazione:
    - L'output finale dello script mostra l'IP pubblico EC2 e la porta applicativa (default 8080)
    - Accedi da browser: `http://<EC2_PUBLIC_IP>:8080`
    - Accesso SSH:
      ```bash
      ssh -i gestioneannotazioni-key.pem ec2-user@<EC2_PUBLIC_IP>
      sudo cat /var/log/cloud-init-output.log
      sudo tail /var/log/cloud-init-output.log --follow
      ```
  - Pulizia/cleanup:
    Rimozione di tutte le risorse create (EC2, RDS, DynamoDB, Security Group, KeyPair, ecc):
    ```bash
    ./script/aws-ec2/stop-all.sh
    ```
    - Attenzione: questo script elimina tutti i dati nei database, se necessario effettuare un backup prima di eseguire lo script, l'operazione di cancellazione è irreversibile.
- Note
  - La creazione e il de-provisioning è idempotente: è possibile rilanciare gli script senza duplicare le risorse
  - Tutte le risorse sono taggate per facile identificazione e cleanup (vedi [Tag delle risorse AWS](#-tag-delle-risorse-aws))
  - L'infrastruttura AWS prevede dei costi, si riassume un breve preventivo:
    - Aurora: circa da 2,4 USD/giorno a 72 USD/mese
    - DynamoDB: circa da 0,01 USD/giorno a 1,25 USD/mese
    - EC2 t2.medium: EC2: da 1,2 USD/giorno a circa 37 USD/mese
- Tabella dei costi stimati per risorse sempre accese (24/7), regione Francoforte (eu-central-1), prezzi AWS settembre 2025:
  | Servizio         | Carico Basso (giorno) | Carico Basso (mese) | Carico Medio/Alto (giorno) | Carico Medio/Alto (mese) |
  |------------------|----------------------|---------------------|----------------------------|--------------------------|
  | Aurora MySQL     | ~2,4 USD             | ~72 USD             | ~2,4 USD                   | ~72 USD                  |
  | DynamoDB         | ~0,01 USD            | ~0,30 USD           | ~0,04 USD                  | ~1,25 USD                |
  | ElasticCache     | ~0,4 USD             | ~12 USD             | ~0,4 USD                   | ~12 USD                  | 
  | EC2 t3.medium    | ~1,2 USD             | ~37 USD             | ~1,2 USD                   | ~37 USD                  |
  | ECR/Storage      | trascurabile         | trascurabile        | trascurabile               | trascurabile             |
  | **Totale**       | **~4 USD**           | **~122 USD**        | **~4.2 USD**               | **~127 USD**             |


### 🐳 Esecuzione su AWS ECS Fargate
Questa modalità consente di eseguire l'intero stack annotazioni su AWS ECS con Fargate, utilizzando container serverless completamente gestiti da AWS. Il provisioning automatizzato include tutte le risorse cloud necessarie (Aurora MySQL, DynamoDB, ECR, ECS Cluster, Task Definition, Service, IAM Roles, Security Groups, ecc.) tramite script Bash e AWS CLI.

- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- Prerequisiti:
  - AWS CLI installata e configurata (`aws configure`)
  - Docker installato per build e push delle immagini
  - Credenziali AWS con permessi per ECS, ECR, RDS, DynamoDB, IAM, VPC, CloudWatch
  - Lo script usa la VPC di default e crea automaticamente tutti i Security Groups necessari

- Provisioning e deploy automatico:
  - Avvio di tutte le risorse e deploy del microservizio su ECS Fargate:
    ```bash
    ./script/aws-ecs/start-all.sh
    ```
    Lo script ci può mettere diversi minuti per la creazione del database aurora e del task ECS!
    Lo script esegue in sequenza:
    1. **Build e Push ECR**: Compilazione Maven, build Docker, creazione repository ECR e push immagine
    2. **IAM Roles**: Creazione Task Role (accesso Aurora/DynamoDB/ElastiCache) e Execution Role (logging CloudWatch)
    3. **Networking**: Creazione Security Groups con regole per HTTP (8080), Aurora (3306), Redis (6379), HTTPS/SSH
    4. **Aurora MySQL**: Provisioning cluster RDS con inizializzazione database e tabelle
    5. **SQS**: Creazione coda SQS `gestioneannotazioni-annotazioni-export` per *l'invio* delle annotazioni confermate e coda `gestioneannotazioni-annotazioni-import` per *l'import* delle annotazioni
    6. **ElastiCache Redis**: Provisioning cluster Redis per lock distribuiti (cache.t3.micro)
    7. **DynamoDB**: Creazione tabelle `annotazioni` e `annotazioni_storico` con attributi ottimizzati
    8. **ECS Deployment**: Creazione cluster, task definition, service con Fargate e auto-scaling
    9. **CloudWatch Logs**: Configurazione logging applicativo con retention automatica
    10. **Endpoint Discovery**: Rilevamento automatico IP pubblico del task per accesso HTTP
      - a volte capita che il task non faccia in tempo a partire e non ritorna l'ip corretto, in questi casi è possibile lanciare lo script per sanare il problema:
        ```bash
        ./script/aws-ecs/check-fargete.sh
        ```
  - Dopo il deploy il database RDS è *vuoto*, per creare le tabelle e gli utenti base è disponibile lo script
      ```
      ./script/aws-ecs/run-ecs-mysql-insert.sh
      ```
      questo script esegue un task ECS per eseguire lo script init-mysql.sql che **DEVE** trovarsi nel path 
      ```
      https://raw.githubusercontent.com/alnao/JavaSpringBootExample/master/script/init-database/init-mysql.sql
      ```
  - Accesso all'applicazione:
    - L'output finale dello script mostra l'IP pubblico del task ECS e la porta applicativa (8080)
    - Accedi da browser: `http://<TASK_PUBLIC_IP>:8080`
    - Endpoint API: `http://<TASK_PUBLIC_IP>:8080/api/annotazioni`
    - Swagger UI: `http://<TASK_PUBLIC_IP>:8080/swagger-ui.html`
    - Health Check: `http://<TASK_PUBLIC_IP>:8080/actuator/health`
  - Test dell'applicazione: è possibile lanciare lo script che verifica il sistema di prenotazione delle annotazioni con lo script
    ```
    ./script/aws-ecs/test-aws-ecs.sh
    ```
  - Monitoring e logs:
    ```bash
    # Verifica stato servizio ECS
    aws ecs describe-services --cluster gestioneannotazioni-cluster --services gestioneannotazioni-service
    # Visualizza logs applicazione
    aws logs tail /ecs/annotazioni --follow
    # Lista task attivi
    aws ecs list-tasks --cluster gestioneannotazioni-cluster
    ```

  - Pulizia/cleanup:
    Rimozione completa di tutte le risorse create (ECS, ECR, RDS, DynamoDB, Security Groups, IAM Roles, CloudWatch Logs, ecc):
    ```bash
    ./script/aws-ecs/stop-all.sh
    ```
    - **Attenzione**: questo script elimina tutti i dati nei database in modo irreversibile. Effettuare backup se necessario prima dell'esecuzione.
    - **Attenzione**: controllare sempre al termine dello script di cleanup, *a volte non cancella tutto*, è possibile eseguirlo più volte per essere sicuri che vengano eliminate tutte le risorse.

- Note tecniche:
  - Il provisioning è idempotente: esecuzione multipla sicura senza duplicazioni
  - Tutte le risorse sono taggate per identificazione e gestione costi (vedi [Tag delle risorse AWS](#-tag-delle-risorse-aws))
  - Service ECS configurato con health check automatici e restart in caso di failure
  - Task definition ottimizzata per Fargate con 1 vCPU e 2GB RAM
  - Networking configurato per accesso pubblico sicuro con Security Groups specifici
  - Aurora endpoint e Redis endpoint automaticamente rilevati e configurati nel container
  - ElastiCache Redis configurato per lock distribuiti su annotazioni con accesso solo interno al VPC

- Tabella dei costi stimati per risorse sempre accese (24/7), regione Francoforte (eu-central-1), prezzi AWS settembre 2025:
  
  | Servizio              | Carico Basso (giorno) | Carico Basso (mese) | Carico Medio (giorno) | Carico Medio (mese) |
  |-----------------------|----------------------|---------------------|----------------------|--------------------|
  | ECS Fargate (1 vCPU, 2GB RAM) | ~0,8 USD (1 task 24/7) | ~24 USD (1 task 24/7) | ~1,6 USD (2 task avg) | ~48 USD (2 task avg) |
  | Aurora MySQL (db.r6g.large) | ~2,4 USD (1 instance) | ~72 USD (1 instance) | ~2,4 USD (1 instance) | ~72 USD (1 instance) |
  | ElastiCache Redis (cache.t3.micro) | ~0,4 USD (1 node) | ~12 USD (1 node) | ~0,4 USD (1 node) | ~12 USD (1 node) |
  | DynamoDB (On-Demand) | ~0,01 USD (<1K RCU/WCU) | ~0,30 USD (<1K RCU/WCU) | ~0,05 USD (~5K RCU/WCU) | ~1,50 USD (~5K RCU/WCU) |
  | ECR Repository (Storage immagini) | ~0,05 USD (~5GB storage) | ~1,50 USD (~5GB storage) | ~0,05 USD (~5GB storage) | ~1,50 USD (~5GB storage) |
  | CloudWatch Logs (Log retention) | ~0,02 USD (~1GB logs) | ~0,60 USD (~1GB logs) | ~0,10 USD (~5GB logs) | ~3,00 USD (~5GB logs) |
  | VPC (Subnet/Route Tables/IGW) | ~0,01 USD (risorse base) | ~0,30 USD (risorse base) | ~0,01 USD (risorse base) | ~0,30 USD (risorse base) |
  | Traffico di Rete (Data Transfer) | ~0,05 USD (~5GB out) | ~1,50 USD (~5GB out) | ~0,20 USD (~20GB out) | ~6,00 USD (~20GB out) |
  | **TOTALE BASE** | **~3,8 USD** | **~114 USD** | **~4,8 USD** | **~147 USD** |
  | Application Load Balancer (opzionale) | ~0,75 USD (se abilitato) | ~22,50 USD (se abilitato) | ~0,75 USD (se abilitato) | ~22,50 USD (se abilitato) |
  | **TOTALE + ALB** | **~4,5 USD** | **~136 USD** | **~5,5 USD** | **~169 USD** |
  | NAT Gateway (per private subnet) | ~1,50 USD (se configurato) | ~45 USD (se configurato) | ~1,50 USD (se configurato) | ~45 USD (se configurato) |
  | **TOTALE + ALB + NAT** | **~6,0 USD** | **~181 USD** | **~7,0 USD** | **~214 USD** |

### 🧱 Esecuzione su AWS EC2 con Terraform
Stessa infrastruttura della sezione [Esecuzione su AWS EC2](#-esecuzione-su-aws-ec2) (Aurora MySQL, DynamoDB, SQS, ElastiCache Redis, EC2 con Docker) descritta in modo dichiarativo con **Terraform** nella cartella [script/aws-terraform-ec2/](script/aws-terraform-ec2/) (dettagli nel suo [README](script/aws-terraform-ec2/README.md)).
- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️ (stessi costi della tabella EC2 qui sopra)
- Prerequisiti:
  - Terraform ≥ 1.10 e AWS CLI configurata (`aws configure`); `curl` e `jq` per il test
  - Bucket S3 per lo state (default `alnao-dev-terraform` in `eu-central-1`), oppure state locale con `TF_STATE_BUCKET=` vuoto
  - **Lo stack bash di `script/aws-ec2` deve essere spento**: le risorse hanno gli stessi nomi (le tabelle DynamoDB sono fisse nel codice), quindi i due stack non possono coesistere nella stessa region
- Provisioning, test e rimozione (dalla root del progetto):
  ```bash
  ./script/aws-terraform-ec2/start-all.sh                 # terraform init + apply, stampa IP, endpoint e comando SSH
  ./script/aws-terraform-ec2/test-aws-terraform-ec2.sh    # health, login, export/import SQS, prenotazione (riepilogo con lib-report)
  ./script/aws-terraform-ec2/stop-all.sh                  # terraform destroy (nessuno snapshot Aurora) e rimozione del .pem
  ```
  - Variabili di shell opzionali: `ENVIRONMENT` (`dev` default, `test`, `production`), `DB_PASS` (password Aurora, mai nei file versionati), `AWS_REGION`, `TF_STATE_BUCKET` / `TF_STATE_REGION` / `TF_STATE_KEY` per lo state
  - Esempio: `ENVIRONMENT=test TF_STATE_BUCKET=mio-bucket ./script/aws-terraform-ec2/start-all.sh`
- Note tecniche:
  - Un file per capitolo dello stack (`iam.tf`, `network.tf`, `database.tf`, `dynamodb.tf`, `sqs.tf`, `cache.tf`, `compute.tf`), nell'ordine in cui lo script bash crea le risorse; lo user data della EC2 è lo stesso dello script (`user_data.sh.tftpl`) con `init-mysql.sql` incorporato
  - Tag: i sei tag standard via `default_tags` del provider con `ManagedBy=Terraform` e `Project=Annotazioni.aws-terraform-ec2` (vedi [Tag delle risorse AWS](#-tag-delle-risorse-aws)); la EC2 porta il marcatore `gestioneannotazioni-terraform-app=true`, diverso da quello dello stack bash, così `script/aws-ec2/stop-all.sh` e `test-aws-ec2.sh` non la vedono
  - State: backend S3 con configurazione parziale completata da `tf-init.sh` (`use_lockfile`, lock nativo S3, niente DynamoDB); con `TF_STATE_BUCKET=` vuoto viene generato un `backend_override.tf` ignorato da git per lo state locale
  - Key pair generata da Terraform (`gestioneannotazioni-terraform-key.pem` nella cartella, ignorato da git): la chiave privata sta anche nello state, accettabile per uno stack demo su bucket privato
  - Verifica senza credenziali: `terraform fmt -check`, `terraform init -backend=false`, `terraform validate` nella cartella
  - **Non lanciare `script/aws-ec2/stop-all.sh` con lo stack Terraform attivo**: cancellerebbe per nome le risorse condivise fuori dallo state; usare sempre `script/aws-terraform-ec2/stop-all.sh`

### 🧱 Esecuzione su AWS ECS Fargate con Terraform
Stessa infrastruttura della sezione [Esecuzione su AWS ECS Fargate](#-esecuzione-su-aws-ecs-fargate) descritta con **Terraform** nella cartella [script/aws-terraform-ecs/](script/aws-terraform-ecs/) (dettagli nel suo [README](script/aws-terraform-ecs/README.md)), con in più l'inizializzazione del database eseguita dal wrapper.
- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️ (stessi costi della tabella ECS qui sopra)
- Prerequisiti:
  - Terraform ≥ 1.10, Docker (build locale dell'immagine, alcuni minuti), AWS CLI configurata, `jq`
  - Bucket S3 per lo state (default `alnao-dev-terraform` in `eu-central-1`), oppure state locale con `TF_STATE_BUCKET=` vuoto
  - **Gli stack ECS bash ed EC2 (bash o Terraform) devono essere spenti**: i nomi delle risorse sono identici (le tabelle DynamoDB sono fisse nel codice)
- Provisioning, test e rimozione (dalla root del progetto):
  ```bash
  ./script/aws-terraform-ecs/start-all.sh                 # init, apply mirato ECR+Aurora, docker build/push, init DB via task Fargate, apply completo, IP del task
  ./script/aws-terraform-ecs/test-aws-terraform-ecs.sh    # endpoint, health, login, transizioni, import SQS, prenotazione (riepilogo con lib-report)
  ./script/aws-terraform-ecs/stop-all.sh                  # terraform destroy, immagini ECR comprese
  ```
  - Variabili di shell opzionali: `ENVIRONMENT`, `DB_PASS`, `AWS_REGION`, `IMAGE_TAG`, `TF_VAR_db_engine_version` (default versione corrente AWS; `5.7.mysql_aurora.2.11.4` per replicare lo script bash), `TF_STATE_*`
- Note tecniche:
  - L'apply è in **due passi**: prima repository ECR e task di inizializzazione (che trascina Aurora, security group, ruoli, cluster ECS e log group), poi build/push dell'immagine e `run-task` di `gestioneannotazioni-mysql-init` (immagine `mysql:8.0`, `init-mysql.sql` incorporato dal file locale, rilanciabile), infine l'apply completo con Redis, DynamoDB, SQS, task definition e service: il container non parte mai senza immagine o senza tabelle
  - Tag: `ManagedBy=Terraform` e `Project=Annotazioni.aws-terraform-ecs` via `default_tags`, i task ereditano i tag del service (vedi [Tag delle risorse AWS](#-tag-delle-risorse-aws))
  - State: stesso meccanismo dello stack EC2 tramite lo script condiviso `script/aws-tf-init.sh`, chiave `annotazioni/aws-terraform-ecs/terraform.tfstate`
  - La password di Aurora compare nelle task definition come nello stack bash (voce Roadmap "Gestione password via secret")
  - **Non lanciare `script/aws-ecs/stop-all.sh` con lo stack Terraform attivo**: cancellerebbe per nome le risorse fuori dallo state

### 🏷️ Tag delle risorse AWS
Tutte le risorse create dagli script di provisioning (`script/aws-ec2`, `script/aws-ecs`, `script/aws-eks`, `script/sqlite-ec2`) ricevono lo stesso set di sei tag, così da poterle riconoscere in console, filtrarle con la CLI e attribuirne i costi in Cost Explorer. I valori e le funzioni che li producono stanno in un solo file, [script/aws-tags.sh](script/aws-tags.sh), caricato con `source` da ogni script.

| Tag | Valore | Note |
|-----|--------|------|
| `Name` | nome proprio della risorsa (es. `gestioneannotazioni-sg`, `annotazioni_storico`, `gestioneannotazioni-aurora-cluster`) | per le risorse senza nome proprio (istanze EC2, volumi, task ECS, load balancer) `gestioneannotazioni-<servizio>`, es. `gestioneannotazioni-ec2`, `gestioneannotazioni-ec2-volume`, `gestioneannotazioni-eks-lb` |
| `Environment` | `dev` (default), `test` o `production` | dalla variabile di shell `ENVIRONMENT`; un valore diverso ferma lo script prima di creare qualsiasi risorsa |
| `Project` | `Annotazioni.<cartella>` | `Annotazioni.aws-ec2`, `Annotazioni.aws-ecs`, `Annotazioni.aws-eks`, `Annotazioni.sqlite-ec2` |
| `Owner` | `AlNao` | fisso |
| `CostCenter` | `Annotazioni` | fisso |
| `ManagedBy` | `Sh` | fisso; riservati `Terraform` e `CloudFormation` per quando esisteranno template |

- Per scegliere l'ambiente basta anteporre la variabile al comando:
  ```bash
  ENVIRONMENT=test ./script/aws-ec2/start-all.sh
  ```
- Le istanze EC2 portano in più il tag marcatore `gestioneannotazioni-app=true` (stack `aws-ec2`) o `gestioneannotazioni-sqlite-ec2-app=true` (stack `sqlite-ec2`): è quello che gli script `stop-all.sh` e di test usano per ritrovarle, non va rimosso.
- Le risorse condivise fra gli stack ECS ed EKS (stesso Aurora, stesse tabelle DynamoDB, stesse code SQS, stesso ElastiCache e repository ECR) conservano il `Project` dello script che le ha create per primo.
- I tag vengono applicati alla creazione: una risorsa che esisteva già prima di questa versione degli script mantiene i tag vecchi. Per allinearla basta un ciclo `stop-all.sh` / `start-all.sh` della sua cartella, oppure un tagging manuale dalla console.
- Per vedere in Cost Explorer i costi per `Project` e `CostCenter` bisogna attivare i due tag come *cost allocation tags* dalla console **Billing → Cost allocation tags** (una volta sola per account; i dati compaiono dopo circa 24 ore).
- Per verificare i tag di tutto lo stack con la CLI:
  ```bash
  aws resourcegroupstaggingapi get-resources --region eu-central-1 \
    --tag-filters Key=CostCenter,Values=Annotazioni \
    --query 'ResourceTagMappingList[].[ResourceARN, Tags[?Key==`Name`].Value | [0], Tags[?Key==`Project`].Value | [0]]' --output table
  ```
- **Elastic IP**: nessuno script ne alloca (le EC2 usano l'IP pubblico auto-assegnato della VPC di default, che non è un EIP). Se però all'istanza ne viene associato uno da fuori (console o altra automazione), `start-all.sh` di `aws-ec2` e `sqlite-ec2` lo rileva e lo tagga (`Name=gestioneannotazioni-ec2-eip` / `gestioneannotazioni-sqlite-ec2-eip`); `stop-all.sh` non lo rilascia, e un EIP non associato continua a costare: `aws ec2 describe-addresses` per vederli, `aws ec2 release-address --allocation-id eipalloc-…` per liberarli.
- **SSM managed-instance**: in Tag Editor compare anche `ssm:managed-instance` senza tag per ogni EC2 dello stack. È la registrazione automatica dell'istanza in Systems Manager (SSM Agent preinstallato in Amazon Linux 2), non una risorsa creata dagli script: per le EC2 (`i-…`) l'API SSM non permette di taggarla separatamente (solo i nodi ibridi `mi-…`), non ha costo e sparisce con l'istanza. I tag validi sono quelli dell'istanza EC2.
- Note per EKS: `eksctl create cluster --tags` propaga i tag agli stack CloudFormation e alle risorse che generano (VPC, nodi); se i nodi risultassero senza tag, si può ripetere con `eksctl create nodegroup --tags`. Il load balancer creato dal `Service` Kubernetes riceve i tag tramite l'annotation `service.beta.kubernetes.io/aws-load-balancer-additional-resource-tags`; se il controller in uso la ignorasse, si aggiungono a mano con `aws elb add-tags --load-balancer-names <nome> --tags ...`.


# &lt; AlNao /&gt;
Tutti i codici sorgente e le informazioni presenti in questo repository sono frutto di un attento e paziente lavoro di sviluppo da parte di AlNao, che si è impegnato a verificarne la correttezza nella misura massima possibile. Qualora parte del codice o dei contenuti sia stato tratto da fonti esterne, la relativa provenienza viene sempre citata, nel rispetto della trasparenza e della proprietà intellettuale. 


Alcuni contenuti e porzioni di codice presenti in questo repository sono stati realizzati anche grazie al supporto di strumenti di intelligenza artificiale, il cui contributo ha permesso di arricchire e velocizzare la produzione del materiale. Ogni informazione e frammento di codice è stato comunque attentamente verificato e validato, con l’obiettivo di garantire la massima qualità e affidabilità dei contenuti offerti. 


Per ulteriori dettagli, approfondimenti o richieste di chiarimento, si invita a consultare il sito [AlNao.it](https://www.alnao.it/).


## License
Made with ❤️ by <a href="https://www.alnao.it">AlNao</a>
&bull; 
Public projects 
<a href="https://www.gnu.org/licenses/gpl-3.0"  valign="middle"> <img src="https://img.shields.io/badge/License-GPL%20v3-blue?style=plastic" alt="GPL v3" valign="middle" /></a>
*Free Software!*


Il software è distribuito secondo i termini della GNU General Public License v3.0. L'uso, la modifica e la ridistribuzione sono consentiti, a condizione che ogni copia o lavoro derivato sia rilasciato con la stessa licenza. Il contenuto è fornito "così com'è", senza alcuna garanzia, esplicita o implicita.


The software is distributed under the terms of the GNU General Public License v3.0. Use, modification, and redistribution are permitted, provided that any copy or derivative work is released under the same license. The content is provided "as is", without any warranty, express or implied.



