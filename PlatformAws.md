# Sistema di Gestione annotazioni - AWS

  <p align="center">
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=black"  height=60/>
    <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white"  height=60/>
  </p>

Progetto realizzato da `< AlNao />` come esempio pratico con Java Spring Boot: consente di creare, modificare e visualizzare annotazioni, utenti con privilegi da moderatore possono confermare le annotazioni e utenti con privilegi da amministratori possono confermare e *inviare* annotazioni a sistemi esterni.


## ☁️ Esecuzione del profilo AWS in locale

Per simulare l'ambiente AWS in locale (MySQL come RDS, DynamoDB Local, Adminer, DynamoDB Admin UI, Spring Boot profilo AWS):
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
  docker-compose -f script/aws-onprem/docker-compose.yml up -d
  ```
  - lo stack crea anche tabelle su Dynamo e database/tabelle su MySql locale
  - lo stack crea anche la coda SQS tramite immagine `localstack`, per everificare lo stato dei messaggi nella coda è possibile eseguire i comandi
    ```bash
    # Lista dalle code disponibili
    docker exec -it gestioneannotazioni-localstack awslocal sqs list-queues --region=eu-central-1
    # Crea la coda se non esiste
    docker exec -it gestioneannotazioni-localstack awslocal sqs create-queue --queue-name annotazioni --region=eu-central-1
    # Lista dei messaggi
    docker exec -it gestioneannotazioni-localstack awslocal sqs receive-message --queue-url http://localhost:4566/000000000000/annotazioni --region=eu-central-1
    # Verifica delle variabili di ambiente 
    docker exec -it gestioneannotazioni-app-aws env | grep AWS
    ```
  - presenta anche uno script `./script/aws-onprem/start-all.sh` che esegue il docker compose
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
  ```bash
  docker-compose -f script/aws-onprem/docker-compose.yml down
  docker volume rm $(docker volume ls -q)
  docker rmi $(docker images -q)
  ```
  - presente anche uno script `./script/aws-onprem/stop-all.sh`


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
    - Creazione della coda SQS utilizzata per l'invio/export delle annotazioni
    - Upload e lancio script di inizializzazione SQL su Aurora (init-mysql.sql)
    - Creazione del Redis con ElasticCache (e di una subnet specifica!)
    - Creazione e configurazione istanza EC2 (Amazon Linux 2)
    - Deploy automatico del jar Spring Boot e avvio con profilo `aws`
    - Configurazione variabili d'ambiente e sicurezza SSH
  - Accesso all'applicazione:
    - L'output finale dello script mostra l'IP pubblico EC2 e la porta applicativa (default 8080)
    - Accedi da browser: `http://<EC2_PUBLIC_IP>:8080`
    - Accesso SSH:
      ```bash
      ssh -i gestioneannotazioni-key.pem ec2-user@<EC2_PUBLIC_IP>
      sudo cat /var/log/cloud-init-output.log
      sudo tail /var/log/cloud-init-output.log --follow
      ```
    - Comando AWS-CLI per la lettura dei messaggi nelle code SQS
      ```
      SQS_QUEUE_NAME=gestioneannotazioni-annotazioni
      SQS_QUEUE_URL=$(aws sqs get-queue-url --queue-name $SQS_QUEUE_NAME --region eu-central-1 --query 'QueueUrl' --output text)
      aws sqs receive-message \
        --queue-url "$SQS_QUEUE_URL" \
        --region eu-central-1 \
        --attribute-names All \
        --message-attribute-names All
      ```
  - Pulizia/cleanup:
    Rimozione di tutte le risorse create (EC2, RDS, DynamoDB, Security Group, KeyPair, ecc):
    ```bash
    ./script/aws-ec2/stop-all.sh
    ```
    - Attenzione: questo script elimina tutti i dati nei database, se necessario effettuare un backup prima di eseguire lo script, l'operazione di cancellazione è irreversibile.
- Note
  - La creazione e il de-provisioning è idempotente: è possibile rilanciare gli script senza duplicare le risorse
  - Tutte le risorse sono taggate per facile identificazione e cleanup
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
    Lo script ci può mettere diversi minuto per la creazione del database aurora e del task ECS!
    Lo script esegue in sequenza:
    1. **Build e Push ECR**: Compilazione Maven, build Docker, creazione repository ECR e push immagine
    2. **IAM Roles**: Creazione Task Role (accesso Aurora/DynamoDB/ElastiCache) e Execution Role (logging CloudWatch)
    3. **Networking**: Creazione Security Groups con regole per HTTP (8080), Aurora (3306), Redis (6379), HTTPS/SSH
    4. **Aurora MySQL**: Provisioning cluster RDS con inizializzazione database e tabelle
    5. **SQS**: Creazione coda SQS per *l'invio* delle annotazioni confermate
    6. **ElastiCache Redis**: Provisioning cluster Redis per lock distribuiti (cache.t3.micro)
    7. **DynamoDB**: Creazione tabelle `annotazioni` e `annotazioni_storico` con attributi ottimizzati
    8. **ECS Deployment**: Creazione cluster, task definition, service con Fargate e auto-scaling
    9. **CloudWatch Logs**: Configurazione logging applicativo con retention automatica
    10. **Endpoint Discovery**: Rilevamento automatico IP pubblico del task per accesso HTTP
      - a volte capita che il task non faccia in tempo a partire e il ritorna l'ip corretto, in questi casi è possibile lanciare lo script
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
    ./script/automatic-test/test-prenotazione-annotazione.sh <indirizzoip>:8080
    ```
  - Comando AWS-CLI per la lettura dei messaggi nelle code SQS
    ```
    SQS_QUEUE_NAME=gestioneannotazioni-annotazioni
    SQS_QUEUE_URL=$(aws sqs get-queue-url --queue-name $SQS_QUEUE_NAME --region eu-central-1 --query 'QueueUrl' --output text)
    aws sqs receive-message \
      --queue-url "$SQS_QUEUE_URL" \
      --region eu-central-1 \
      --attribute-names All \
      --message-attribute-names All
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
  - Tutte le risorse sono taggate per identificazione e gestione costi
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



