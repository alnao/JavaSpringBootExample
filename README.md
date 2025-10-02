# Sistema di Gestione annotazioni

Progetto realizzato da < AlNao /> come esempio pratico con Java Spring Boot: consente di creare, modificare e visualizzare annotazioni, utenti con privilegi da moderatore possono confermare la annotazione e amministratori possono confermare e *inviare* annotazioni a sistemi esterni.

La soluzione è strutturata in moduli multipli, basata su Spring Boot e sull’architettura esagonale (Hexagonal Architecture), con pieno supporto al deployment sia in ambienti on-premise che su cloud AWS. Il progetto è pensato per essere agnostico rispetto al cloud provider, al DBMS utilizzato e ai sistemi di interfaccia: puoi adattarlo facilmente a diversi ambienti, database e frontend.


## 📚 Indice rapido

- 🛠️ [Struttura progetto](#️-struttura-progetto)
- ⚙️ [Esecuzione](#-esecuzione)
- 📡 [API Endpoints](#-api-endpoints)
- 📊 [Monitoring con actuator](#-monitoring-con-actuator)
- 📖 [Documentazione API con Swagger / OpenAPI](#-documentazione-api-con-swagger--openapi)
- 📈 [Analisi qualità e coverage con SonarQube](#-analisi-qualità-e-coverage-con-sonarqube)
- 🔒 [Sistema di autenticazione](#-Sistema-di-autenticazione)
- 🐳 [Deploy e utilizzo con DockerHub](#-deploy-e-utilizzo-con-dockerhub)
- 🐳 [Deploy completo con Docker Compose](#-deploy-completo-con-docker-compose)
- ☸️ [Deploy su Minikube e Kubernetes locale)](#-deploy-su-minikube-kubernetes-locale)
- 📦 [Versione SQLite per Replit](#-Versione-SQLite-per-Replit)
- 🐳 [Deploy AWS-onprem](#-Deploy-AWS-onprem-MySQL-e-DynamoDB-Local)
- 🚀 [Deploy su AWS EC2](#-Deploy-su-AWS-EC2)
- 🐳 [Deploy su AWS ECS Fargate](#-deploy-su-aws-ecs-fargate)
- 📝 [Roadmap & todo-list](#-Roadmap-&-todo-list)


## 🛠️ Struttura progetto:
- Il progetto segue i principi dell'*Hexagonal Architecture* (Ports and Adapters) e si basa su un'architettura a microservizi modulare:
  ```
  📦 progetto
  ├── 📁 core                  # Interfacce e domini (Hexagonal Core)
  ├── 📁 adapter-api           # REST API Controllers
  ├── 📁 adapter-web           # Risorse statiche e mini-sito di prova
  ├── 📁 adapter-aws           # Implementazione AWS (DynamoDB + MySQL/Aurora)
  ├── 📁 adapter-kafka         # Componenti per la gestione delle code Kafka (ricezione e invio delle annotazioni)
  ├── 📁 adapter-onprem        # Implementazione On-Premise (MongoDB + PostgreSQL)
  ├── 📁 adapter-sqlite        # Implementazione SQLite (con solo il database SQLite locale)
  └── 📁 application           # Applicazione principale Spring Boot
  ```
- **Caratteristiche**:
  - **Multi-ambiente**: Configurazioni dedicate per ambienti On-Premise e AWS Cloud, con profili Spring attivabili dinamicamente. Supporto per PostgreSQL, MySQL, MongoDB, DynamoDB
  - **Deploy flessibile**: Supporto per Docker, Docker Compose, Minikube/Kubernetes, AWS EC2, AWS ECS Fargate.
  - **Architettura esagonale**: Separazione netta tra business logic, API, e infrastruttura, con moduli dedicati per ogni adapter.
  - **Gestione code**: Supporto per la gestione di code come Kafka/SQS per la ricezione e l'invio delle annotazioni
  - **REST API**: Endpoint completi per gestione dei dati. Tutti gli endpoint seguono le convenzioni REST, con metodi HTTP chiari (GET, POST, PUT, DELETE) e risposte in formato JSON. Tutte le operazioni sensibili sono protette da autenticazione JWT e, dove richiesto, da autorizzazione basata su ruolo.
  - **🔒 Autenticazione avanzata**: Gestione utenti, refresh token e JWT con configurazione esterna. 
    - *coming soon*: Integrazione con provider OAuth2 (Google, GitHub, Microsoft).
- **Prerequisiti**:
  - Il profilo *SQLite* in locale: Java 17+, Maven 3.8+, PostgreSQL 13+, MongoDB 4.4+ con Docker opzionale
  - Il profilo *SQLite* in replit: profilo replit attivo e rilascio su progetto GitHub! *Può bastare il profilo gratuito*
  - Il profilo *On-Premise* semplice: Java 17+, Maven 3.8+, PostgreSQL 13+, MongoDB
  - Il profilo *On-Premise* con docker: I precedenti con Docker & Docker-compose
  - Il profilo *AWS* eseguito in locale: I precedenti con Docker & Docker-compose
  - Il profilo *AWS* eseguito on cloud: AWS Account con accesso a RDS MySQL e DynamoDB. *Occhio ai costi perchè alcuni dei servizi usati prevede dei costi di esecuzione PayAsYouGo*


## ⚙️ Esecuzione
- Build del progetto in ambiente di sviluppo
  ```bash
  # Build completo
  mvn clean package
  # Build senza test
  mvn clean package -DskipTests
  ```
- Esecuzione profilo SQLite in locale con database locale, senza bisogno di nessun server DBMS
  ```
  mvn clean package
  java -jar application/target/application-1.0.0.jar \
    --spring.profiles.active=sqlite \
    --spring.datasource.url=jdbc:sqlite:/tmp/database.sqlite \
    --server.port=8082
  ```
  - Comando utile per creare un nuovo utente
    ```
    curl -X POST http://localhost:8082/api/auth/register   -H "Content-Type: application/json"   -d '{
      "username": "alnao2",
      "password": "$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi",
      "email": "admin@example.com"
    }'
    ```
- Esecuzione profilo On-Premise con esecuzione diretta del jar, nel sistema devono essere avviati i servizi DBMS (MongoDB + PostgreSQL)
    ```bash
    # Profilo on-premise di default
    java -jar application/target/application-1.0.0.jar

    # Oppure specificando il profilo
    java -jar application/target/application-1.0.0.jar --spring.profiles.active=onprem
    ```
- Esecuzione profilo On-Premise con il docker-compose che avvia anche i servizi DBMS
    ```bash
    docker-compose up -d --build
    ```
    - l'applicazione web di esempio viene resa disponibile al endpoint
      ```
      http://localhost:8082/
      ```
    - per rimuovere tutto 
      ```bash
      docker-compose down --remove-orphans
      docker network prune -f
      docker volume rm $(docker volume ls -q)
      docker rmi $(docker images -q)
      ```
    - comandi utili
        ```bash
        # Accesso ai log della applicazione
        docker logs gestioneannotazioni-app --tail 500
        # Esecuzione di query nel database postgres
        docker exec -it gestioneannotazioni-postgres psql -U gestioneannotazioni_user -d gestioneannotazioni -c "\d users;"
        docker exec -it gestioneannotazioni-postgres psql -U gestioneannotazioni_user -d gestioneannotazioni -c "SELECT username, email, account_type FROM users;"
        docker exec -it gestioneannotazioni-postgres psql -U gestioneannotazioni_user -d gestioneannotazioni -c "SELECT username, password FROM users WHERE username='alnao';"

        node -c adapter-web/src/main/resources/static/js/annotazioni.js
        ```
    - il monitor di kafka è disponibile al
      ```
      http://localhost:8085/
      ```

## 📡 API Endpoints
- Eseguendo il sistema in locale la base degli URL è `http://localhost:8080` (8081/8085 nel caso di esecuzione tramite docker-compose su Minikube o AWS)
- API di autenticazione gestite da AuthController:
    | Metodo | Endpoint | Descrizione |
    |--------|----------|-------------|
    | POST | `/api/auth/login` | Login locale con username e password |
    | POST | `/api/auth/register` | Registrazione nuovo utente locale |
    | GET  | `/api/auth/me` | Profilo utente autenticato |
    | GET  | `/api/auth/providers` | Lista provider OAuth2 disponibili |
    | POST | `/api/auth/refresh` | Rinnovo del token JWT |
    | POST | `/api/auth/logout` | Logout e invalidazione token |
- Le risorse per le Annotazioni sono:
    | Metodo | Endpoint | Descrizione |
    |--------|----------|-------------|
    | POST | `/api/annotazioni` | Crea nuova annotazione |
    | GET | `/api/annotazioni` | Lista tutte le annotazioni |
    | GET | `/api/annotazioni/{id}` | Ottiene annotazione per ID |
    | PUT | `/api/annotazioni/{id}` | Aggiorna annotazione |
    | DELETE | `/api/annotazioni/{id}` | Elimina annotazione |
    | GET | `/api/annotazioni/utente/{utente}` | Annotazioni per utente |
    | GET | `/api/annotazioni/categoria/{categoria}` | Annotazioni per categoria |
    | GET | `/api/annotazioni/pubbliche` | Solo annotazioni pubbliche |
    | POST | `/api/annotazioni/search` | Ricerca per testo |
    | GET | `/api/annotazioni/stats` | Statistiche |
    | PUT | `/api/annotazioni/{id}/visibilita` | Imposta visibilità pubblica |
    | PUT | `/api/annotazioni/{id}/categoria` | Imposta categoria |
    | PUT | `/api/annotazioni/{id}/tags` | Imposta tags |
    | PUT | `/api/annotazioni/{id}/priorita` | Imposta priorità |
  - Creazione di un'annotazione:
      ```bash
      curl -X POST http://localhost:8080/api/annotazioni \
      -H "Content-Type: application/json" \
      -d '{
          "valoreNota": "Questa è una nota importante",
          "descrizione": "Descrizione della nota",
          "utente": "utente-demo"
      }'
      ```
  - Ricerca per testo e altri metodi di ricerca:
      ```bash
      curl -X GET http://localhost:8081/api/annotazioni/cerca?testo=importante 
      curl -X GET http://localhost:8081/api/annotazioni/utente/utente-demo
      curl -X GET http://localhost:8081/api/annotazioni/categoria/importante 
      curl -X GET http://localhost:8081/api/annotazioni/pubbliche
      curl -X GET http://localhost:8081/api/annotazioni/statistiche
      
      ```



## 📊 Monitoring con actuator
L'applicazione espone endpoint Actuator per il monitoring:
- Health: `http://localhost:8080/actuator/health`
- Metrics: `http://localhost:8080/actuator/metrics`
- Info: `http://localhost:8080/actuator/info`
- Environment: `http://localhost:8080/actuator/env`

Infatti è configurato nel `pom.xml` la dipendenza:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
Nel file di configurazione `application.yaml` è presente il blocco:
```yaml
management:
    endpoints:
        web:
        exposure:
            include: health,info,metrics,env
    endpoint:
        health:
        show-details: always
```


## 📖 Documentazione API con Swagger / OpenAPI
L'applicazione espone la documentazione interattiva delle API REST tramite Swagger UI (OpenAPI 3):
- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)  (o `/swagger-ui/index.html`)
  - In ambiente Docker Compose l'endpoint è [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
- Abilitata con l'aggiunta la dipendenza `springdoc-openapi-starter-webmvc-ui` in `adapter-api/pom.xml`:
  ```xml
  <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.5.0</version>
  </dependency>
  ```
  - e creata la classe di configurazione `SwaggerConfig.java` per personalizzare info e gruppi di API.
    ```
    @Configuration
    public class SwaggerConfig {
        @Bean
        public OpenAPI getGestioneAnnotazioniOpenAPI() {
            return new OpenAPI()
                    .info(new Info().title("Sistema di Gestione Annotazioni API")
                            .description("API per la gestione delle Annotazioni, versioning e storico note.")
                            .version("v1.0.0")
                            .license(new License().name("GPL v3").url("https://www.gnu.org/licenses/gpl-3.0")))
                    .externalDocs(new ExternalDocumentation()
                            .description("Documentazione progetto e repository")
                            .url("https://www.alnao.it/"));
        }
    }
    ```
  - con la possibilità di aggiungere dati nelle annotation OpenAPI ai controller/metodi per arricchire la documentazione.
- *Note di sicurezza*: in ambiente di produzione si consiglia di limitare l'accesso a Swagger UI (che dovrebbe essere attivo solo su ambienti di test/sviluppo).



## 📈 Analisi qualità e coverage con SonarQube
L'applicazione supporta l'analisi statica del codice, la code coverage e la qualità tramite SonarQube. Ecco come avviare e utilizzare SonarQube in locale:

- **Avvio SonarQube tramite Docker**:
    ```bash
    # comando diretto
    docker run -d --name sonarqube -e SONAR_ES_BOOTSTRAP_CHECKS_DISABLE=true -p 9000:9000 sonarqube:latest
    
    # comando con docker-compose (più robusto)
    cd ./script/sonarqube
    docker-compose up

    # comandi per la verifica
    docker ps
    docker logs -f sonarqube
    docker start sonarqube
    ```
    L'interfaccia sarà disponibile su [http://localhost:9000](http://localhost:9000)

- **Primo accesso**:
    - Username: `admin`
    - Password: `admin`
    - Al primo accesso ti verrà chiesto di cambiare la password.

- **Creazione token personale**:
    1. Vai su [http://localhost:9000/account/security](http://localhost:9000/account/security)
    2. Crea un nuovo token (esempio: `sqa_xxxxxxxxxxxxxxxxxxxx`)

- **Esecuzione analisi Maven con coverage**:
    ```bash
    mvn clean verify sonar:sonar \
      -Dsonar.login=sqa_96b98122159fb242ae6b85a0f0ba42d82c41e06d \
      -Dsonar.host.url=http://localhost:9000 \
      -Ponprem
    ```
    - Assicurati che il report di coverage sia generato (JaCoCo è già configurato nei vari moduli).

- **Dashboard e risultati**:
    - Vai su [http://localhost:9000/dashboard?id=it.alnao.annotazioni%3Aannotazioni-parent](http://localhost:9000/dashboard?id=it.alnao.annotazioni%3Aannotazioni-parent) per vedere la qualità, la coverage e i dettagli del progetto.

- **Note**:
    - Se la coverage non appare, assicurati che i test siano eseguiti e che i report `jacoco.xml` siano generati nei vari moduli (`target/site/jacoco/jacoco.xml`).
    - Se la coverage non viene calcolata, il motivo può essere che il disco del server è pieno, si vede con il comando 
      ```bash
      docker exec -it 07de393b8656 cat /opt/sonarqube/logs/es.log
      ```
      che ritorna un errore del tipo
      ```
      2025.09.01 13:25:11 WARN  es[][o.e.c.r.a.DiskThresholdMonitor] flood stage disk watermark [95%] exceeded on [txaoVj8zTtCfBRE4_SfPVQ][sonarqube][/opt/sonarqube/data/es7/nodes/0] free: 3gb[3.3%], all indices on this node will be marked read-only
      ```
    - Puoi personalizzare le regole di qualità e i badge direttamente dalla dashboard SonarQube.


## 🔒 Sistema di autenticazione

Il sistema di autenticazione è progettato per garantire sicurezza, flessibilità e facilità d'integrazione in tutti i moduli del progetto. Le principali caratteristiche sono:

- **Autenticazione locale con JWT**: Gli utenti possono registrarsi e autenticarsi tramite username e password. Dopo la login viene restituito un token JWT da utilizzare per tutte le richieste protette.
- **Gestione utenti**: Endpoint dedicati per la registrazione (`/api/auth/register`), login (`/api/auth/login`), recupero profilo utente (`/api/auth/me`), refresh token e logout.
- **Ruoli e autorizzazioni**: Le operazioni sensibili sono protette da autorizzazione basata su ruolo (es. ADMIN, USER), con validazione automatica dei permessi.
- **Provider OAuth2 (in sviluppo)**: *coming soon* È prevista l'integrazione con provider esterni come Google, GitHub e Microsoft per login federata tramite OAuth2/OpenID Connect.
- **Configurazione modulare**: La logica di autenticazione è separata dal dominio applicativo e facilmente estendibile, con adapter dedicati per ogni tipo di storage (PostgreSQL, MongoDB, SQLite, DynamoDB).
- **Sicurezza**: Password salvate in modo sicuro (hash e salt), validazione input, gestione sicura dei token e delle sessioni.
- **API RESTful**: Tutte le operazioni di autenticazione e gestione utenti sono esposte tramite API REST, con risposte in formato JSON e documentazione OpenAPI/Swagger.

Esempi di istruzioni `curl` per consumare le API per registrarsi ed eseguire la login:
```bash
# Registrazione utente
curl -X POST http://localhost:8082/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "alnao",
    "password": "$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi",
    "email": "admin@example.com"
  }'

# Login utente
curl -X POST http://localhost:8082/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "bellissimo"
  }'
```


## 🐳 Deploy e utilizzo con DockerHub
L'immagine ufficiale dell'applicazione è pubblicata su [DockerHub](https://hub.docker.com/r/alnao/gestioneannotazioni) e può essere scaricata ed eseguita direttamente, senza necessità di build locale.
- **Compilazione e push dell'immagine**
    ```bash
    docker login
    docker build -t alnao/gestioneannotazioni:latest .
    docker push alnao/gestioneannotazioni:latest
    ```
    oppure lanciare lo script 
    ```bash
    ./script/push-image-docker-hub.sh
    ```
- **Pull dell'immagine**:
    ```bash
    docker pull alnao/gestioneannotazioni:latest
    ```
    L'immagine viene aggiornata con le ultime versioni *stabili*.
- **Esecuzione rapida** (della versione sqlite):
    ```bash
    docker run --rm -p 8082:8082 \
      -e SPRING_PROFILES_ACTIVE=sqlite \
      -e SPRING_DATASOURCE_URL=jdbc:sqlite:/tmp/database.sqlite \
      -e SERVER_PORT=8082 \
      alnao/gestioneannotazioni:latest
    ```
    Nota: si può avviare il profilo *sqlite* per eseguire l'immagine senza altri servizi, oppure l'applicazione con il profilo *onprem* se nel sistema sono avviati anche i servizi MongoDb, Postgresql e Kafka come nel prossimo punto.
- **Esecuzione completa** 🔌 Rete Docker condivisa, alternativa robusta ma senza docker-compose:
    Possibile eseguire tutto con docker (senza docker-compose):
    ```bash
    # Creazione rete
    docker network create annotazioni-network

    # Esecuzione mongo
    docker run -d --name annotazioni-mongo \
      --network annotazioni-network \
      -p 27017:27017 \
      -e MONGO_INITDB_DATABASE=annotazioni \
      -e MONGO_INITDB_ROOT_USERNAME=demo \
      -e MONGO_INITDB_ROOT_PASSWORD=demo \
      mongo:4.4
    # Creazione document dentro Mongo
    docker cp script/init-database/init-mongodb.js annotazioni-mongo:/init-mongodb.js
    docker exec -it annotazioni-mongo mongo -u demo -p demo --authenticationDatabase admin /init-mongodb.js

    # Esecuzione postgresql
    docker run -d --name annotazioni-postgres \
      --network annotazioni-network \
      -p 5432:5432 \
      -e POSTGRES_DB=annotazioni \
      -e POSTGRES_USER=demo \
      -e POSTGRES_PASSWORD=demo \
      postgres:13
    # Creazione database nel postgresql
    docker cp script/init-database/init-postgres.sql annotazioni-postgres:/init-postgres.sql
    docker exec -it annotazioni-postgres psql -U demo -d annotazioni -f /init-postgres.sql

    # Esecuzione di Kafka e Zookeeper
    docker run  --rm \
      --network annotazioni-network \
      --name annotazioni-zookeeper \
      -e ZOOKEEPER_CLIENT_PORT=2181 \
      -e ZOOKEEPER_TICK_TIME=2000 \
      -p 2181:2181 \
      confluentinc/cp-zookeeper:7.4.0

    # Zookeeper
    docker run  --rm \
      --network annotazioni-network \
      --name annotazioni-kafka \
      -p 9092:9092 \
      -e KAFKA_BROKER_ID=1 \
      -e KAFKA_ZOOKEEPER_CONNECT=annotazioni-zookeeper:2181 \
      -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://annotazioni-kafka:9092 \
      -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 \
      confluentinc/cp-kafka:7.4.0

    # Kafka-ui
    docker run  --rm -p 8085:8080 \
      --name annotazioni-kafka-ui \
      --network annotazioni-network \
      -e KAFKA_CLUSTERS_0_NAME=gestioneannotazioni-cluster \
      -e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=annotazioni-kafka:9092 \
      -e KAFKA_CLUSTERS_0_ZOOKEEPER=zookeeper:2181 \
      provectuslabs/kafka-ui:latest

    # Esecuzione servizio 
    docker run  --rm -p 8082:8080 --name annotazioni-app \
      --network annotazioni-network \
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://annotazioni-postgres:5432/annotazioni \
      -e SPRING_DATA_MONGODB_URI=mongodb://demo:demo@annotazioni-mongo:27017/annotazioni?authSource=admin \
      -e SPRING_DATASOURCE_USERNAME=demo \
      -e SPRING_DATASOURCE_PASSWORD=demo \
      -e KAFKA_BROKER_URL=annotazioni-kafka:9092 \
      alnao/gestioneannotazioni:latest

    # Applicazione disponibile al url
    http://localhost:8082
    # Kafka-ui disponibile al u rl
    http://localhost:8085

    # Per vedere i container nella rete
    docker network inspect annotazioni-network

    # Per fermare tutto e rimuovere la rete
    docker stop annotazioni-app annotazioni-mongo annotazioni-postgres annotazioni-kafka annotazioni-kafka-ui
    docker rm annotazioni-app annotazioni-mongo annotazioni-postgres annotazioni-kafka annotazioni-kafka-ui
    docker network rm annotazioni-network
    docker network prune -f
    docker volume rm $(docker volume ls -q)
    docker rmi $(docker images -q)
    ```
- **Note**:
    - L'immagine non contiene dati sensibili quindi non c'è problema se viene pubblicata
    - Utilizzare sempre variabili d'ambiente sicure per le password e le connessioni DB in produzione.
    - Tutto questo enorme casino può essere evitato con docker-compose,minikube e kubernetes. Vedere le sezioni dedicate.


## 🐳 Deploy completo con Docker Compose

Per semplificare l’avvio di tutti i servizi necessari (applicazione, PostgreSQL, MongoDB) puoi utilizzare `docker-compose`. Questo permette di gestire tutto con un solo comando, senza dover creare manualmente reti o container.

- **Esempio di file `docker-compose.yml`**:
    ```yaml
    version: '3.8'
    services:
      postgres:
        image: postgres:13
        container_name: annotazioni-postgres
        environment:
          POSTGRES_DB: annotazioni
          POSTGRES_USER: demo
          POSTGRES_PASSWORD: demo
        ports:
          - "5432:5432"
        networks:
          - annotazioni-network

      mongo:
        image: mongo:4.4
        container_name: annotazioni-mongo
        environment:
          MONGO_INITDB_DATABASE: annotazioni
          MONGO_INITDB_ROOT_USERNAME: demo
          MONGO_INITDB_ROOT_PASSWORD: demo
        ports:
          - "27017:27017"
        networks:
          - annotazioni-network

      app:
        image: alnao/annotazioni:latest
        container_name: annotazioni-app
        depends_on:
          - postgres
          - mongo
        environment:
          SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/annotazioni
          SPRING_DATASOURCE_USERNAME: demo
          SPRING_DATASOURCE_PASSWORD: demo
          SPRING_DATA_MONGODB_URI: mongodb://demo:demo@mongo:27017/annotazioni?authSource=admin
        ports:
          - "8080:8080"
        networks:
          - annotazioni-network

    networks:
      annotazioni-network:
        driver: bridge
    ```
- **Avvio dello stack**:
    ```bash
    docker-compose up -d
    ```
- **Fermare e rimuovere tutto**:
    ```bash
    docker-compose down
    docker volume rm $(docker volume ls -q)
    docker rmi $(docker images -q)
    ```
- **Note**:
    - L’applicazione diventa disponibile su [http://localhost:8080](http://localhost:8080)
    - Possibile personalizzare porte, variabili d’ambiente e configurazioni secondo le varie esigenze.
    - Per la produzione, necessario usare password sicure, sistemi di backup e sicurezza dei dati.



## ☸️ Deploy su Minikube (Kubernetes locale)
L’applicazione e i database posso essere eseguiti anche su Minikube, l’ambiente Kubernetes locale, per simulare un cluster cloud-ready.
- Prerequisiti: 
    - Minikube installato ([guida ufficiale](https://minikube.sigs.k8s.io/docs/start/))
    - Kubectl installato
    - Freelens/OpenLens consigliato per la gestione dei pod, service e risorse
- Avvio Minikube:
    ```bash
    minikube start --memory=4096 --cpus=2
    ```
- Manifest già pronti:
    Nella cartella `script/minikube-onprem` trovi i manifest YAML già pronti per avviare tutta l'infrastruttura, presente script che esegue nella giusta sequenza gli script di `kubectl apply`, lo script da lanciare è:
    ```bash
    ./script/minikube-onprem/start-all.sh
    ```
- Accesso all’applicazione:
    - Usando l’Ingress, aggiungendo al file hosts la riga:
      ```
      127.0.0.1 annotazioni.local
      ```
      e visitando [http://annotazioni.local](http://annotazioni.local)
    - Oppure usando il NodePort:
      ```bash
      minikube service annotazioni-app
      ```
      e visitando [http://localhost:30080](http://localhost:30080)
    - Oppure con *freelens* si può creare l'endpoint selezionado il service specifico.
- Note:
    - Anche kafka e il suo kafka-ui è disponibile come ingress oppure usando freelens si crea l'endpoint
    - I dati di MongoDB e PostgreSQL sono persistenti grazie ai PVC di Kubernetes, a meno di usare lo script di `stop-all.sh` che rimuove anche i volumi persistenti.
    - Viene usata l'immagine `alnao/gestioneannotazioni:latest` su dockerHub e non una immagine creata in sistema locale.
    - Per rimuovere tutto lo script da lanciare è
      ```bash
      ./script/minikube-onprem/stop-all.sh
      minikube delete
      ```

## 📦 Versione SQLite per Replit
Sviluppato un adapter specifico per usare sqlite per tutte le basi dati necessarie al corretto funzionamento del servizio, studiato per funzionare anche nel cloud Replit.
- La versione che usa SqLite ha una classe che crea tre utenti di prova partendo dai dati dell'application YAML, è presente proprietà per disattivare questa funzionalità. Il componente è stato creato per velocizzare gli sviluppi e i test, questo componente va rimosso in un sistema di produzione. In alternatica è sempre possibile creare gli utenti con le API:
    ```
    curl -X POST http://localhost:8082/api/auth/register \
      -H "Content-Type: application/json" \
      -d '{
        "username": "alnao",
        "password": "$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi",
        "email": "alnao@example.com"
      }'
    curl -X POST http://localhost:8082/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{
        "username": "admin",
        "password": "xxxxxxxxxxxxxxxxxxx"
      }'
    ```
- Utilizzado SQLite come unico database per tutte le funzionalità (annotazioni, utenti, storico) non ha nessuna dipendenza da servizi esterni: è possibile eseguire tutto in locale, ideale per prove locali o test. Previsto un profilo Spring Boot specifico `sqlite`. I comandi per eseguire il microservizio in locale con questo profilo sono:
  ```
  mvn clean package
  java -jar application/target/application-1.0.0.jar \
    --spring.profiles.active=sqlite \
    --spring.datasource.url=jdbc:sqlite:/tmp/database.sqlite \
    --server.port=8082
  ```
- E' stato creato un docker-compose specifico, così da gestire il volume dei dati con docker. Script di avvio e arresto già pronti per esecuzione locale: per eseguire tutto in locale eseguire lo script:
  ```
  cd script/sqlite-locale/
  ./start-all.sh
  ```
  - L'applicazione web sarà disponibile su [http://localhost:8082](http://localhost:8082)
  - Interfaccia di gestione SQLite su [http://localhost:8084](http://localhost:8084)
  - Fermare l'esecuzione
    ```
    cd script/replit-locale
    ./stop-all.sh
    ```
- Per l'esecuzione nel sistema **Replit**, i passi da eseguire sono:
  - Eseguire login su [replit.com](https://replit.com/) con utenza, non serve avere abilitata la versione a pagamento.
  - Selezionare la funzionalità `Import code or design` e selezionare il tipo `github`
  - Nella schermata di configurazione inserire il repository pubblico
    - per esempio `https://github.com/alnao/JavaSpringBootExample`
  - Lasciare che il sistema scarichi il progetto e compili, in teoria l'IA di Replit intuirà da sola che deve avviare il progetto con il profilo `sqlite`. Se non lo fà bisgna indicarlo nella chat dell'agente che esegue il microservizio.
  - Si può chiedere alla chat se il servizio è attivo e di ritornare l'endpoint che sarà del tipo:
    ```
    https://xxx-xxx-xxx.worf.replit.dev
    ```
  - Se la creazione degli utenti è disabilitata, è possibile creare un utente di prova con postaman/curl, per esempio:
    ```
    curl -X POST https://xxx-xxx-xxx.worf.replit.dev/api/auth/register \
      -H "Content-Type: application/json" \
      -d '{
        "username": "alnao",
        "password": "$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi",
        "email": "alnao@example.com"
      }'
    curl -X POST https://xxx-xxx-xxx.worf.replit.dev/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{
        "username": "alnao",
        "password": "bellissimo"
      }'
    ```
  - Verificare con browser che il servizio è disponibile all'endpoint: `https://xxx-xxx-xxx.worf.replit.dev`
  - Il replit creato nel cloud risulta poi disponibile su:
    ```
    https://replit.com/@alnao84/JavaSpringBootExample
    ```
- Versione **Sqlite** su **AWS-EC2**: è stata sviluppata anche uno script per eseguire il microservizio in una istanza EC2 con il profilo sqlite con docker e senza bisogno di RDS, Dynamo e ECS. 
  - Script di creazione dello stack (Key, SecurityGroup e avvio istanza EC2):
    ```
    ./script/sqlite-ec2/start-all.sh 
    ```
  - Comandi per collegarsi all'istanza EC2, verificare l'output del user-data e la creazione dell'utente:
    ```
    ssh -i gestioneannotazioni-sqlite-ec2-key.pem ec2-user@x.y.z.a
    sudo cat /var/log/cloud-init-output.log
    sudo tail /var/log/cloud-init-output.log --follow

    curl -X POST http://localhost:8082/api/auth/register \
      -H "Content-Type: application/json" \
      -d '{
        "username": "alnao",
        "password": "$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi",
        "email": "alnao@example.com"
      }'
    ```
  - Script di deprovisioning di tutte le risorse create:
    ```
    ./script/sqlite-ec2/stop-all.sh 
    ```


## 🐳 Deploy AWS-onprem (MySQL e DynamoDB Local)

Per simulare l'ambiente AWS in locale (MySQL come RDS, DynamoDB Local, Adminer, DynamoDB Admin UI, Spring Boot profilo AWS):
- Prima di eseguire il comando di compose bisogna verficare che la versione dell'immagine su DockerHub sia aggiornata!
    ```bash
    ./script/push-image-docker-hub.sh
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
  - **Frontend**:        [http://localhost:8085](http://localhost:8085)
  - **Backend API**:     [http://localhost:8085/api/annotazioni](http://localhost:8085/api/annotazioni)
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


## 🚀 Deploy su AWS EC2
Questa modalità consente di eseguire l'intero stack annotazioni su AWS EC2, con provisioning completamente automatizzato di tutte le risorse cloud necessarie (Aurora MySQL, DynamoDB, EC2, Security Group, IAM Role, KeyPair, ecc.) tramite script Bash e AWS CLI.
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
  | EC2 t3.medium    | ~1,2 USD             | ~37 USD             | ~1,2 USD                   | ~37 USD                  |
  | ECR/Storage      | trascurabile         | trascurabile        | trascurabile               | trascurabile             |
  | **Totale**       | **~3,6 USD**         | **~110 USD**        | **~3,7 USD**               | **~115 USD**             |


## 🐳 Deploy su AWS ECS Fargate
Questa modalità consente di eseguire l'intero stack annotazioni su AWS ECS con Fargate, utilizzando container serverless completamente gestiti da AWS. Il provisioning automatizzato include tutte le risorse cloud necessarie (Aurora MySQL, DynamoDB, ECR, ECS Cluster, Task Definition, Service, IAM Roles, Security Groups, ecc.) tramite script Bash e AWS CLI.

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
    2. **IAM Roles**: Creazione Task Role (accesso Aurora/DynamoDB) e Execution Role (logging CloudWatch)
    3. **Networking**: Creazione Security Groups con regole per HTTP (8080), Aurora (3306), HTTPS/SSH
    4. **Aurora MySQL**: Provisioning cluster RDS con inizializzazione database e tabelle
    5. **SQS**: Creazione coda SQS per *l'invio* delle annotazioni confermate
    5. **DynamoDB**: Creazione tabelle `annotazioni` e `annotazioni_storico` con attributi ottimizzati
    6. **ECS Deployment**: Creazione cluster, task definition, service con Fargate e auto-scaling
    7. **CloudWatch Logs**: Configurazione logging applicativo con retention automatica
    8. **Endpoint Discovery**: Rilevamento automatico IP pubblico del task per accesso HTTP
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
  - Aurora endpoint automaticamente rilevato e configurato nel container

- Tabella dei costi stimati per risorse sempre accese (24/7), regione Francoforte (eu-central-1), prezzi AWS settembre 2025:
  
  | Servizio              | Carico Basso (giorno) | Carico Basso (mese) | Carico Medio (giorno) | Carico Medio (mese) |
  |-----------------------|----------------------|---------------------|----------------------|--------------------|
  | ECS Fargate (1 vCPU, 2GB RAM) | ~0,8 USD (1 task 24/7) | ~24 USD (1 task 24/7) | ~1,6 USD (2 task avg) | ~48 USD (2 task avg) |
  | Aurora MySQL (db.r6g.large) | ~2,4 USD (1 instance) | ~72 USD (1 instance) | ~2,4 USD (1 instance) | ~72 USD (1 instance) |
  | DynamoDB (On-Demand) | ~0,01 USD (<1K RCU/WCU) | ~0,30 USD (<1K RCU/WCU) | ~0,05 USD (~5K RCU/WCU) | ~1,50 USD (~5K RCU/WCU) |
  | ECR Repository (Storage immagini) | ~0,05 USD (~5GB storage) | ~1,50 USD (~5GB storage) | ~0,05 USD (~5GB storage) | ~1,50 USD (~5GB storage) |
  | CloudWatch Logs (Log retention) | ~0,02 USD (~1GB logs) | ~0,60 USD (~1GB logs) | ~0,10 USD (~5GB logs) | ~3,00 USD (~5GB logs) |
  | VPC (Subnet/Route Tables/IGW) | ~0,01 USD (risorse base) | ~0,30 USD (risorse base) | ~0,01 USD (risorse base) | ~0,30 USD (risorse base) |
  | Traffico di Rete (Data Transfer) | ~0,05 USD (~5GB out) | ~1,50 USD (~5GB out) | ~0,20 USD (~20GB out) | ~6,00 USD (~20GB out) |
  | **TOTALE BASE** | **~3,4 USD** | **~102 USD** | **~4,4 USD** | **~135 USD** |
  | Application Load Balancer (opzionale) | ~0,75 USD (se abilitato) | ~22,50 USD (se abilitato) | ~0,75 USD (se abilitato) | ~22,50 USD (se abilitato) |
  | **TOTALE + ALB** | **~4,1 USD** | **~124 USD** | **~5,1 USD** | **~157 USD** |
  | NAT Gateway (per private subnet) | ~1,50 USD (se configurato) | ~45 USD (se configurato) | ~1,50 USD (se configurato) | ~45 USD (se configurato) |
  | **TOTALE + ALB + NAT** | **~5,6 USD** | **~169 USD** | **~6,6 USD** | **~202 USD** |


## 📝 Roadmap & todo-list
- ✅ ⚙️ Creazione progetto con maven, creazione dei moduli adapter, adapter web con pagina web di esempio, test generale di esecuzione
  - ✅ 📝 Funzione di modifica annotazioni con registro con precedenti versioni delle note
  - ✅ 📖 Configurazione di OpenApi-Swagger e Quality-SonarQube, test coverage e compilazione dei moduli
  - ✅ 📦 Creazione adapter con implementazione con SQLite come unica base dati
    - ✅ ☁️ Sviluppo script per esecuzione profilo sqlite in sistema Replit
    - ✅ ⚙️ Sviluppo script per esecuzione profilo sqlite in sistema AWS-EC2 con Docker senza RDS e Dynamo
    - ✅ 🧿 Script per creazione di tre profili in ogni ambiente per adapter sqlite
  - ✅ 🤖 Gestione dell'applicazione in *gestione annotazioni* e test applicazione web di esempio
    - ✅ 🛠️ Test applicazione web di esempio anche su AWS
    - ✅ 🔧 Modifica nome adapter "app" e "port" in "application" e "core"
    - ✅ 🎯 Creazione portService, modifica ai Controller-api e spostamento logiche dai Controller nei Service nel core
- ✅ 🔧 Creazione enum Stato di una annotazione
  - ✅ 🔄 Aggiunta campo "Stato" nei metadati delle annotazioni nelle tabelle
  - ✅ 🧮 Nuova tabella StoricoStati, sviluppo service e port per la gestione dello storico
  - ✅ 🕸️ Modifica service per cambio stato che modifica il metadata e non il valore più la tabella storico
  - ✅ 🧩 Service per modificar lo stato con salvataggio nella tabella StoricoStati
  - ✅ 🧑‍🔬 Inserimento di una nuova annotazione in stato INSERITA
  - ✅ 🛰️ Gestione dello stato DAINVIARE come ultimo stato possibile da API/Web.
  - 🚧 🧱 Verifica che utenti non possano fare operazioni il cui ruolo non lo prevede
- ✅ 🐳 Build e deploy su DockerHub della versione *OnPrem*
  - ✅ 🐳 configurazione di docker-compose con MongoDb e Postgresql
  - ✅ ☸️ Esecuzione su Kubernetes/Minikube locale con yaml dedicati
- ✅ ☁️ Esecuzione con docker-compose della versione AWS su sistema locale con Mysql e DynamoDB 
  - ✅ 🐳 Deploy su AWS usando EC2 per eseguire il container docker, script scritto in AWS-CLI per il provisioning delle risorse necessarie (Aurora-RDS-Mysql e DynamoDB ) e la creazione della EC2 con lancio del docker con `user_data`
  - ✅ 🐳 Deploy su AWS usando ECS, Fargate e repository ECR (senza DockerHub), script scritto in AWS-CLI per il provisioning delle risorse necessarie (Aurora-RDS-Mysql e DynamoDB ) e lancio del task su ECS. Non previsto sistema di scaling up e/o bilanciatore ALB.
- ✅ 🔒 Autenticazione e autorizzazione (Spring Security) e token Jwt
  - ✅ 👥 introduzione sistema di verifica degli utenti e validazione richieste con tabella utenti
  - ✅ 📝 Gestione multiutente e modifica annotazioni con utente diverso dal creatore, test nell'applicazione web
  - ✅ 🛡️ Centralità dei service JwtService e UserService nel core senza `adapter-security`
  - 🚧 👥 Sistema di lock che impedisca che due utenti modifichino la stessa annotazione allo stesso momento
  - 🚧 🧑‍🤝‍🧑 Gestione modifica annotazione con lock
- ✅ ⚙️ Evoluzione adapter con integrazione con altri sistemi
  - ✅ 🧬 Gestione delle annotazioni in stato INVIATA
  - ✅ 📚 Export annotazioni: creazione service che permetta di inviare notifiche via coda (kafka o sqs) con creazione `adapter-kafka` e che con frequenza invii delle annotazioni concluse con cambio di stato
  - ✅ ☁️ Configurazione del servizio SQS nell'adapter AWS e test nelle versioni EC2 e ECS
- 🚧 🏁 Test finale di tutti i punti precedenti e tag della versione 0.1.0
- 🚧 ☁️ Integrazione con Azure
  - ✅ Creazione del adapter Azure e inizio sviluppi
  - 🚧 Prima esecuzione in locale adapter azure
  - 🚧 Scrittura degli script deploy su Azure
- 🚧 ☁️ Esecuzione su Cloud
  - 🚧 🐳 Deploy su AWS su EKS
  - 🚧 🔧 Sistem di Deploy con Kubernetes Helm charts
  - 🚧 📈 Auto-Scaling Policies: Horizontal Pod Autoscaler (HPA) e Vertical Pod Autoscaler (VPA) per Kubernetes
  - 🚧 🔄 Import annotazioni (JSON e/o CSV): creazione service per l'import di annotazioni con cambio di stato dopo averle importate con implementazioni su tutti gli adapter
  - 🚧 🎯 Notifiche real-time (WebSocket): creazione `adapter-notifier` che permetta ad utenti di registrarsi su WebSocket e ricevere notifiche su cambio stato delle proprie notifiche
    - 🚧 👥 Social Reminders: Notifiche quando qualcuno interagisce con annotazioni modificate
  - 🚧 🧭 Sistema che gestisce la scadenza di una annotazione con spring-batch che elabora tutte le annotazioni rifiutate o scadute, con nuovo stato scadute.
  - 🚧 💾 Backup & Disaster Recovery: Cross-region backup, point-in-time recovery, RTO/RPO compliance
  - 🚧 🔐 OAuth2/OIDC Provider: Integrazione con provider esterni (Google, Microsoft, GitHub) + SSO enterprise
- 🚧 🏁 Test finale di tutti i punti precedenti e tag della versione 0.2.0
- 🚧 🗃️ Sistema di caching con redis
  - 🚧 ⚡ Redis Caching Layer: Cache multi-livello (L1: in-memory, L2: Redis) con invalidation strategies e cache warming
  - 🚧 📊 Read Replicas: Separazione read/write con eventual consistency e load balancing intelligente
  - 🚧 🔒 API Rate Limiting: Rate limiting intelligente con burst allowance, IP whitelisting, geographic restrictions
- 🚧 🔍 Elasticsearch Integration: Ricerca full-text avanzata con highlighting, auto-complete, ricerca semantica
- 🚧 🏗️ GitOps Workflow: ArgoCD/Flux per deployment automatici, configuration drift detection
- 🚧 🧪 Testing Pyramid: Unit + Integration + E2E + Performance + Security testing automatizzati
- 🚧 📦 Container Security: Vulnerability scanning (Trivy/Snyk), distroless images, rootless containers
- 🚧 🎯 Feature Flags: LaunchDarkly/ConfigCat integration per feature toggling, A/B testing, gradual
- 🚧 💬 Comment Threads: Sistema di commenti su singole annotazioni con threading e notifications
- 🚧 📎 File Attachments: Supporto allegati (immagini, PDF, documenti) con preview e versioning
- 🚧 📝 Templates & Forms: Template predefiniti per annotazioni (meeting notes, bug reports, ideas) con campi strutturati
- 🚧 🔄 Annotation Workflows: Stati delle annotazioni (draft→review→approved→published) con approval process e notifiche
- 🚧 ✅ Annotation-to-Task Conversion: Trasforma automaticamente annotazioni in todo items con parsing intelligente di date, persone, azioni
- 🚧 📅 Smart Date Recognition: NLP per riconoscere date naturali ("domani", "la prossima settimana", "tra 3 giorni") e convertirle in deadline
- 🚧 🔄 Recurring Tasks: Todo ricorrenti (giornalieri, settimanali, mensili) generati automaticamente da template di annotazioni
- 🚧 ⏰ Time Boxing: Stima automatica del tempo necessario per task basata su annotazioni simili completate
- 🚧 📈 Progress Tracking: Visualizzazione progresso con barre, percentuali, streak counters
- 🚧 🔗 Task Dependencies: Link tra todo items per gestire sequenze e blocchi
- 🚧 ⏰ Context-Aware Reminders: Promemoria basati su location, tempo, altre attività ("Ricorda quando arrivi in ufficio")
- 🚧 Weekly Digest: Riassunto settimanale con achievement, todo completati, annotazioni più accedute
- 🚧 🎤 Voice Notes: Registrazione audio con trascrizione automatica e timestamp
- fantasie dell'IA
  - 🚧 📋 Recommendation Engine: Sistema di raccomandazioni basato su ML per suggerire annotazioni correlate, utenti simili, contenuti rilevanti
  - 🚧 🤖 AI-Powered Insights: Integrazione OpenAI/Bedrock per suggerimenti automatici di categorizzazione, sentiment analysis delle note, auto-completamento intelligente
  - 🚧 📱 Mobile-First PWA: Progressive Web App con offline-sync, push notifications, gesture navigation
  - 🚧 🎨 Rich Text Editor: Editor WYSIWYG con markdown, syntax highlighting, embed multimedia, link preview
  - 🚧 ☁️ Serverless Functions: AWS Lambda/Azure Functions per task asincroni (email, reporting, cleanup)
  - 🚧 🔧 Admin Panel: Interfaccia amministrativa per configurazione sistema, user management, monitoring
  - 🚧 📚 Developer Portal: API documentation interattiva, SDK multi-linguaggio, code examples
  - 🚧 📖 Migration Tools: Import/export da altri sistemi, data transformation, migration assistants
  - 🚧 🎤 Voice-to-Text Advanced: Transcription multilingua con speaker identification e emotion detection
  - 🚧 🤖 Conversational Annotation: Chatbot intelligente per creare annotazioni tramite dialogo naturale
  - 🚧 🌱 Carbon Footprint Tracking: Monitoring dell'impatto ambientale dell'infrastruttura
  - 🚧 ♻️ Green Computing Optimization: Automatic migration a data centers con energia rinnovabile
  - 🚧 📊 Sustainability Metrics: KPI per misurare efficienza energetica e carbon impact
  - 🚧 🌿 Eco-Friendly Features: Dark mode per battery saving, compression algorithms, lazy loading


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



