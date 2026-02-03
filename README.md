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
| `aws` | ![AWS](https://img.shields.io/badge/AWS-FF9900?style=flat-square&logo=amazonaws&logoColor=white) | ![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white) | ![DynamoDB](https://img.shields.io/badge/DynamoDB-4053D6?style=flat-square&logo=amazondynamodb&logoColor=white) | ![SQS](https://img.shields.io/badge/SQS-FF9900?style=flat-square&logo=amazonaws&logoColor=white) | ![Elasticache for Redis](https://img.shields.io/badge/ElastiCache%20for%20Redis-DC382D?style=flat-square&logo=redis&logoColor=white) | 
| `azure` | ![Azure](https://img.shields.io/badge/Azure-0078D4?style=flat-square&logo=microsoftazure&logoColor=white) | ![SQL Server](https://img.shields.io/badge/SQL%20Server-CC2927?style=flat-square&logo=microsoftsqlserver&logoColor=white) | ![Cosmos DB](https://img.shields.io/badge/Cosmos%20DB-0089D6?style=flat-square&logo=azurecosmosdb&logoColor=white) | ![Service Bus](https://img.shields.io/badge/Service%20Bus-0089D6?style=flat-square&logo=microsoftazure&logoColor=white) | ![Azure Cache for Redis](https://img.shields.io/badge/Cache%20for%20Redis-DC382D?style=flat-square&logo=redis&logoColor=white) | 


## 📚 Indice rapido
- 📝 [Roadmap & todo-list](./Roadmap.md)
  - 📖 [Test di non regressione](./Roadmap.md#-Test-di-non-regressione) ad ogni rilascio *bisognerebbe* eseguire un test di non regressione completo!
- 🛠️ [Struttura progetto](#-struttura-progetto)
  - ⚙️ [Esecuzione locale](#-esecuzione-locale)
  - 📡 [API Endpoints](#-api-endpoints)
  - 📊 [Monitoring con actuator](#-monitoring-con-actuator)
  - 📖 [Documentazione API con Swagger / OpenAPI](#-documentazione-api-con-swagger--openapi)
  - 📈 [Analisi qualità e coverage con SonarQube](#-analisi-qualità-e-coverage-con-sonarqube)
  - ⏰ [Sistema di lock distribuito con Redis](#-Redis)
  - 📖 [Frontend con JavaFx](#-Frontend-con-JavaFx)
  - 🔒 [Sistema di autenticazione](#-Sistema-di-autenticazione)
- 🐳 [Deploy ed esecuzione con DockerHub](./PlatformDockerHub.md/#-deploy-ed-esecuzione-con-dockerhub)
  - 🐳 [Esecuzione completa con Docker Compose (con Mongo e Postgresql)](./PlatformDockerHub.md#-Esecuzione-completa-con-Docker-Compose)
  - ☸️ [Esecuzione su Minikube e Kubernetes locale](./PlatformDockerHub.md#-Esecuzione-su-Minikube-e-Kubernetes-locale)
  - 📦 [Versione SQLite per Replit](./PlatformDockerHub.md#-Versione-SQLite-per-Replit)
- ☁️ [Esecuzione del profilo AWS (con MySql e Dynamo)](./PlatformDockerAws.md#-Esecuzione-del-profilo-AWS-in-locale)
  - 🚀 [Esecuzione su AWS EC2](./PlatformDockerAws.md#-Esecuzione-su-AWS-EC2)
  - 🐳 [Esecuzione su AWS ECS Fargate](./PlatformDockerAws.md#-Esecuzione-su-aws-ecs-fargate)
- ☁️ [Esecuzione locale profilo Azure (con CosmosDB e SqlServer)](./PlatformDockerAzure.md#-Esecuzione-locale-profilo-Azure)
  - 🚀 [Esecuzione locale profilo Azure con db remoti su Azure](./PlatformDockerAzure.md#-Esecuzione-locale-profilo-Azure-con-db-remoti-su-Azure)
  - 🐳 [Esecuzione locale profilo Kube con db remoti su Azure](./PlatformDockerAzure.md#-Esecuzione-locale-profilo-Kube-con-db-remoti-su-Azure)
  - 🚀 [Esecuzione su VirtualMachine Azure del profilo Azure](./PlatformDockerAzure.md#-Esecuzione-su-VirtualMachine-Azure-del-profilo-Azure)
  - 🚀 [Esecuzione su Azure Container Instances del profilo Azure](./PlatformDockerAzure.md#-Esecuzione-su-Azure-Container-Instances-del-profilo-Azure)



## 🛠️ Struttura progetto
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
  ├── 📁 adapter-javafx        # Componenti di un front-end sviluppato con JavaFX (compatibile solo con Sqlite)
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

### 📖 Frontend con JavaFx
L'adapter JavaFX fornisce un'interfaccia desktop nativa per la gestione delle annotazioni, completamente integrata con l'architettura esagonale del progetto. Utilizza JavaFX 21 per la UI, Spring Boot per dependency injection e SQLite come database embedded.
- ⚠️ L'esecuzione di questo frontend funziona solo con sqlite, non è stato testato su altri profili! ⚠️

**Caratteristiche:**
- Interfaccia grafica desktop con login e CRUD completo
- TableView per visualizzare tutte le annotazioni con filtri dinamici
- Form dettaglio con validazione per creazione/modifica
- Pattern MVVM con ViewModel per binding JavaFX properties
- Database SQLite locale con Hibernate auto-DDL
- Nessuna dipendenza da server web esterni

**Esecuzione:**
```bash
# Da root del progetto
mvn javafx:run -pl adapter-javafx

# Oppure compilare e eseguire il JAR
mvn clean package -pl adapter-javafx -DskipTests
java -jar adapter-javafx/target/adapter-javafx-0.0.2.jar
```

**Configurazione:** Il file `adapter-javafx/src/main/resources/application.properties` permette di personalizzare il path del database SQLite e altre impostazioni. La documentazione completa è disponibile nel `adapter-javafx/README.md`. Di default è configato il path locale `adapter-javafx/data/gestione_annotazioni_javafx.db`.


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



