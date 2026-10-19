## Why

Nel profilo `sqlite` le credenziali stanno in chiaro nei file versionati: le
password degli utenti di default (`admin/admin`, `moderatore/cattivo`,
`alnao/bellissimo`) in `application-sqlite.yml` e, per tutti i profili, il
`JWT_SECRET` di default in `application.yml`. Nessuno script di deploy passa
`JWT_SECRET`, quindi ogni ambiente firma i token con una chiave pubblicata su
GitHub: chiunque legga il repository può costruire un token `ADMIN` valido.
In più, all'avvio `DataInitializer` scrive a log (livello INFO) le password
degli utenti di default.

Questa è la prima di cinque change dello step "Gestione password via secret"
(Roadmap): `sqlite` viene per primo perché è il profilo più semplice e fissa il
pattern che gli altri seguiranno.

## What Changes

- **BREAKING** Le password degli utenti di default del profilo `sqlite` non
  hanno più valori in `application-sqlite.yml`: arrivano da variabili
  d'ambiente (una per utente). Se la creazione utenti è attiva e una password
  manca, l'applicazione non parte e il messaggio indica la variabile mancante.
- I valori fissi di oggi (password degli utenti e `JWT_SECRET`) si spostano in
  `application-dev.yml`, dichiarati con il nome della variabile d'ambiente: il
  profilo `dev` diventa il modo esplicito per avviare l'app in locale con
  credenziali note e pubbliche. Le variabili d'ambiente hanno sempre la
  precedenza.
- `script/sqlite-locale`: le credenziali arrivano da un file `.env` non
  versionato; viene versionato un `.env.example` con i nomi delle variabili e
  valori segnaposto. Lo script di avvio si ferma se `.env` manca.
- `script/sqlite-ec2`: lo user-data genera sull'istanza un file statico di
  credenziali (permessi ristretti, valori casuali) e avvia il container con
  quel file. I valori non escono dall'istanza; lo script indica come leggerli
  via ssh.
- Entrambi i deploy `sqlite` passano anche `JWT_SECRET` all'applicazione.
- Script di rotazione per `sqlite` (locale ed EC2): genera un nuovo valore,
  aggiorna il file di credenziali, riavvia il container e attende che l'app
  risponda. Ruotare `JWT_SECRET` invalida gli access token già emessi: gli
  utenti rifanno il login (il flusso di refresh oggi non emette un nuovo
  access token, vedi Non-goals).
- Le password degli utenti di default non compaiono più nei log.
- `test-sqlite-onprem.sh` avvia l'app con i profili `sqlite,dev`, così i test
  continuano a usare le credenziali note.
- Vincolo nuovo: nessuno script di deploy attiva il profilo `dev`.

## Capabilities

### New Capabilities

- `credenziali-secret`: come l'applicazione riceve le credenziali (password
  dei backend, password degli utenti iniziali, `JWT_SECRET`) senza valori in
  chiaro nei file versionati, come si comporta quando ne manca una, come si
  avvia in locale con il profilo `dev` e come si ruota una credenziale. Questa
  change la crea con gli scenari del profilo `sqlite`; le change successive
  aggiungono gli scenari degli altri profili.

### Modified Capabilities

Nessuna.

## Profili coinvolti

- `sqlite`: coinvolto (oggetto della change), compresa la variante `replit`
  che usa lo stesso adapter.
- `kube`, `aws`, `azure`: non coinvolti in questa change, vedi Non-goals.

## Non-goals

- **Profili `kube`, `aws`, `azure`**: coperti dalle change successive
  `secret-kube`, `secret-aws`, `secret-azure`, ognuna con il proprio secret
  store (Kubernetes Secret, AWS Secrets Manager, Azure Key Vault). Sono
  separate perché ogni profilo ha deploy e strumenti di rotazione diversi e
  `azure` si può verificare solo su cloud.
- **Rimozione del default di `JWT_SECRET` da `application.yml`** (e dei client
  secret OAuth): resta fino alla change di chiusura `secret-default-comuni`.
  Toglierlo adesso fermerebbe i profili non ancora migrati.
- **Lettura dei secret da parte dell'applicazione** (Spring Cloud AWS/Azure):
  scartata in esplorazione. I secret si risolvono al deploy e l'app continua a
  leggere variabili d'ambiente.
- **Rotazione delle password degli utenti già creati**: gli utenti di default
  vengono creati solo se non esistono, quindi cambiare la variabile su un
  database già popolato non cambia la password salvata. Lo script di rotazione
  copre `JWT_SECRET`; per gli utenti la password si cambia dall'applicazione o
  ricreando il database. Va scritto nella documentazione dello script.
- **Refresh dell'access token dopo una rotazione**: emerso in apply, il login
  non emette refresh token e `/api/auth/refresh` non restituisce un nuovo
  access token. Completare il flusso tocca `core` e API ed è lavoro separato,
  registrato in Roadmap.

## Impact

- **Core**: non toccato.
- **adapter-sqlite**: `DataInitializerProperties` (password mascherata nella
  rappresentazione testuale usata dal log) ed eventuale controllo esplicito
  delle password mancanti; test relativi.
- **application**: `application-sqlite.yml` (via i valori in chiaro),
  `application-dev.yml` (valori fissi locali).
- **Script**: `script/sqlite-locale/{docker-compose.yml,start-all.sh,Dockerfile}`
  (il Dockerfile usava un'immagine base `openjdk:17` non più disponibile:
  emerso in apply, corretto per poter verificare la change),
  `.env.example` nuovo, `script/sqlite-ec2/start-all.sh` (user-data), script di
  rotazione nuovi, `script/automatic-test/test-sqlite-onprem.sh`.
- **Repository**: `.gitignore` esclude il `.env` di `sqlite-locale`.
- **Documentazione**: README (avvio locale con `sqlite,dev`, `.env`),
  Roadmap (voce sqlite dello step secret).
- **Dipendenze**: nessuna nuova.
