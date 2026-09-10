## Why

Le regole di transizione di stato sono dati di configurazione caricati da
`cambiamentoStati.yaml`. Se il file manca, e' malformato o contiene uno stato o
un ruolo non riconosciuto, il caricamento fallisce, l'errore viene loggato e
l'applicazione **parte comunque** con un insieme di transizioni di ripiego che
contiene una sola voce priva di utilita' operativa.

L'effetto per chi usa il sistema e' che ogni cambio di stato viene rifiutato con
HTTP 403, cioe' con lo stesso codice di una transizione legittimamente non
permessa: un guasto di configurazione si presenta come un problema di permessi.
Su un profilo cloud, dove la diagnosi passa dai log di un container, questo puo'
costare ore prima che qualcuno sospetti il file YAML.

Il file e' impacchettato dentro il jar: se non si carica non e' una condizione di
esercizio, e' un errore di build o di packaging. Non ha senso proseguire l'avvio.

## What Changes

- **BREAKING** L'applicazione non completa l'avvio se le transizioni di stato non
  sono caricabili. Oggi parte in stato degradato; dopo questa change termina con
  un errore esplicito.
- Il messaggio di errore indica la causa (file assente, YAML non valido, stato o
  ruolo sconosciuto) e, dove applicabile, la voce di configurazione responsabile.
- Viene rimosso l'insieme di transizioni di ripiego cablato nel codice: le regole
  di workflow restano un'unica fonte di verita'.
- Una configurazione che si carica ma non contiene alcuna transizione e'
  considerata un errore, non un insieme vuoto valido.

## Capabilities

### New Capabilities

Nessuna.

### Modified Capabilities

- `annotazioni-stati`: si aggiunge un requisito sulla obbligatorieta' della
  configurazione delle transizioni all'avvio. I requisiti esistenti su quali
  transizioni sono permesse e su come vengono tracciate non cambiano.

## Impact

- `core`: `ValidatoreTransizioniStatoService` (caricamento e ripiego),
  `TransizioniStatoConfig`.
- Configurazione: `core/src/main/resources/cambiamentoStati.yaml` diventa un file
  obbligatorio a runtime.
- Deployment: tutti e quattro i profili (`sqlite`, `kube`, `aws`, `azure`). Il
  file e' nel jar e non e' sovrascritto per profilo, quindi la modifica ha lo
  stesso effetto ovunque; su Kubernetes, ECS e ACI un avvio fallito si manifesta
  come container che non passa in stato healthy.
- Test: `ValidatoreTransizioniStatoServiceTest`,
  `ValidatoreTransizioniStatoServiceGerarchiaTest`.
- Nessuna nuova dipendenza Maven.
