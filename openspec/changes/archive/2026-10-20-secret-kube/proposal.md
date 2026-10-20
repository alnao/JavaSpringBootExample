## Why

Nel profilo `kube` tutte le credenziali stanno in chiaro nei file versionati:
la password di Postgres e quella dell'utente root di Mongo nel
`docker-compose.yml` di root (anche dentro `MONGODB_URI` dell'applicazione),
`demo/demo` in ogni Deployment di `script/minikube` e nei comandi di
`script/minikube/start-all.sh`, le stesse password del compose in
`script/docker-run-kube-profile.sh`. Gli utenti iniziali (`admin/admin`,
`moderatore/cattivo`, `alnao/bellissimo`) sono hash bcrypt fissi in
`script/init-database/init-postgres.sql`, quindi le loro password sono
pubbliche. Nessun deploy `kube` passa `JWT_SECRET`, per cui i token sono
firmati con la chiave pubblicata in `application.yml`: chi si registra può
firmarsi un token con ruolo `ADMIN`.

È la seconda delle change dello step "Gestione password via secret"
(Roadmap), dopo `secret-sqlite`, di cui riprende il pattern: valori fissi solo
nel profilo `dev`, fail-fast se manca una credenziale, script di rotazione.

## What Changes

- **BREAKING** Utenti iniziali del profilo `kube`: escono da
  `init-postgres.sql` e vengono creati all'avvio dall'applicazione, con le
  password da `APP_USER_ADMIN_PASSWORD`, `APP_USER_MODERATORE_PASSWORD` e
  `APP_USER_ALNAO_PASSWORD`. Se la creazione è attiva e una password manca,
  l'applicazione non parte e indica la variabile mancante.
- La creazione degli utenti iniziali si sposta da `adapter-sqlite` al `core`,
  indipendente dal profilo e attivata da configurazione: `sqlite` la usa con
  lo stesso comportamento di oggi, `kube` la attiva, `aws` e `azure` potranno
  riusarla nelle loro change.
- **BREAKING** `application-kube.yml` non ha più default in chiaro per le
  credenziali (password di Postgres, URI di Mongo): i valori locali fissi
  stanno solo nel profilo `dev`, come per `sqlite`.
- `docker-compose.yml` di root e `script/docker-run-kube-profile.sh`: le
  credenziali (Postgres, Mongo, mongo-express, `JWT_SECRET`, password degli
  utenti iniziali) arrivano da un file `.env` non versionato nella root, con un
  `.env.example` versionato. Lo stesso valore alimenta sia il container del
  database sia l'applicazione.
- `script/minikube`: le credenziali stanno in un **Kubernetes Secret**, che
  `start-all.sh` crea con valori casuali se non esiste ancora; i Deployment lo
  leggono con `secretKeyRef` e non contengono più valori. I comandi di
  inizializzazione dei database leggono le credenziali dal Secret.
- Nomi delle variabili allineati: anche i Deployment di minikube usano
  `POSTGRES_USERNAME`, `POSTGRES_PASSWORD` e `MONGODB_URI` come
  `application-kube.yml` e il compose, al posto di `SPRING_DATASOURCE_*` e
  `SPRING_DATA_MONGODB_URI`.
- Script di rotazione per compose e minikube: `JWT_SECRET` e password dei
  database (Postgres, utente root di Mongo), aggiornate anche sul database
  prima di riavviare l'applicazione.
- `script/azure-dbremoti-mongo-runlocale/start-all.sh`, che usa il profilo
  `kube` e `init-postgres.sql`, passa anche `JWT_SECRET` e le password degli
  utenti iniziali, altrimenti il fail-fast ne fermerebbe l'avvio.
- Test automatici `kube` (`test-kube-onprem-docker-compose.sh`,
  `test-minikube.sh`) adeguati: usano credenziali di test generate o note,
  senza toccare il `.env` o il Secret di chi sviluppa.

## Capabilities

### New Capabilities

Nessuna.

### Modified Capabilities

- `credenziali-secret`: i requisiti esistenti ricevono gli scenari del
  profilo `kube` (compose e minikube); la rotazione copre anche le password
  dei database.

## Profili coinvolti

- `kube`: coinvolto (oggetto della change), in docker-compose, con
  `docker-run-kube-profile.sh` e su minikube.
- `sqlite`: coinvolto solo per lo spostamento della creazione degli utenti
  iniziali nel `core`, senza cambiare comportamento.
- `azure`: coinvolto solo per `script/azure-dbremoti-mongo-runlocale`, che
  avvia l'applicazione con il profilo `kube`.
- `aws`: non coinvolto, vedi Non-goals.

Tocca il **core** (creazione degli utenti iniziali, oggi in `adapter-sqlite`)
oltre a `adapter-sqlite`, configurazione dell'applicazione, script e test.

## Non-goals

- **Profilo `aws` e resto di `azure`**: coperti da `secret-aws` e
  `secret-azure`. In particolare gli utenti iniziali di `aws` e `azure`
  restano negli SQL di init finché quelle change non attivano la creazione dal
  `core`.
- **Autenticazione su Redis e Kafka**: oggi non ne hanno (Kafka in
  PLAINTEXT); attivarla è una modifica funzionale separata, come deciso nello
  step.
- **Rimozione del default di `JWT_SECRET` da `application.yml`**: resta fino
  alla change di chiusura `secret-default-comuni`.
- **Rotazione delle password degli utenti già creati**: come in `sqlite`, gli
  utenti iniziali si creano solo se non esistono; cambiare la variabile non
  cambia la password salvata.
- **Cartella `script/azure-onprem-non-funziona`**: fuori perimetro come nelle
  change precedenti.
- **Lettura dei secret da parte dell'applicazione**: i secret si risolvono al
  deploy (Secret montato come variabili d'ambiente, `.env` di compose).

## Impact

- **core**: nuova creazione degli utenti iniziali condivisa (configurazione,
  initializer con controllo delle password mancanti), test spostati da
  `adapter-sqlite`.
- **adapter-sqlite**: rimozione di `DataInitializer` e
  `DataInitializerProperties`, sostituiti dal `core`.
- **application**: `application-kube.yml` (niente default in chiaro, utenti
  iniziali dichiarati), `application-sqlite.yml` (stessa configurazione degli
  utenti, ora letta dal `core`), `application-dev.yml` (valori fissi `kube`).
- **Script e configurazione**: `docker-compose.yml`, `.env.example` di root,
  `script/docker-run-kube-profile.sh`, `script/minikube/*` (Secret,
  Deployment, `start-all.sh`, rotazione), `script/init-database/init-postgres.sql`
  (senza utenti), `script/azure-dbremoti-mongo-runlocale/start-all.sh`.
- **Test**: `test-kube-onprem-docker-compose.sh`, `test-minikube.sh`, test JUnit
  dell'initializer.
- **Documentazione**: README (kube con compose e minikube), Roadmap.
- **Dipendenze**: nessuna nuova.
- **Costi**: nessuno durante la change. Il test di
  `azure-dbremoti-mongo-runlocale` crea risorse su Azure e resta un task
  manuale dell'utente.
