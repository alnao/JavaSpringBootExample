# Annotazioni Lock Specification

## Purpose

Definisce la prenotazione esclusiva di un'annotazione in modifica: come un utente
acquisisce, mantiene e rilascia il diritto esclusivo di modificarla, e cosa
succede a chi tenta di modificarla nel frattempo. Serve a impedire che due utenti
sovrascrivano a vicenda le proprie modifiche.

## Requirements

### Requirement: Prenotazione esclusiva per la modifica

Un utente SHALL poter prenotare un'annotazione per la modifica. Finche' la
prenotazione e' attiva, nessun altro utente SHALL poterla prenotare.

Un tentativo di prenotazione su un'annotazione gia' prenotata da altri SHALL
essere rifiutato con HTTP 409 e SHALL indicare l'utente che detiene la
prenotazione.

#### Scenario: Prima prenotazione

- **WHEN** un utente prenota un'annotazione libera
- **THEN** riceve HTTP 200 con l'istante di acquisizione e quello di scadenza
- **AND** l'annotazione risulta prenotata a suo nome

#### Scenario: Prenotazione contesa

- **WHEN** l'utente `mario` prenota un'annotazione gia' prenotata da `luigi`
- **THEN** `mario` riceve HTTP 409 con codice errore `ANNOTATION_ALREADY_LOCKED`
- **AND** il messaggio riporta che l'annotazione e' in modifica da `luigi`
- **AND** la prenotazione di `luigi` resta valida e invariata

#### Scenario: Prenotazione ripetuta dallo stesso utente

- **WHEN** un utente prenota un'annotazione che ha gia' prenotato lui stesso
- **THEN** riceve HTTP 200
- **AND** la prenotazione resta a suo nome con la scadenza prolungata

### Requirement: Scadenza automatica della prenotazione

Una prenotazione SHALL avere una durata massima, oltre la quale decade
automaticamente senza bisogno di alcun intervento. La durata di default SHALL
essere configurabile per ambiente tramite variabile d'ambiente, e il richiedente
SHALL poterla indicare esplicitamente al momento della prenotazione.

La scadenza automatica serve a evitare che un client che si interrompe lasci
un'annotazione bloccata a tempo indeterminato.

#### Scenario: Prenotazione scaduta

- **WHEN** e' trascorso il tempo di validita' di una prenotazione senza che
  l'utente l'abbia rilasciata
- **THEN** l'annotazione risulta non prenotata
- **AND** un altro utente puo' prenotarla con successo

#### Scenario: Durata esplicita

- **WHEN** un utente prenota un'annotazione indicando una durata di 120 secondi
- **THEN** l'istante di scadenza restituito e' 120 secondi dopo l'acquisizione

#### Scenario: Durata di default

- **WHEN** un utente prenota un'annotazione senza indicare una durata
- **THEN** viene applicata la durata di default configurata per l'ambiente

### Requirement: Rilascio riservato al proprietario

Solo l'utente che detiene una prenotazione SHALL poterla rilasciare. Il
tentativo di rilascio da parte di un altro utente SHALL essere rifiutato e SHALL
lasciare la prenotazione attiva.

#### Scenario: Rilascio da parte del proprietario

- **WHEN** l'utente che detiene la prenotazione la rilascia
- **THEN** riceve HTTP 204
- **AND** l'annotazione risulta immediatamente non prenotata

#### Scenario: Rilascio da parte di un estraneo

- **WHEN** l'utente `mario` tenta di rilasciare una prenotazione detenuta da
  `luigi`
- **THEN** `mario` riceve HTTP 409 con codice errore `NOT_LOCK_OWNER`
- **AND** la prenotazione di `luigi` resta attiva

### Requirement: Modifica consentita solo al proprietario della prenotazione

Se un'annotazione e' prenotata, il sistema SHALL rifiutare con HTTP 409 ogni
richiesta di modifica proveniente da un utente diverso dal proprietario della
prenotazione, senza applicare alcuna modifica.

Questa verifica SHALL valere anche quando il client non ha chiamato prima
l'operazione di prenotazione: e' la modifica in se' a dover essere protetta, non
soltanto il flusso previsto dall'interfaccia.

#### Scenario: Modifica da parte di un utente diverso

- **WHEN** l'utente `mario` richiede la modifica di un'annotazione prenotata da
  `luigi`
- **THEN** `mario` riceve HTTP 409
- **AND** il contenuto dell'annotazione resta invariato
- **AND** non viene creata alcuna nuova versione della nota

#### Scenario: Modifica da parte del proprietario

- **WHEN** l'utente che detiene la prenotazione modifica l'annotazione
- **THEN** la modifica viene applicata con HTTP 200

#### Scenario: Modifica di un'annotazione libera

- **WHEN** un utente modifica un'annotazione non prenotata da nessuno
- **THEN** la modifica viene applicata con HTTP 200

### Requirement: Stato della prenotazione consultabile

Il sistema SHALL permettere di conoscere in qualsiasi momento se un'annotazione
e' prenotata e, in caso affermativo, da quale utente. Il frontend usa questa
informazione per mostrare all'utente il motivo per cui non puo' modificarla.

#### Scenario: Consultazione di un'annotazione prenotata

- **WHEN** un client richiede lo stato di prenotazione di un'annotazione
  prenotata da `luigi`
- **THEN** riceve HTTP 200 con indicazione di annotazione prenotata e
  proprietario `luigi`

#### Scenario: Consultazione di un'annotazione libera

- **WHEN** un client richiede lo stato di prenotazione di un'annotazione libera
- **THEN** riceve HTTP 200 con indicazione di annotazione non prenotata e nessun
  proprietario

### Requirement: Annotazione inesistente

Le operazioni di prenotazione, rilascio e consultazione dello stato SHALL
rispondere HTTP 404 quando l'annotazione indicata non esiste, senza creare alcuna
prenotazione.

#### Scenario: Prenotazione di un'annotazione inesistente

- **WHEN** un utente prenota un identificativo che non corrisponde ad alcuna
  annotazione
- **THEN** riceve HTTP 404
- **AND** non viene registrata alcuna prenotazione per quell'identificativo

### Requirement: Mutua esclusione fra istanze applicative

Nei profili che prevedono piu' istanze dell'applicazione in esecuzione (`kube`,
`aws`, `azure`) la prenotazione SHALL essere condivisa fra tutte le istanze: una
prenotazione acquisita su un'istanza SHALL essere visibile e vincolante per tutte
le altre.

Nel profilo `sqlite`, pensato per esecuzione a istanza singola, la prenotazione
SHALL garantire la mutua esclusione all'interno del singolo processo. Il
comportamento osservabile via API SHALL restare identico a quello degli altri
profili.

#### Scenario: Prenotazione acquisita su un'altra istanza

- **WHEN** `luigi` prenota un'annotazione sull'istanza A di un profilo
  multi-istanza
- **AND** `mario` tenta di prenotare la stessa annotazione sull'istanza B
- **THEN** `mario` riceve HTTP 409

#### Scenario: Stessa risposta su ogni profilo

- **WHEN** la stessa sequenza prenota, prenota-da-altro-utente, rilascia viene
  eseguita su ciascuno dei quattro profili
- **THEN** ogni profilo restituisce la stessa sequenza di codici HTTP
  200, 409, 204

### Requirement: Nessuna prenotazione implicita

Il sistema NON SHALL acquisire prenotazioni per conto dell'utente come effetto
collaterale di un'altra operazione. Una prenotazione SHALL esistere solo se
l'utente l'ha richiesta esplicitamente.

La verifica che una modifica provenga dal proprietario della prenotazione SHALL
essere applicata in un unico punto, valido per tutti i profili, e non SHALL
essere duplicata con regole o durate diverse a seconda della tecnologia di
persistenza.

#### Scenario: Modifica senza prenotazione preesistente

- **WHEN** un utente modifica un'annotazione che nessuno ha prenotato
- **THEN** la modifica viene applicata
- **AND** al termine dell'operazione l'annotazione risulta ancora non prenotata

#### Scenario: Stessa regola su ogni profilo

- **WHEN** la stessa sequenza di modifica viene eseguita su ciascuno dei profili
  `sqlite`, `kube`, `aws` e `azure`
- **THEN** lo stato di prenotazione osservabile al termine e' identico su tutti

### Requirement: La prenotazione sopravvive alla modifica

Una prenotazione acquisita esplicitamente SHALL restare attiva dopo un
aggiornamento andato a buon fine, fino al rilascio da parte del proprietario o
alla sua scadenza naturale.

Questo permette a un client di prenotare una volta e salvare piu' volte senza che
si apra, fra un salvataggio e l'altro, una finestra in cui un altro utente puo'
prenotare la stessa annotazione.

#### Scenario: Salvataggi successivi con una sola prenotazione

- **WHEN** l'utente `luigi` prenota un'annotazione e la modifica due volte di
  seguito
- **THEN** entrambe le modifiche vengono applicate
- **AND** la prenotazione risulta ancora attiva e intestata a `luigi` dopo
  ciascuna modifica

#### Scenario: Nessuna finestra libera dopo il salvataggio

- **WHEN** l'utente `luigi` prenota un'annotazione e la modifica
- **AND** subito dopo `mario` tenta di prenotare la stessa annotazione
- **THEN** `mario` riceve HTTP 409

#### Scenario: Rilascio esplicito dopo la modifica

- **WHEN** l'utente che ha modificato l'annotazione rilascia la prenotazione
- **THEN** l'annotazione risulta non prenotata
- **AND** un altro utente puo' prenotarla con successo
