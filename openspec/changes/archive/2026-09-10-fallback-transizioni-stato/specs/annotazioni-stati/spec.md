## ADDED Requirements

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
