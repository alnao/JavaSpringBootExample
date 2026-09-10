## ADDED Requirements

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
