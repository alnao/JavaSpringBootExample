# Sistema di Gestione Annotazioni

Progetto sviluppato da &lt; AlNao /&gt; come esempio di progetto con Java Spring Boot.

Un sistema di gestione annotazioni multi-modulo basato su Spring Boot che implementa l'architettura esagonale (Hexagonal Architecture) con supporto per deployment sia on-premise che cloud AWS.


## 📚 Indice rapido

- [🛠️ Struttura progetto](#️-struttura-progetto)
- [🏃‍♂️ Esecuzione](#-esecuzione)
- [📡 API Endpoints](#-api-endpoints)
- [📊 Monitoring con actuator](#-monitoring-con-actuator)
- [📖 Documentazione API con Swagger / OpenAPI](#-documentazione-api-con-swagger--openapi)
- [📈 Analisi qualità e coverage con SonarQube](#-analisi-qualità-e-coverage-con-sonarqube)
- [🐳 Deploy e utilizzo con DockerHub](#-deploy-e-utilizzo-con-dockerhub)
- [🐳 Deploy completo con Docker Compose](#-deploy-completo-con-docker-compose)
- [☸️ Deploy su Minikube (Kubernetes locale)](#️-deploy-su-minikube-kubernetes-locale)
- [📝 Roadmap / TODO](#-todo--roadmap)
- [🚧 Coming Soon](#-coming-soon)
  - [🔒 Sicurezza](#-sicurezza)
  - [Deployment On-Premise](#deployment-on-premise)
  - [Deployment AWS EC2](#deployment-aws-ec2)
  - [Deployment AWS ECS/Fargate](#deployment-aws-ecsfargate)



## 🛠️ Struttura progetto:
Il progetto segue i principi dell'*Hexagonal Architecture* (Ports and Adapters) e si basa su un'architettura a microservizi modulare:
```
📦 annotazioni-parent
├── 📁 adapter-port          # Interfacce e domini (Hexagonal Core)
├── 📁 adapter-api           # REST API Controllers
├── 📁 adapter-web           # Risorse statiche e configurazioni web
├── 📁 adapter-aws           # Implementazione AWS (MySQL + DynamoDB)
├── 📁 adapter-onprem        # Implementazione On-Premise (PostgreSQL + MongoDB)
└── 📁 adapter-app           # Applicazione principale Spring Boot
```
Caratteristiche:
- **Multi-database**: Supporto per PostgreSQL, MySQL, MongoDB, DynamoDB
- **Multi-ambiente**: Configurazioni separate per AWS e On-Premise
- **Architettura esagonale**: Separazione netta tra business logic e infrastruttura
- **REST API**: Endpoint completi per gestione annotazioni
- **Profili Spring**: Attivazione automatica delle implementazioni corrette
- **Transazionalità**: Gestione delle transazioni cross-database
- **Configurazione esterna**: Supporto per variabili d'ambiente

Prerequisiti:
- On-Premise semplice: Java 17+, Maven 3.8+, PostgreSQL 13+, MongoDB 4.4+
- On-Premise con docker: Docker & Docker-compose
- Ambiente AWS: Java 17+, Maven 3.8+, AWS Account con accesso a RDS MySQL e DynamoDB


## 🏃‍♂️ Esecuzione
- Build del progetto in ambiente di sviluppo
  ```bash
  # Build completo
  mvn clean package
  # Build senza test
  mvn clean package -DskipTests
  ```
- Esecuzione On-Premise con esecuzione diretta del jar (nel sistema devono essere avviati i servizi di database)
    ```bash
    # Profilo on-premise (default)
    java -jar adapter-app/target/adapter-app-1.0.0.jar

    # Oppure specificando il profilo
    java -jar adapter-app/target/adapter-app-1.0.0.jar --spring.profiles.active=onprem
    ```
- Esecuzione On-Premise con il docker-compose:
    ```bash
    docker-compose build --no-cache app
    docker-compose up
    ```
- Esecuzione profilo AWS *coming soon*
    ```bash
    # Profilo AWS
    java -jar adapter-app/target/adapter-app-1.0.0.jar --spring.profiles.active=aws
    ```

## 📡 API Endpoints
- Eseguendo il sistema in locale la base degli URL è `http://localhost:8080` (8081 nel caso di docker-compose)
- Le risorse base sono:
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
- Operazioni sui Metadati:
    | Metodo | Endpoint | Descrizione |
    |--------|----------|-------------|
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
  - In ambiente Docker Compose: [http://localhost:8081/swagger-ui.html](http://localhost:8081/swagger-ui.html)
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
        public OpenAPI annotazioniOpenAPI() {
            return new OpenAPI()
                    .info(new Info().title("Sistema di Gestione Annotazioni API")
                            .description("API per la gestione delle annotazioni, versioning e storico note.")
                            .version("v1.0.0")
                            .license(new License().name("GPL v3").url("https://www.gnu.org/licenses/gpl-3.0")))
                    .externalDocs(new ExternalDocumentation()
                            .description("Documentazione progetto e repository")
                            .url("https://www.alnao.it/"));
        }
    }
    ```
  - con la possibilità di aggiungere annotazioni OpenAPI ai controller/metodi per arricchire la documentazione.
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
      -Dsonar.login=<il-tuo-token> \
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


## 🐳 Deploy e utilizzo con DockerHub
L'immagine ufficiale dell'applicazione è pubblicata su [DockerHub](https://hub.docker.com/r/alnao/annotazioni) e può essere scaricata ed eseguita direttamente, senza necessità di build locale.
- **Compilazione e push dell'immagine**
    ```bash
    docker login
    docker build -t alnao/annotazioni:latest .
    docker push alnao/annotazioni:latest
    ```
- **Pull dell'immagine**:
    ```bash
    docker pull alnao/annotazioni:latest
    ```
    L'immagine viene aggiornata con le ultime versioni *stabili*.
- **Esecuzione rapida**:
    ```bash
    docker run --rm -p 8080:8080 alnao/annotazioni:latest
    ```
    L'applicazione sarà disponibile su [http://localhost:8080](http://localhost:8080) ma nel sistema devono esserci già installati e ben configuati MongoDb e Postgresql.
- **Esecuzione completa**: 🔌 Rete Docker condivisa (alternativa più robusta)
    Possibile eseguire tutto con docker (senza docker-compose):
    ```bash
    # Creazione rete
    docker network create annotazioni-network

    # Esecuzione mongo
    docker run -d --name annotazioni-mongo \
      -p 27017:27017 \
      -e MONGO_INITDB_DATABASE=annotazioni \
      -e MONGO_INITDB_ROOT_USERNAME=demo \
      -e MONGO_INITDB_ROOT_PASSWORD=demo \
      mongo:4.4

    # Esecuzione postgresql
    docker run -d --name postgres-annotazioni \
      --network annotazioni-network \
      -p 5432:5432 \
      -e POSTGRES_DB=annotazioni \
      -e POSTGRES_USER=demo \
      -e POSTGRES_PASSWORD=demo \
      postgres:13

    # Esecuzione servizio 
    docker run --rm -p 8090:8080 \
      --network annotazioni-network \
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://annotazioni-postgres:5432/annotazioni \
      -e SPRING_DATA_MONGODB_URI=mongodb://demo:demo@annotazioni-mongo:27017/annotazioni?authSource=admin \
      -e SPRING_DATASOURCE_USERNAME=demo \
      -e SPRING_DATASOURCE_PASSWORD=demo \
      alnao/annotazioni:latest

    # Per vedere i container nella rete
    docker network inspect annotazioni-network

    # Per fermare tutto e rimuovere la rete
    docker stop annotazioni-mongo postgres-annotazioni
    docker rm annotazioni-mongo postgres-annotazioni
    docker network rm annotazioni-network
    ```
- **Note**:
    - L'immagine non contiene dati sensibili quindi non c'è problema se viene salvato
    - Utilizzare sempre variabili d'ambiente sicure per le password e le connessioni DB in produzione.
    - Tutto sto casino può essere evitato con docker-compose,minikube e kubernetes. Vedere le sezioni dedicate.



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
    ```
- **Note**:
    - L’applicazione diventa disponibile su [http://localhost:8080](http://localhost:8080)
    - Possibile personalizzare porte, variabili d’ambiente e configurazioni secondo le varie esigenze.
    - Per la produzione, necessario usare password sicure, sistemi di backup e sicurezza dei dati.



## ☸️ Deploy su Minikube (Kubernetes locale)
L’applicazione e i database posso essere eseguiti anche su Minikube, l’ambiente Kubernetes locale, per simulare un cluster cloud-ready.
- **Prerequisiti**: 
    - Minikube installato ([guida ufficiale](https://minikube.sigs.k8s.io/docs/start/))
    - Kubectl installato
    - Freelens/OpenLens consigliato per la gestione dei pod, service e risorse
- **Avvio Minikube**:
    ```bash
    minikube start --memory=4096 --cpus=2
    ```
- **Manifest già pronti**:
    Nella cartella `script/minikube-onprem` trovi i manifest YAML già pronti per avviare tutta l'infrastruttura, presente script che esegue nella giusta sequenza gli script di `kubectl apply`, lo script da lanciare è:
    ```bash
    ./script/minikube-onprem/start-all.sh
    ```
- **Accesso all’applicazione**:
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
- **Note**:
    - I dati di MongoDB e PostgreSQL sono persistenti grazie ai PVC di Kubernetes, a meno di usare lo script di `stop-all.sh` che rimuove anche i volumi persistenti.
    - Viene usata l'immagine `alnao/annotazioni:latest` su dockerHub e non una immagine creata in sistema locale.


## 📝 TODO / Roadmap
- ✅ Creazione progetto e primo test con pagina web di esempio
- ✅ Introduzione modifica nota e documento con vecchie versioni delle note
- ✅ Configurazione di OpenApi-Swagger e Quality-SonarQube
- ✅ Build e deploy su DockerHub, configurazione di docker-compose
- ✅ Run on Minikube
- 🚧 Test con Mysql e DynamoDB 
- 🚧 Deploy su AWS su EC2 / Lightsail
- 🚧 Deploy su AWS su Fargate/ECS
- 🚧 Deploy su AWS su EKS
- 🚧 Sistema di caching e ottimizzazione
- 🚧 Sistem di Deploy con Kubernetes Helm charts
- 🚧 Autenticazione e autorizzazione (Spring Security) e token Jwt
- 🚧 Gestione multiutente e versioning annotazioni
- 🚧 Export/Import annotazioni (JSON, CSV)
- 🚧 Export/Import annotazioni (Kafka)
- 🚧 Notifiche real-time (WebSocket)
- 🚧 API rate limiting
- 🚧 Backup automatico
- 🚧 Elasticsearch per ricerca avanzata
- 🚧 CI/CD pipeline
- 🚧 Mobile app (React Native)
- 🚧 Feature: Mobile app (React Native)


## 🚧 Coming Soon

### Coming soon: Configurazione AWS
1. **RDS MySQL**:
```bash
# Crea istanza RDS MySQL via AWS CLI o Console
aws rds create-db-instance \
  --db-instance-identifier annotazioni-db \
  --db-instance-class db.t3.micro \
  --engine mysql \
  --master-username admin \
  --master-user-password your-password \
  --allocated-storage 20
```
2. **DynamoDB**:
```bash
# Crea tabella DynamoDB
aws dynamodb create-table \
  --table-name annotazioni \
  --attribute-definitions AttributeName=id,AttributeType=S \
  --key-schema AttributeName=id,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST
```
3. **Variabili d'ambiente**:
```bash
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY=your-access-key
export AWS_SECRET_KEY=your-secret-key
export AWS_RDS_URL=jdbc:mysql://your-rds-endpoint:3306/annotazioni
export AWS_RDS_USERNAME=admin
export AWS_RDS_PASSWORD=your-password
export DYNAMODB_TABLE_NAME=annotazioni
```





### 🔒 Sicurezza
1. **Database**: Utilizza sempre password forti e connessioni SSL
2. **AWS**: Configura IAM roles con permessi minimi necessari
3. **Application**: Configura HTTPS in production
4. **Monitoring**: Limita l'accesso agli endpoint Actuator

### Deployment On-Premise
```bash
# Build
mvn clean package -DskipTests
mvn clean compile -pl adapter-app -am

# Deploy
java -jar adapter-app/target/adapter-app-1.0.0.jar \
  --spring.profiles.active=onprem \
  --server.port=8080
```

### Deployment AWS EC2
```bash
# Configurazione istanza EC2
sudo yum update -y
sudo yum install -y java-17-amazon-corretto

# Deploy applicazione
java -jar adapter-app-1.0.0.jar \
  --spring.profiles.active=aws \
  --server.port=8080
```

### Deployment AWS ECS/Fargate
```json
{
  "family": "annotazioni-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "256",
  "memory": "512",
  "containerDefinitions": [
    {
      "name": "annotazioni",
      "image": "your-ecr-repo/annotazioni:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "aws"
        }
      ]
    }
  ]
}
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



