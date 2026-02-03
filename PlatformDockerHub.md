# Sistema di Gestione annotazioni - platform DockerHub

  <p align="center">
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=black"  height=60/>
    <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white"  height=60/>
  </p>

Progetto realizzato da `< AlNao />` come esempio pratico con Java Spring Boot: consente di creare, modificare e visualizzare annotazioni, utenti con privilegi da moderatore possono confermare le annotazioni e utenti con privilegi da amministratori possono confermare e *inviare* annotazioni a sistemi esterni.


## 🐳 Deploy ed esecuzione con DockerHub
L'immagine ufficiale dell'applicazione è pubblicata su [DockerHub](https://hub.docker.com/r/alnao/gestioneannotazioni) e può essere scaricata ed eseguita direttamente, senza necessità di build locale.
- **Compilazione e push dell'immagine**
    ```bash
    docker login
    docker build -t alnao/gestioneannotazioni:latest .
    docker push alnao/gestioneannotazioni:latest
    ```
    oppure lanciare lo script 
    ```bash
    ./script/push-image-docker-hub.sh
    ```
- **Pull dell'immagine**:
    ```bash
    docker pull alnao/gestioneannotazioni:latest
    ```
    L'immagine viene aggiornata con le ultime versioni *stabili*.
- **Esecuzione rapida** (della versione sqlite):
    ```bash
    docker run --rm -p 8082:8082 \
      -e SPRING_PROFILES_ACTIVE=sqlite \
      -e SPRING_DATASOURCE_URL=jdbc:sqlite:/tmp/database.sqlite \
      -e SERVER_PORT=8082 \
      alnao/gestioneannotazioni:latest
    ```
    Nota: si può avviare il profilo *sqlite* per eseguire l'immagine senza altri servizi, oppure l'applicazione con il profilo *kube* se nel sistema sono avviati anche i servizi MongoDb, Postgresql e Kafka come nel prossimo punto.
- **Esecuzione completa** 🔌 Rete Docker condivisa, alternativa robusta ma senza docker-compose:
    Possibile eseguire tutto con docker (senza docker-compose):
    ```bash
    # Creazione rete
    docker network create annotazioni-network

    # Esecuzione mongo
    docker run -d --name annotazioni-mongo \
      --network annotazioni-network \
      -p 27017:27017 \
      -e MONGO_INITDB_DATABASE=gestioneannotazioni \
      -e MONGO_INITDB_ROOT_USERNAME=admin \
      -e MONGO_INITDB_ROOT_PASSWORD=admin123 \
      mongo:4.4
    # Creazione document dentro Mongo
    docker cp script/init-database/init-mongodb.js annotazioni-mongo:/init-mongodb.js
    docker exec -it annotazioni-mongo mongo -u demo -p demo --authenticationDatabase admin /init-mongodb.js

    # Esecuzione postgresql
    docker run -d --name annotazioni-postgres \
      --network annotazioni-network \
      -p 5432:5432 \
      -e POSTGRES_DB=gestioneannotazioni \
      -e POSTGRES_USER=gestioneannotazioni_user \
      -e POSTGRES_PASSWORD=gestioneannotazioni_pass \
      postgres:13
    # Creazione database nel postgresql
    docker cp script/init-database/init-postgres.sql annotazioni-postgres:/init-postgres.sql
    docker exec -it annotazioni-postgres psql -U demo -d annotazioni -f /init-postgres.sql

    # Esecuzione di Kafka e Zookeeper
    docker run  --rm \
      --network annotazioni-network \
      --name annotazioni-zookeeper \
      -e ZOOKEEPER_CLIENT_PORT=2181 \
      -e ZOOKEEPER_TICK_TIME=2000 \
      -p 2181:2181 \
      confluentinc/cp-zookeeper:7.4.0

    # Zookeeper
    docker run  --rm \
      --network annotazioni-network \
      --name annotazioni-kafka \
      -p 9092:9092 \
      -e KAFKA_BROKER_ID=1 \
      -e KAFKA_ZOOKEEPER_CONNECT=annotazioni-zookeeper:2181 \
      -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://annotazioni-kafka:9092 \
      -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9092 \
      confluentinc/cp-kafka:7.4.0

    # Kafka-ui
    docker run  --rm -p 8085:8080 \
      --name annotazioni-kafka-ui \
      --network annotazioni-network \
      -e KAFKA_CLUSTERS_0_NAME=gestioneannotazioni-cluster \
      -e KAFKA_CLUSTERS_0_BOOTSTRAPSERVERS=annotazioni-kafka:9092 \
      -e KAFKA_CLUSTERS_0_ZOOKEEPER=zookeeper:2181 \
      provectuslabs/kafka-ui:latest

    # Esecuzione servizio 
    docker run  --rm -p 8082:8080 --name gestioneannotazioni-app \
      --network javaspringbootexample_gestioneannotazioni-network \
      -e SPRING_PROFILES_ACTIVE="kube" \
      -e POSTGRES_URL="jdbc:postgresql://postgres:5432/gestioneannotazioni" \
      -e POSTGRES_USERNAME="gestioneannotazioni_user" \
      -e POSTGRES_PASSWORD="gestioneannotazioni_pass" \
      -e MONGODB_URI="mongodb://admin:admin123@mongodb:27017/gestioneannotazioni_db?authSource=admin" \
      -e KAFKA_BROKER_URL="kafka-server:29092" \
      alnao/gestioneannotazioni:latest

    # Applicazione disponibile al url
    http://localhost:8082
    # Kafka-ui disponibile al u rl
    http://localhost:8085

    # Per vedere i container nella rete
    docker network inspect annotazioni-network

    # Per fermare tutto e rimuovere la rete
    docker stop annotazioni-app annotazioni-mongo annotazioni-postgres annotazioni-kafka annotazioni-kafka-ui
    docker rm annotazioni-app annotazioni-mongo annotazioni-postgres annotazioni-kafka annotazioni-kafka-ui
    docker network rm annotazioni-network
    docker network prune -f
    docker volume rm $(docker volume ls -q)
    docker rmi $(docker images -q)
    ```
- **Note**:
  - L'immagine non contiene dati sensibili quindi non c'è problema se viene pubblicata
  - Utilizzare sempre variabili d'ambiente sicure per le password e le connessioni DB in produzione.
  - Tutto questo enorme casino può essere evitato con docker-compose,minikube e kubernetes. Vedere le sezioni dedicate.


### 🐳 Esecuzione completa con Docker Compose

Per semplificare l’avvio di tutti i servizi necessari (applicazione, PostgreSQL, MongoDB) puoi utilizzare `docker-compose`. Questo permette di gestire tutto con un solo comando, senza dover creare manualmente reti o container.

- **Esempio di file `docker-compose.yml`**:
    ```yaml
    version: '3.8'
    services:
      ... #mongo, postgresql, kafka e redis!

      app:
        image: alnao/annotazioni:latest
        container_name: annotazioni-app
        depends_on:
          - postgres
          - mongo
        environment:
          SPRING_PROFILES_ACTIVE: kube
          POSTGRES_URL: jdbc:postgresql://postgres:5432/gestioneannotazioni
          POSTGRES_USERNAME: gestioneannotazioni_user
          POSTGRES_PASSWORD: gestioneannotazioni_pass
          MONGODB_URI: mongodb://admin:admin123@mongodb:27017/gestioneannotazioni_db?authSource=admin
          KAFKA_BROKER_URL: kafka-server:29092
          REDIS_HOST: redis
          REDIS_PORT: 6379
          EXPORT_ANNOTAZIONI_CRON_EXPRESSION: "0 */2 * * * *" # ogni 2 minuti
          SERVER_PORT: 8080
        ports:
          - "8080:8080"
        networks:
          - annotazioni-network

    networks:
      annotazioni-network:
        driver: bridge
    ```
- **Avvio dello stack**:
    ```bash
    docker-compose up -d
    ```
- **Fermare e rimuovere tutto**:
    ```bash
    docker-compose down
    docker volume rm $(docker volume ls -q)
    docker rmi $(docker images -q)
    ```
- **Note**:
    - L’applicazione diventa disponibile su [http://localhost:8082](http://localhost:8082)
    - Possibile personalizzare porte, variabili d’ambiente e configurazioni secondo le varie esigenze.
    - Per la produzione, necessario usare password sicure, sistemi di backup e sicurezza dei dati.



### ☸️ Esecuzione su Minikube e Kubernetes locale
L’applicazione e i database posso essere eseguiti anche su Minikube, l’ambiente Kubernetes locale, per simulare un cluster cloud-ready.
- Prerequisiti: 
    - Minikube installato ([guida ufficiale](https://minikube.sigs.k8s.io/docs/start/))
    - Kubectl installato
    - Freelens/OpenLens consigliato per la gestione dei pod, service e risorse
- Avvio Minikube:
    ```bash
    minikube start --memory=8096 --cpus=4
    ```
    nota: sono tante risorse, forse si possono ridurre un po'!
- Manifest già pronti:
    Nella cartella `script/minikube` trovi i manifest YAML già pronti per avviare tutta l'infrastruttura, presente script che esegue nella giusta sequenza gli script di `kubectl apply`, lo script da lanciare è:
    ```bash
    ./script/minikube/start-all.sh
    ```
- Accesso all’applicazione:
    - Usando l’Ingress, aggiungendo al file hosts la riga:
      ```
      127.0.0.1 annotazioni.local
      ```
      e visitando [http://annotazioni.local](http://annotazioni.local)
    - Oppure usando il NodePort:
      ```bash
      minikube service annotazioni-app
      ```
      e visitando [http://localhost:30080](http://localhost:30080)
    - Oppure con *freelens* si può creare l'endpoint selezionado il service specifico.
- Note:
    - Anche kafka e il suo kafka-ui è disponibile come ingress oppure usando freelens si crea l'endpoint
    - I dati di MongoDB e PostgreSQL sono persistenti grazie ai PVC di Kubernetes, a meno di usare lo script di `stop-all.sh` che rimuove anche i volumi persistenti.
    - Viene usata l'immagine `alnao/gestioneannotazioni:latest` su dockerHub e non una immagine creata in sistema locale.
    - Per rimuovere tutto lo script da lanciare è
      ```bash
      ./script/minikube/stop-all.sh
      minikube delete
      ```

## 📦 Versione SQLite per Replit
Sviluppato un adapter specifico per usare sqlite per tutte le basi dati necessarie al corretto funzionamento del servizio, studiato per funzionare anche nel cloud Replit.
- La versione che usa SqLite ha una classe che crea tre utenti di prova partendo dai dati dell'application YAML, è presente proprietà per disattivare questa funzionalità. Il componente è stato creato per velocizzare gli sviluppi e i test, questo componente va rimosso in un sistema di produzione. In alternatica è sempre possibile creare gli utenti con le API:
    ```
    curl -X POST http://localhost:8082/api/auth/register \
      -H "Content-Type: application/json" \
      -d '{
        "username": "alnao",
        "password": "$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi",
        "email": "alnao@example.com"
      }'
    curl -X POST http://localhost:8082/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{
        "username": "admin",
        "password": "xxxxxxxxxxxxxxxxxxx"
      }'
    ```
- Utilizzado SQLite come unico database per tutte le funzionalità (annotazioni, utenti, storico) non ha nessuna dipendenza da servizi esterni: è possibile eseguire tutto in locale, ideale per prove locali o test. Previsto un profilo Spring Boot specifico `sqlite`. I comandi per eseguire il microservizio in locale con questo profilo sono:
  ```
  mvn clean package
  java -jar application/target/application-*.jar \
    --spring.profiles.active=sqlite \
    --spring.datasource.url=jdbc:sqlite:/tmp/database.sqlite \
    --server.port=8082
  ```
- E' stato creato un docker-compose specifico, così da gestire il volume dei dati con docker. Script di avvio e arresto già pronti per esecuzione locale: per eseguire tutto in locale eseguire lo script:
  ```
  cd script/sqlite-locale/
  ./start-all.sh
  ```
  - L'applicazione web sarà disponibile su [http://localhost:8082](http://localhost:8082)
  - Interfaccia di gestione SQLite su [http://localhost:8084](http://localhost:8084)
  - Fermare l'esecuzione
    ```
    cd script/replit-locale
    ./stop-all.sh
    ```
- Per l'esecuzione nel sistema **Replit**, i passi da eseguire sono:
  - Eseguire login su [replit.com](https://replit.com/) con utenza, non serve avere abilitata la versione a pagamento.
  - Selezionare la funzionalità `Import code or design` e selezionare il tipo `github`
  - Nella schermata di configurazione inserire il repository pubblico
    - per esempio `https://github.com/alnao/JavaSpringBootExample`
  - Lasciare che il sistema scarichi il progetto e compili, in teoria l'IA di Replit intuirà da sola che deve avviare il progetto con il profilo `sqlite`. Se non lo fà bisgna indicarlo nella chat dell'agente che esegue il microservizio.
  - Si può chiedere alla chat se il servizio è attivo e di ritornare l'endpoint che sarà del tipo:
    ```
    https://xxx-xxx-xxx.worf.replit.dev
    ```
  - Se la creazione degli utenti è disabilitata, è possibile creare un utente di prova con postaman/curl, per esempio:
    ```
    curl -X POST https://xxx-xxx-xxx.worf.replit.dev/api/auth/register \
      -H "Content-Type: application/json" \
      -d '{
        "username": "alnao",
        "password": "$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi",
        "email": "alnao@example.com"
      }'
    curl -X POST https://xxx-xxx-xxx.worf.replit.dev/api/auth/login \
      -H "Content-Type: application/json" \
      -d '{
        "username": "alnao",
        "password": "bellissimo"
      }'
    ```
  - Verificare con browser che il servizio è disponibile all'endpoint: `https://xxx-xxx-xxx.worf.replit.dev`
  - Il replit creato nel cloud risulta poi disponibile su:
    ```
    https://replit.com/@alnao84/JavaSpringBootExample
    ```
- Versione **Sqlite** su **AWS-EC2**: è stata sviluppata anche uno script per eseguire il microservizio in una istanza EC2 con il profilo sqlite con docker e senza bisogno di RDS, Dynamo e ECS. 
  - Script di creazione dello stack (Key, SecurityGroup e avvio istanza EC2):
    ```
    ./script/sqlite-ec2/start-all.sh 
    ```
  - Comandi per collegarsi all'istanza EC2, verificare l'output del user-data e la creazione dell'utente:
    ```
    ssh -i gestioneannotazioni-sqlite-ec2-key.pem ec2-user@x.y.z.a
    sudo cat /var/log/cloud-init-output.log
    sudo tail /var/log/cloud-init-output.log --follow

    curl -X POST http://localhost:8082/api/auth/register \
      -H "Content-Type: application/json" \
      -d '{
        "username": "alnao",
        "password": "$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi",
        "email": "alnao@example.com"
      }'
    ```
  - Script di deprovisioning di tutte le risorse create:
    ```
    ./script/sqlite-ec2/stop-all.sh 
    ```



# &lt; AlNao /&gt;
Tutti i codici sorgente e le informazioni presenti in questo repository sono frutto di un attento e paziente lavoro di sviluppo da parte di AlNao, che si è impegnato a verificarne la correttezza nella misura massima possibile. Qualora parte del codice o dei contenuti sia stato tratto da fonti esterne, la relativa provenienza viene sempre citata, nel rispetto della trasparenza e della proprietà intellettuale. 


Alcuni contenuti e porzioni di codice presenti in questo repository sono stati realizzati anche grazie al supporto di strumenti di intelligenza artificiale, il cui contributo ha permesso di arricchire e velocizzare la produzione del materiale. Ogni informazione e frammento di codice è stato comunque attentamente verificato e validato, con l’obiettivo di garantire la massima qualità e affidabilità dei contenuti offerti. 


Per ulteriori dettagli, approfondimenti o richieste di chiarimento, si invita a consultare il sito [AlNao.it](https://www.alnao.it/).


## License
Made with ❤️ by <a href="https://www.alnao.it">AlNao</a>
&bull; 
Public projects 
<a href="https://www.gnu.org/licenses/gpl-3.0"  valign="middle"> <img src="https://img.shields.io/badge/License-GPL%20v3-blue?style=plastic" alt="GPL v3" valign="middle" /></a>
*Free Software!*


Il software è distribuito secondo i termini della GNU General Public License v3.0. L'uso, la modifica e la ridistribuzione sono consentiti, a condizione che ogni copia o lavoro derivato sia rilasciato con la stessa licenza. Il contenuto è fornito "così com'è", senza alcuna garanzia, esplicita o implicita.


The software is distributed under the terms of the GNU General Public License v3.0. Use, modification, and redistribution are permitted, provided that any copy or derivative work is released under the same license. The content is provided "as is", without any warranty, express or implied.



