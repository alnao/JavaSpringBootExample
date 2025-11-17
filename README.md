# Sistema di Gestione annotazioni

  <p align="center">
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=black"  height=60/>
    <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white"  height=60/>
  </p>

Progetto realizzato da `< AlNao />` come esempio pratico con Java Spring Boot: consente di creare, modificare e visualizzare annotazioni, utenti con privilegi da moderatore possono confermare le annotazioni e utenti con privilegi da amministratori possono confermare e *inviare* annotazioni a sistemi esterni.


La soluzione è strutturata in moduli multipli, basata su Spring Boot e sull’architettura esagonale ([Hexagonal Architecture](https://en.wikipedia.org/wiki/Hexagonal_architecture_(software))), con pieno supporto al deployment sia in ambienti on-premise che su cloud come AWS e Azure sfruttando Docker e Kubernetes.


Il progetto è pensato per essere agnostico rispetto al cloud provider: sono sviluppate implementazioni per Replit, AWS e Azure. Il DBMS utilizzato dipende dal profilo selezionato:


| Profilo | Sistema/Cloud | DBMS Sql | DBMS No-Sql | Export | Lock annotazioni |
|--------|----------|-------------|-------------|----------|--------------------|
| `kube` | ![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat-square&logo=kubernetes&logoColor=white) | ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white) | ![MongoDB](https://img.shields.io/badge/MongoDB-47A248?style=flat-square&logo=mongodb&logoColor=white) | ![Kafka](https://img.shields.io/badge/Kafka-434F40?style=flat-square&logo=apachekafka&logoColor=white) | ![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white) | 
| `sqlite` | ![Replit](https://img.shields.io/badge/Replit-F26207?style=flat-square&logo=replit&logoColor=white) | ![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white) | ![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white) | ![SQLite](https://img.shields.io/badge/SQLite-003B57?style=flat-square&logo=sqlite&logoColor=white) | ![Java](https://img.shields.io/badge/ConcurrentHashMap-ED8B00?style=flat-square&logo=openjdk&logoColor=white) | 
| `aws` | ![AWS](https://img.shields.io/badge/AWS-FF9900?style=flat-square&logo=amazonaws&logoColor=white) | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white) | ![DynamoDB](https://img.shields.io/badge/DynamoDB-4053D6?style=flat-square&logo=amazondynamodb&logoColor=white) | ![SQS](https://img.shields.io/badge/SQS-FF9900?style=flat-square&logo=amazonaws&logoColor=white) | ![Redis](https://img.shields.io/badge/ElastiCache%20for%20Redis-DC382D?style=flat-square&logo=redis&logoColor=white) | 
| `azure` | ![Azure](https://img.shields.io/badge/Azure-0078D4?style=flat-square&logo=microsoftazure&logoColor=white) | ![SQL Server](https://img.shields.io/badge/SQL%20Server-CC2927?style=flat-square&logo=microsoftsqlserver&logoColor=white) | ![Cosmos DB](https://img.shields.io/badge/Cosmos%20DB-0089D6?style=flat-square&logo=azurecosmosdb&logoColor=white) | ![Service Bus](https://img.shields.io/badge/Service%20Bus-0089D6?style=flat-square&logo=microsoftazure&logoColor=white) | ![Azure Cache for Redis](https://img.shields.io/badge/Cache%20for%20Redis-DC382D?style=flat-square&logo=redis&logoColor=white) | 


## 📚 Indice rapido

- 🛠️ [Struttura progetto](#-struttura-progetto)
  - ⚙️ [Esecuzione locale](#-Esecuzione-locale)
  - 📡 [API Endpoints](#-api-endpoints)
  - 📊 [Monitoring con actuator](#-monitoring-con-actuator)
  - 📖 [Documentazione API con Swagger / OpenAPI](#-documentazione-api-con-swagger--openapi)
  - 📈 [Analisi qualità e coverage con SonarQube](#-analisi-qualità-e-coverage-con-sonarqube)
  - ⏰ [Sistema di lock distribuito con Redis](#-Redis)
  - 🔒 [Sistema di autenticazione](#-Sistema-di-autenticazione)
- 🐳 [Deploy ed esecuzione con DockerHub](#-deploy-ed-esecuzione-con-dockerhub)
  - 🐳 [Esecuzione completa con Docker Compose (con Mongo e Postgresql)](#-Esecuzione-completa-con-Docker-Compose)
  - ☸️ [Esecuzione su Minikube e Kubernetes locale](#-Esecuzione-su-Minikube-e-Kubernetes-locale)
  - 📦 [Versione SQLite per Replit](#-Versione-SQLite-per-Replit)
- ☁️ [Esecuzione del profilo AWS (con MySql e Dynamo)](#-Esecuzione-del-profilo-AWS-in-locale)
  - 🚀 [Esecuzione su AWS EC2](#-Esecuzione-su-AWS-EC2)
  - 🐳 [Esecuzione su AWS ECS Fargate](#-Esecuzione-su-aws-ecs-fargate)
- ☁️ [Esecuzione locale profilo Azure (con CosmosDB e SqlServer)](#-Esecuzione-locale-profilo-Azure)
  - 🚀 [Esecuzione locale profilo Azure con db remoti su Azure](#-Esecuzione-locale-profilo-Azure-con-db-remoti-su-Azure)
  - 🐳 [Esecuzione locale profilo Kube con db remoti su Azure](#-Esecuzione-locale-profilo-Kube-con-db-remoti-su-Azure)
  - 🚀 [Esecuzione su VirtualMachine Azure del profilo Azure](#-Esecuzione-su-VirtualMachine-Azure-del-profilo-Azure)
  - 🚀 [Esecuzione su Azure Container Instances del profilo Azure](#-Esecuzione-su-Azure-Container-Instances-del-profilo-Azure)
- 📝 [Roadmap & todo-list](#-Roadmap-&-todo-list)
  - ✅ [Test di non regressione](#-Test-di-non-regressione)


## 🛠️ Struttura progetto:
- Il progetto segue i principi dell'*Hexagonal Architecture* (Ports and Adapters) e si basa su un'architettura a microservizi modulare:
  ```
  📦 progetto
  ├── 📁 core                  # Interfacce e domini (Hexagonal Core)
  ├── 📁 adapter-api           # REST API Controllers
  ├── 📁 adapter-aws           # Implementazione AWS (DynamoDB + MySQL/Aurora)
  ├── 📁 adapter-azure         # Implementazione Azure (CosmosDB + SqlServer)
  ├── 📁 adapter-kafka         # Componenti per la gestione delle code Kafka (profilo kube)
  ├── 📁 adapter-mongodb       # Implementazione per la gestione di MongoDB (profilo kube)
  ├── 📁 adapter-postgresql    # Implementazione per la gestione di PostgreSQL (profilo kube)
  ├── 📁 adapter-redis         # Sistema di lock distribuiti con Redis
  ├── 📁 adapter-sqlite        # Implementazione SQLite (con solo il database SQLite locale)
  ├── 📁 adapter-web           # Risorse statiche e mini-sito di prova
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
  - Il profilo *On-Premise* con docker: Sistema docker installato 
  - Il profilo *AWS* eseguito in locale: Sistema docker installato 
  - Il profilo *AWS* eseguito on cloud: Account AWS con accesso a RDS MySQL e DynamoDB.
    - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
  - Il profilo *Azure* eseguito in locale *non funziona perchè l'immagine Cosmos non funziona*
  - Il profilo *Azure* eseguito on cluod su VirtualMachine e/o ContainerInstances: Account Azure con accesso a Cosmos e MsSql
    - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️

### ⚙️ Esecuzione locale
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
  java -jar application/target/application-*.jar \
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

### 📡 API Endpoints
- Eseguendo il sistema in locale la base degli URL è `http://localhost:8080` (8081/8082 nel caso di esecuzione tramite docker-compose su Minikube o AWS)
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
  - Prenotazione/lock di un'annotazione (acquisisce lock per XX secondi)
    ```
    curl -X POST http://localhost:8080/api/annotazioni/{id}/prenota \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"utente":"admin"}'
    ```
    - Verifica stato prenotazione
      ```
      curl -X GET http://localhost:8080/api/annotazioni/{id}/prenota/stato \
        -H "Authorization: Bearer $TOKEN"
      ```
    - Rilasciare una prenotazione manualmente
      ```
      curl -X DELETE http://localhost:8080/api/annotazioni/{id}/prenota \
        -H "Authorization: Bearer $TOKEN" \
        -H "Content-Type: application/json" \
        -d '{"utente":"admin"}'
      ```


### 📊 Monitoring con actuator
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


### 📖 Documentazione API con Swagger / OpenAPI
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



### 📈 Analisi qualità e coverage con SonarQube
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
      -Pkube
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

### ⏰ Redis
Redis è integrato nell'applicazione come sistema di **lock distribuito** per gestire la prenotazione delle annotazioni e prevenire modifiche concorrenti. L'integrazione utilizza `Redisson` come client Redis per Spring Boot.
- **Funzionalità di Lock Distribuito**
  - Prenotazione annotazioni: Gli utenti possono "prenotare" un'annotazione per modificarla in esclusiva per XX secondi
  - Prevenzione conflitti: Se un'annotazione è bloccata da un utente, altri utenti non possono modificarla fino al rilascio del lock
  - Auto-rilascio: I lock vengono automaticamente rilasciati dopo XX secondi o quando l'utente completa la modifica
- **Configurazione**
  La configurazione Redis si trova in:
  - `application/src/main/resources/application-kube.yml`: Configurazione per profilo Docker/Kubernetes con Redisson
  - `adapter-redis/.../service/RedisLockService.java`: Implementazione lock distribuito con Redisson
  - `adapter-sqlite/.../service/InMemoryLockService.java`: Implementazione lock in-memory per profilo sqlite
  - **Nota**: Nel profilo `sqlite`, Redis non è necessario e viene automaticamente disabilitato tramite auto-configuration excludes
- **Implementazioni Alternative**
  - Profili `kube` (Docker/Kubernetes), `aws` e `azure`: Utilizza Redis distribuito via Redisson per ambienti multi-istanza
  - Profilo `sqlite`**: Utilizza `InMemoryLockService` con `ConcurrentHashMap` per ambienti single-instance (suggerito per sviluppo/test)
- **API di Prenotazione**
  - Prenota un'annotazione (acquisisce lock per XX secondi)
    ```
    curl -X POST http://localhost:8080/api/annotazioni/{id}/prenota \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"utente":"admin"}'
    ```
  - Verifica stato prenotazione
    ```
    curl -X GET http://localhost:8080/api/annotazioni/{id}/prenota/stato \
      -H "Authorization: Bearer $TOKEN"
    ```
  - Rilascia prenotazione manualmente
    ```
    curl -X DELETE http://localhost:8080/api/annotazioni/{id}/prenota \
      -H "Authorization: Bearer $TOKEN" \
      -H "Content-Type: application/json" \
      -d '{"utente":"admin"}'
    ```
- Comandi Utili Redis
  - Visualizza tutti i lock attivi
    ```
    docker exec gestioneannotazioni-redis redis-cli HGETALL "annotation:lock:owners"
    ```
  - Visualizza tutte le chiavi
    ```
    docker exec gestioneannotazioni-redis redis-cli KEYS "*"
    ```
  - Pulisci completamente Redis (attenzione: operazione distruttiva perchè cancella tutta la memoria) 
    ```
    docker exec gestioneannotazioni-redis redis-cli FLUSHALL
    ```
  - Monitora comandi in real-time
    ```
    docker exec gestioneannotazioni-redis redis-cli MONITOR
    ```


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


## 🐳 Deploy ed esecuzione con DockerHub
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
    Nota: si può avviare il profilo *sqlite* per eseguire l'immagine senza altri servizi, oppure l'applicazione con il profilo *kube* se nel sistema sono avviati anche i servizi MongoDb, Postgresql e Kafka come nel prossimo punto.
- **Esecuzione completa** 🔌 Rete Docker condivisa, alternativa robusta ma senza docker-compose:
    Possibile eseguire tutto con docker (senza docker-compose):
    ```bash
    # Creazione rete
    docker network create annotazioni-network

    # Esecuzione mongo
    docker run -d --name annotazioni-mongo \
      --network annotazioni-network \
      -p 27017:27017 \
      -e MONGO_INITDB_DATABASE=gestioneannotazioni \
      -e MONGO_INITDB_ROOT_USERNAME=admin \
      -e MONGO_INITDB_ROOT_PASSWORD=admin123 \
      mongo:4.4
    # Creazione document dentro Mongo
    docker cp script/init-database/init-mongodb.js annotazioni-mongo:/init-mongodb.js
    docker exec -it annotazioni-mongo mongo -u demo -p demo --authenticationDatabase admin /init-mongodb.js

    # Esecuzione postgresql
    docker run -d --name annotazioni-postgres \
      --network annotazioni-network \
      -p 5432:5432 \
      -e POSTGRES_DB=gestioneannotazioni \
      -e POSTGRES_USER=gestioneannotazioni_user \
      -e POSTGRES_PASSWORD=gestioneannotazioni_pass \
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
    docker run  --rm -p 8082:8080 --name gestioneannotazioni-app \
      --network javaspringbootexample_gestioneannotazioni-network \
      -e SPRING_PROFILES_ACTIVE="kube" \
      -e POSTGRES_URL="jdbc:postgresql://postgres:5432/gestioneannotazioni" \
      -e POSTGRES_USERNAME="gestioneannotazioni_user" \
      -e POSTGRES_PASSWORD="gestioneannotazioni_pass" \
      -e MONGODB_URI="mongodb://admin:admin123@mongodb:27017/gestioneannotazioni_db?authSource=admin" \
      -e KAFKA_BROKER_URL="kafka-server:29092" \
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


### 🐳 Esecuzione completa con Docker Compose

Per semplificare l’avvio di tutti i servizi necessari (applicazione, PostgreSQL, MongoDB) puoi utilizzare `docker-compose`. Questo permette di gestire tutto con un solo comando, senza dover creare manualmente reti o container.

- **Esempio di file `docker-compose.yml`**:
    ```yaml
    version: '3.8'
    services:
      ... #mongo, postgresql, kafka e redis!

      app:
        image: alnao/annotazioni:latest
        container_name: annotazioni-app
        depends_on:
          - postgres
          - mongo
        environment:
          SPRING_PROFILES_ACTIVE: kube
          POSTGRES_URL: jdbc:postgresql://postgres:5432/gestioneannotazioni
          POSTGRES_USERNAME: gestioneannotazioni_user
          POSTGRES_PASSWORD: gestioneannotazioni_pass
          MONGODB_URI: mongodb://admin:admin123@mongodb:27017/gestioneannotazioni_db?authSource=admin
          KAFKA_BROKER_URL: kafka-server:29092
          REDIS_HOST: redis
          REDIS_PORT: 6379
          EXPORT_ANNOTAZIONI_CRON_EXPRESSION: "0 */2 * * * *" # ogni 2 minuti
          SERVER_PORT: 8080
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
    - L’applicazione diventa disponibile su [http://localhost:8082](http://localhost:8082)
    - Possibile personalizzare porte, variabili d’ambiente e configurazioni secondo le varie esigenze.
    - Per la produzione, necessario usare password sicure, sistemi di backup e sicurezza dei dati.



### ☸️ Esecuzione su Minikube e Kubernetes locale
L’applicazione e i database posso essere eseguiti anche su Minikube, l’ambiente Kubernetes locale, per simulare un cluster cloud-ready.
- Prerequisiti: 
    - Minikube installato ([guida ufficiale](https://minikube.sigs.k8s.io/docs/start/))
    - Kubectl installato
    - Freelens/OpenLens consigliato per la gestione dei pod, service e risorse
- Avvio Minikube:
    ```bash
    minikube start --memory=8096 --cpus=4
    ```
    nota: sono tante risorse, forse si possono ridurre un po'!
- Manifest già pronti:
    Nella cartella `script/minikube` trovi i manifest YAML già pronti per avviare tutta l'infrastruttura, presente script che esegue nella giusta sequenza gli script di `kubectl apply`, lo script da lanciare è:
    ```bash
    ./script/minikube/start-all.sh
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
      ./script/minikube/stop-all.sh
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
  java -jar application/target/application-*.jar \
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


## ☁️ Esecuzione locale profilo Azure

Ho perso molte ore a capire come far funzionare CosmosDB in locale usando l'immagine ufficiale
```
mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:mongodb
```
ma alla fine ⚠️ **non funziona** ⚠️

C'è una cartella `azure-onprem-non-funziona` dentro alla cartella `script` così come promemoria delle prove fatte e dei comandi eseguiti. Se in futuro la situazione cambierà sistemerò questo esempio.


L’emulatore Linux in Docker è pensato solo per test container-to-container, e non supporta SDK dal host o a volte neanche da container separati se non dal container ufficiale che lo avvia. **CosmosDB Emulator Linux in Docker non implementa correttamente tutte le API richieste dagli SDK esterni.** Funziona solo con richieste dal container stesso o dalla rete interna Docker dove gira l’emulatore.


Microsoft documenta che il Linux Emulator è destinato a test container-to-container, non per SDK dal host o container separati. Alcune porte interne (10250+) devono essere esposte e certificate corrette mappate, cosa che il container ufficiale Linux non fa completamente.


⚠️ Limiti importanti
- Python / Java / C# SDK esterni → non funzionano correttamente su Linux Emulator fuori dal container ufficiale.
- curl funziona solo perché fa richieste HTTP molto semplici.
- MongoDB endpoint funziona meglio perché l’emulatore implementa almeno un protocollo minimale Mongo compatibile, ma anche qui bisogna fare tutto container-to-container.
- Non esistono workaround affidabili per usare l’SDK dal host Linux/macOS o da container separati senza problemi di TLS/JSON RPC.


### 🚀 Esecuzione locale profilo Azure con db remoti su Azure
Script bash per la creazione automatica di risorse Azure (CosmosDB + SQL Server + ServiceBus) ed esecuzione dell'applicazione Spring Boot in locale con Docker.
- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- 📋 **Prerequisiti**
  - Azure CLI installato e autenticato (`az login`)
  - Docker installato e in esecuzione
  - Immagine Docker `alnao/gestioneannotazioni:latest`
- Componenti creati dallo script
  0. **Login**: per essere eseguito necessita della login eseguita con il comando `az login`
  1. **Crea Resource Group** su Azure nella regione North Europe
  2. **Provisiona CosmosDB** (tier Free) con database e container per annotazioni
  3. **Provisiona SQL Server** (tier Basic) con database per metadati e autenticazione
  4. **Configura Firewall** per accesso locale e servizi Azure
  5. **Inizializza Database** con tabelle (`users`, `annotazione_metadata`, `storico_stati`) e utenti di test
  6. **Provisiona ServiceBus** come servizio per la gestione delle code di invio annotazioni
  7. **Avvia Container Docker** nel sistema locale (non nel cloud) con configurazione automatica
- ▶️ Esecuzione
  ```bash
  ./script/azure-dbremoti-cosmos-runlocale/start-all.sh
  ```
  - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- Esecuzione script di test 
  ```bash
  ./script/azure-dbremoti-cosmos-runlocale/test.sh
  ```
- Rimozione completa
  ```bash
  ./script/azure-dbremoti-cosmos-runlocale/stop-all.sh
  ```
- ⚠️ Note importanti
  - CosmosDB Free Tier: Limitato a 1000 RU/s e 25GB storage. Solo 1 account Free per subscription.
  - SQL Server Basic: 5 DTU e 2GB storage. Costo stimato: ~5€/mese.
  - Firewall: Lo script configura l'accesso dal tuo IP. Aggiorna la regola se l'IP cambia.
  - Password: Cambia P@ssw0rd123! con una password sicura prima di eseguire.
  - Connection String: Salva le connection string restituite dai comandi 8 e 13 in modo sicuro.
  - ⚠️ Costi: Anche con tier Free/Basic, SQL Server ha costi mensili. Monitorare sempre i costi ⚠️
    | Risorsa | Tier/SKU | Costo Orario | Costo Giornaliero | Costo Settimanale | Costo Mensile |
    |---------|----------|--------------|-------------------|-------------------|---------------|
    | **Cosmos DB SQL API** | Free Tier (1000 RU/s, 25GB) | €0.00 | €0.00 | €0.00 | **€0.00** |
    | **SQL Server Basic** | 5 DTU, 2GB | €0.0068 | €0.16 | €1.14 | **~€5.00** |
    | **Storage** | 2GB incluso | €0.00 | €0.00 | €0.00 | €0.00 |
    | **Egress Data** | <100GB/mese | ~€0.00 | ~€0.01 | ~€0.07 | **~€0.30** |
    | **TOTALE 24/7** | | **€0.0068/h** | **€0.17/day** | **€1.21/week** | **~€5.30/mese** |


### 🚀 Esecuzione locale profilo Kube con db remoti su Azure
Script bash per la creazione automatica di risorse Azure con profilo *kube* (Cosmos con compatibilità Mongo e Postgresql) ed esecuzione dell'applicazione Spring Boot in locale con Docker.
- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- 📋 **Prerequisiti**
  - Azure CLI installato e autenticato (`az login`)
  - Docker installato e in esecuzione
  - Immagine Docker `alnao/gestioneannotazioni:latest`
- Componenti creati dallo script
  0. **Login**: per essere eseguito necessita della login eseguita con il comando `az login`
  1. **Crea Resource Group** su Azure nella regione North Europe
  2. **Provisiona CosmosDB con compatibilità MongoDb** (tier Free) con database e container per annotazioni
  3. **Provisiona Postgresql** con database per metadati e autenticazione
  4. **Configura Firewall** per accesso locale e servizi Azure
  5. **Inizializza Database** con tabelle (`users`, `annotazione_metadata`, `storico_stati`) e utenti di test
  6. **Avvia Container Docker** con configurazione automatica
- ▶️ Esecuzione
  ```bash
  ./script/azure-dbremoti-mongo-runlocale/start-all.sh
  ```
  - Esecuzione del test automatico
    ```bash
    ./script/azure-dbremoti-cosmos-aci/test.sh
    ```
- Rimozione completa
  ```bash
  ./script/azure-dbremoti-mongo-runlocale/stop-all.sh
  ```

- ⚠️ Note importanti
  - ⚠️ Costi: Anche con tier Free/Basic, i database hanno costi mensili. Monitorare sempre i costi ⚠️
    | Risorsa | Tier/SKU | Costo Orario | Costo Giornaliero | Costo Settimanale | Costo Mensile |
    |---------|----------|--------------|-------------------|-------------------|---------------|
    | **Cosmos DB MongoDB API** | Free Tier (1000 RU/s, 25GB) | €0.00 | €0.00 | €0.00 | **€0.00** |
    | **PostgreSQL Flexible** | Standard_B1ms | €0.0165 | €0.40 | €2.77 | **~€12.00** |
    | **Storage PostgreSQL** | 32GB | €0.0048 | €0.12 | €0.81 | **~€3.50** |
    | **Backup** | 32GB (7 giorni) | €0.0014 | €0.03 | €0.24 | **~€1.00** |
    | **Egress Data** | <100GB/mese | ~€0.00 | ~€0.01 | ~€0.07 | **~€0.30** |
    | **TOTALE 24/7** | | **€0.0227/h** | **€0.56/day** | **€3.89/week** | **~€16.80/mese** |


### 🚀 Esecuzione su VirtualMachine Azure del profilo Azure
Script bash per la creazione automatica di risorse Azure (CosmosDB + SQL Server + ServiceBus) ed esecuzione dell'applicazione Spring Boot in una Virtual Machine su Azure
- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- 📋 **Prerequisiti**
  - Azure CLI installato e autenticato (`az login`)
  - Docker installato e in esecuzione
  - Immagine Docker `alnao/gestioneannotazioni:latest`
- Componenti creati dallo script
  0. **Login**: per essere eseguito necessita della login eseguita con il comando `az login`
  1. **Crea Resource Group** su Azure nella regione North Europe
  2. **Provisiona CosmosDB** (tier Free) con database e container per annotazioni
  3. **Provisiona SQL Server** (tier Basic) con database per metadati e autenticazione
  4. **Configura Firewall** per accesso locale e servizi Azure
  5. **Inizializza Database** con tabelle (`users`, `annotazione_metadata`, `storico_stati`) e utenti di test
  6. **Provisiona ServiceBus** come servizio per la gestione delle code di invio annotazioni
  7. **Virtual Machine** come macchine virtuale dove viene eseguito l'immagine docker del servizio
  8. **Configurazione rete** per accesso della VM verso il database SQL

- ▶️ Esecuzione
  ```bash
  ./script/azure-dbremoti-cosmos-vm/start-all.sh
  ```
  - Esecuzione del test automatico
    ```bash
    ./script/azure-dbremoti-cosmos-aci/test.sh
    ```
- Rimozione completa
  ```bash
  ./script/azure-dbremoti-cosmos-vm/stop-all.sh
  ```
- ⚠️ Note importanti
  - CosmosDB Free Tier: Limitato a 1000 RU/s e 25GB storage. Solo 1 account Free per subscription.
  - SQL Server Basic: 5 DTU e 2GB storage. Costo stimato: ~5€/mese.
  - VM Standard_B1s: 1 vCPU, 1GB RAM. Ideal per carichi leggeri con burstable performance.
  - Firewall: Lo script configura l'accesso dal tuo IP e dalla VM. Aggiorna le regole se l'IP cambia.
  - Password: Cambia P@ssw0rd123! con una password sicura prima di eseguire.
  - Connection String: Salva le connection string restituite dai comandi in modo sicuro.
  - SSH Key: La chiave SSH viene generata automaticamente in `$HOME/.ssh/azure-vm-key.pub`
  - ⚠️ Costi: Con l'aggiunta della VM, i costi aumentano. Spegni la VM quando non la usi per risparmiare. ⚠️

  | Risorsa | Tier/SKU | Costo Orario | Costo Giornaliero | Costo Settimanale | Costo Mensile |
  |---------|----------|--------------|-------------------|-------------------|---------------|
  | **Cosmos DB SQL API** | Free Tier (1000 RU/s, 25GB) | €0.00 | €0.00 | €0.00 | **€0.00** |
  | **SQL Server Basic** | 5 DTU, 2GB | €0.0068 | €0.16 | €1.14 | **~€5.00** |
  | **VM Standard_B1s** | 1 vCPU, 1GB RAM | €0.0105 | €0.25 | €1.76 | **~€7.60** |
  | **VM IP Pubblico Standard** | Static IP | €0.0043 | €0.10 | €0.72 | **~€3.10** |
  | **VM OS Disk** | 32GB Standard HDD | €0.0006 | €0.01 | €0.10 | **~€0.40** |
  | **Storage SQL** | 2GB incluso | €0.00 | €0.00 | €0.00 | €0.00 |
  | **Egress Data** | <100GB/mese | ~€0.00 | ~€0.01 | ~€0.07 | **~€0.30** |
  | **Service Bus Standard** | 12.5M ops/mese | €0.0118 | €0.28 | €1.99 | **~€8.50** |
  | **TOTALE 24/7** | | **€0.0340/h** | **€0.81/day** | **€5.78/week** | **~€24.90/mese** |


### 🚀 Esecuzione su Azure Container Instances del profilo Azure

Script bash per la creazione automatica di risorse Azure (CosmosDB + SQL Server) ed esecuzione dell'applicazione Spring Boot in un **Azure Container Instance (ACI)**


- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
  | Periodo | Costo Totale (senza free tier) | Costo Reale (con ottimizzazioni) |
  |---------|--------------------------------|-----------------------------------|
  | **Giornaliero** | **€1.49** | **€1.26** |
  | **Settimanale** | **€10.43** | **€8.82** |
  | **Mensile** | **€44.70** | **€37.80** |
- 📋 **Prerequisiti**
  - Azure CLI installato e autenticato (`az login`)
  - Immagine Docker `alnao/gestioneannotazioni:latest`. Sostituito con Amazon Container Registry perchè: 
    - Please be aware that Docker Hub has recently introduced a pull rate limit on Docker images. When specifying an image from the Docker Hub registry, this may impact your container instance. [Learn more](https://docs.docker.com/docker-hub/download-rate-limit).
- Le risorse create da questo esempio sono:
  | Name | Type |
  | -----|------|
  | gestioneannotazioni-logs | Microsoft.OperationalInsights/workspaces |
  | gestioneannotazioni-cosmos | Microsoft.DocumentDB/databaseAccounts |
  | gestioneannotazioni-sql | Microsoft.Sql/servers |
  | gestioneannotazioni-sql/gestioneannotazioni | Microsoft.Sql/servers/databases |
  | gestioneannotazioni-sql/master | Microsoft.Sql/servers/databases |
  | gestioneannotazioniacisa | Microsoft.Storage/storageAccounts |
  | gestioneannotazioniacr | Microsoft.ContainerRegistry/registries |
  | gestioneannotazioni-servicebus | Microsoft.ServiceBus/namespaces |
  | gestioneannotazioni-aci | Microsoft.ContainerInstance/containerGroups |

- ▶️ **Esecuzione**
  ```bash
  ./script/azure-dbremoti-cosmos-aci/start-all.sh
  ```
  - Esecuzione del test automatico
    ```bash
    ./script/azure-dbremoti-cosmos-aci/test.sh
    ```
- Rimozione completa
  ```bash
  ./script/azure-dbremoti-cosmos-aci/stop-all.sh
  ```
- ⚠️ **Note importanti**
  - CosmosDB Free Tier: Limitato a 1000 RU/s e 25GB storage. Solo 1 account Free per subscription.
  - SQL Server Basic: 5 DTU e 2GB storage. Costo stimato: ~5€/mese.
  - ACI: Container eseguito in subnet privata, accessibile solo dal tuo IP (configurato automaticamente).
  - Firewall: Lo script configura l'accesso dal tuo IP. Aggiorna la regola se l'IP cambia.
  - Password: Cambia P@ssw0rd123! con una password sicura prima di eseguire.
  - Connection String: Salva le connection string restituite dai comandi in modo sicuro.
  - ⚠️ Costi: Anche con tier Free/Basic, SQL Server e ACI hanno costi mensili. Monitorare sempre i costi ⚠️
    | Servizio | SKU/Tier | Free Tier | Costo Giornaliero | Costo Settimanale | Costo Mensile | Note |
    |----------|----------|-----------|-------------------|-------------------|---------------|------|
    | **Resource Group** | Standard | ✅ Sempre gratuito | €0.00 | €0.00 | €0.00 | Container logico |
    | **Azure Cosmos DB** | Free Tier | ✅ Sì (400 RU/s + 25GB) | €0.00 | €0.00 | €0.00 | 2 container x 400 RU/s = 800 RU/s totali |
    | **Azure SQL Server** | Server | ✅ Solo server | €0.00 | €0.00 | €0.00 | Il server stesso è gratuito |
    | **Azure SQL Database** | Basic (2GB) | ❌ No | €0.17 | €1.19 | €5.10 | 5 DTU, max 2GB storage |
    | **Azure Container Instances** | 1 vCPU, 2GB RAM | ⚠️ Parziale | €1.03 | €7.21 | €30.90 | ~€0.043/ora (24h/giorno) |
    | **Azure Container Registry** | Basic | ⚠️ Primi 12 mesi | €0.17 | €1.19 | €5.10 | 10GB storage inclusi |
    | **Storage Account** | Standard LRS | ⚠️ Primi 12 mesi | €0.01 | €0.07 | €0.30 | Quota minima per share |
    | **Log Analytics Workspace** | Pay-as-you-go | ⚠️ 5GB/mese free | €0.03 | €0.21 | €0.90 | ~100MB/giorno stimati |
    | **Service Bus** | Standard | ❌ No | €0.03 | €0.21 | €0.90 | 1M operazioni base incluse |
    | **Virtual Network** | Standard | ✅ Sempre gratuito | €0.00 | €0.00 | €0.00 | Solo se `CREATE_VNET=SI` |
    | **Network Security Group** | Standard | ✅ Sempre gratuito | €0.00 | €0.00 | €0.00 | Solo se `CREATE_VNET=SI` |
    | **Bandwidth (Egress)** | Zone 1 (EU) | ⚠️ 100GB/mese free | €0.05 | €0.35 | €1.50 | Stima 2GB/mese oltre free tier |


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
  - ✅ 🧱 Verifica che utenti non possano fare operazioni il cui ruolo non lo prevede
    - Test Eseguito: chiamata transazione `http://localhost:8082/api/annotazioni/xxx-xxx-xxx-xxx-xxx/stato` con PATCH method `{vecchioStato: "CONFERMATA", nuovoStato: "MODIFICATA", utente: "admin"}` e ritornato errore 403 e nei log si vede il messaggio `Transizione non permessa: Transizione non permessa: da CONFERMATA a MODIFICATA per ruolo ADMIN`
- ✅ 🐳 Build e deploy su DockerHub della versione *kube* (ex OnPrem)
  - ✅ 🐳 configurazione di docker-compose con MongoDb e Postgresql
  - ✅ ☸️ Esecuzione su Kubernetes/Minikube locale con yaml dedicati
- ✅ ☁️ Esecuzione con docker-compose della versione AWS su sistema locale con Mysql e DynamoDB 
  - ✅ 🐳 Deploy su AWS usando EC2 per eseguire il container docker, script scritto in AWS-CLI per il provisioning delle risorse necessarie (Aurora-RDS-Mysql e DynamoDB ) e la creazione della EC2 con lancio del docker con `user_data`
  - ✅ 🐳 Deploy su AWS usando ECS, Fargate e repository ECR (senza DockerHub), script scritto in AWS-CLI per il provisioning delle risorse necessarie (Aurora-RDS-Mysql e DynamoDB ) e lancio del task su ECS. Non previsto sistema di scaling up e/o bilanciatore ALB.
- ✅ 🔒 Autenticazione e autorizzazione (Spring Security) e token Jwt
  - ✅ 👥 introduzione sistema di verifica degli utenti e validazione richieste con tabella utenti
  - ✅ 📝 Gestione multiutente e modifica annotazioni con utente diverso dal creatore, test nell'applicazione web
  - ✅ 🛡️ Centralità dei service JwtService e UserService nel core senza `adapter-security`
- ✅ ⚙️ Evoluzione adapter con integrazione con altri sistemi
  - ✅ 🧬 Gestione delle annotazioni in stato INVIATA
  - ✅ 📚 Export annotazioni: creazione service che permetta di inviare notifiche via coda (kafka o sqs) con creazione `adapter-kafka` e che con frequenza invii delle annotazioni concluse con cambio di stato
  - ✅ ☁️ Configurazione del servizio SQS nell'adapter AWS e test nelle versioni EC2 e ECS
- ✅ 🏁 Test finale di tutti i punti precedenti e tag della versione 0.0.1 e inizio versione 0.0.2
  - ✅ 📡 Rilascio immagine 0.0.1 su DockerHub
      ```bash
      docker tag alnao/gestioneannotazioni:latest alnao/gestioneannotazioni:0.0.1
      docker push alnao/gestioneannotazioni:0.0.1
      ```
- ✅ ☁️ Integrazione con Azure
  - ✅ 🔩 Creazione del adapter Azure e sviluppo implementazioni per cosmos e ms-sql server.
  - ✅ 🖥️ Prima esecuzione in locale adapter azure *che non funziona*
  - ✅ ▶️ Script deploy su Azure della versione con cosmos e sqlserver con run in locale
  - ✅ 🎯 Script deploy su Azure della versione con cosmos-mongodb e postgresql con run in locale
  - ✅ 📖 Export annotazioni verso servizio Azure service dockerbus
  - ✅ 🔧 Verifica inserimento in storico stati quando una annotazione viene inviata
  - ✅ 📝 Esportazione delle annotazioni su Azure in sistema code 
  - ✅ 🚀 Script deploy su Azure della versione con cosmos e sqlserver con run in VM-azure
  - ✅ 🛠️ Script deploy su Azure con Azure Container Instances (ACI)
- 🚧 ☸️ Esecuzione su Cloud in infrastruttura Kubernetes
  - ✅ 🐳 Cambio nome profilo da *OnPrem* ad *Kube*
  - 🚧 🤖 Deploy su AWS su EKS del profilo Kube
  - 🚧 📦 Deploy su Azure con Azure Container Apps (ACA non è Kubernetes *ma quasi*)
  - 🚧 ⚙️ Deploy su Azure su AKS del profilo Kube
  - 🚧 🎡 Script deploy su Azure della versione con cosmos-mongo e postgres con run in VM-azure
  - 🚧 🔧 Sistem di Deploy con Kubernetes Helm charts del profilo Kube
  - 🚧 📈 Auto-Scaling Policies: Horizontal Pod Autoscaler (HPA) e Vertical Pod Autoscaler (VPA) per Kubernetes
- 🚧 🗃️ Sistema evoluto di gestione annotazioni
  - ✅ 🧑‍🤝‍🧑 Gestione modifica annotazione con annotazione `@Version` di JPA (vedi Entity AnnotazioneMetadataEntity di Postgresql). *Non funziona perchè il Service esegue un refresh della versione all'interno del metodo aggiornaAnnotazione quindi non andrebbe in errore in caso di contesa*
  - ✅ 👥 Sistema di lock con Redis che impedisce che due utenti modifichino la stessa annotazione allo stesso momento
    - ✅ 🔒 Implementazione Redis con Redisson per profili kube, aws, azure
    - ✅ 💾 Implementazione in-memory per profilo sqlite
    - ✅ 🎯 Gestione eccezioni con HTTP 409 CONFLICT quando annotazione è già in modifica
    - ✅ 🔄 Api per bloccare una annotazione da un utente specifico
    - ✅ 🛠️ Creazione script test specifo per il blocci di annotazioni e integrazione degli script di test 
    - ✅ ☁️ Modifica script profilo AWS per servizio redis on Cloud
    - 🚧 ☁️ Modifica script profilo Azure per servizio redis on Cloud
    - 🚧 ⚙️ Modifica al frontend per gestire le prenotazioni di una annotazione quando si entra nel dettaglio
    - 🚧 ⚙️ Modifica al frontend per visualizzare l'errore specifico se qualcun'altro ha bloccato quella annotazione
    - 🚧 🔧 Nell'elenco delle annotazioni, indicare se una annotazione è bloccata da qualcuno, modifica del frontend
    - 🚧 🤖 Test finali del frontend e conclusione processo di prenotazione delle annotazioni!
  - 🚧 🕸️ Gestione invio notifiche singolo se ci sono più istanze dell'applicazione in esecuzione (esempio minikube)
  - 🚧 🔄 Import annotazioni (JSON e/o CSV): creazione service per l'import di annotazioni con cambio di stato dopo averle importate con implementazioni su tutti gli adapter
  - 🚧 🎯 Notifiche real-time (WebSocket): creazione `adapter-notifier` che permetta ad utenti di registrarsi su WebSocket e ricevere notifiche su cambio stato delle proprie annotazioni
    - 🚧 👥 Social Reminders: Notifiche quando qualcuno interagisce con annotazioni modificate
  - 🚧 🧭 Sistema che gestisce la scadenza di una annotazione con spring-batch che elabora tutte le annotazioni rifiutate o scadute, con nuovo stato scadute.
  - 🚧 💾 Backup & Disaster Recovery: Cross-region backup, point-in-time recovery, RTO/RPO compliance
  - 🚧 🔐 OAuth2/OIDC Provider: Integrazione con provider esterni (Google, Microsoft, GitHub) + SSO enterprise
- 🚧 🏁 Test finale di tutti i punti precedenti e tag della versione 0.2.0
- 🚧 🛡️ Gestione password via secret
  - 🚧 🔒 Gestione password tramite setret di Kubernetes nel profilo Kube
  - 🚧 🔒 Gestione password tramite AWS Secret manager nel profilo aws
  - 🚧 🔒 Gestione password tramite Azure key vault nel profilo azure
  - 🚧 🔒 Gestione password tramite File statici nel profilo sqlite
- 🚧 🎯 Sistema di caricamento annotazioni avanzato, caricare annotazioni tramite stream dati
- 🚧 🗃️ Idee per il futuro
  - 🚧 🏗️ GitOps Workflow: ArgoCD/Flux per deployment automatici, configuration drift detection
  - 🚧 🧪 Testing Pyramid: Unit + Integration + E2E + Performance + Security testing automatizzati
  - 🚧 📎 File Attachments: Supporto allegati (immagini, PDF, documenti) con preview e versioning
  - 🚧 ⚡ Redis Caching Layer: Cache multi-livello (L1: in-memory, L2: Redis) con invalidation strategies e cache warming
  - 🚧 📊 Read Replicas: Separazione read/write con eventual consistency e load balancing intelligente
  - 🚧 🔍 Elasticsearch Integration: Ricerca full-text avanzata con highlighting, auto-complete, ricerca semantica
- Fantasie dell'IA
  - 🚧 📦 Container Security: Vulnerability scanning (Trivy/Snyk), distroless images, rootless containers
  - 🚧 🎯 Feature Flags: LaunchDarkly/ConfigCat integration per feature toggling, A/B testing, gradual
  - 🚧 💬 Comment Threads: Sistema di commenti su singole annotazioni con threading e notifications
  - 🚧 📝 Templates & Forms: Template predefiniti per annotazioni (meeting notes, bug reports, ideas) con campi strutturati
  - 🚧 🔄 Annotation Workflows: Stati delle annotazioni (draft→review→approved→published) con approval process e notifiche
  - 🚧 📅 Smart Date Recognition: NLP per riconoscere date naturali ("domani", "la prossima settimana", "tra 3 giorni") e convertirle in deadline
  - 🚧 ⏰ Time Boxing: Stima automatica del tempo necessario per task basata su annotazioni simili completate
  - 🚧 📈 Progress Tracking: Visualizzazione progresso con barre, percentuali, streak counters
  - 🚧 🔗 Task Dependencies: Link tra annotazioni per gestire sequenze e blocchi
  - 🚧 ⏰ Context-Aware Reminders: Promemoria basati su location, tempo, altre attività ("Ricorda quando arrivi in ufficio")
  - 🚧 Weekly Digest: Riassunto settimanale con achievement, todo completati, annotazioni più accedute
  - 🚧 🎤 Voice Notes: Registrazione audio con trascrizione automatica e timestamp
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


### Test di non regressione
Per ogni modifica, prima del rilascio, *bisognerebbe* eseguire un test di non regressione su tutta l'applicazione. I test da eseguire sono:
- Compilazione e upload/push dell'immagine
  ```bash
  ./script/docker-build.sh 
  ./script/push-image-docker-hub.sh 
  ```
  risultato atteso: nessun errore
- Pulizia globale prima di partire (meglio partire da situazione pulita con volumi vuoti!)
  ```bash
  docker volume rm $(docker volume ls -q)
  ```
- Script generale per eseguire tutti i gli script di test *automatici*
  ```
  ./script/automatic-test/test-all.sh
  ```
  - Che esegue gli script
    ```
    # Script per eseguire il profilo `sqlite` eseguito in locale (con solo sqlite) senza docker
    ./script/automatic-test/test-sqlite-onprem.sh
    # Script per eseguire il profilo `kube` eseguito in locale (con Postgresql e MongoDB) con docker compose
    ./script/automatic-test/test-aws-onprem.sh
    # Script per eseguire il profilo `kube` eseguito in locale con **minikube** e **kubernetes**
    ./script/automatic-test/test-minikube.sh
    ```

- Profilo `aws` in Cloud AWS con MySql e MySql ed esecuzione su EC2
  - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
  - Script per creare lo stack in AWS (RDS, Dynamo e EC2)
    ```bash
    ./script/aws-ec2/start-all.sh
    ```
    - L'output finale dello script mostra l'IP pubblico EC2 e la porta applicativa (default 8080)
      - Accedi da browser: `http://<EC2_PUBLIC_IP>:8080`
  - L'invio delle annotazioni avviene in una coda SQS reale, le istruzioni per leggere
    ```bash
    SQS_QUEUE_NAME=gestioneannotazioni-annotazioni
    SQS_QUEUE_URL=$(aws sqs get-queue-url --queue-name $SQS_QUEUE_NAME --region eu-central-1 --query 'QueueUrl' --output text)
    aws sqs receive-message --queue-url "$SQS_QUEUE_URL" --region eu-central-1 --attribute-names All --message-attribute-names All
    ```
  - Rimozione dello stack
    ```
    ./script/aws-ec2/stop-all.sh
    ```
- Profilo `azure` in Cloud Azure con MySql e MySql ed esecuzione in locale
  - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
  - Script per creare lo stack in Azure (Cosmos e MsSql) ma esecuzione del microservizio in locale
    ```bash
    ./script/azure-dbremoti-cosmos-runlocale/start-all.sh
    ```
  - Verifica che le annotazioni sono correttamente inviate nella console web del servizio ServiceBus
  - L'applicazione web sarà disponibile in locale all'url [http://localhost:8082](http://localhost:8082)
  - Rimozione dello stack
    ```bash
    ./script/azure-dbremoti-cosmos-runlocale/stop-all.sh
    ```
- Profilo `azure` in Cloud Azure con MySql e MySql ed esecuzione in VirtualMachine su azure
  - Script per creare lo stack in Azure (Cosmos e MsSql) e l'esecuzione del microservizio una VistualMachine su Azure
    ```bash
    ./script/azure-dbremoti-cosmos-vm/start-all.sh
    ```
  - L'applicazione web sarà disponibile all'url ritornato dallo script
  - Verifica che le annotazioni sono correttamente inviate nella console web del servizio ServiceBus
  - Rimozione dello stack
    ```bash
    ./script/azure-dbremoti-cosmos-runlocale/stop-all.sh
    ```
- Profilo `kube` in cloud Azure con Postgresql e MongoDB ed esecuzione in locale
  - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
  - Script per creare lo stack in Azure (Postgresql e Cosmos-Mongo) ma esecuzione del microservizio in locale
    ```bash
    ./script/azure-dbremoti-mongo-runlocale/start-all.sh
    ```
    - In questo script è stata disattivata l'esportazione delle annotazioni
  - L'applicazione web sarà disponibile in locale all'url [http://localhost:8082](http://localhost:8082)
    - Rimozione dello stack
    ```bash
    ./script/azure-dbremoti-mongo-runlocale/stop-all.sh
    ```
- Pulizia finale del sistema docker locale
  ```
  minikube delete
  docker-compose down --remove-orphans
  docker network prune -f
  docker volume rm $(docker volume ls -q)
  docker rmi $(docker images -q)
  ```


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



