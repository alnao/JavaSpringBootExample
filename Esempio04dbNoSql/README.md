# Esempio04dbNoSql
Progetto di esempio che crea un microservizio che si collega a Mondo e DynamoDB nello stesso momento
Il progetto può essere eseguito in locale con `docker` e `docker-compose` e si ceraziono i componenti:
- **Mongo** server locale
- **DynamoDB** server locale
- **Dynamo admin** con l'immagine `aaronshaf/dynamodb-admin`
- **Mongo admin** con l'immagine di `mongo-express`
- Microservizio **esempio04** creato con il suo `Dockerfile` e compilato con *maven*, utilizza anche actuator per i dati verso Prometheus.
- **Prometheus** è il motore di raccolta e archiviazione delle metriche. **Grafana** è l'interfaccia utente visiva che interroga Prometheus e presenta i dati in dashboard significative.

Il docker-compose espone su porta 8070 il microservizio agli endpoint:
- versione mongo: `http://localhost:8070/apim/users`
- versione dynamo: `http://localhost:8070/apid/users`


la porta 8070 è stata scelta perchè 8080 potrebbe essere usata altri servizi.


*Attenzione*: l'esecuzione in sistemi locali potrebbe necessitare di diversi Gb di spazio (circa 4Gb sommando tutte le immagini necessarie).


Il progetto è studiato per essere eseguito anche su usando i servizi ECR, DocumentDb, Dynamo e **ECS** con FARGATE (script AWS-CLI senza CloudFormation). La versione AWS ha qualche problema e, per funzionare, bisogna togliere completamente i pezzi su Mongo perchè non funziona *e non so il motivo*.


*Attenzione*: l'esecuzione su AWS potrebbe provocare degli addebiti in quanto certi servizi sono a pagamento. Stima approssimativa dei costi giornalieri
1. DocumentDB: Istanza db.t3.medium: $0.068/ora Totale giornaliero: 0.068(orario)×24(ore al giorno)=$1.63 al giorno
2. ECS Task (Fargate) CPU: 0.25 vCPU → $0.04047/vCPU/ora × 0.25 = $0.0101175/ora Memoria: 0.512 GB → $0.004447/GB/ora × 0.512 ≈ $0.002277/ora Totale orario: $0.0101175 + $0.002277 ≈ $0.0124/ora Totale giornaliero: 0.0124(orario)×24(ore al giorno)=$0.2976 al giorno
3. DynamoDB: Modalità PAY_PER_REQUEST: gratuito fino a una certa soglia. Supponendo un uso minimo, possiamo ignorare i costi per ora.
4. CloudWatch Logs: Se i log non superano i 5 GB mensili, nessun costo.
5. Altri costi (VPC, Security Group, IAM) : gratuiti.
- Totale: circa 2$ al giorno con un uso limitato.


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



