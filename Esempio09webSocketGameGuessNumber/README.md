# 🎯 Guess Game - Java Spring Boot Cloud-Agnostic Version

**🎯 Happy Gaming!** 🎮

## 📋 Descrizione

Versione migrata del gioco "Indovina il numero" da AWS Lambda/DynamoDB disponibile nel repository:
```
https://github.com/alnao/AwsCloudFormationExamples/tree/master/Esempio30WebSocket
```
Questa versione è realizzata con **Java Spring Boot + MongoDB**, completamente **cloud-agnostica** e **containerizzata** per deploy su Docker e Kubernetes.


Questo gioco è un esempio didattico: quasi tutto il codice è stato generato con GitHub-Copilot e successivamente verificato ma presenta alcune inesattezze ed errori. 


⚠️ Il gioco non prevede nessun sistema di autenticazione/autorizzazione ⚠️


### 🎮 Come Funziona il Gioco

1. **Connessione**: I giocatori si connettono tramite WebSocket con un nickname
2. **Impostazione Numero**: Ogni giocatore può impostare un numero segreto (1-100)
3. **Indovinare**: I giocatori tentano di indovinare i numeri degli altri
4. **Punteggio**: I giocatori hanno un punteggio che varia in base a quello che fanno durante il gioco
   - +3 punti per chi indovina correttamente
   - -1 punti per chi viene indovinato
   - -1 punto per indovinare sbagliato
5. **Rate Limiting**: Ogni giocatore ha un limite di tentativi per 24h (configurabile, default 6)
6. **Amministrazione**: Gli admin possono bannare/sbannare giocatori e inviare broadcast

### 🎯 Caratteristiche Principali

- **Real-time multiplayer** tramite WebSocket
- **REST API** per operazioni CRUD
- **MongoDB** per persistenza dati
- **Cloud-agnostic** (nessuna dipendenza cloud specifica)
- **Containerizzato** con Docker
- **Deploy Kubernetes** con manifesti e Helm charts
- **Interfaccia web moderna** con SockJS/STOMP
- **Admin Panel** completo per gestione gioco
- **Sistema Ban/Unban** giocatori con controlli multi-livello
- **Rate Limiting** tentativi per giocatore (configurabile)
- **Broadcast messaggi admin** real-time
- **Configurazione scoring** flessibile tramite properties
- **Cleanup automatico** giocatori inattivi
- **Logging dettagliato** eventi di gioco

## 🏗️ Architettura

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Spring Boot    │    │    MongoDB      │
│   (HTML/JS)     │◄──►│   Application    │◄──►│   Database      │
│                 │    │                  │    │                 │
│ • WebSocket     │    │ • REST API       │    │ • Players       │
│ • SockJS/STOMP  │    │ • WebSocket      │    │ • Match Logs    │
│ • Admin Panel   │    │ • Business Logic │    │ • Indexes       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

### 🔧 Componenti Tecnologici

- **Backend**: Java 17 + Spring Boot 3.x
- **Database**: MongoDB 6.0+
- **WebSocket**: Spring WebSocket + SockJS + STOMP
- **Build**: Maven
- **Container**: Docker + Docker Compose
- **Orchestration**: Kubernetes + Helm
- **Frontend**: HTML5 + JavaScript (SockJS client)

## 📁 Struttura del Progetto

```
Java/
├── src/main/java/com/alnao/guessgame/
│   ├── GuessGameApplication.java           # Main Spring Boot class
│   ├── controller/                         # REST Controllers
│   │   ├── GameController.java            # Game operations API
│   │   └── AdminController.java           # Admin/logging API
│   ├── websocket/                         # WebSocket handling
│   │   ├── WebSocketController.java       # WS message handler
│   │   └── WebSocketEventListener.java    # Connection events
│   ├── service/                           # Business logic
│   │   ├── PlayerService.java             # Player management
│   │   ├── MatchLogService.java           # Event logging
│   │   └── WebSocketService.java          # WS messaging
│   ├── repository/                        # Data access
│   │   ├── PlayerRepository.java          # Player CRUD
│   │   └── MatchLogRepository.java        # Log CRUD
│   ├── model/                             # Data models
│   │   ├── Player.java                    # Player entity
│   │   └── MatchLog.java                  # Event log entity
│   ├── dto/                               # Data Transfer Objects
│   │   ├── GuessRequest.java              # Guess payload
│   │   ├── SetNumberRequest.java          # Set number payload
│   │   └── ApiResponse.java               # Generic response
│   ├── config/                            # Configuration
│       ├── WebSocketConfig.java           # WebSocket setup
│       ├── MongoConfig.java               # MongoDB setup
│       ├── CorsConfig.java                # CORS configuration
│       └── GameScoringConfig.java         # Game scoring properties
├── src/main/resources/
│   ├── application.yml                    # App configuration
│   ├── static/
│   │   ├── index.html                     # Main game frontend
│   │   └── admin.html                     # Admin panel interface
├── k8s/                                   # Kubernetes manifests
│   ├── deployment.yaml                    # App deployment
│   ├── mongodb.yaml                       # MongoDB setup
│   └── helm/                              # Helm chart
│       ├── Chart.yaml
│       ├── values.yaml
│       └── templates/
├── docker-compose.yml                     # Docker Compose setup
├── Dockerfile                             # Container definition
├── init-mongo.js                          # MongoDB initialization
├── build-and-run.sh                       # Build/deploy script
├── pom.xml                                # Maven dependencies
└── README.md                              # This file
```

## 🚀 Quick Start

### Prerequisiti

- **Java 17+**
- **Maven 3.6+**
- **Docker** e **Docker Compose**
- **Kubernetes** (opzionale, per deploy K8s)
- **Helm** (opzionale, per deploy Helm)

### 1. Clone e Build

```bash
cd Java/
mvn clean package -DskipTests
```

### 2. Esecuzione con Docker Compose (Raccomandato)

```bash
# Usa lo script automatico
./build-and-run.sh docker

# Oppure manualmente
docker-compose up --build -d
```

**Servizi disponibili:**
- **App**: http://localhost:8080
- **MongoDB Admin**: http://localhost:8081 (admin/admin123)

### 3. Esecuzione Locale (Solo App)

```bash
# Avvia MongoDB separatamente
docker run -d --name mongodb -p 27017:27017 \
  -e MONGO_INITDB_ROOT_USERNAME=admin \
  -e MONGO_INITDB_ROOT_PASSWORD=password123 \
  mongo:6.0

# Configura variabili d'ambiente
export MONGODB_URI="mongodb://admin:password123@localhost:27017/guessgame?authSource=admin"

# Avvia l'applicazione
mvn spring-boot:run
```

## 🐳 Deploy con Docker

### Build Manuale

```bash
# Build jar
mvn clean package -DskipTests

# Build immagine Docker
docker build -t guessgame:latest .

# Run con compose
docker-compose up -d

# Verificare i log dell'applicazione java
docker compose logs guessgame-app | tail -20

# Per riavviare ricompilando il progetto in caso di modifiche alle classi Java
docker compose down --remove-orphans && docker compose build --no-cache && docker compose up --remove-orphans --force-recreate

localhost:8082
```


### Configurazione Docker Compose

Il file `docker-compose.yml` include:
- **App Java**: esposta su porta 8080
- **MongoDB**: con persistenza e inizializzazione
- **Mongo Express**: UI di admin per MongoDB
- **Networking**: rete isolata per i servizi
- **Health checks**: per tutti i servizi

## ☸️ Deploy su Kubernetes

### Deploy con Manifesti

```bash
# Usa lo script automatico
./build-and-run.sh k8s

# Oppure manualmente
kubectl create namespace guessgame
kubectl apply -f k8s/mongodb.yaml
kubectl apply -f k8s/deployment.yaml
```

### Deploy con Helm

```bash
# Usa lo script automatico
./build-and-run.sh helm

# Oppure manualmente
helm repo add bitnami https://charts.bitnami.com/bitnami
helm install guessgame k8s/helm/ --namespace guessgame --create-namespace
```

### Accesso su Kubernetes

Aggiungi al file `/etc/hosts`:
```
127.0.0.1 guessgame.local
```

Poi visita: http://guessgame.local

## 🔧 Configurazione

### Variabili d'Ambiente

| Variabile | Default | Descrizione |
|-----------|---------|-------------|
| `MONGODB_URI` | `mongodb://localhost:27017/guessgame` | URI connessione MongoDB |
| `SERVER_PORT` | `8080` | Porta applicazione |
| `LOG_LEVEL` | `INFO` | Livello di logging |
| `PLAYER_INACTIVITY_TIMEOUT` | `30` | Timeout inattività giocatori (minuti) |
| `NUMBER_MIN` | `1` | Numero minimo per il gioco |
| `NUMBER_MAX` | `100` | Numero massimo per il gioco |
| `SCORE_GUESS_CORRECT` | `3` | Punti per indovinare |
| `SCORE_TARGET_FOUND` | `-1` | Punti per essere indovinati |
| `SCORE_PENALTY` | `1` | Penalità per errore |
| `MAX_GUESSES_PER_DAY` | `6` | Limite tentativi per giocatore ogni 24h |

### Profili Spring

- **`development`**: Logging debug, configurazioni di sviluppo
- **`production`**: Logging ottimizzato, configurazioni produzione

```bash
# Specifica profilo
export SPRING_PROFILES_ACTIVE=production
```

## 📊 API Reference

### REST Endpoints

#### Game Operations
- `POST /api/game/join?connectionId={id}&nickname={name}` - Unisciti al gioco
- `POST /api/game/set-number?connectionId={id}` - Imposta numero segreto
- `POST /api/game/guess?connectionId={id}` - Fai un tentativo
- `GET /api/game/scores` - Classifica giocatori
- `GET /api/game/active-players` - Giocatori attivi
- `POST /api/game/disconnect?connectionId={id}` - Disconnetti giocatore
- `POST /api/game/ban?connectionId={id}` - Banna giocatore
- `POST /api/game/cleanup?inactivityMinutes={minutes}` - Pulizia giocatori inattivi
- `GET /api/game/banned-players` - Lista giocatori bannati

#### Admin Operations
- `GET /api/admin/logs` - Tutti i log di gioco
- `GET /api/admin/logs/recent?hours={hours}` - Log recenti
- `GET /api/admin/logs/by-event?event={event}` - Log per evento
- `GET /api/admin/logs/by-player?connectionId={id}` - Log per giocatore
- `POST /api/admin/broadcast` - Invia messaggio broadcast a tutti i giocatori
- `POST /api/admin/unban?nickname={name}` - Sbanna un giocatore

### WebSocket Endpoints

#### Client → Server
- `/app/game/join` - Unisciti tramite WebSocket
- `/app/game/ping` - Heartbeat
- `/app/game/set-number` - Imposta numero
- `/app/game/guess` - Fai tentativo

#### Server → Client
- `/topic/game` - Eventi broadcast a tutti
- `/user/queue/personal` - Messaggi personali

## 🎨 Frontend

L'interfaccia web include due pagine principali:

### Pagina Giocatore (`index.html`)
- **Connessione WebSocket** con SockJS/STOMP
- **Gestione gioco** (connetti, imposta numero, indovina)
- **Visualizzazione real-time** di giocatori e punteggi
- **Log eventi** di gioco
- **UI moderna** con design responsive
- **Controllo ban** preventivo e blocco UI se bannato
- **Messaggi broadcast admin** in tempo reale
- **Gestione errori** e rate limiting tentativi

### Pannello Admin (`admin.html`)
- **Gestione giocatori attivi** con visualizzazione e controlli
- **Sistema ban/unban** con pulsanti diretti
- **Lista giocatori bannati** con funzione di sblocco
- **Broadcast messaggi** a tutti i giocatori connessi
- **Cleanup giocatori inattivi** configurabile
- **Visualizzazione log** eventi di gioco in tempo reale
- **Interfaccia amministratore** dedicata e sicura

### Caratteristiche Frontend

- Auto-riconnessione WebSocket
- Gestione errori robusta
- Aggiornamenti real-time
- Interface amministratore integrata
- Supporto mobile/responsive
- **Controlli ban multi-livello**:
  - Verifica preventiva prima della connessione
  - Blocco UI immediato se bannato durante il gioco
  - Disconnessione automatica su ban
- **Rate limiting visuale**: Messaggi informativi sui limiti tentativi
- **Broadcast admin**: Ricezione e visualizzazione messaggi amministratori
- **Pannello admin completo**: Gestione ban, cleanup, log, broadcast

### Funzionamento 
Cosa succede durante il build:
- Maven compila il codice Java
- La pagina HTML viene inclusa automaticamente nel JAR (da `src/main/resources/static/`)
- Spring Boot crea un JAR "fat" che contiene:
  - L'applicazione Java
  - Tutte le dipendenze
  - I file statici (HTML, CSS, JS)
  - Un server web embedded (Tomcat)
- Il progetto presenta questi alcuni punti di forza e molti punti deboli:
  - ✅ Simplicità: Un solo container contiene tutto (frontend + backend)
  - ✅ Simplicità: Non serve web server separato (nginx, apache)
  - ✅ Integrazione: Frontend e backend nello stesso progetto
  - ✅ Sviluppo: Un solo comando per avviare tutto il sistema
  - ⚠️ Scalabilità: Frontend e backend scalano insieme.
  - ⚠️ Scalabilità: Non si può cacheare il frontend separatamente
  - ⚠️ Separazione: Frontend e backend sono accoppiati ed è difficile avere team separati


## 🛡️ Funzionalità di Amministrazione e Sicurezza

### Sistema Ban/Unban Avanzato

Il sistema implementa controlli ban multi-livello per garantire sicurezza e controllo:

#### Protezioni Frontend
- **Controllo preventivo**: Verifica ban prima della connessione WebSocket
- **Blocco UI immediato**: Disabilitazione controlli e overlay se bannato
- **Disconnessione automatica**: Su ricezione messaggio ban via WebSocket

#### Protezioni Backend  
- **Controller REST**: Verifica ban in tutti gli endpoint di gioco
- **Service Layer**: Controlli a livello business logic
- **WebSocket Handler**: Verifica ban per comunicazioni real-time

#### Funzionalità Admin
- **Ban immediato**: Disconnessione e ban in tempo reale
- **Unban flessibile**: Rimozione ban tramite nickname
- **Lista gestione**: Visualizzazione e gestione giocatori bannati
- **Log completo**: Tracciamento eventi ban/unban

### Rate Limiting Intelligente

Sistema di controllo tentativi per prevenire spam e abuse:

```yaml
game:
  scoring:
    max-guesses-per-day: 6  # Configurabile
```

- **Limite giornaliero**: Reset automatico ogni 24h
- **Persistenza**: Stato salvato in MongoDB
- **Feedback visivo**: Messaggi informativi per il giocatore
- **Bypass admin**: Possibilità di reset tramite pannello admin

### Broadcast Amministratore

Sistema messaging real-time per comunicazioni importanti:

- **Messaggi broadcast**: Invio a tutti i giocatori connessi
- **Ricezione real-time**: Via WebSocket con notifica visiva
- **Persistenza**: Log automatico di tutti i broadcast
- **Interfaccia dedicata**: Pannello admin con form di invio

### Configurazione Scoring Flessibile

Sistema di punteggi completamente configurabile tramite properties:

```yaml
game:
  scoring:
    guess-correct: 3      # Punti per indovinare
    target-found: -1      # Punti per essere indovinati  
    guess-wrong-penalty: 1 # Penalità per errore
    max-guesses-per-day: 6 # Limite tentativi
```

- **Hot reload**: Modifiche senza riavvio (con Spring DevTools)
- **Validazione**: Controlli automatici valori configurazione
- **Override**: Possibilità override via variabili ambiente

## 🔄 Confronto con Versione AWS

### Migrazione Completata

| Componente AWS | Equivalente Java/MongoDB |
|----------------|--------------------------|
| **API Gateway WebSocket** | Spring WebSocket + SockJS |
| **API Gateway REST** | Spring Boot REST Controllers |
| **Lambda Functions** | Spring Services + Controllers |
| **DynamoDB** | MongoDB Collections |
| **CloudWatch Logs** | Spring Boot Logging + MongoDB |
| **IAM Permissions** | Spring Security (configurabile) |

### Vantaggi della Migrazione

✅ **Cloud Agnostic**: Deploy ovunque (AWS, Azure, GCP, on-premise)  
✅ **Costi Prevedibili**: Nessun costo variabile per richieste  
✅ **Scalabilità Controllata**: Kubernetes HPA e VPA  
✅ **Debugging Semplificato**: Logging tradizionale, debugging locale  
✅ **Meno Complessità**: Meno servizi da gestire  
✅ **Performance Migliori**: Connessioni persistenti, meno latenza  
✅ **Amministrazione Avanzata**: Sistema ban/unban, broadcast, rate limiting  
✅ **Configurazione Flessibile**: Scoring e limiti completamente configurabili  
✅ **Security Multi-Layer**: Controlli ban frontend + backend + WebSocket  
✅ **Monitoring Integrato**: Log strutturati, eventi tracciati, admin panel  

### Considerazioni

⚠️ **Gestione Infrastruttura**: Richiede più conoscenza operativa  
⚠️ **Alta Disponibilità**: Da configurare manualmente  
⚠️ **Monitoring**: Richiede stack di monitoring dedicato  

## 📈 Monitoring e Observability

### Health Checks

L'applicazione espone endpoint Actuator:
- `/actuator/health` - Stato applicazione e MongoDB
- `/actuator/info` - Informazioni applicazione
- `/actuator/metrics` - Metriche Micrometer

### Logging

Configurazione logging strutturato:
```yaml
logging:
  level:
    com.alnao.guessgame: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

### Metriche Custom

Il service layer include metriche per:
- Connessioni WebSocket attive
- Tentativi di indovinare per minuto
- Latenza operazioni database
- Eventi di gioco per tipo

## 🧪 Testing

### Unit Tests

```bash
# Esegui tutti i test
mvn test

# Test con coverage
mvn test jacoco:report
```

### Integration Tests

```bash
# Test con database embedded
mvn test -Dspring.profiles.active=test
```

### Smoke Tests Funzionalità

```bash
# Test API base
curl http://localhost:8080/api/game/active-players

# Test admin endpoints
curl http://localhost:8080/api/admin/logs/recent?hours=1
curl http://localhost:8080/api/game/banned-players

# Test configurazione scoring
curl http://localhost:8080/actuator/configprops | grep scoring

# Test health check
curl http://localhost:8080/actuator/health
```

### Test Scenario Ban/Unban

```bash
# 1. Crea player e bannalo
curl -X POST "http://localhost:8080/api/game/join?connectionId=test123&nickname=TestPlayer"
curl -X POST "http://localhost:8080/api/game/ban?connectionId=test123"

# 2. Verifica che sia bannato
curl http://localhost:8080/api/game/banned-players

# 3. Testa che non possa più fare azioni
curl -X POST "http://localhost:8080/api/game/set-number?connectionId=test123" \
  -H "Content-Type: application/json" -d '{"number": 42}'

# 4. Sbanna player
curl -X POST "http://localhost:8080/api/admin/unban?nickname=TestPlayer"

# 5. Verifica che possa riconnettersi
curl -X POST "http://localhost:8080/api/game/join?connectionId=test456&nickname=TestPlayer"
```

### Load Testing

Esempio con Artillery:
```bash
# Installa artillery
npm install -g artillery

# Test WebSocket
artillery run artillery-websocket-test.yml
```

## 🔒 Sicurezza

### Configurazioni Implementate

- **CORS**: Configurato per accesso cross-origin
- **Input Validation**: Validazione payload API
- **MongoDB**: Autenticazione abilitata
- **Container Security**: User non-root, filesystem read-only
- **Network**: Isolamento network nei container

### Miglioramenti Suggeriti per Produzione

- **HTTPS/TLS**: Termination a livello ingress
- **Rate Limiting**: Nginx/Kong rate limiting
- **Authentication**: Spring Security + JWT
- **Authorization**: RBAC per admin functions
- **Secrets Management**: Kubernetes secrets o Vault
- **Network Policies**: Isolamento network K8s

## 🛠️ Script di Utilità

### build-and-run.sh

Script automatico per build e deploy:

```bash
# Build solo applicazione
./build-and-run.sh build

# Run con Docker Compose
./build-and-run.sh docker

# Deploy Kubernetes
./build-and-run.sh k8s

# Deploy Helm
./build-and-run.sh helm

# Cleanup
./build-and-run.sh cleanup all
```

## 🐛 Troubleshooting

### Problemi Comuni

1. **MongoDB Connessione Fallita**
   ```bash
   # Verifica che MongoDB sia in running
   docker ps | grep mongodb
   
   # Check logs
   docker logs guessgame-mongodb
   ```

2. **WebSocket Connessione Rifiutata**
   ```bash
   # Verifica CORS e firewall
   curl -H "Origin: http://localhost:3000" http://localhost:8080/ws/info
   ```

3. **Kubernetes Pod Non Avvia**
   ```bash
   # Check pod status
   kubectl describe pod -l app=guessgame-app -n guessgame
   
   # Check logs
   kubectl logs -l app=guessgame-app -n guessgame
   ```

4. **Player Bannato Non Può Connettersi**
   ```bash
   # Verifica stato ban via API
   curl http://localhost:8080/api/game/banned-players
   
   # Sbanna via API admin
   curl -X POST "http://localhost:8080/api/admin/unban?nickname=NICKNAME"
   ```

5. **Rate Limiting Tentativi**
   ```bash
   # Verifica configurazione
   curl http://localhost:8080/actuator/configprops | grep scoring
   
   # Reset manuale (richiede restart o admin panel)
   ```

6. **Broadcast Non Ricevuti**
   ```bash
   # Test connessione WebSocket
   curl http://localhost:8080/ws/info
   
   # Verifica sottoscrizione topic
   # Frontend deve essere collegato a /topic/game
   ```

### Debug Mode

```bash
# Abilita debug logging
export LOG_LEVEL=DEBUG
export SPRING_PROFILES_ACTIVE=development
```

## ❓ FAQ (Frequently Asked Questions)

### Gestione Ban e Sicurezza

**Q: Come funziona il sistema di ban multi-livello?**  
A: Il sistema verifica lo stato ban a 3 livelli:
- **Frontend**: Controllo preventivo prima della connessione
- **REST API**: Verifica su ogni chiamata API di gioco  
- **WebSocket**: Controllo su ogni messaggio WebSocket

**Q: Un player bannato può aggirare il sistema?**  
A: No, i controlli sono implementati sia lato client che server. Anche se bypassa il frontend, le API backend rifiuteranno le richieste.

**Q: Come si sbanna un giocatore?**  
A: Tramite il pannello admin (`admin.html`) o chiamata diretta API:
```bash
curl -X POST "http://localhost:8080/api/admin/unban?nickname=NICKNAME"
```

### Rate Limiting e Configurazione

**Q: Come modifico il limite di tentativi per giocatore?**  
A: Modifica `application.yml` o usa variabile ambiente:
```bash
export GAME_SCORING_MAX_GUESSES_PER_DAY=10
```

**Q: Il rate limit è persistente tra riavvii?**  
A: Sì, lo stato è salvato in MongoDB nella collezione `players`.

**Q: Come resetto i tentativi di un giocatore?**  
A: Attualmente tramite cleanup manuale database o riavvio app. Feature admin in sviluppo.

### Broadcast e Admin

**Q: I messaggi broadcast sono persistenti?**  
A: Sì, tutti i broadcast sono loggati nella collezione `matchLogs` con evento `ADMIN_BROADCAST`.

**Q: Come accedo al pannello admin?**  
A: Visita `http://localhost:8080/admin.html` (o il tuo dominio + `/admin.html`).

**Q: Il pannello admin è protetto?**  
A: Attualmente no. Per produzione implementare autenticazione Spring Security.

### Troubleshooting

**Q: WebSocket non si connette dopo ban**  
A: Normale, il sistema blocca preventivamente la connessione. Verifica stato ban con API.

**Q: I punteggi non cambiano con la nuova configurazione**  
A: Riavvia l'applicazione per caricare le nuove properties o usa Spring DevTools per hot reload.

**Q: Come verifico se un giocatore è attualmente bannato?**  
A: `curl http://localhost:8080/api/game/banned-players` o controlla il pannello admin.

## ☁️ Deploy su AWS

Il progetto è cloud-agnostico e può essere eseguito su AWS in diversi modi, a seconda delle esigenze di scalabilità, gestione e automazione. Ecco alcune alternative pratiche:

### 1. Amazon EC2 + Docker Compose
- Avvia una o più istanze EC2 (Linux, t2.medium o superiore consigliato).
- Installa Docker e Docker Compose:
  ```bash
  sudo yum update -y
  sudo amazon-linux-extras install docker
  sudo service docker start
  sudo usermod -a -G docker ec2-user
  sudo curl -L "https://github.com/docker/compose/releases/download/1.29.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
  ```
- Copia il progetto sull’istanza (via SCP, Git, S3, ecc).
- Lancia:
  ```bash
  cd Java/
  ./build-and-run.sh docker
  # oppure
  docker-compose up -d
  ```
- Espone le porte 8080 (app) e 8081 (Mongo Express) tramite Security Group.

### 2. Amazon ECS (Elastic Container Service)
- Costruisci le immagini Docker:
  ```bash
  docker build -t guessgame-app .
  docker tag guessgame-app:latest <account-id>.dkr.ecr.<region>.amazonaws.com/guessgame-app:latest
  docker push <account-id>.dkr.ecr.<region>.amazonaws.com/guessgame-app:latest
  # Fai lo stesso per MongoDB se vuoi usare una custom image
  ```
- Crea un cluster ECS (Fargate consigliato per gestione serverless).
- Definisci i task/service per app e MongoDB.
- Configura networking (VPC, subnet, security group) e, se serve, un Application Load Balancer.
- Consulta la [guida ECS + Compose](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/cmd-ecs-compose.html).

### 3. Amazon EKS (Elastic Kubernetes Service)
- Crea un cluster EKS (puoi usare anche [eksctl](https://eksctl.io/)).
- Usa i manifesti Kubernetes e/o Helm chart già forniti nella cartella `k8s/`:
  ```bash
  kubectl apply -f k8s/mongodb.yaml
  kubectl apply -f k8s/deployment.yaml
  # oppure
  helm install guessgame k8s/helm/ --namespace guessgame --create-namespace
  ```
- Configura un Ingress Controller (ALB/NLB) per esporre l’app.
- Gestisci scaling, aggiornamenti e monitoring tramite strumenti Kubernetes.

### 4. AWS Elastic Beanstalk (Multi-Container Docker)
- Prepara un file `Dockerrun.aws.json` (v3) per multi-container (vedi [doc](https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/create_deploy_docker_v3.html)).
- Carica il progetto su Elastic Beanstalk (supporta Docker Compose).
- Elastic Beanstalk gestisce provisioning, scaling e monitoring.

### 5. AWS Lightsail (per ambienti semplici)
- Crea una istanza Lightsail con Docker preinstallato.
- Deploya con Docker Compose come su EC2.


### Note
- Per tutte le soluzioni container, puoi usare MongoDB gestito (Amazon DocumentDB) oppure un container MongoDB nel cluster.
- Ricorda di configurare variabili d’ambiente e security group per l’accesso alle porte necessarie.
- Per produzione, valuta backup, monitoring, scaling e sicurezza (VPC, IAM, Secrets Manager, ecc).


# AlNao.it
Tutti i codici sorgente e le informazioni presenti in questo repository sono frutto di un attento e paziente lavoro di sviluppo da parte di Alberto Nao, che si è impegnato a verificarne la correttezza nella misura massima possibile. Qualora parte del codice o dei contenuti sia stato tratto da fonti esterne, la relativa provenienza viene sempre citata, nel rispetto della trasparenza e della proprietà intellettuale. 


Alcuni contenuti e porzioni di codice presenti in questo repository sono stati realizzati anche grazie al supporto di strumenti di intelligenza artificiale, il cui contributo ha permesso di arricchire e velocizzare la produzione del materiale. Ogni informazione e frammento di codice è stato comunque attentamente verificato e validato, con l’obiettivo di garantire la massima qualità e affidabilità dei contenuti offerti. 


Per ulteriori dettagli, approfondimenti o richieste di chiarimento, si invita a consultare il sito [alnao.it](https://www.alnao.it/).


## License
Public projects 
<a href="https://it.wikipedia.org/wiki/GNU_General_Public_License"  valign="middle"><img src="https://img.shields.io/badge/License-GNU-blue" style="height:22px;"  valign="middle"></a> 
*Free Software!*


E' garantito il permesso di copiare, distribuire e/o modificare questo documento in base ai termini della GNU Free Documentation License, Versione 1.2 o ogni versione successiva pubblicata dalla Free Software Foundation. Permission is granted to copy, distribute and/or modify this document under the terms of the GNU Free Documentation License, Version 1.2 or any later version published by the Free Software Foundation.

