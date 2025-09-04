# Sistema di Autenticazione Ibrido - Spring Boot

## Panoramica

Il progetto implementa un sistema di autenticazione completo e modulare utilizzando l'architettura esagonale di Spring Boot. Il sistema supporta sia l'autenticazione locale che quella OAuth2, con implementazioni per ambienti OnPrem e AWS.

## Architettura

### Adapter Port (`adapter-port`)
Contiene le interfacce e il dominio del sistema:

- **Domini (`auth` package):**
  - `User`: Entità utente principale
  - `UserRole`: Enum per i ruoli (USER, ADMIN, SUPER_ADMIN)
  - `AccountType`: Enum per i tipi di account (LOCAL, GOOGLE, GITHUB)
  - `UserProvider`: Entità per collegare provider OAuth2
  - `RefreshToken`: Entità per gestire i token di refresh

- **Repository Interfaces:**
  - `UserRepository`: CRUD e query personalizzate per utenti
  - `UserProviderRepository`: Gestione provider OAuth2
  - `RefreshTokenRepository`: Gestione refresh token

- **Service Interfaces:**
  - `UserService`: Logica di business per utenti
  - `JwtService`: Gestione token JWT
  - `AuthService`: Servizi di autenticazione

### Adapter API (`adapter-api`)
Livello REST API con:

- **DTOs:** Request/Response objects per l'API
- **Controller:** `AuthController` con endpoint per registrazione, login, refresh token
- **Configurazione:** Mapping e validazione

### Adapter OnPrem (`adapter-onprem`)
Implementazione per deployment on-premise:

- **Database:** PostgreSQL + Spring Data JPA
- **Entities:** JPA entities per tutte le entità del dominio
- **Repositories:** Implementazioni con Spring Data JPA
- **Services:** Business logic per ambiente OnPrem
- **Security:** Configurazione Spring Security con JWT

### Adapter AWS (`adapter-aws`)
Implementazione per deployment cloud AWS:

- **Database Dual:**
  - DynamoDB per NoSQL (user metadata, providers)
  - MySQL/RDS per dati relazionali (refresh tokens)
- **Entities:** 
  - DynamoDB entities con AWS SDK Enhanced Client
  - MySQL JPA entities
- **Repositories:** Implementazioni per entrambi i database
- **Services:** Business logic per ambiente AWS
- **Security:** Configurazione Spring Security ottimizzata per cloud

### Adapter App (`adapter-app`)
Main application con configurazioni per i diversi profili:

- **Profiles:**
  - `onprem`: Usa PostgreSQL e MongoDB
  - `aws`: Usa DynamoDB e MySQL
- **Configurazioni:** `application-onprem.yml`, `application-aws.yml`

## Funzionalità Implementate

### 1. Autenticazione Locale
- Registrazione utenti con email/password
- Login con username o email
- Hash password con BCrypt
- Validazione email

### 2. Autenticazione OAuth2
- Supporto Google OAuth2
- Supporto GitHub OAuth2
- Collegamento automatico account esistenti
- Gestione provider multipli per utente

### 3. Gestione Token JWT
- Token di accesso con scadenza configurabile
- Refresh token per rinnovo automatico
- Revoca token
- Blacklist token (per logout)

### 4. Gestione Ruoli
- Sistema ruoli gerarchico (USER < ADMIN < SUPER_ADMIN)
- Autorizzazioni basate su ruoli
- Endpoint per gestione ruoli (solo ADMIN)

### 5. Sicurezza
- CORS configurato per frontend
- CSRF protection disabilitato per API stateless
- Password policy con validazione
- Rate limiting (configurabile)

## API Endpoints

### Autenticazione
```
POST /api/auth/register      - Registrazione nuovo utente
POST /api/auth/login         - Login con credenziali
POST /api/auth/refresh       - Rinnovo token con refresh token
POST /api/auth/logout        - Logout e revoca token
GET  /api/auth/me           - Informazioni utente corrente
```

### Gestione Utenti (Admin)
```
GET    /api/auth/users              - Lista tutti gli utenti
GET    /api/auth/users/{id}         - Dettagli utente specifico
PUT    /api/auth/users/{id}/role    - Modifica ruolo utente
DELETE /api/auth/users/{id}         - Elimina utente
```

### OAuth2
```
GET /oauth2/authorization/google  - Avvia OAuth2 Google
GET /oauth2/authorization/github  - Avvia OAuth2 GitHub
```

## Database Schema

### PostgreSQL (OnPrem)
- Tabelle: `users`, `user_providers`, `refresh_tokens`
- Indici ottimizzati per query frequenti
- Foreign key constraints

### DynamoDB (AWS)
- Tabelle: `SpringBootExample-Users`, `SpringBootExample-UserProviders`, `SpringBootExample-RefreshTokens`
- Partition/Sort keys ottimizzati
- TTL per refresh token

### MySQL (AWS)
- Schema compatibile con PostgreSQL
- Engine InnoDB per transazioni ACID
- Charset UTF8MB4 per supporto emoji

## Configurazione

### Variabili d'Ambiente

#### OnPrem
```bash
POSTGRES_URL=jdbc:postgresql://localhost:5432/springbootexample
POSTGRES_USER=springbootexample
POSTGRES_PASSWORD=password
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
```

#### AWS
```bash
MYSQL_HOST=your-rds-endpoint
MYSQL_DATABASE=springbootexample
MYSQL_USER=springbootexample
MYSQL_PASSWORD=password
DYNAMODB_TABLE_PREFIX=SpringBootExample
AWS_REGION=eu-west-1
JWT_SECRET=your-secret-key
```

## Deploy

### OnPrem con Docker
```bash
docker-compose -f script/aws-onprem/docker-compose.yml up -d
```

### AWS ECS/Fargate
- Container con profilo `aws`
- RDS MySQL + DynamoDB
- ALB con SSL termination
- IAM roles per DynamoDB access

## Test

Ogni modulo include test unitari e di integrazione:
```bash
mvn test                    # Tutti i test
mvn test -pl adapter-onprem # Solo OnPrem
mvn test -pl adapter-aws    # Solo AWS
```

## Sicurezza

### Configurazioni Applicate
- Password hashing con BCrypt (strength 12)
- JWT con algoritmo HMAC256
- HTTPS obbligatorio in produzione
- CORS restrictive policy
- Input validation con Bean Validation
- SQL injection protection con JPA
- XSS protection con headers appropriati

### Best Practices
- Secrets gestiti tramite environment variables
- Token rotation automatica
- Audit logging per operazioni sensibili
- Rate limiting per endpoint pubblici
- Account lockout dopo tentativi falliti

## Monitoraggio

- Health checks per database connections
- Metrics per autenticazioni riuscite/fallite
- Logging strutturato per audit trail
- Alerting per anomalie di sicurezza

Il sistema è pronto per la produzione e supporta scaling orizzontale in entrambi gli ambienti (OnPrem e AWS).
