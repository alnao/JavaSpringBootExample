# Esempio05testJunit5Mokito
Progetto di esempio che crea un microservizio che implementa anche unit-test con `junit5` e `mokito`.
- La TDD **Test-Driven Development** è una metodologia di sviluppo software in cui si scrivono prima i test automatici per una nuova funzionalità, e solo dopo il codice minimo necessario per far passare quei test. Questo approccio garantisce che il codice sia testabile, ben progettato e che le funzionalità soddisfino i requisiti, riducendo i bug e migliorando la manutenibilità.
- **Mockito** è un framework di mocking Java che ci permette di creare oggetti "finti" (mock) delle dipendenze di una classe, isolando l'unità di codice che stiamo testando. In questo esempio, lo abbiamo usato per simulare il comportamento di UserRepository nei test di LoginService e UserServiceImpl, senza dover interagire con un database reale.


Il progetto può essere eseguito in locale con `docker` e `docker-compose` che permette di creare velocemente una infrastruttura con database mysql pronta all'uso e il microservizio in esecuzione. **Non ho tempo di fare i comandi per eseguire l'esemnpio anche su AWS**.
Presente anche un `docker-compose` dedicato per eseguire **SonarQube** per la verifica del codice (coverage, smell, bug...). Presente la documentazione creata con **Swagger**.


## Comandi base per l'esecuzione in locale
- Comando per l'esecuzione dei test completi
    ```
    mvn clean test package
    ```
- Comando per il rilascio di tutta l'archiettura
    ```
    docker compose up --build
    ```
    Nota: il microservizio ci mette un po' a partire perchè deve aspettare che il server MySql sia partito. Per evitare il problema che il microservizio si rompa in avvio è stato fatto uno script `start_microservice.sh` che aspetta che il MySql sia attivo prima di partire.
- Comandi per il monitoraggio del database MySql
    ```
    docker logs es05-mysql-db
    docker exec -it es05-mysql-db mysql -u root -pstupendo
    > SHOW DATABASES;
    > USE Applicazione;
    > SHOW TABLES;
    ```
- Comandi curl  per provare il funzionamento
    ```
    curl -X POST "http://localhost:8045/api/login?nome=alnao&password=bellissimo"
    curl -X POST "http://localhost:8045/api/users" -H "Content-Type: application/json" -d '{"nome":"NuovoUtente","password":"password123"  }'
    curl -X GET "http://localhost:8045/api/users"
    curl -X GET "http://localhost:8045/api/users/1"
    ```
- Comando per la distruzione di tutta l'architettura
    ```
    docker-compose down --volumes --rmi all
    ```
- Comando per pulire tutto il docker-system
    ```
    docker system prune -a
    ```

# Configurazione di SonarQube
**SonarQube** è una piattaforma per l'analisi statica del codice che identifica bug, vulnerabilità e "code smells" per migliorare la qualità del software. In questo contesto, SonarQube si integra con JaCoCo, un tool che genera report sulla copertura dei test unitari, permettendo a SonarQube di visualizzare e monitorare quanto del codice è coperto dai test. 


- Per configurare il progetto bisogna aggiungere i plugin di `sonar-maven` e `jacoco`, vedere il file `pom.xml` nella sezione dedicata ai plugins.
- Disponibile un docker-compose specifico che esegue SonarQube in locale, comando per eseguire:
    ```
    docker-compose -f docker-compose-sonarqube.yml up -d
    ```
- Una volta partito è possibile accedere all'interfaccia web di SonarQube `http://localhost:9000` (credenziali di default sono admin/admin). Dopo aver effettuato il primo accesso a SonarQube, su "My Account" -> "Security" per generare un nuovo token da configuare come proprietà nel `pom.xml` (al punto `YOUR_SONARQUBE_TOKEN`).
- Per eseguire il calcolo della coverage e il sonar:
    ```
    mvn clean verify sonar:sonar
    ```
- Il risultato dell'analisi è disponibile nell'interfaccia web di SonarQube `http://localhost:9000` con i risultati dell'analisi (code smells, bug, vulnerabilità, copertura del codice, ecc.).
- Per distruggere tutto basta lanciare i comandi
    ```
    docker-compose -f docker-compose-sonarqube.yml stop
    docker-compose -f docker-compose-sonarqube.yml down -v
    ```

# AlNao.it
Nessun contenuto in questo repository è stato creato con IA o automaticamente, tutto il codice è stato scritto con molta pazienza da Alberto Nao. Se il codice è stato preso da altri siti/progetti è sempre indicata la fonte. Per maggior informazioni visitare il sito [alnao.it](https://www.alnao.it/).


## License
Public projects 
<a href="https://it.wikipedia.org/wiki/GNU_General_Public_License"  valign="middle"><img src="https://img.shields.io/badge/License-GNU-blue" style="height:22px;"  valign="middle"></a> 
*Free Software!*
