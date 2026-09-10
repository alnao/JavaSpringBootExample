## Context

Il port service del `core` verifica, prima di delegare all'adapter, che
l'annotazione non sia prenotata da un utente diverso da quello che sta
modificando, e in caso contrario interrompe l'operazione con l'eccezione che il
controller traduce in 409. Questa verifica non acquisisce nulla: si limita a
leggere lo stato della prenotazione.

L'adapter `kube` esegue in aggiunta una propria sequenza: tenta di acquisire una
prenotazione di durata cablata, tratta il fallimento come contesa, e nel blocco
finale rilascia. Il rilascio non distingue fra la prenotazione che l'adapter ha
appena acquisito e quella che l'utente aveva gia': in entrambi i casi al termine
dell'operazione l'annotazione risulta libera. Vedi proposal.md - Why.

Il servizio di lock e' iniettato nel port service come dipendenza opzionale.

## Goals / Non-Goals

**Goals:**

- Un solo punto in cui si decide se una modifica e' consentita rispetto alla
  prenotazione.
- Comportamento della prenotazione dopo la modifica uguale sui quattro profili.

**Non-Goals:**

- Cambiare il modo in cui la prenotazione viene acquisita o rilasciata dalle API
  dedicate.
- Cambiare la durata di default della prenotazione o come viene configurata.
- Rendere la modifica un'operazione atomica rispetto ad altre modifiche
  concorrenti: la prenotazione resta un meccanismo cooperativo, non una
  transazione. Chi non prenota puo' ancora sovrascrivere una annotazione libera.
- Rivedere l'iniezione opzionale del servizio di lock nel port service.

## Decisions

**Rimuovere la logica dall'adapter invece di replicarla negli altri tre.**
La direzione opposta - portare acquisizione e rilascio anche in `sqlite`, `aws` e
`azure` - renderebbe i profili coerenti fra loro ma sposterebbe una regola di
dominio dentro quattro adapter, e manterrebbe la prenotazione implicita descritta
sotto. La verifica nel `core` copre gia' il caso, quindi la rimozione non lascia
scoperto nulla.

**La prenotazione implicita e' il difetto, non il rilascio.**
Si potrebbe correggere il solo rilascio, tenendo l'acquisizione e rilasciando
soltanto quando e' stato l'adapter ad acquisire. Sarebbe piu' conservativo ma
lascerebbe in piedi un lock acquisito di nascosto per la durata dell'operazione,
con una durata diversa da quella configurata per il sistema: due prenotazioni con
regole diverse per lo stesso oggetto. Meglio non acquisire affatto.

**Il comportamento atteso va scritto nella specifica prima di cambiarlo.**
Cosa succede alla prenotazione dopo un salvataggio oggi non e' dichiarato da
nessuna parte: e' la ragione per cui i profili hanno potuto divergere senza che
nessun test se ne accorgesse. I due requisiti aggiunti fissano il contratto, e i
test derivano da quelli.

**Il confronto fra profili e' parte della verifica, non un controllo aggiuntivo.**
Dato che la divergenza riguarda un solo profilo, la prova che la change funziona
e' che la stessa sequenza osservata su `kube` e su un altro profilo produca lo
stesso stato di prenotazione finale.

## Risks / Trade-offs

**Un client potrebbe dipendere del rilascio implicito sul profilo `kube`** → Il
frontend web e quello JavaFX vanno verificati: se prenotano, salvano e non
rilasciano, dopo la change l'annotazione resta prenotata fino alla scadenza. Se
emerge questa dipendenza, la correzione e' un rilascio esplicito lato client, non
il ripristino del comportamento implicito.

**La finestra di scadenza diventa piu' visibile** → Con la prenotazione che
sopravvive al salvataggio, un utente che chiude il browser lascia l'annotazione
bloccata fino allo scadere del timeout configurato. E' il comportamento gia' in
vigore sugli altri tre profili, quindi non introduce un caso nuovo, ma su `kube`
sara' osservabile per la prima volta.

**I test esistenti nell'adapter verificano il comportamento rimosso** → Vanno
riscritti in termini di contratto, non semplicemente cancellati: quello che
serve verificare e' che l'aggiornamento non alteri lo stato della prenotazione.

## Migration Plan

Nessuna migrazione di dati ne' di configurazione. Le prenotazioni vivono in Redis
con scadenza propria e non sopravvivono al rilascio di una nuova versione in modo
significativo.
