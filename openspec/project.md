# Project Context — Sistema di Gestione Annotazioni

Contesto di progetto per OpenSpec e per gli agenti AI che lavorano su questo
repository. Descrive **come è fatto il sistema e come si lavora**; il *cosa il
sistema garantisce* sta in `openspec/specs/`.

## Cos'è

Applicazione Java / Spring Boot per la gestione di **annotazioni**: creazione,
modifica con storico versioni, workflow di stati con approvazione, import ed
export verso sistemi esterni via coda.

- **Utente (`USER`)**: crea e modifica le proprie annotazioni.
- **Moderatore (`MODERATOR`)**: conferma le annotazioni.
- **Amministratore (`ADMIN`)**: conferma e marca per l'invio a sistemi esterni.
- **Sistema (`SYSTEM`)**: ruolo tecnico per import/export schedulati.

Autore: `< AlNao />`. Nasce come esempio pratico e didattico, quindi la
**leggibilità e la coerenza fra i moduli contano quanto la funzionalità**.

## Stack

- Java 21 (migrato da 17), Spring Boot 3.3.5, Maven multi-modulo
- `groupId` `it.alnao.springbootexample`, versione corrente **0.0.2**
- Spring Security + JWT (jjwt 0.12.3), springdoc-openapi 2.5.0
- AWS SDK v2 2.21.29, MongoDB driver 4.11.1, PostgreSQL 42.7.1, MySQL 8.2.0
- Redis/Redisson per i lock distribuiti, ShedLock per le schedulazioni
- JaCoCo su tutti i moduli, SonarQube via `script/sonarqube/docker-compose.yml`
- Docker, Docker Compose, Kubernetes/Minikube, AWS (EC2, ECS Fargate, EKS), Azure (VM, ACI)
- Sviluppo su Debian 13

## Architettura — esagonale (Ports & Adapters)

Regola strutturale non negoziabile: **la logica di dominio vive in `core/`, gli
adapter implementano soltanto i port.**

```
core/                 domini, service, port (interfacce), security, scheduler
adapter-api/          REST controller, DTO, mapper, Swagger, exception handler
adapter-web/          risorse statiche e mini-sito di prova
adapter-javafx/       frontend desktop (solo profilo sqlite)
adapter-sqlite/       SQLite: sql + no-sql + code + lock in-memory
adapter-postgresql/   PostgreSQL (profilo kube)
adapter-mongodb/      MongoDB (profilo kube)
adapter-kafka/        code Kafka, import + export (profilo kube)
adapter-aws/          DynamoDB, MySQL/Aurora, SQS (profilo aws)
adapter-azure/        Cosmos DB, SQL Server, Service Bus (profilo azure)
adapter-redis/        lock distribuiti Redisson + config ShedLock
application/          applicazione Spring Boot principale, application*.yml
```

Punti chiave nel `core`:

- **Domini**: `Annotazione`, `AnnotazioneMetadata`, `AnnotazioneCompleta`,
  `AnnotazioneStoricoStati`, `StatoAnnotazione`, `TransizioneStato`,
  `auth/{User, UserRole, RefreshToken, UserProvider, AccountType}`
- **Port** (interfacce implementate dagli adapter):
  `repository/AnnotazioneRepository`, `repository/AnnotazioneMetadataRepository`,
  `repository/auth/{UserRepository, RefreshTokenRepository, UserProviderRepository}`,
  `portService/AnnotazioniPortService`
- **Service**: `AnnotazioneService` (+ `AbstractAnnotazioneService`),
  `AnnotazioneStoricoStatiService`, `ValidatoreTransizioniStatoService`,
  `AnnotazioneLockService`, `AnnotazioneImportService`, `AnnotazioneInvioService`,
  `auth/{JwtService, UserService}`
- **Scheduler**: `AnnotazioneImportScheduler`, `AnnotazioneInvioScheduler`
- **Config**: `TransizioniStatoConfig`, `AnnotazioneImportProperties`,
  `AnnotazioneInvioProperties`, `JwtConfig`, `SecurityConfig`, `NoSqlTableConfig`

### Stati di un'annotazione

`INSERITA`, `MODIFICATA`, `IMPORTATA`, `CONFERMATA`, `RIFIUTATA`, `DAINVIARE`,
`INVIATA`, `SCADUTA`, `BANNATA`, `ERRORE`.

Le transizioni ammesse dipendono dal **ruolo** e sono dichiarate in
configurazione (`TransizioniStatoConfig`), validate da
`ValidatoreTransizioniStatoService`. Ogni cambio di stato aggiorna il metadata
**e** aggiunge una riga in `StoricoStati`. Una transizione non ammessa risponde
**HTTP 403**. `DAINVIARE` è l'ultimo stato raggiungibile da API/Web: `INVIATA`
lo imposta soltanto lo scheduler di export.

## I quattro profili Spring

Il sistema è **agnostico rispetto al cloud provider**. Ogni adapter è attivato
da `@Profile`; la stessa funzionalità deve valere su tutti i profili, con
tecnologie diverse.

| Profilo  | SQL         | No-SQL     | Coda        | Lock                    |
|----------|-------------|------------|-------------|-------------------------|
| `sqlite` | SQLite      | SQLite     | SQLite      | `ConcurrentHashMap` in-memory |
| `kube`   | PostgreSQL  | MongoDB    | Kafka       | Redis/Redisson          |
| `aws`    | MySQL/Aurora| DynamoDB   | SQS         | ElastiCache for Redis   |
| `azure`  | SQL Server  | Cosmos DB  | Service Bus | Azure Cache for Redis   |

Esiste anche un profilo `replit` (una sola classe) come variante di `sqlite`.

**Implicazione per ogni proposta OpenSpec**: una feature che tocca persistenza,
code o lock è *incompleta* finché non è coperta su tutti e quattro i profili.
I task devono elencarli esplicitamente, uno per uno. Se un profilo viene escluso
di proposito, va scritto nel `proposal.md` con la motivazione.

Note note: il profilo `azure` **non funziona in locale** (l'immagine Cosmos non
è utilizzabile), va provato su cloud. Cartella `script/azure-onprem-non-funziona/`
conservata come testimonianza, non come codice vivo.

## Convenzioni di codice

- **Constructor injection obbligatoria**: niente `@Autowired` su campi
  (refactor già fatto, segnalazione Sonar). I nuovi bean seguono la stessa forma.
- Nomi di dominio, service e API **in italiano** (`Annotazione`, `StoricoStati`,
  `/api/annotazioni`); nomi tecnici di framework in inglese.
- La logica non scende mai nei controller: i controller di `adapter-api`
  delegano ai service del `core` tramite i port.
- Un nuovo backend tecnologico = un nuovo adapter con `@Profile`, mai un `if`
  sul profilo dentro al `core`.
- Le configurazioni runtime sono parametriche via env var (vedi
  `IMPORT_ANNOTAZIONI_ENABLED`, `IMPORT_ANNOTAZIONI_CRON_EXPRESSION`, ...) e
  dichiarate nelle `*Properties` del core.
- Coverage attesa **> 90%**, zero security issue aperte su SonarQube.

## Comandi

```bash
# build completo (con test)
mvn clean package
# build veloce
mvn clean package -DskipTests
# test di un sottoinsieme di moduli
mvn -q -pl adapter-aws,core -am test -DskipITs
```

Script (in `script/`, da lanciare dalla **root** del progetto):

- `script/automatic-test/test-all.sh` — regressione completa: kube via
  docker-compose, sqlite locale, aws in locale, minikube. Scrive
  `automatic-test-YYYYMMDD.log` e in coda al log un riepilogo con l'elenco delle
  esecuzioni e la tabella dei test per profilo. Esce con codice diverso da zero
  se almeno un profilo ha riportato errori.
- `script/automatic-test/lib-report.sh` — libreria condivisa dagli script di
  test: `test_ok` / `test_ko` registrano i singoli esiti, `report_run_inizio` /
  `report_chiusura` delimitano l'esecuzione di un profilo. Un test nuovo va
  registrato con queste funzioni, altrimenti non compare nel riepilogo.
- test mirati: `test-sqlite-onprem.sh`, `test-kube-onprem-docker-compose.sh`,
  `test-aws-onprem.sh`, `test-minikube.sh`, `test-azure-cosmos-runlocale.sh`,
  `test-import-kafka.sh`, `test-prenotazione-annotazione.sh`
- deploy/provisioning: `aws-ec2/`, `aws-ecs/`, `aws-eks/`, `aws-onprem/`,
  `azure-dbremoti-*/`, `minikube/`, `sqlite-locale/`, `sqlite-ec2/`
- immagini: `docker-build.sh`, `push-image-docker-hub.sh`
  (`alnao/gestioneannotazioni` su Docker Hub)

Prima di dichiarare completa una change che tocca il runtime: build pulita +
lo script di test del profilo interessato. Per un rilascio: `test-all.sh`.

## Documentazione

- [README.md](../README.md) — struttura, API, esecuzione locale, Swagger, Sonar, JavaFX, auth
- [Roadmap.md](../Roadmap.md) — roadmap e todo-list storica con ✅/🚧, usata anche come changelog narrativo
- [PlatformDockerHub.md](../PlatformDockerHub.md), [PlatformAws.md](../PlatformAws.md), [PlatformAzure.md](../PlatformAzure.md) — guide per piattaforma

Divisione dei ruoli da mantenere: **`Roadmap.md` racconta cosa è stato fatto**,
**`openspec/specs/` dichiara cosa il sistema garantisce**. Quando una change
viene archiviata, si aggiornano le spec e si spunta la voce in Roadmap.

## Vincoli operativi

- ⚠️ I profili `aws` e `azure` girano su cloud reale e **generano costi**:
  nessun provisioning va eseguito senza richiesta esplicita.
- Gli agenti AI non eseguono comandi git (commit, push, pull, merge) e non
  escono dalla cartella di lavoro. Convenzione ereditata dai piani in
  `.AlNaoAIRunners/`.
- Nessuna credenziale in chiaro nei file versionati; la gestione via secret per
  profilo è lavoro in corso (Kubernetes Secret, AWS Secrets Manager, Azure Key
  Vault, file statici per `sqlite`).
- `application/data/` deve esistere per i test locali:
  `mkdir -p application/data`.
