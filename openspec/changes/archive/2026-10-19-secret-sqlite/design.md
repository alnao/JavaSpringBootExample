## Context

Motivazione e perimetro in `proposal.md`; requisiti in
`specs/credenziali-secret/spec.md`. Stato attuale rilevante per il come:

- Gli utenti iniziali del profilo `sqlite` sono dichiarati in
  `application-sqlite.yml` (`app.init.users.default-users`), legati a
  `DataInitializerProperties` (`@ConfigurationProperties`, adapter-sqlite) e
  creati da `DataInitializer` (`ApplicationRunner`) solo se lo username non
  esiste già. `DataInitializer` logga a INFO l'intero oggetto properties, il
  cui `toString()` include le password.
- `JWT_SECRET` ha un default in `application.yml`; nessuno script lo passa.
- Il profilo `dev` (`application-dev.yml`) oggi contiene solo logging e
  actuator e nessuno script lo attiva.
- Il flusso di refresh non è completo (verificato in apply): il login non
  emette refresh token e `/api/auth/refresh` restituisce il refresh token
  ricevuto invece di un nuovo access token. Dopo una rotazione di
  `JWT_SECRET` l'unico modo per avere un token valido è un nuovo login.
- `sqlite-locale` usa docker-compose con `environment:` in chiaro;
  `sqlite-ec2` passa le variabili con `docker run -e` dentro uno user-data
  racchiuso in heredoc con `'EOF'` (nessuna espansione lato chi lancia).
- `/actuator/env` è esposto ma richiede autenticazione; Spring Boot 3 maschera
  i valori per default (`show-values: never`) e la configurazione attuale non
  lo cambia.

## Goals / Non-Goals

**Goals:**

- Fissare il pattern che `kube`, `aws` e `azure` replicheranno: nomi delle
  variabili, ruolo di `dev`, file di credenziali non versionato, script di
  rotazione "ruota, riavvia, attendi UP".
- Nessun cambiamento nel `core` e nessuna dipendenza nuova.

**Non-Goals:**

- Nessun meccanismo generico di validazione delle credenziali per tutti i
  profili: il controllo di questa change riguarda solo le password degli
  utenti iniziali di `sqlite`. Il fail-fast su `JWT_SECRET` arriva con
  `secret-default-comuni`.
- Nessuna modifica a `DataInitializer` per aggiornare utenti già esistenti.

## Decisions

### D1. Password degli utenti: default vuoto esplicito + controllo all'avvio

In `application-sqlite.yml` ogni password diventa
`${APP_USER_<USERNAME>_PASSWORD:}` (default vuoto esplicito). All'avvio, se
`app.init.users.enabled` è vero, adapter-sqlite verifica che ogni utente
iniziale abbia una password non vuota; altrimenti lancia un'eccezione che
ferma il contesto Spring con il messaggio
`Credenziale mancante: APP_USER_<USERNAME>_PASSWORD`. Il nome della variabile
si ricava dallo username in maiuscolo, per convenzione.

Il controllo sta nell'adapter, accanto a chi usa le password: è logica di
avvio del profilo `sqlite`, non dominio, e il `core` resta intatto.

Alternative scartate:
- *Placeholder senza default* (`${APP_USER_ADMIN_PASSWORD}`): il binder delle
  `@ConfigurationProperties` lascia il testo letterale del placeholder invece
  di fallire, e la password diventerebbe la stringa `${...}`, nota a tutti.
  Confermato in apply da `DataInitializerPropertiesBindingTest`: senza default
  la password vale `${APP_USER_..._PASSWORD}`, con default vuoto vale `""`.
- *Bean Validation (`@Validated` + `@NotBlank`)*: valida anche quando la
  creazione utenti è disattivata e il messaggio non riporta il nome della
  variabile d'ambiente.

### D2. Valori fissi nel profilo dev, con il nome della variabile

`application-dev.yml` dichiara chiavi di primo livello con lo stesso nome
delle variabili (`JWT_SECRET`, `APP_USER_ADMIN_PASSWORD`, ...). I placeholder
`${APP_USER_ADMIN_PASSWORD:}` le risolvono da qualsiasi property source e le
variabili d'ambiente hanno precedenza sui file yml, quindi lo scenario
"variabile prevale su dev" è garantito dall'ordine standard di Spring Boot,
senza codice.

Le change successive aggiungeranno qui le credenziali dei propri profili
(`POSTGRES_PASSWORD`, `AWS_RDS_PASSWORD`, ...): un solo file, un solo posto
dove stanno i valori pubblici.

Alternativa scartata: un `application-sqlite-dev.yml` per ogni profilo.
Moltiplica i file e i profili da ricordare; i nomi delle variabili sono già
distinti per profilo e non collidono.

`JWT_SECRET` in `dev` riprende il valore oggi in `application.yml`, che resta
dov'è fino a `secret-default-comuni`.

### D3. sqlite-locale: `.env` obbligatorio, letto da compose

`docker-compose.yml` sostituisce le password in `environment:` con
`env_file: .env` e mantiene in `environment:` solo le variabili non segrete.
`start-all.sh` controlla che `.env` esista accanto al compose e altrimenti
termina indicando `cp .env.example .env`. `.env.example` elenca le quattro
variabili con valori segnaposto (`cambiami-...`) e una riga di commento su
come generare `JWT_SECRET` (`openssl rand -base64 64`).

In `.gitignore` si aggiunge una regola generica `.env` (con
`!.env.example`): vale anche per i compose dei profili successivi.

Alternativa scartata: generare `.env` automaticamente se manca. È più comodo,
ma nasconde il passaggio che la change vuole rendere esplicito e lascia
l'utente senza sapere la password di `admin`.

### D4. sqlite-ec2: file generato sull'istanza, script di avvio riusabile

Lo user-data (heredoc non espanso, eseguito come root) fa:

1. `umask 077`, crea `/etc/gestioneannotazioni/`;
2. se `app.env` non esiste, lo genera con `openssl rand`: `JWT_SECRET` da 64
   byte in base64 (>= 512 bit, requisito di HS512) e una password da 18 byte
   in base64 per ogni utente. File `600`, owner root;
3. scrive `/etc/gestioneannotazioni/run-app.sh`, che rimuove il container se
   esiste e lo avvia con `docker run --env-file /etc/gestioneannotazioni/app.env`
   più le variabili non segrete di oggi;
4. lancia `run-app.sh`.

Nessun valore viene stampato: l'output dello user-data finisce in
`/var/log/cloud-init-output.log`. `start-all.sh`, in chiusura, stampa il
comando ssh per leggere il file (`sudo cat /etc/gestioneannotazioni/app.env`).

`run-app.sh` esiste perché il riavvio dopo una rotazione deve ricreare il
container con esattamente gli stessi parametri: averli in un solo file
sull'istanza evita di duplicare il `docker run` nello script di rotazione.

Alternativa scartata: valori generati da `start-all.sh` e scritti nello
user-data. Lo user-data è leggibile con `ec2:DescribeInstanceAttribute` e
dall'IMDS dell'istanza, quindi le credenziali uscirebbero dal perimetro.

### D5. Rotazione: un `rotate-secret.sh` per deploy, solo `JWT_SECRET`

Firma comune per tutti i profili: `rotate-secret.sh <NOME_VARIABILE>`. Passi:

1. rifiuta chiavi diverse da `JWT_SECRET` (in `sqlite` non esiste una
   sorgente esterna da aggiornare, e la password di un utente già creato non
   cambia al riavvio: messaggio esplicito);
2. genera il nuovo valore con `openssl rand -base64 64`;
3. aggiorna la riga nel file di credenziali (`sed` sulla sola riga
   `JWT_SECRET=`, file temporaneo + `mv` per non lasciarlo a metà);
4. ricrea il container: in locale
   `docker-compose up -d --force-recreate spring-app`, su EC2 `run-app.sh` via
   ssh. `docker restart` non basta, perché le variabili d'ambiente di un
   container si fissano alla creazione;
5. attende `GET /actuator/health` = `UP` (polling, timeout configurabile) e
   termina con errore se non arriva.

Su EC2 lo script ritrova istanza e IP con il tag marcatore
`gestioneannotazioni-sqlite-ec2-app=true`, come gli script esistenti, e usa
la key pair `.pem` creata da `start-all.sh`.

Lo scheletro (passi 1, 2, 5) è lo stesso che useranno `kube`, `aws` e
`azure`; cambiano solo il passo 3 (store) e il passo 4 (riavvio), più il
passo "aggiorna la sorgente" per le password dei database.

### D6. Password mascherata nei log

`UserConfig.toString()` restituisce `password='****'`. Il log di
`DataInitializer` resta com'è (utile per vedere username, email e ruoli) e
smette di esporre le password. È la correzione più piccola che soddisfa la
spec, senza toccare i punti di log.

### D7. Controllo "nessun deploy attiva dev"

`test-sqlite-onprem.sh` aggiunge un passo registrato con `test_ok`/`test_ko`:
cerca in `script/` (escluso `script/automatic-test/`), in `Dockerfile` e nei
`docker-compose*.yml` le attivazioni del profilo `dev`
(`SPRING_PROFILES_ACTIVE`, `spring.profiles.active`,
`-Dspring-boot.run.profiles`, `--spring.profiles.active`) e fallisce elencando
i file trovati. Sta nel test `sqlite` perché è il primo profilo migrato; le
change successive non devono ripeterlo.

`test-sqlite-onprem.sh` avvia l'app con `--spring.profiles.active=sqlite,dev`
e continua a usare `admin/admin`. `test-prenotazione-annotazione.sh` non
cambia in questa change.

Emerso in apply: `application-dev.yml` disattiva l'export
(`gestione-annotazioni.export-annotazioni.enabled: false`), mentre
`test-sqlite-onprem.sh` verifica proprio l'export in `annotazioni_inviate`.
Il test aggiunge quindi `--gestione-annotazioni.export-annotazioni.enabled=true`
sulla riga di comando (precedenza massima), invece di cambiare il
comportamento di `dev` per chi sviluppa.

## Risks / Trade-offs

- [Volume `sqlite-data` già popolato in locale: gli utenti esistenti
  mantengono le password vecchie (`admin/admin`) anche con un `.env` nuovo]
  → README e messaggio finale di `start-all.sh` indicano
  `docker-compose down -v` per ripartire da un database vuoto.
- [Il profilo `dev` attiva anche logging DEBUG/TRACE e tutti gli endpoint
  actuator: nei test locali i log diventano più verbosi] → accettato, è il
  comportamento atteso in sviluppo; il controllo D7 impedisce che `dev` arrivi
  in un deploy.
- [Rotazione su EC2 via ssh: il security group apre la 22 solo all'IP di chi
  ha lanciato `start-all.sh`; se l'IP è cambiato lo script fallisce] → lo
  script verifica la raggiungibilità prima di modificare il file e, in caso
  di errore, indica la regola da aggiornare. Nessuna modifica automatica al
  security group.
- [`.env.example` copiato senza cambiare i segnaposto: l'app parte con
  password note] → accettato: il segnaposto è esplicito (`cambiami-...`) e il
  README lo segnala. Rifiutare i segnaposto all'avvio sarebbe logica in più
  solo per questo caso.
- [Finestra di indisponibilità durante la rotazione, il tempo di ricreare un
  container] → accettata: `sqlite` è a istanza singola per natura.

## Migration Plan

1. Build e test con i profili `sqlite,dev`: comportamento invariato per chi
   sviluppa in locale.
2. `sqlite-locale`: chi lo usa crea `.env` da `.env.example` prima del
   prossimo `start-all.sh`; per applicare le password nuove serve un database
   vuoto (`docker-compose down -v`).
3. `sqlite-ec2`: nessuna migrazione, ogni avvio crea un'istanza nuova.

Rollback: ripristinare i valori in `application-sqlite.yml` e le variabili in
chiaro negli script. Nessun dato da migrare.

## Open Questions

- Timeout e intervallo del polling di health negli script di rotazione: si
  fissano in apply prendendo i valori già usati dagli script di test.
