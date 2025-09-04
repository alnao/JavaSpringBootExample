# Sistema di Autenticazione - Implementazione Completata

## Panoramica
È stato implementato un sistema di autenticazione ibrido completo che supporta sia l'autenticazione locale che OAuth2 (Google, GitHub, Microsoft) seguendo l'architettura esagonale.

## Struttura Implementata

### 1. Domain Model (adapter-port/domain/auth/)
- **User**: Entità utente principale con supporto per account locali e OAuth2
- **UserRole**: Enumerazione per i ruoli (ADMIN, USER)
- **AccountType**: Tipo di account (LOCAL, GOOGLE, GITHUB, MICROSOFT)
- **UserProvider**: Provider OAuth2 collegati all'utente
- **RefreshToken**: Token per il refresh dei JWT

### 2. Repository Interfaces (adapter-port/repository/auth/)
- **UserRepository**: Operazioni CRUD e query specifiche per utenti
- **UserProviderRepository**: Gestione provider OAuth2
- **RefreshTokenRepository**: Gestione refresh token

### 3. Service Interfaces (adapter-port/service/auth/)
- **UserService**: Business logic per gestione utenti
- **JwtService**: Generazione e validazione JWT
- **UserStatistics**: DTO per statistiche utenti

### 4. API Layer (adapter-api/)
#### DTOs (dto/auth/)
- **LoginRequest**: Dati di login
- **RegisterRequest**: Dati di registrazione
- **JwtResponse**: Risposta con token JWT
- **UserProfileResponse**: Dati profilo utente
- **OAuth2ProviderInfo**: Informazioni provider OAuth2

#### Controller (controller/auth/)
- **AuthController**: Endpoint REST per autenticazione
  - `POST /api/auth/login` - Login locale
  - `POST /api/auth/register` - Registrazione utente
  - `GET /api/auth/profile` - Profilo utente (autenticato)
  - `POST /api/auth/logout` - Logout
  - `POST /api/auth/refresh` - Refresh token
  - `GET /api/auth/oauth2/providers` - Lista provider OAuth2

### 5. OnPrem Implementation (adapter-onprem/)
#### Entities JPA (entity/auth/)
- **UserEntity**: Entity PostgreSQL per utenti
- **UserProviderEntity**: Entity per provider OAuth2
- **RefreshTokenEntity**: Entity per refresh token

#### Repository Implementation (repository/auth/)
- **UserJpaRepository**: Spring Data JPA repository
- **UserRepositoryImpl**: Implementazione del repository pattern
- **UserProviderRepositoryImpl**: Implementazione provider
- **RefreshTokenRepositoryImpl**: Implementazione refresh token

#### Service Implementation (service/auth/)
- **UserServiceImpl**: Implementazione business logic utenti
- **JwtServiceImpl**: Implementazione JWT con JJWT

#### Security Configuration (config/ & security/)
- **SecurityConfig**: Configurazione Spring Security
- **JwtAuthenticationFilter**: Filtro JWT personalizzato
- **JwtAuthenticationEntryPoint**: Entry point per errori di autenticazione

## Funzionalità Implementate

### Autenticazione Locale
- Registrazione utenti con username/email/password
- Login con validazione password (BCrypt)
- Gestione ruoli utente (ADMIN/USER)
- Verifica email
- Cambio password

### Autenticazione OAuth2
- Integrazione Google, GitHub, Microsoft
- Linking/unlinking provider a account esistenti
- Auto-registrazione da provider OAuth2
- Gestione profili da provider esterni

### JWT Token Management
- Generazione JWT con claims personalizzati
- Refresh token con scadenza configurabile
- Invalidazione token
- Cleanup automatico token scaduti

### Security Features
- Password encoding con BCrypt
- CORS configuration
- Session stateless
- Protected endpoints per ruoli
- OAuth2 login flow

## Configurazione Database

### Schema PostgreSQL
Script SQL creato in `/script/init-database/init-auth-postgres.sql`:
- Tabella `users` con tutti i campi necessari
- Tabella `user_providers` per OAuth2 linking
- Tabella `refresh_tokens` per JWT refresh
- Indici per performance

### Configuration Profile
File `application-onprem.yml` con:
- Configurazione database PostgreSQL
- Configurazione OAuth2 providers
- JWT settings (secret, expiration)
- CORS settings
- Logging configuration

## Prossimi Passi per l'Utilizzo

### 1. Setup Database
```bash
# Avviare PostgreSQL
docker run --name postgres-auth \
  -e POSTGRES_DB=gestionepersonale \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 -d postgres:15

# Eseguire script di inizializzazione
psql -h localhost -U postgres -d gestionepersonale -f script/init-database/init-auth-postgres.sql
```

### 2. Configurazione OAuth2
Impostare le variabili d'ambiente:
```bash
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"
export GITHUB_CLIENT_ID="your-github-client-id"
export GITHUB_CLIENT_SECRET="your-github-client-secret"
export MICROSOFT_CLIENT_ID="your-microsoft-client-id"
export MICROSOFT_CLIENT_SECRET="your-microsoft-client-secret"
export JWT_SECRET="your-256-bit-secret"
```

### 3. Avvio Applicazione
```bash
# Avviare con profilo onprem
java -jar adapter-app/target/adapter-app-1.0.0.jar --spring.profiles.active=onprem
```

### 4. Test API
```bash
# Registrazione
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","email":"test@example.com","password":"password123"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"password123"}'

# Profilo (con JWT token)
curl -X GET http://localhost:8080/api/auth/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Implementazioni Future
- Adapter AWS con DynamoDB/MySQL
- Adapter per altri provider OAuth2
- Rate limiting per login
- Password reset via email
- Two-factor authentication
- Audit logging per security events
