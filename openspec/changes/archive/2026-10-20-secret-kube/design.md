## Context

Motivazione in `proposal.md`, requisiti in `specs/credenziali-secret/spec.md`.
Stato attuale rilevante per il come:

- La creazione degli utenti iniziali sta in `adapter-sqlite`
  (`DataInitializer` + `DataInitializerProperties`, `@Profile("sqlite")`),
  con il controllo delle password mancanti in un `@PostConstruct` (eseguito
  prima che il web server si metta in ascolto) e la password mascherata nel
  log, come stabilito da `secret-sqlite`. Il frontend JavaFX usa la stessa
  classe con il profilo `sqlite` e una lista utenti vuota.
- Il port `UserRepository` è implementato solo per `sqlite`, `kube`
  (adapter-postgresql) e `aws` (adapter-aws): **il profilo `azure` non ne ha
  uno**. `PasswordEncoder` è un bean del `core` (`JwtConfig`).
- Nel profilo `kube` gli utenti iniziali sono righe di
  `script/init-database/init-postgres.sql` (hash bcrypt fissi, id non usati
  altrove), caricate dal container Postgres solo su volume vuoto.
- `application-kube.yml` legge `POSTGRES_USERNAME`, `POSTGRES_PASSWORD`
  (default `gestione_annotazioni_pass`) e `MONGODB_URI` (default senza
  credenziali). Le credenziali passano da `@ConfigurationProperties`
  (`DataSourceProperties`, Mongo): come verificato in `secret-sqlite`, un
  placeholder senza valore resta testo letterale invece di fallire, quindi
  l'errore arriverebbe come "autenticazione fallita" senza il nome della
  variabile.
- Il `docker-compose.yml` di root non ha uno script di avvio: si lancia con
  `docker-compose up`. I test lo usano con `docker-compose up -d --build` e lo
  smontano con `down --volumes`.
- Minikube: i Deployment hanno i valori in chiaro (`demo`), l'app usa
  `SPRING_DATASOURCE_*` e `SPRING_DATA_MONGODB_URI`, nessun Deployment ha
  probe; `start-all.sh` inizializza i database con `kubectl exec ... mongo -u
  demo -p demo` e `psql -U demo`.
- `script/azure-dbremoti-mongo-runlocale/start-all.sh` avvia l'app con
  `docker run` e il profilo `kube`, passando `SPRING_DATASOURCE_*`, e salva le
  variabili in `.env-azure-dbremoti-mongo-runlocale` (già nel `.gitignore`).

## Goals / Non-Goals

**Goals:**

- Un solo meccanismo nel `core` per gli utenti iniziali, riusabile da `aws` e
  `azure` senza codice nuovo, senza `if` sul profilo.
- Errore all'avvio con il nome della variabile anche per le credenziali dei
  database, prima di qualsiasi tentativo di connessione.
- Stesso elenco di variabili per compose, `docker run`, minikube e `dev`.

**Non-Goals:**

- Nessuna modifica al dominio `User` né ai repository: l'initializer usa il
  port esistente.
- Nessuna auth nuova su Redis e Kafka, nessun cambio a `application.yml` per
  `JWT_SECRET` (resta a `secret-default-comuni`).

## Decisions

### D1. Utenti iniziali nel core, attivi solo se configurati

`DataInitializer` e `DataInitializerProperties` passano da `adapter-sqlite` al
`core` (package `core.config.init`, nomi italiani `UtentiInizialiInitializer`
e `UtentiInizialiProperties`), con lo stesso comportamento: controllo delle
password in `@PostConstruct`, creazione in `ApplicationRunner` solo degli
utenti mancanti, password mascherata nel `toString`.

Il bean è annotato `@ConditionalOnProperty(prefix = "app.init.users", name =
"enabled", havingValue = "true")`: dove la creazione non è attiva il bean non
esiste e non chiede `UserRepository`. Così `azure`, che non implementa il
port, parte come oggi, e nel `core` non c'è nessun riferimento al profilo.

La lista dei tre utenti (username, email, ruolo, password
`${APP_USER_<USERNAME>_PASSWORD:}`) si sposta in `application.yml` con
`enabled: false`; `application-sqlite.yml` e `application-kube.yml` hanno solo
`app.init.users.enabled: ${APP_CREATE_USERS_ENABLED:true}`. Il frontend JavaFX
dichiara `app.init.users.enabled=true` nel suo `application.properties` ma non
include `application.yml` dell'applicazione: lista vuota, nessuna creazione,
come oggi.

I test di `DataInitializerTest`, `DataInitializerPropertiesTest` e
`DataInitializerPropertiesBindingTest` si spostano nel `core` con i nomi
nuovi; più un test di contesto che verifica l'assenza del bean con
`enabled=false`.

Alternative scartate: copia in `adapter-postgresql` (codice duplicato, da
ricopiare per `aws`); `Optional<UserRepository>` nel core (il bean esisterebbe
anche dove non serve e un errore di configurazione passerebbe in silenzio).

### D2. Credenziali obbligatorie dichiarate per profilo, verificate prima del contesto

Nuovo `EnvironmentPostProcessor` nel `core` (registrato in
`META-INF/spring.factories`), eseguito prima della creazione del contesto: legge
la proprietà `app.credenziali.obbligatorie` (lista di nomi di variabili) e, se
una di esse è assente o vuota nell'ambiente risolto, interrompe l'avvio con
`Credenziale mancante: <NOMI>`. `application-kube.yml` dichiara
`POSTGRES_PASSWORD` e `MONGODB_URI`. Il post-processor ha ordine
`Ordered.LOWEST_PRECEDENCE`, quindi gira dopo quello di Spring Boot che carica
`application*.yml` (altrimenti non vedrebbe né la lista né i valori dei
profili). Il valore del profilo `dev` conta come
presente (le chiavi di primo livello di `application-dev.yml` sono proprietà
dell'ambiente), quindi `kube,dev` parte.

È generico: `aws` e `azure` dichiareranno le loro variabili nelle rispettive
change, e `secret-default-comuni` potrà aggiungere `JWT_SECRET` a tutti i
profili con una riga.

Alternativa scartata: un bean con `@PostConstruct`. L'ordine rispetto alla
creazione di `DataSource` ed `EntityManagerFactory` non è garantito, e
l'errore di connessione arriverebbe prima del messaggio con il nome della
variabile.

### D3. Valori e nomi delle variabili

| Variabile | Usata da |
|---|---|
| `POSTGRES_USERNAME`, `POSTGRES_PASSWORD` | container Postgres (`POSTGRES_USER`/`POSTGRES_PASSWORD`), app |
| `MONGO_ROOT_USERNAME`, `MONGO_ROOT_PASSWORD` | container Mongo, mongo-express; l'app riceve `MONGODB_URI` composto da questi |
| `MONGO_EXPRESS_USERNAME`, `MONGO_EXPRESS_PASSWORD` | basic auth di mongo-express |
| `JWT_SECRET`, `APP_USER_*_PASSWORD` | app |

`MONGODB_URI` non è un segreto separato: si compone dai due valori di Mongo,
con l'interpolazione di compose (`mongodb://${MONGO_ROOT_USERNAME}:${MONGO_ROOT_PASSWORD}@...`)
e con le dipendenze fra variabili di Kubernetes (`$(MONGO_ROOT_USERNAME)`).
Per questo i valori generati (minikube, test, rotazione) sono esadecimali
(`openssl rand -hex`), senza caratteri da codificare in un URI;
`JWT_SECRET` resta `openssl rand -base64 64`.

`application-dev.yml` riceve i valori locali di oggi: `POSTGRES_PASSWORD:
gestione_annotazioni_pass`, `MONGODB_URI` con `admin:admin123` su
`localhost:27017`.

### D4. docker-compose di root e docker-run: `.env` con controllo di compose

Il `docker-compose.yml` usa l'interpolazione da `.env` (file letto
automaticamente da compose nella root) con la forma
`${POSTGRES_PASSWORD:?crea .env da .env.example}`: senza il valore compose si
ferma prima di creare i container, con il nome della variabile. Nessuna
password resta nel file. `script/docker-run-kube-profile.sh` carica lo stesso
`.env` (`set -a; . ./.env`), si ferma se manca e passa le variabili con `-e`.

Nuovo `.env.example` in root con tutte le variabili di D3 e valori
segnaposto. La regola `.env` del `.gitignore` aggiunta da `secret-sqlite`
copre già il file.

I test usano `docker-compose --env-file <file temporaneo>` (valori generati,
password utenti note ai test: `admin`, `cattivo`, `bellissimo`), così non
leggono né modificano il `.env` di chi sviluppa.

### D5. Minikube: Secret generato se manca, Deployment con `secretKeyRef`

`start-all.sh`, dopo il namespace, crea il Secret
`gestioneannotazioni-credenziali` solo se non esiste: scrive i valori generati
in un file temporaneo (`umask 077`), lo passa a `kubectl create secret generic
--from-env-file` e lo cancella, così i valori non compaiono negli argomenti dei
processi né nell'output. I Deployment di Postgres, Mongo, mongo-express e app
leggono le variabili con `valueFrom.secretKeyRef`; l'app compone
`MONGODB_URI` come in D3 e passa a `POSTGRES_USERNAME`/`POSTGRES_PASSWORD`.

L'inizializzazione dei database usa le variabili già presenti nei pod:
`kubectl exec <pod> -- sh -c 'mongo -u "$MONGO_INITDB_ROOT_USERNAME" -p
"$MONGO_INITDB_ROOT_PASSWORD" ...'` e `psql -U "$POSTGRES_USER"`, quindi le
password non passano mai dall'host. Per leggere le credenziali, `start-all.sh`
stampa il comando `kubectl get secret ... -o jsonpath=... | base64 -d`.

`test-minikube.sh` crea il Secret prima di `start-all.sh`, con valori
generati e le password utenti note ai test: `start-all.sh` lo trova e non lo
rigenera.

### D6. Rotazione: due script con lo stesso scheletro di sqlite

- `script/docker-compose-rotate-secret.sh <VARIABILE>` (per il compose di root,
  che non ha una cartella propria);
- `script/minikube/rotate-secret.sh <VARIABILE>`.

Variabili ammesse: `JWT_SECRET`, `POSTGRES_PASSWORD`, `MONGO_ROOT_PASSWORD`;
`APP_USER_*` rifiutate con il messaggio di `secret-sqlite`. Passi:

1. genera il valore (D3);
2. per le password dei database lo cambia prima sul database, autenticandosi
   con il valore corrente letto dalla sorgente: `ALTER USER ... PASSWORD` via
   `psql` nel container o pod Postgres, `db.changeUserPassword` via `mongo`
   nel container o pod Mongo; il nuovo valore passa con una variabile
   d'ambiente dell'`exec`, non come argomento;
3. aggiorna la sorgente (riga del `.env` con file temporaneo + `mv`; Secret
   con `kubectl create secret ... --dry-run=client -o yaml | kubectl apply -f
   -`);
4. riavvia app e, per Mongo, mongo-express (`docker-compose up -d
   --force-recreate app mongo-express`; `kubectl rollout restart` +
   `rollout status`);
5. attende `UP`: con compose su `http://localhost:8082/actuator/health`; su
   minikube con un `kubectl port-forward` temporaneo verso il Service
   dell'app, perché i Deployment non hanno probe.

Le connessioni già aperte nel pool sopravvivono al cambio di password sul
database; le nuove usano il valore nuovo dopo il riavvio dell'app.

### D7. init-postgres.sql senza utenti

Il blocco `INSERT INTO users` viene rimosso; schema e dati di esempio restano.
Il commento con il comando `bcrypt` per generare gli hash viene tolto insieme
agli hash.

### D8. azure-dbremoti-mongo-runlocale

Lo script passa all'app `POSTGRES_USERNAME`/`POSTGRES_PASSWORD` al posto di
`SPRING_DATASOURCE_USERNAME`/`PASSWORD` (richiesto da D2) e aggiunge
`JWT_SECRET` e le tre `APP_USER_*_PASSWORD`. Se il suo file
`.env-azure-dbremoti-mongo-runlocale` non le contiene già, le genera e le
aggiunge, con la stessa logica "genera se manca" di minikube. Verifica: `bash
-n` e dry-run con stub di `az` e `docker`; il test su Azure è manuale.

## Risks / Trade-offs

- [Volumi esistenti di Postgres (compose o PVC di minikube) contengono ancora
  gli utenti dell'SQL con le password pubbliche] → documentato nel README:
  per applicare le password nuove serve un volume vuoto (`docker-compose down
  -v`, cancellazione della PVC). L'initializer non tocca utenti esistenti per
  requisito.
- [Spostare l'initializer nel core cambia il codice di `sqlite`] → stessi
  test spostati, regressione `test-sqlite-onprem.sh` alla fine; nessun cambio
  di comportamento osservabile.
- [`EnvironmentPostProcessor` è un'estensione meno nota] → classe piccola,
  commentata, con test unitario su un `MockEnvironment`.
- [La rotazione su minikube dipende da `kubectl port-forward` per la verifica
  di salute] → timeout esplicito e chiusura del port-forward in un `trap`.
- [`azure-dbremoti-mongo-runlocale` non è verificabile in locale] → dry-run con
  stub; test manuale su Azure annotato nei task.
- [Test più lenti: `test-minikube.sh` e il compose kube sono già i test più
  lunghi della regressione] → nessun avvio aggiuntivo: i test riusano
  l'avvio che fanno già.

## Migration Plan

1. Build e test JUnit (`core`, `adapter-sqlite`).
2. Chi usa il compose di root crea `.env` da `.env.example`; per applicare le
   password nuove a un volume esistente `docker-compose down -v`.
3. Su minikube il primo `start-all.sh` crea il Secret; su un cluster con PVC
   esistenti valgono le password vecchie fino alla loro cancellazione.

Rollback: ripristinare manifest, compose, yml e `init-postgres.sql` dalla
versione precedente; l'initializer del `core` si disattiva con
`APP_CREATE_USERS_ENABLED=false`.
