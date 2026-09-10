## 1. Caricamento delle transizioni nel core

- [x] 1.1 Introdurre in `core` un'eccezione dedicata al fallimento del
      caricamento delle transizioni, con messaggio che riporta causa e, quando
      nota, la voce di configurazione responsabile
- [x] 1.2 In `ValidatoreTransizioniStatoService` rimuovere l'insieme di
      transizioni di ripiego e sostituire la cattura generica con il rilancio
      della nuova eccezione, mantenendo un solo messaggio di log di livello ERROR
      prima della terminazione
- [x] 1.3 Convertire le voci una alla volta, arricchendo il messaggio di errore
      con indice e descrizione della voce quando lo stato o il ruolo non sono
      riconosciuti
- [x] 1.4 Trattare come errore il caso di configurazione caricata ma priva di
      transizioni
- [x] 1.5 Verificare che l'eccezione sollevata durante l'inizializzazione
      impedisca il completamento del contesto Spring e la terminazione del
      processo con codice diverso da zero

## 2. Test del core

- [x] 2.1 Test di caricamento fallito: configurazione assente, non
      interpretabile, con stato inesistente, con ruolo inesistente, vuota; per
      ognuno verificare l'eccezione e il contenuto del messaggio
- [x] 2.2 Test sul file realmente impacchettato: si deserializza e ogni voce
      nomina stati e ruoli esistenti
- [x] 2.3 Aggiornare `ValidatoreTransizioniStatoServiceTest` e
      `ValidatoreTransizioniStatoServiceGerarchiaTest` dove si appoggiavano al
      comportamento di ripiego
- [x] 2.4 Verificare la coverage del modulo `core` con
      `mvn -q -pl core -am test`

## 3. Verifica per profilo

- [x] 3.1 Profilo `sqlite`: avvio con configurazione valida e avvio con
      configurazione manomessa, verificando il messaggio di errore e la mancata
      esposizione degli endpoint
- [x] 3.2 Profilo `kube`: stessa verifica su container, controllando che il
      messaggio sia leggibile nei log del container e che il container non passi
      in stato healthy
- [x] 3.3 Profili `aws` e `azure`: verifica del solo avvio con configurazione
      valida, per confermare l'assenza di regressioni. Nessun provisioning di
      risorse cloud nuove
      - `aws` verificato in locale con `script/aws-onprem/docker-compose.yml`:
        avvio completato, 19 transizioni caricate, health UP
      - `azure` verificato dal proprietario del progetto sul proprio account:
        funzionante
- [x] 3.4 Verificare che il chiamante di un cambio di stato su applicazione
      correttamente avviata continui a ricevere 403 per le transizioni non
      permesse, cioe' che la semantica del 403 non sia stata alterata

## 4. Documentazione e chiusura

- [x] 4.1 Documentare in `README.md` che la configurazione delle transizioni e'
      obbligatoria all'avvio e come si presenta l'errore
- [x] 4.2 Eseguire lo script di regressione del profilo interessato:
      `./script/automatic-test/test-sqlite-onprem.sh` e
      `./script/automatic-test/test-kube-onprem-docker-compose.sh`
      - Eseguiti con autorizzazione esplicita del proprietario del progetto,
        cleanup distruttivo incluso
      - `test-sqlite-onprem.sh`: superato per intero, compreso il test di
        scadenza automatica della prenotazione
      - `test-kube-onprem-docker-compose.sh`: superato salvo il controllo dei
        messaggi Kafka, che usa `docker exec -it` e quindi funziona solo da
        terminale interattivo; export verificato a parte senza `-it`,
        annotazione pubblicata sul topic e stato finale INVIATA
- [x] 4.3 Spuntare la voce corrispondente in `Roadmap.md`
