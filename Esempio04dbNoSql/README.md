# Esempio04dbNoSql
Progetto di esempio che crea un microservizio che si collega a Mondo e DynamoDB nello stesso momento
Il progetto può essere eseguito in locale con `docker` e `docker-compose` e si ceraziono i componenti:
- **Mongo** server locale
- **DynamoDB** server locale
- **Dynamo admin** con l'immagine `aaronshaf/dynamodb-admin`
- **Mongo admin** con l'immagine di `mongo-express`
- Microservizio **esempio04** creato con il suo `Dockerfile` e compilato con *maven*, utilizza anche actuator per i dati verso Prometheus.
- **Prometheus** è il motore di raccolta e archiviazione delle metriche. **Grafana** è l'interfaccia utente visiva che interroga Prometheus e presenta i dati in dashboard significative.


## Comandi base per locale
- Nel progetto è presente un `makefile` e un `start.sh` che facilitano le fasi di rilascio.
- Comando per la compilazione del progetto java
    ```
    mvn clean install package
    ```
- Comando per il rilascio di tutta l'archiettura
    ```
    make up
    ```
    (oppure `./start.sh`)
- Comando per la distruzione di tutta l'architettura
    ```
    make down
    ```
    (oppure `docker-compose down`)
- Comando per pulire tutto il docker-system
    ```
    docker system prune -a
    ```

## Comandi per deploy su AWS
- Comando rilascio
    ```
    ./aws/deploy.sh
    ```
- Comando push dell'immagine nel repository **ECR**
    ```
    ./aws/ecr_deploy.sh
    ```
- Comando distruzione totale
    ```
    ./aws/clean_up.sh
    ```

## Creazione del progetto con Claude.ia
- voglio fare un nuovo progetto: un microservizio in java spring boot collegato ad una tabella dynamo e/o mongo (voglio entrambe le soluzioni) che usi actuator e maven. questo microservizio deve funzionare in locale sul mio pc, su docker/kubernetes e su AWS. Voglio usare GitHub e DockerHub. Intanto scrivimi il codice del microservizio in java 
- ora vorrei eseguire il tutto in locale con docker, quindi prima fammi il docker file per compilare e creare l'immagine, poi fammi il docker compose e poi fammi altri componenti che tu ritieni necessario per eseguire il tutto nel mio pc locale (ma voglio che i server vengano eseguiti solo su docker) 
- questo è il mio docker-file funzionante del microservizio , ho modificato il microservizio per usare sia Dynamo sia Mongo assieme nella stessa istanza ma quello non te lo passo perchè non ti serve. ora voglio eseguire tutto in AWS usando Dynamo e MongoDb  con DocumentDB, scrivimi i comandi per il deploy usando la AWS CLI in un unico script . utilizza VPC e subnet di default, la region deve essere parametrica con default "eu-central-1", il nome dei componenti deve essere "esempio04"
    - questa è la mia sequenza per il rilascio su ECR ma fammi uno script dedicato dove regione, id_Account e nome progetto siano parametrici
- Se volessi eseguire tutto questo fuori da AWS ma non nel mio pc locale, che opzioni mi dai?
    - Altri cloud: Google Cloud Platform (GCP), Microsoft Azure, Oracle Cloud Infrastructure (OCI)
    - Saas: Railway, Render, Fly.io, DigitalOcean App Platform

# AlNao.it
Nessun contenuto in questo repository è stato creato con IA o automaticamente, tutto il codice è stato scritto con molta pazienza da Alberto Nao. Se il codice è stato preso da altri siti/progetti è sempre indicata la fonte. Per maggior informazioni visitare il sito [alnao.it](https://www.alnao.it/).

## License
Public projects 
<a href="https://it.wikipedia.org/wiki/GNU_General_Public_License"  valign="middle"><img src="https://img.shields.io/badge/License-GNU-blue" style="height:22px;"  valign="middle"></a> 
*Free Software!*



