Fatto: ho aggiunto lo stato `IMPORTATA` e duplicato i permessi di cambio stato usando come modello `MODIFICATA` (inclusi i flussi da `INSERITA` e le transizioni di moderazione verso `CONFERMATA`/`BANNATA` in `core/src/main/resources/cambiamentoStati.yaml`, oltre all’enum in `StatoAnnotazione`).  
Ho anche aggiornato le superfici UI correlate (web + JavaFX) per riconoscere/filtrare/mostrare `IMPORTATA` con mapping coerenti a `MODIFICATA` dove appropriato.  
Verifica eseguita: `mvn -q -pl core,adapter-javafx,adapter-web -am test -DskipITs` passata (nota: il baseline generale aveva già failure ambientale SQLite non legata a questa modifica).


