## 1. Allineamento dell'adapter kube

- [x] 1.1 In `adapter-mongodb`, rimuovere da `aggiornaAnnotazione`
      l'acquisizione della prenotazione, il relativo trattamento della contesa e
      il rilascio nel blocco finale
- [x] 1.2 Rimuovere la dipendenza dal servizio di lock dal costruttore della
      classe se non risulta usata altrove, mantenendo la constructor injection
      per le dipendenze rimaste
- [x] 1.3 Verificare che la verifica nel port service del `core` continui a
      produrre 409 quando la modifica proviene da un utente diverso dal
      proprietario della prenotazione, senza modifiche al `core`

## 2. Test

- [x] 2.1 Riscrivere `AnnotazioneServiceImplLockTest` in termini di contratto:
      l'aggiornamento non acquisisce e non rilascia prenotazioni
- [x] 2.2 Test sul port service del `core`: dopo un aggiornamento da parte del
      proprietario, la prenotazione risulta ancora attiva e intestata a lui
- [x] 2.3 Test sul port service del `core`: due aggiornamenti consecutivi con
      una sola prenotazione vanno entrambi a buon fine
- [x] 2.4 Verificare la coverage dei moduli toccati con
      `mvn -q -pl core,adapter-mongodb -am test`

## 3. Verifica per profilo

- [x] 3.1 Profilo `kube`: sequenza prenota, modifica, verifica stato
      prenotazione, seconda modifica, rilascio; la prenotazione risulta attiva
      dopo ciascuna modifica
- [x] 3.2 Profilo `kube`: con `luigi` che ha prenotato e appena modificato,
      `mario` che tenta di prenotare riceve 409
- [x] 3.3 Profilo `sqlite`: stessa sequenza del punto 3.1, per confermare che il
      comportamento coincide
- [x] 3.4 Profili `aws` e `azure`: nessuna modifica attesa; verifica di non
      regressione sulla sequenza prenota, modifica, rilascio senza provisioning
      di risorse cloud nuove
      - `aws` verificato in locale con `script/aws-onprem/docker-compose.yml`:
        sequenza identica a kube e sqlite (200/200/prenotata/200/409/409/204)
      - `azure` verificato dal proprietario del progetto sul proprio account:
        funzionante

## 4. Client e chiusura

- [x] 4.1 Verificare se il frontend web e `adapter-javafx` si affidavano al
      rilascio implicito dopo il salvataggio sul profilo `kube` e, in tal caso,
      aggiungere il rilascio esplicito
- [x] 4.2 Eseguire
      `./script/automatic-test/test-kube-onprem-docker-compose.sh` e
      `./script/automatic-test/test-prenotazione-annotazione.sh`
      - Eseguiti con autorizzazione esplicita del proprietario del progetto,
        cleanup distruttivo incluso
      - `test-sqlite-onprem.sh`: superato per intero, compreso il test di
        scadenza automatica della prenotazione
      - `test-kube-onprem-docker-compose.sh`: superato salvo il controllo dei
        messaggi Kafka, che usa `docker exec -it` e quindi funziona solo da
        terminale interattivo; export verificato a parte senza `-it`,
        annotazione pubblicata sul topic e stato finale INVIATA
- [x] 4.3 Spuntare la voce corrispondente in `Roadmap.md`
