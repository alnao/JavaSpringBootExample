# Adapter Replit

Questo modulo implementa l'adapter per l'ambiente **Replit**, utilizzando:
- **SQLite** come database relazionale (al posto di MySQL/PostgreSQL)
- **ReplitDB** come database NoSQL key-value (al posto di MongoDB/DynamoDB)

## Caratteristiche

### Database SQLite
- Database leggero e self-contained
- Supporto completo JPA tramite Hibernate Community Dialects
- File database: `replit_db.sqlite`
- Entità implementate:
  - `UserSQLiteEntity`
  - `RefreshTokenSQLiteEntity`
  - `UserProviderSQLiteEntity`
  - `AnnotazioneMetadataSQLiteEntity`

### ReplitDB
- Database key-value HTTP-based di Replit
- Implementazione HTTP client con WebClient
- Gestione JSON per serializzazione/deserializzazione
- Repository implementati:
  - `AnnotazioneReplitDBRepository`
  - `AnnotazioneStoricoReplitDBRepository`

### JWT Service
- Implementazione specifica per Replit: `JwtReplitService`
- Chiave segreta configurabile tramite environment variable
- Supporto completo per access token e refresh token

## Configurazione

### Profilo Spring
Attivare il profilo `replit` in `application.yml`:

```yaml
spring:
  profiles:
    active: replit
```

### Variabili Ambiente
```bash
REPLIT_DB_URL=https://kv.replit.com/v0
JWT_SECRET=your-secret-key-here
```

### Database SQLite
Il database SQLite viene creato automaticamente nel file `replit_db.sqlite` nella directory di lavoro.

### ReplitDB
ReplitDB viene acceduto tramite l'URL fornito da Replit automaticamente.

## Dipendenze

- **SQLite JDBC**: 3.42.0.0
- **Hibernate Community Dialects**: 6.2.7.Final
- **Spring WebFlux**: Per client HTTP ReplitDB
- **Jackson**: Per serializzazione JSON

## Limitazioni

### SQLite
- Non supporta tutti i tipi di dato di MySQL/PostgreSQL
- Alcune funzionalità avanzate SQL potrebbero non essere disponibili
- Performance limitate per applicazioni con alto carico

### ReplitDB
- Database key-value semplice, non supporta query complesse
- Operazioni di ricerca implementate con filtering in-memory
- Latenza rete per ogni operazione HTTP
- Limiti di storage e performance di Replit

## Utilizzo

### Avvio Applicazione
```bash
mvn spring-boot:run -Dspring.profiles.active=replit
```

### Deployment su Replit
1. Importa il progetto su Replit
2. Configura le variabili ambiente
3. L'applicazione si avvierà automaticamente con il profilo Replit

## Conversione da Altri Profili

I dati possono essere migrati da altri profili utilizzando le implementazioni dei metodi `fromDomain()` e `toDomain()` delle entità.

## Test

I test sono configurati per utilizzare database in-memory e mock per ReplitDB.

## Performance

Per ottimizzare le performance in ambiente Replit:
- Utilizzare caching per ridurre chiamate HTTP a ReplitDB
- Implementare connection pooling per SQLite
- Monitorare l'utilizzo della rete per ReplitDB
