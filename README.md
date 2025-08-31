# Sistema di Gestione Annotazioni

Progetto sviluppato da &lt; AlNao /&gt; come esempio di progetto con Java Spring Boot.

Un sistema di gestione annotazioni multi-modulo basato su Spring Boot che implementa l'architettura esagonale (Hexagonal Architecture) con supporto per deployment sia on-premise che cloud AWS.

## 📝 TODO / Roadmap
- [✅] Creazione progetto e primo test
- [🚧] Bug fixing su metodo ricerca
- [🚧] Documentazione con swagger
- [🚧] Build e deploy su DockerHub
- [🚧] Deploy su AWS su EC2 / Lightsail
- [🚧] Deploy su AWS su Fargate/ECS
- [🚧] Kubernetes Helm charts
- [🚧] Deploy su AWS su EKS
- [🚧] Autenticazione e autorizzazione (Spring Security) e token Jwt
- [🚧] Gestione versioning annotazioni
- [🚧] Export/Import annotazioni (JSON, CSV)
- [🚧] Export/Import annotazioni (Kafka)
- [🚧] Notifiche real-time (WebSocket)
- [🚧] API rate limiting
- [🚧] Backup automatico
- [🚧] Elasticsearch per ricerca avanzata
- [🚧] CI/CD pipeline
- [🚧] Mobile app (React Native)


## 🛠️ Struttura progetto:
Il progetto segue i principi dell'*Hexagonal Architecture* (Ports and Adapters) e si basa su un'architettura a microservizi modulare:
```
📦 annotazioni-parent
├── 📁 module-port          # Interfacce e domini (Hexagonal Core)
├── 📁 module-api           # REST API Controllers
├── 📁 module-web           # Risorse statiche e configurazioni web
├── 📁 module-aws           # Implementazione AWS (MySQL + DynamoDB)
├── 📁 module-onprem        # Implementazione On-Premise (PostgreSQL + MongoDB)
└── 📁 module-app           # Applicazione principale Spring Boot
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
- Build del progetto
  ```bash
  # Build completo
  mvn clean package
  # Build senza test
  mvn clean package -DskipTests
  ```
- Esecuzione On-Premise lanciando il jar direttamente
    ```bash
    # Profilo on-premise (default)
    java -jar module-app/target/module-app-1.0.0.jar

    # Oppure specificando il profilo
    java -jar module-app/target/module-app-1.0.0.jar --spring.profiles.active=onprem
    ```
- Esecuzione On-Premise con il docker-compose:
    ```bash
    docker-compose build --no-cache app
    docker-compose up
    ```
- Esecuzione AWS
    ```bash
    # Profilo AWS
    java -jar module-app/target/module-app-1.0.0.jar --spring.profiles.active=aws
    ```

## 📡 API Endpoints
- Base URL: `http://localhost:8080` (8081 nel caso di docker-compose)
- Risorse base
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
- Operazioni sui Metadati
    | Metodo | Endpoint | Descrizione |
    |--------|----------|-------------|
    | PUT | `/api/annotazioni/{id}/visibilita` | Imposta visibilità pubblica |
    | PUT | `/api/annotazioni/{id}/categoria` | Imposta categoria |
    | PUT | `/api/annotazioni/{id}/tags` | Imposta tags |
    | PUT | `/api/annotazioni/{id}/priorita` | Imposta priorità |
- Creare un'annotazione:
    ```bash
    curl -X POST http://localhost:8080/api/annotazioni \
    -H "Content-Type: application/json" \
    -d '{
        "valoreNota": "Questa è una nota importante",
        "descrizione": "Descrizione della nota",
        "utente": "mario.rossi"
    }'
    ```
- Ricerca per testo**:
    ```bash
    curl -X POST http://localhost:8080/api/annotazioni/search -H "Content-Type: application/json" -d '{"testo": "bello"}'
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
mvn clean compile -pl module-app -am

# Deploy
java -jar module-app/target/module-app-1.0.0.jar \
  --spring.profiles.active=onprem \
  --server.port=8080
```

### Deployment AWS EC2
```bash
# Configurazione istanza EC2
sudo yum update -y
sudo yum install -y java-17-amazon-corretto

# Deploy applicazione
java -jar module-app-1.0.0.jar \
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



