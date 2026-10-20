# credenziali-secret Specification

## Purpose
Definisce come l'applicazione riceve le credenziali (password degli utenti
iniziali, password dei backend, chiave di firma dei token `JWT_SECRET`) senza
valori in chiaro nei file versionati, come si comporta quando ne manca una,
come si avvia in locale con valori noti e come si ruota una credenziale.

## Requirements

### Requirement: Credenziali fornite dall'ambiente di esecuzione

Ogni deploy SHALL fornire all'applicazione le credenziali come variabili
d'ambiente provenienti da una sorgente non versionata. Nel profilo `sqlite` le
credenziali SHALL essere la chiave `JWT_SECRET` e, quando la creazione degli
utenti iniziali è attiva, le password `APP_USER_ADMIN_PASSWORD`,
`APP_USER_MODERATORE_PASSWORD` e `APP_USER_ALNAO_PASSWORD`. Nel profilo `kube`
SHALL essere anche `POSTGRES_PASSWORD` e `MONGODB_URI` (con le credenziali di
Mongo), e i container dei database e degli strumenti di amministrazione
(mongo-express) SHALL ricevere le loro password dalla stessa sorgente. I file
di configurazione versionati dei profili `sqlite` e `kube` e i manifest
Kubernetes versionati MUST NOT contenere valori per queste credenziali.

#### Scenario: sqlite in locale con docker-compose

- **WHEN** l'ambiente `sqlite-locale` viene avviato con un file `.env` che
  imposta `APP_USER_ADMIN_PASSWORD=Prova-123` e un `JWT_SECRET` proprio
- **THEN** `POST /api/auth/login` con `admin` / `Prova-123` risponde 200 con un
  token
- **AND** lo stesso login con `admin` / `admin` risponde 401

#### Scenario: sqlite su EC2

- **WHEN** viene eseguito lo script di avvio di `sqlite-ec2` su un account
  privo delle risorse
- **THEN** l'istanza contiene un file di credenziali leggibile solo da root,
  con valori generati sull'istanza
- **AND** il login di `admin` riesce con la password letta da quel file e
  fallisce con `admin`

#### Scenario: kube con docker-compose

- **WHEN** il `docker-compose.yml` di root viene avviato su volumi vuoti con un
  file `.env` che imposta password proprie per Postgres, Mongo e
  `APP_USER_ADMIN_PASSWORD=Prova-123`
- **THEN** l'applicazione si collega a Postgres e Mongo e risponde `UP`
- **AND** il login con `admin` / `Prova-123` risponde 200 e con `admin` /
  `admin` risponde 401

#### Scenario: kube su minikube

- **WHEN** lo script di avvio di `script/minikube` viene eseguito su un cluster
  senza il Secret delle credenziali
- **THEN** il Secret viene creato con valori casuali e i Deployment leggono le
  credenziali da lì
- **AND** il login di `admin` riesce con la password letta dal Secret e
  fallisce con `admin`

#### Scenario: Token firmato con la chiave pubblica di default

- **WHEN** a un'applicazione avviata da `sqlite-locale`, `sqlite-ec2`, dal
  `docker-compose.yml` di root o da `script/minikube` viene inviato un token
  firmato con il valore di `JWT_SECRET` presente in `application.yml`
- **THEN** la richiesta a un endpoint protetto risponde 401

### Requirement: Avvio interrotto se manca una credenziale

L'applicazione SHALL rifiutarsi di partire quando una credenziale richiesta
dalla configurazione attiva è assente o vuota. Il messaggio di errore SHALL
indicare il nome della variabile d'ambiente mancante e MUST NOT contenere il
valore di altre credenziali.

#### Scenario: Password di un utente iniziale mancante (sqlite)

- **WHEN** l'applicazione viene avviata con il profilo `sqlite`, la creazione
  degli utenti iniziali attiva e `APP_USER_MODERATORE_PASSWORD` non impostata
- **THEN** l'applicazione termina senza mettersi in ascolto sulla porta HTTP
- **AND** il log riporta `APP_USER_MODERATORE_PASSWORD` come variabile mancante

#### Scenario: Creazione utenti disattivata (sqlite)

- **WHEN** l'applicazione viene avviata con il profilo `sqlite`,
  `APP_CREATE_USERS_ENABLED=false` e nessuna variabile `APP_USER_*_PASSWORD`
- **THEN** l'applicazione parte e `GET /actuator/health` risponde `UP`

#### Scenario: Password di un utente iniziale mancante (kube)

- **WHEN** l'applicazione viene avviata con il profilo `kube` e
  `APP_USER_ALNAO_PASSWORD` non impostata
- **THEN** l'applicazione termina senza mettersi in ascolto sulla porta HTTP
- **AND** il log riporta `APP_USER_ALNAO_PASSWORD` come variabile mancante

#### Scenario: Password del database mancante (kube)

- **WHEN** l'applicazione viene avviata con il profilo `kube` senza
  `POSTGRES_PASSWORD`
- **THEN** l'applicazione termina senza mettersi in ascolto sulla porta HTTP
- **AND** il log riporta `POSTGRES_PASSWORD` come variabile mancante, senza
  tentare la connessione al database

#### Scenario: File .env mancante (kube con docker-compose)

- **WHEN** il `docker-compose.yml` di root viene avviato senza il file `.env`
- **THEN** docker-compose termina con errore prima di creare i container
- **AND** il messaggio indica la variabile mancante e di creare `.env` a
  partire da `.env.example`

### Requirement: Valori fissi per l'uso locale nel profilo dev

Il profilo `dev`, attivato insieme al profilo tecnologico, SHALL fornire
valori fissi e pubblici per tutte le credenziali, così che l'applicazione
parta in locale senza file di credenziali. Una variabile d'ambiente impostata
SHALL prevalere sul valore del profilo `dev`.

#### Scenario: Avvio locale con sqlite e dev

- **WHEN** l'applicazione viene avviata con i profili `sqlite,dev` e nessuna
  variabile di credenziale impostata
- **THEN** l'applicazione parte
- **AND** il login con `admin` / `admin` e con `alnao` / `bellissimo` risponde
  200

#### Scenario: Variabile d'ambiente prevale su dev

- **WHEN** l'applicazione viene avviata con i profili `sqlite,dev`, un
  database vuoto e `APP_USER_ADMIN_PASSWORD=Altra-456`
- **THEN** il login con `admin` / `Altra-456` risponde 200
- **AND** il login con `admin` / `admin` risponde 401

#### Scenario: Avvio locale con kube e dev

- **WHEN** l'applicazione viene avviata con i profili `kube,dev`, nessuna
  variabile di credenziale impostata e Postgres e Mongo raggiungibili con le
  credenziali del profilo `dev`
- **THEN** l'applicazione parte senza errori di credenziali mancanti
- **AND** il login con `admin` / `admin` risponde 200

### Requirement: Profilo dev escluso dai deploy

Nessuno script di deploy o di provisioning del repository SHALL attivare il
profilo `dev`. L'unica eccezione ammessa sono gli script di test automatici
in `script/automatic-test`, che avviano l'applicazione in locale.

#### Scenario: Controllo sugli script di deploy

- **WHEN** viene eseguita la regressione con `test-sqlite-onprem.sh`
- **THEN** il test verifica che nessuno script fuori da
  `script/automatic-test` attivi il profilo `dev` e registra l'esito nel
  riepilogo
- **AND** se uno script lo attiva il test risulta fallito con il nome del
  file

### Requirement: Credenziali non esposte

I valori delle credenziali MUST NOT comparire nei log dell'applicazione, negli
output degli script di avvio e nelle risposte HTTP. I file versionati SHALL
contenere al massimo i nomi delle variabili con valori segnaposto.

#### Scenario: Log di avvio con utenti iniziali (sqlite)

- **WHEN** l'applicazione `sqlite` crea gli utenti iniziali all'avvio
- **THEN** il log riporta username, email e ruolo degli utenti
- **AND** nessuna riga del log contiene le loro password

#### Scenario: Log di avvio con utenti iniziali (kube)

- **WHEN** l'applicazione `kube` crea gli utenti iniziali all'avvio
- **THEN** il log riporta username, email e ruolo degli utenti
- **AND** nessuna riga del log contiene le loro password o quelle dei database

#### Scenario: Endpoint env dell'actuator

- **WHEN** un utente autenticato chiama `GET /actuator/env`
- **THEN** i valori di `JWT_SECRET`, delle variabili `APP_USER_*_PASSWORD`, di
  `POSTGRES_PASSWORD` e di `MONGODB_URI` risultano mascherati

#### Scenario: File di esempio versionato (sqlite)

- **WHEN** si guarda il contenuto versionato di `script/sqlite-locale`
- **THEN** esiste un `.env.example` con i nomi di tutte le variabili di
  credenziale e valori segnaposto
- **AND** il file `.env` è escluso dal versionamento

#### Scenario: File di esempio versionato (kube)

- **WHEN** si guarda il contenuto versionato della root e di `script/minikube`
- **THEN** nella root esiste un `.env.example` con i nomi di tutte le
  variabili di credenziale del profilo `kube` e valori segnaposto, e il file
  `.env` è escluso dal versionamento
- **AND** nessun manifest di `script/minikube` contiene valori di credenziali

#### Scenario: File di credenziali assente (sqlite)

- **WHEN** lo script di avvio di `sqlite-locale` viene lanciato senza il file
  `.env`
- **THEN** lo script termina con errore prima di avviare i container
- **AND** il messaggio indica di creare `.env` a partire da `.env.example`

#### Scenario: Avvio di minikube

- **WHEN** lo script di avvio di `script/minikube` crea il Secret e
  inizializza i database
- **THEN** l'output dello script non contiene i valori del Secret

### Requirement: Rotazione di una credenziale

Per ogni deploy SHALL esistere uno script di rotazione che genera un nuovo
valore casuale per la credenziale indicata, aggiorna la sorgente delle
credenziali, riavvia l'applicazione e attende che `GET /actuator/health`
risponda `UP`. Per la password di un database lo script SHALL prima
aggiornarla sul database stesso. Lo script SHALL terminare con errore se
l'applicazione non torna `UP` entro il tempo previsto. Lo script MUST NOT
stampare il nuovo valore.

#### Scenario: Rotazione di JWT_SECRET (sqlite in locale)

- **WHEN** un client ha ottenuto un access token con il login e viene
  eseguito lo script di rotazione di `sqlite-locale` per `JWT_SECRET`
- **THEN** lo script termina con successo dopo che l'applicazione risponde
  `UP`
- **AND** il vecchio access token su un endpoint protetto risponde 401
- **AND** un nuovo login restituisce un access token che risponde 200

#### Scenario: Rotazione di JWT_SECRET (sqlite su EC2)

- **WHEN** viene eseguito lo script di rotazione di `sqlite-ec2` per
  `JWT_SECRET` su uno stack avviato
- **THEN** il file di credenziali sull'istanza contiene un nuovo valore
- **AND** l'applicazione risponde `UP` e il vecchio access token risponde 401

#### Scenario: Credenziale non ruotabile (sqlite)

- **WHEN** lo script di rotazione di `sqlite-locale` o di `sqlite-ec2` viene
  lanciato per una password `APP_USER_*_PASSWORD`
- **THEN** lo script termina con errore senza modificare nulla
- **AND** il messaggio spiega che la password di un utente già creato non
  cambia al riavvio

#### Scenario: Rotazione di JWT_SECRET (kube)

- **WHEN** viene eseguito lo script di rotazione del `docker-compose.yml` di
  root o di `script/minikube` per `JWT_SECRET` su uno stack avviato
- **THEN** lo script termina con successo dopo che l'applicazione risponde
  `UP`
- **AND** il vecchio access token risponde 401 e un nuovo login risponde 200

#### Scenario: Rotazione della password di Postgres (kube)

- **WHEN** viene eseguito lo script di rotazione per `POSTGRES_PASSWORD` su uno
  stack `kube` avviato (docker-compose o minikube)
- **THEN** la password viene cambiata su Postgres e nella sorgente delle
  credenziali
- **AND** l'applicazione riavviata risponde `UP` e `GET /api/annotazioni` con
  un token valido risponde 200

#### Scenario: Rotazione della password di Mongo (kube)

- **WHEN** viene eseguito lo script di rotazione per la password dell'utente
  root di Mongo su uno stack `kube` avviato
- **THEN** la password viene cambiata su Mongo, nella sorgente delle
  credenziali e quindi in `MONGODB_URI`
- **AND** l'applicazione riavviata risponde `UP` e la creazione di
  un'annotazione risponde con successo

#### Scenario: Credenziale non ruotabile (kube)

- **WHEN** lo script di rotazione di `kube` viene lanciato per una password
  `APP_USER_*_PASSWORD`
- **THEN** lo script termina con errore senza modificare nulla
- **AND** il messaggio spiega che la password di un utente già creato non
  cambia al riavvio

### Requirement: Utenti iniziali creati all'avvio

Quando la creazione degli utenti iniziali è attiva, l'applicazione SHALL
creare all'avvio gli utenti `admin` (ruolo `ADMIN`), `moderatore` (ruolo
`MODERATOR`) e `alnao` (ruolo `USER`) che non esistono ancora, con le password
lette da `APP_USER_ADMIN_PASSWORD`, `APP_USER_MODERATORE_PASSWORD` e
`APP_USER_ALNAO_PASSWORD`. Un utente già esistente MUST NOT essere modificato.
I file versionati di inizializzazione dei database MUST NOT contenere utenti
con password. La creazione SHALL essere attiva nei profili `sqlite` e `kube`;
nei profili in cui non è attiva l'applicazione SHALL partire senza richiedere
quelle variabili.

#### Scenario: Database vuoto (sqlite)

- **WHEN** l'applicazione `sqlite` parte su un database vuoto con le tre
  variabili impostate
- **THEN** il login dei tre utenti con le password delle variabili risponde 200

#### Scenario: Database vuoto (kube)

- **WHEN** l'applicazione `kube` parte su un database Postgres inizializzato
  con lo script versionato e con le tre variabili impostate
- **THEN** il login dei tre utenti con le password delle variabili risponde 200
- **AND** prima dell'avvio dell'applicazione la tabella degli utenti è vuota

#### Scenario: Utente già esistente (kube)

- **WHEN** l'applicazione `kube` riparte con `APP_USER_ADMIN_PASSWORD` cambiata
  su un database in cui `admin` esiste già
- **THEN** il login di `admin` riesce con la password precedente e fallisce con
  quella nuova

#### Scenario: Profilo senza creazione utenti (aws)

- **WHEN** l'applicazione viene avviata con il profilo `aws` senza alcuna
  variabile `APP_USER_*_PASSWORD`
- **THEN** l'avvio non viene interrotto per le password degli utenti iniziali
