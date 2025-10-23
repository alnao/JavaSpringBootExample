# CosmosDB Linux Emulator - Limitazioni e Uso
Ho perso molte ore a capire come far funzionare CosmosDB in locale usando l'immagine ufficiale
```
mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:mongodb
```
ma alla fine **non funziona**. C'è una cartella `azure-onprem-non-funziona` dentro alla cartella `script` così come promemoria delle prove fatte e dei comandi eseguiti. Se in futuro la situazione cambierà sistemerò questo esempio.


L’emulatore Linux in Docker è pensato solo per test container-to-container, e non supporta SDK dal host o a volte neanche da container separati se non dal container ufficiale che lo avvia. **CosmosDB Emulator Linux in Docker non implementa correttamente tutte le API richieste dagli SDK esterni.** Funziona solo con richieste dal container stesso o dalla rete interna Docker dove gira l’emulatore.


Microsoft documenta che il Linux Emulator è destinato a test container-to-container, non per SDK dal host o container separati. Alcune porte interne (10250+) devono essere esposte e certificate corrette mappate, cosa che il container ufficiale Linux non fa completamente.


## Cos'è l'immagine

* È il **CosmosDB Emulator per Linux**, pensato per container Docker.
* Permette di **simulare alcune funzionalità di CosmosDB** localmente.
* **Supporta solo alcuni endpoint**:

  * TCP/HTTPS minimale
  * MongoDB API (`:mongodb`)
  * Data Explorer base (alcuni endpoint REST)

❌ **Non implementa completamente la SQL API** richiesta dai **Java/Python SDK standard** (`CosmosClient`).


## Cosa puoi fare con il Linux Emulator

* Testare **connessione TCP/HTTPS** → `curl -k https://cosmos-emulator:8081`
* Testare **MongoDB API** usando driver MongoDB
* Debug di networking e certificati
* Importazione del certificato self-signed per le prove SSL


## Cosa NON puoi fare

* Non puoi usare **Java SDK `CosmosClient`**
* Non puoi usare **Python SDK `CosmosClient`**
* Non puoi fare operazioni CRUD reali su SQL API


## Perché Microsoft la mette a disposizione

* L’immagine è utile per **scenari di test limitati** o **containerized CI/CD**, ma **non sostituisce il Windows/macOS Emulator** per SQL API.
* L’obiettivo è dare agli sviluppatori Linux un **ambiente minimale**, ma non completo per sviluppo Java/Python.


## Come fare per sviluppare Java/Python in locale

Se vuoi usare i **Java/Python SDK standard** e fare test veri:

1. **Windows/macOS host** → CosmosDB Emulator completo
2. **Azure Cosmos reale** → crea un account gratuito, collegalo dai container Linux
3. **Linux Docker + mock** → solo se vuoi fare test senza il vero backend CosmosDB


💡 **In sintesi:**
Linux Emulator serve **solo per test limitati e MongoDB API**, non per sviluppare un'app Java/Python reale che usa SQL API. Per lo sviluppo completo devi **emulare o usare un Cosmos reale**.






## Comandi iniziali
Per simulare l'ambiente Azure in locale (SqlServer come database, CosmosDB in locale, Adminer, Spring Boot profilo Azure):
```bash
# Creazione manuale della immagine
mvn clean install
docker rmi alnao/gestioneannotazioni:latest
docker build -t alnao/gestioneannotazioni:latest .

# Creare la immagine
./script/push-image-docker-hub.sh

# Rilasciare
  ./script/azure-onprem-non-funziona/start-all.sh  

# Distruggere tutto
./script/azure-onprem-non-funziona/stop-all.sh  

# Comandi per la verifica che cosmos sia attivo
curl -v -k https://localhost:8081/_explorer/emulator.pem

# Creazione di un certificato auto-signed (usato per una prova che *ovviamente* non ha funzionato)
openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cosmosdb-cert.crt -days 365 -nodes \
  -subj "/C=IT/ST=IT/L=Local/O=Dev/OU=Dev/CN=cosmosdb"
```

## Errore del certificato
Il Cosmos DB Emulator genera un certificato self-signed con hostname = localhost.
Nel tuo container invece tu ti connetti a: `https://cosmosdb:8081`
➡️ quindi il nome nel certificato non combacia con il nome DNS del container (cosmosdb). Java, durante la handshake SSL, rifiuta il certificato perché non è emesso per "cosmosdb", ma per "localhost". E anche se lo importi, l’host mismatch causa comunque un errore PKIX path building failed. 
Risolto con
- immagine per scaricare il certificato su un volume locale
  ```yaml
    cosmos-cert-downloader:
      image: curlimages/curl:latest
      container_name: cosmos-cert-downloader
      depends_on:
        cosmosdb:
          condition: service_healthy
      user: "0:0"  # 👈 SOLUZIONE: Forza l'uso di root (UID:GID = 0:0)
      command: >
        sh -c "
        echo 'Downloading CosmosDB certificate...';
        curl --insecure https://cosmosdb:8081/_explorer/emulator.pem -o /certs/cosmosdb-cert.crt &&
        chmod 644 /certs/cosmosdb-cert.crt &&
        echo 'Certificate downloaded successfully' &&
        cat /certs/cosmosdb-cert.crt && 
        ls -lh /certs/cosmosdb-cert.crt
        "
      volumes:
        - cosmos_certs:/certs
      networks:
        - azure-network
  ```
- importazione del certificato
  ```yaml
    app:
      image: alnao/gestioneannotazioni:latest
      container_name: gestioneannotazioni-app-azure
      depends_on:
        sqlserver:
          condition: service_healthy
        cosmosdb:
          condition: service_healthy
        cosmos-cert-downloader:
          condition: service_completed_successfully
      extra_hosts:
        - "cosmosdb:127.0.0.1"
      environment:
        JAVA_OPTS: >-
          -Xms256m -Xmx512m -Dio.netty.handler.ssl.noOpenSsl=true
          -Djavax.net.ssl.trustStoreType=JKS
          -Djavax.net.ssl.trustStore=
          -Djavax.net.ssl.trustStorePassword=
          -Dcom.azure.cosmos.implementation.ignoreInvalidSSL=true
        # Profilo Spring Boot
        SPRING_PROFILES_ACTIVE: azure

      # Comando per importare il certificato prima di avviare l'app
      command: >
        sh -c "
          echo '📋 Importing CosmosDB certificate...';
          ls -la /certs/;
          cp /certs/cosmosdb-cert.crt /usr/local/share/ca-certificates/;
          CACERTS_PATH=$$(find /usr -name cacerts 2>/dev/null | head -n 1);
          echo \"Found cacerts at: $$CACERTS_PATH\";
          keytool -import -trustcacerts -noprompt \
            -alias cosmosdb-emulator \
            -file /certs/cosmosdb-cert.crt \
            -keystore $$CACERTS_PATH \
            -storepass changeit;
          echo '✅ Certificate imported successfully';
          echo '🔍 Listing certificates in Java truststore...';
          keytool -list -keystore $$CACERTS_PATH -storepass changeit | grep cosmosdb || echo 'cosmosdb cert not found';
          update-ca-certificates;
          echo '🚀 Starting application...';
          su -s /bin/sh appuser -c \"java $$JAVA_OPTS -jar /app/app.jar\"
        "
      volumes:
        - cosmos_certs:/certs
      ports:
        - "8085:8080"
      networks:
        - azure-network
      healthcheck:
        test: curl -f http://localhost:8080/actuator/health || exit 1
        interval: 30s
        timeout: 10s
        retries: 3
        start_period: 40s
  ```
- comandi vari docker
  ```bash
  # esecuzione della curl via docker
  docker run --rm \
    --network gestioneannotazioni-azure-network \
    -v cosmos_certs:/cosmos-certs \
    -u "0:0" \
    curlimages/curl:latest \
    sh -c "curl --insecure https://cosmosdb:8081/_explorer/emulator.pem -o /cosmos-certs/cosmosdb-cert.crt && \
          chmod 644 /cosmos-certs/cosmosdb-cert.crt && \
          ls -lh /cosmos-certs/"
  # esecuzione della applicazione a mano
  docker run --rm --name test-azure \
    --network gestioneannotazioni-azure-network \
    --add-host cosmosdb:127.0.0.1 \
    -e JAVA_OPTS="-Xms256m -Xmx512m -Dio.netty.handler.ssl.noOpenSsl=true -Djavax.net.ssl.trustStoreType=JKS -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword=  -Dcom.azure.cosmos.implementation.ignoreInvalidSSL=true " \
    -e SPRING_PROFILES_ACTIVE=azure \
    -e LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=DEBUG \
    -e LOGGING_LEVEL_IT_ALNAO=DEBUG \
    -e SPRING_DATASOURCE_URL="jdbc:sqlserver://sqlserver:1433;databaseName=master;encrypt=false;trustServerCertificate=true" \
    -e SPRING_DATASOURCE_USERNAME=sa \
    -e SPRING_DATASOURCE_PASSWORD=GestioneAnnotazioni123! \
    -e AZURE_COSMOS_URI=https://cosmosdb:8081 \
    -e AZURE_COSMOS_KEY=C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw== \
    -v cosmos_certs:/certs \
    -u root \
    -p 8085:8080 \
    --entrypoint sh \
    alnao/gestioneannotazioni:latest \
    -c "echo '📋 Importing CosmosDB certificate...'; \
        ls -la /certs/ && \
        cp /certs/cosmosdb-cert.crt /usr/local/share/ca-certificates/ && \
        CACERTS_PATH=\$(find /usr -name cacerts 2>/dev/null | head -n 1); \
        echo \"Found cacerts at: \$CACERTS_PATH\"; \
        keytool -import -trustcacerts -noprompt \
          -alias cosmosdb-emulator \
          -file /certs/cosmosdb-cert.crt \
          -keystore \$CACERTS_PATH \
          -storepass changeit && \
        echo '✅ Certificate imported successfully'; \
        echo '🔍 Listing certificates in Java truststore...'; \
        keytool -list -keystore /opt/java/openjdk/lib/security/cacerts -storepass changeit | grep cosmosdb; \
        update-ca-certificates && \
        echo '✅ Certificate imported successfully'; \
        echo '🚀 Starting application...'; \
        su -s /bin/sh appuser -c 'java \$JAVA_OPTS -jar /app/app.jar'"
  # Altro comando per eseguire l'aggplcazione con import del ssl-truststore
  docker run  --name test-azure \
    --network gestioneannotazioni-azure-network \
    --add-host cosmosdb:127.0.0.1 \
    -e JAVA_OPTS="-Xms256m -Xmx512m -Dio.netty.handler.ssl.noOpenSsl=true -Djavax.net.ssl.trustStoreType=JKS -Djavax.net.ssl.trustStore= -Djavax.net.ssl.trustStorePassword=  -Dcom.azure.cosmos.implementation.ignoreInvalidSSL=true " \
    -e SPRING_PROFILES_ACTIVE=azure \
    -e LOGGING_LEVEL_ORG_SPRINGFRAMEWORK=DEBUG \
    -e LOGGING_LEVEL_IT_ALNAO=DEBUG \
    -e SPRING_DATASOURCE_URL="jdbc:sqlserver://sqlserver:1433;databaseName=master;encrypt=false;trustServerCertificate=true" \
    -e SPRING_DATASOURCE_USERNAME=sa \
    -e SPRING_DATASOURCE_PASSWORD=GestioneAnnotazioni123! \
    -e AZURE_COSMOS_URI=https://cosmosdb:8081 \
    -e AZURE_COSMOS_KEY=C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw== \
    -v cosmos_certs:/certs \
    -u root \
    -p 8085:8080 \
    alnao/gestioneannotazioni:latest
  # Collegarsi alla immagine
  docker exec -it gestioneannotazioni-app-azure /bin/bash
  ```
- comandi vari da eseguire dentro all'immagine cosmos per tentare di collegarsi
  ```bash
  apt-get install python  python3-pip nano
  pip3 install azure-cosmos --break-system-packages
  python3 -c "from azure.cosmos import CosmosClient; print('CosmosClient installed')"
  python3 -c "from azure.cosmos import CosmosClient; c=CosmosClient('cu','C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==', connection_verify=False, request_timeout=30); print(list(c.list_databases()))"
  python3 -c "
  from azure.cosmos import CosmosClient
  client = CosmosClient(
      'https://cosmosdb:8081',
      credential='C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==',
      connection_verify=False,
      request_timeout=30
  )
  print(list(client.list_databases()))
  "
  ```

- comandi vari su docker
  ```bash
  docker exec -it debug-cosmos sh
  docker exec -it gestioneannotazioni-cosmosdb

  apt-get update
  apt-get install -y curl iputils-ping
  apt-get install -y python3  python3-pip nano
  pip3 install azure-cosmos

  curl -v -k "https://cosmos-emulator:8081/_explorer/emulator.pem"

  python3 -c "
  from azure.cosmos import CosmosClient
  client = CosmosClient(
      'https://cosmos-emulator:8081',
      credential='C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==',
      connection_verify=False,
      request_timeout=30
  )
  print(list(client.list_databases()))
  "
  ```
- prova di docker-compose 
  ```yaml
  cosmosdb:
    #image: mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:latest
    image:  mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:vnext-preview
    container_name: gestioneannotazioni-cosmosdb
    environment:
      AZURE_COSMOS_EMULATOR_PARTITION_COUNT: 10
      AZURE_COSMOS_EMULATOR_IP_ADDRESS_OVERRIDE: "0.0.0.0"
    ports:
      - "8081:8081"   # CosmosDB Emulator API
      - "10251:10251" # Data Explorer
      - "10252:10252" # Mongo API
      - "10253:10253" # Table API
      - "10254:10254" # Cassandra API
      - "10255:10255" # Gremlin API
    volumes:
      - cosmosdb_data:/tmp/cosmos
    networks:
      - azure-network
    healthcheck:
      test: ["CMD-SHELL", "curl -k https://127.0.0.1:8081/ -w '%{http_code}' -o /dev/null -s | grep -E '(200|401|403)' || exit 1"]
      interval: 30s
      timeout: 10s
      retries: 10
      start_period: 60s
  cosmos-emulator:
    image: mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:vnext-preview
    container_name: cosmos-emulator
    # Avvia l'emulatore con protocollo HTTPS (necessario per l'SDK Java)
    command: --protocol https
    ports:
      # Porta principale di Cosmos DB
      - "8081:8081"
      # Porta di Data Explorer (interfaccia web)
      - "1234:1234" 
    environment:
      # Abilita la persistenza dei dati
      - AZURE_COSMOS_EMULATOR_ENABLE_DATA_PERSISTENCE=true
    volumes:
      # Mappa il volume per la persistenza dei dati
      - cosmos_data:/cosmos/data
      # Mappa il volume per salvare i certificati generati
      - cosmos_certs:/cosmos/certs
    networks:
      - azure-network
    restart: always
    healthcheck:
      test: ["CMD-SHELL", "curl -k -f https://localhost:8081/_explorer/index.html || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 5
  ```
- prova che non funziona di esecuzione di cosmos
  ```bash

  docker run -it --rm \
    --network gestioneannotazioni-azure-network \
    mongo:7.0 \
    mongosh "mongodb://cosmosdb:10255/?ssl=true&replicaSet=globaldb&retrywrites=false&maxIdleTimeMS=120000" \
    --username localhost \
    --password 'C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==' \
    --tlsAllowInvalidCertificates

  use testdb
  db.testcollection.insertOne({ name: "Hello CosmosDB" })
  db.testcollection.find()

  docker run --rm \
    --network host \
    mongo:7.0 \
    mongosh "mongodb://cosmosdb:10255/gestioneannotazioni?ssl=true&replicaSet=globaldb&retrywrites=false" \
    --username yourUser \
    --password 'C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==' \
    --tlsAllowInvalidCertificates \
    --eval 'db.test.insertOne({message: "Hello from Docker!"})'
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
