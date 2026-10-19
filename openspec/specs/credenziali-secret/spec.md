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
`APP_USER_MODERATORE_PASSWORD` e `APP_USER_ALNAO_PASSWORD`. I file di
configurazione versionati del profilo `sqlite` MUST NOT contenere valori per
queste password.

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

#### Scenario: Token firmato con la chiave pubblica di default

- **WHEN** a un'applicazione `sqlite` avviata da `sqlite-locale` o da
  `sqlite-ec2` viene inviato un token firmato con il valore di `JWT_SECRET`
  presente in `application.yml`
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

#### Scenario: Endpoint env dell'actuator

- **WHEN** un utente autenticato chiama `GET /actuator/env`
- **THEN** i valori di `JWT_SECRET` e delle variabili `APP_USER_*_PASSWORD`
  risultano mascherati

#### Scenario: File di esempio versionato (sqlite)

- **WHEN** si guarda il contenuto versionato di `script/sqlite-locale`
- **THEN** esiste un `.env.example` con i nomi di tutte le variabili di
  credenziale e valori segnaposto
- **AND** il file `.env` è escluso dal versionamento

#### Scenario: File di credenziali assente (sqlite)

- **WHEN** lo script di avvio di `sqlite-locale` viene lanciato senza il file
  `.env`
- **THEN** lo script termina con errore prima di avviare i container
- **AND** il messaggio indica di creare `.env` a partire da `.env.example`

### Requirement: Rotazione di una credenziale

Per ogni deploy SHALL esistere uno script di rotazione che genera un nuovo
valore casuale per la credenziale indicata, aggiorna la sorgente delle
credenziali, riavvia l'applicazione e attende che `GET /actuator/health`
risponda `UP`. Lo script SHALL terminare con errore se l'applicazione non
torna `UP` entro il tempo previsto. Lo script MUST NOT stampare il nuovo
valore.

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
