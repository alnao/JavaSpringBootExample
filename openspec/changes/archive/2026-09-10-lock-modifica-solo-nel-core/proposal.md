## Why

La verifica che una modifica provenga dal proprietario della prenotazione e' gia'
implementata nel `core`, nel port service, e vale per tutti i profili.
L'adapter `kube` la ripete a modo suo: acquisisce una prenotazione di durata
cablata prima di aggiornare e la rilascia al termine dell'operazione.

Da qui due problemi.

Il primo e' osservabile da chi usa le API: sul profilo `kube`, dopo un
aggiornamento, la prenotazione che l'utente aveva acquisito esplicitamente
risulta rilasciata, mentre sugli altri tre profili resta attiva fino alla sua
scadenza naturale. Un frontend che prenota, salva e poi salva di nuovo si
comporta in modo diverso a seconda di dove e' installato, e nella finestra fra il
primo e il secondo salvataggio un altro utente puo' inserirsi.

Il secondo e' strutturale: la stessa regola e' scritta in due posti, con due
durate diverse, e la copia nell'adapter contraddice la separazione fra core e
adapter su cui e' costruito il progetto.

## What Changes

- La verifica della prenotazione in fase di aggiornamento resta esclusivamente
  nel `core`. L'adapter `kube` smette di acquisire e rilasciare prenotazioni per
  conto proprio.
- **BREAKING** Sul profilo `kube` una prenotazione acquisita esplicitamente
  sopravvive all'aggiornamento, come gia' avviene sugli altri profili. Un client
  che oggi si affida al rilascio implicito dopo il salvataggio deve rilasciarla
  esplicitamente.
- Viene reso esplicito nella specifica cosa succede alla prenotazione dopo una
  modifica, oggi non dichiarato.

## Capabilities

### New Capabilities

Nessuna.

### Modified Capabilities

- `annotazioni-lock`: si aggiunge un requisito sulla sorte della prenotazione
  dopo un aggiornamento andato a buon fine e sull'assenza di prenotazioni
  implicite. I requisiti su acquisizione, rilascio e rifiuto con 409 non
  cambiano.

## Impact

- `adapter-mongodb`: `AnnotazioneServiceImpl.aggiornaAnnotazione`, rimozione
  dell'acquisizione e del rilascio e della dipendenza dal servizio di lock se non
  usata altrove nella classe.
- `core`: nessuna modifica funzionale attesa; la verifica esistente nel port
  service diventa l'unica.
- Profili: solo `kube` cambia comportamento. `sqlite`, `aws` e `azure` gia' si
  comportano come descritto e servono come riferimento di confronto.
- Test: `AnnotazioneServiceImplLockTest` in `adapter-mongodb` va rivisto, dato
  che verifica proprio il comportamento che viene rimosso.
- Nessuna nuova dipendenza Maven. Nessuna modifica alle API o allo schema dati.
