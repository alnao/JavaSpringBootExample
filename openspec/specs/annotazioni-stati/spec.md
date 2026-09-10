# Annotazioni Stati Specification

## Purpose

Definisce il ciclo di vita di un'annotazione: quali stati puo' assumere, quali
transizioni sono consentite a quale ruolo utente, e come ogni cambio di stato
viene tracciato nello storico. E' il contratto che rende il workflow di
approvazione identico su tutti i profili di deployment.

## Requirements

### Requirement: Insieme chiuso degli stati

Un'annotazione SHALL trovarsi sempre in esattamente uno dei seguenti stati:
`INSERITA`, `MODIFICATA`, `IMPORTATA`, `CONFERMATA`, `RIFIUTATA`, `DAINVIARE`,
`INVIATA`, `SCADUTA`, `BANNATA`, `ERRORE`.

Una richiesta di cambio stato verso un valore fuori da questo insieme SHALL
essere rifiutata senza modificare l'annotazione.

#### Scenario: Stato di destinazione inesistente

- **WHEN** viene richiesto un cambio stato verso un valore non appartenente
  all'insieme (ad esempio `APPROVATA`)
- **THEN** la richiesta e' rifiutata con HTTP 400
- **AND** lo stato dell'annotazione resta invariato
- **AND** nessuna riga viene aggiunta allo storico stati

#### Scenario: Annotazione appena creata

- **WHEN** un utente crea una nuova annotazione
- **THEN** l'annotazione risulta in stato `INSERITA`

### Requirement: Transizioni consentite per ruolo

Il sistema SHALL consentire un cambio di stato solo se la coppia
(stato di partenza, stato di arrivo) e' dichiarata come consentita per il ruolo
dell'utente richiedente. Le combinazioni consentite sono dati di configurazione,
non codice, e SHALL essere modificabili senza rilasciare una nuova versione
dell'applicativo.

Una transizione non consentita SHALL essere rifiutata con HTTP 403 e SHALL
lasciare invariati sia lo stato dell'annotazione sia lo storico.

#### Scenario: Moderatore conferma un'annotazione inserita

- **WHEN** un utente con ruolo `MODERATOR` richiede la transizione
  `INSERITA` → `CONFERMATA`
- **THEN** la richiesta ha successo con HTTP 200
- **AND** l'annotazione risulta in stato `CONFERMATA`

#### Scenario: Admin tenta di riportare una confermata a modificata

- **WHEN** un utente con ruolo `ADMIN` richiede la transizione
  `CONFERMATA` → `MODIFICATA`
- **THEN** la richiesta e' rifiutata con HTTP 403
- **AND** l'annotazione resta in stato `CONFERMATA`

#### Scenario: Utente semplice tenta di confermare

- **WHEN** un utente con ruolo `USER` richiede la transizione
  `INSERITA` → `CONFERMATA`
- **THEN** la richiesta e' rifiutata con HTTP 403

#### Scenario: Transizione verso lo stesso stato

- **WHEN** viene richiesta una transizione il cui stato di arrivo coincide con
  quello di partenza
- **THEN** la richiesta e' accettata indipendentemente dal ruolo

### Requirement: Gerarchia dei ruoli

Il sistema SHALL applicare una gerarchia fra i ruoli nella valutazione delle
transizioni: un ruolo superiore eredita le transizioni consentite ai ruoli
inferiori. L'ordine e' `ADMIN` > `MODERATOR` > `USER`.

Il ruolo `SYSTEM` SHALL restare fuori dalla gerarchia: le transizioni riservate
a `SYSTEM` non sono eseguibili da nessun ruolo umano, incluso `ADMIN`.

#### Scenario: Admin esegue una transizione dichiarata per USER

- **WHEN** un utente con ruolo `ADMIN` richiede una transizione dichiarata come
  consentita al ruolo `USER`
- **THEN** la richiesta ha successo

#### Scenario: Admin tenta una transizione riservata a SYSTEM

- **WHEN** un utente con ruolo `ADMIN` richiede una transizione dichiarata come
  consentita al solo ruolo `SYSTEM`
- **THEN** la richiesta e' rifiutata con HTTP 403

#### Scenario: Moderatore tenta una transizione riservata ad ADMIN

- **WHEN** un utente con ruolo `MODERATOR` richiede la transizione
  `CONFERMATA` → `DAINVIARE`, dichiarata per il ruolo `ADMIN`
- **THEN** la richiesta e' rifiutata con HTTP 403

### Requirement: Tracciamento nello storico stati

Ogni cambio di stato andato a buon fine SHALL produrre una riga nello storico
stati contenente: identificativo dell'annotazione, versione della nota al momento
del cambio, stato di partenza, stato di arrivo, utente richiedente, data e ora,
e una nota descrittiva dell'operazione.

Lo storico SHALL essere append-only: le righe gia' scritte non vengono
aggiornate ne' cancellate dai cambi di stato successivi.

#### Scenario: Storico di un'annotazione confermata

- **WHEN** un'annotazione passa da `INSERITA` a `CONFERMATA` per mano
  dell'utente `moderatore1`
- **THEN** lo storico contiene una nuova riga con stato di partenza `INSERITA`,
  stato di arrivo `CONFERMATA` e utente `moderatore1`
- **AND** le righe preesistenti dello storico sono invariate

#### Scenario: Nessuno storico per una transizione rifiutata

- **WHEN** una richiesta di cambio stato viene rifiutata con HTTP 403
- **THEN** lo storico dell'annotazione non contiene nuove righe

### Requirement: Confine fra operazioni utente e operazioni di sistema

`DAINVIARE` SHALL essere l'ultimo stato raggiungibile tramite API REST o
interfaccia web: rappresenta la volonta' di un amministratore di inviare
l'annotazione a sistemi esterni.

Lo stato `INVIATA` SHALL essere impostato esclusivamente dal processo schedulato
di export, a valle dell'invio effettivo sulla coda del profilo attivo.

#### Scenario: Admin marca un'annotazione per l'invio

- **WHEN** un utente con ruolo `ADMIN` richiede la transizione
  `CONFERMATA` → `DAINVIARE`
- **THEN** la richiesta ha successo e l'annotazione risulta in `DAINVIARE`
- **AND** l'annotazione non risulta ancora inviata a sistemi esterni

#### Scenario: Passaggio a INVIATA da parte dello scheduler

- **WHEN** lo scheduler di export invia con successo su coda un'annotazione in
  stato `DAINVIARE`
- **THEN** l'annotazione risulta in stato `INVIATA`
- **AND** lo storico contiene la riga `DAINVIARE` → `INVIATA`

#### Scenario: Tentativo di impostare INVIATA via API

- **WHEN** un utente di qualsiasi ruolo richiede via API la transizione
  `DAINVIARE` → `INVIATA`
- **THEN** la richiesta e' rifiutata con HTTP 403

### Requirement: Utente richiedente identificato

Un cambio di stato SHALL essere eseguito solo se l'utente richiedente esiste ed
e' possibile determinarne il ruolo. Una richiesta che riporta un utente
sconosciuto SHALL essere rifiutata senza modificare l'annotazione.

#### Scenario: Utente inesistente

- **WHEN** viene richiesto un cambio stato indicando un utente non presente in
  anagrafica
- **THEN** la richiesta e' rifiutata con HTTP 400
- **AND** lo stato dell'annotazione resta invariato

### Requirement: Elenco delle transizioni consultabile

Il sistema SHALL esporre l'elenco completo delle transizioni configurate, con
stato di partenza, stato di arrivo, ruolo richiesto e descrizione, cosi' che i
client possano presentare all'utente le sole azioni disponibili.

#### Scenario: Consultazione delle transizioni

- **WHEN** un client richiede l'elenco delle transizioni di stato
- **THEN** riceve HTTP 200 con l'elenco delle transizioni configurate
- **AND** ogni voce riporta stato di partenza, stato di arrivo, ruolo richiesto
  e descrizione

### Requirement: Comportamento uniforme su tutti i profili

Le regole di transizione, i codici di errore e il contenuto dello storico stati
SHALL essere identici su tutti i profili di deployment (`sqlite`, `kube`, `aws`,
`azure`), indipendentemente dalla tecnologia di persistenza sottostante.

#### Scenario: Stessa transizione rifiutata su ogni profilo

- **WHEN** la stessa transizione non consentita viene richiesta su ciascuno dei
  quattro profili
- **THEN** ogni profilo risponde HTTP 403
- **AND** in nessun profilo l'annotazione o lo storico vengono modificati

### Requirement: Configurazione delle transizioni obbligatoria all'avvio

Le transizioni di stato consentite SHALL essere caricate da configurazione
all'avvio dell'applicazione. Se il caricamento non produce un insieme di
transizioni valido, l'applicazione SHALL interrompere l'avvio con un errore
esplicito invece di rendersi disponibile in stato degradato.

Sono considerati caricamento non valido: configurazione assente, sintassi non
interpretabile, riferimento a uno stato o a un ruolo non riconosciuto, e insieme
di transizioni vuoto.

Il messaggio di errore SHALL indicare la natura del problema e, quando il
problema riguarda una singola voce, quale voce lo ha causato.

Il sistema NON SHALL disporre di un insieme di transizioni di ripiego definito
nel codice: le regole di workflow restano descritte esclusivamente in
configurazione.

#### Scenario: Configurazione assente

- **WHEN** l'applicazione viene avviata e la configurazione delle transizioni non
  e' reperibile
- **THEN** l'avvio termina con errore
- **AND** il log riporta che la configurazione delle transizioni non e' stata
  trovata
- **AND** nessun endpoint dell'applicazione risulta raggiungibile

#### Scenario: Configurazione sintatticamente non valida

- **WHEN** l'applicazione viene avviata e la configurazione delle transizioni non
  e' interpretabile
- **THEN** l'avvio termina con errore riportando la causa

#### Scenario: Riferimento a uno stato inesistente

- **WHEN** la configurazione contiene una transizione che indica uno stato non
  appartenente all'insieme degli stati ammessi
- **THEN** l'avvio termina con errore
- **AND** il messaggio identifica la voce di configurazione responsabile

#### Scenario: Riferimento a un ruolo inesistente

- **WHEN** la configurazione contiene una transizione che indica un ruolo utente
  non riconosciuto
- **THEN** l'avvio termina con errore
- **AND** il messaggio identifica la voce di configurazione responsabile

#### Scenario: Configurazione vuota

- **WHEN** la configurazione si carica correttamente ma non dichiara alcuna
  transizione
- **THEN** l'avvio termina con errore

#### Scenario: Configurazione valida

- **WHEN** l'applicazione viene avviata con una configurazione valida
- **THEN** l'avvio si completa
- **AND** il log riporta il numero di transizioni caricate
- **AND** i cambi di stato si comportano secondo le transizioni configurate

#### Scenario: Stesso comportamento su ogni profilo

- **WHEN** l'applicazione viene avviata con configurazione non valida su ciascuno
  dei profili `sqlite`, `kube`, `aws` e `azure`
- **THEN** in ogni profilo l'avvio termina con errore
- **AND** in nessun profilo l'applicazione accetta richieste di cambio stato
