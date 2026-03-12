# Sistema di Gestione annotazioni - roadmap and todo-list

  <p align="center">
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=black"  height=60/>
    <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white"  height=60/>
  </p>

Progetto realizzato da `< AlNao />` come esempio pratico con Java Spring Boot: consente di creare, modificare e visualizzare annotazioni, utenti con privilegi da moderatore possono confermare le annotazioni e utenti con privilegi da amministratori possono confermare e *inviare* annotazioni a sistemi esterni.

# 📝 Roadmap & todo-list

- ✅ ⚙️ Creazione progetto con maven, creazione dei moduli adapter, adapter web con pagina web di esempio, test generale di esecuzione
  - ✅ 📝 Funzione di modifica annotazioni con registro con precedenti versioni delle note
  - ✅ 📖 Configurazione di OpenApi-Swagger e Quality-SonarQube, test coverage e compilazione dei moduli
  - ✅ 📦 Creazione adapter con implementazione con SQLite come unica base dati
    - ✅ ☁️ Sviluppo script per esecuzione profilo sqlite in sistema Replit
    - ✅ ⚙️ Sviluppo script per esecuzione profilo sqlite in sistema AWS-EC2 con Docker senza RDS e Dynamo
    - ✅ 🧿 Script per creazione di tre profili in ogni ambiente per adapter sqlite
    - ✅ 📖 Sviluppo adapter-frontend con JavaFx per solo profilo sqlite
  - ✅ 🤖 Gestione dell'applicazione in *gestione annotazioni* e test applicazione web di esempio
    - ✅ 🛠️ Test applicazione web di esempio anche su AWS
    - ✅ 🔧 Modifica nome adapter "app" e "port" in "application" e "core"
    - ✅ 🎯 Creazione portService, modifica ai Controller-api e spostamento logiche dai Controller nei Service nel core
- ✅ 🔧 Creazione enum Stato di una annotazione
  - ✅ 🔄 Aggiunta campo "Stato" nei metadati delle annotazioni nelle tabelle
  - ✅ 🧮 Nuova tabella StoricoStati, sviluppo service e port per la gestione dello storico
  - ✅ 🕸️ Modifica service per cambio stato che modifica il metadata e non il valore più la tabella storico
  - ✅ 🧩 Service per modificar lo stato con salvataggio nella tabella StoricoStati
  - ✅ 🧑‍🔬 Inserimento di una nuova annotazione in stato INSERITA
  - ✅ 🛰️ Gestione dello stato DAINVIARE come ultimo stato possibile da API/Web.
  - ✅ 🧱 Verifica che utenti non possano fare operazioni il cui ruolo non lo prevede
    - Test Eseguito: chiamata transazione `http://localhost:8082/api/annotazioni/xxx-xxx-xxx-xxx-xxx/stato` con PATCH method `{vecchioStato: "CONFERMATA", nuovoStato: "MODIFICATA", utente: "admin"}` e ritornato errore 403 e nei log si vede il messaggio `Transizione non permessa: Transizione non permessa: da CONFERMATA a MODIFICATA per ruolo ADMIN`
- ✅ 🐳 Build e deploy su DockerHub della versione *kube* (ex OnPrem)
  - ✅ 🐳 configurazione di docker-compose con MongoDb e Postgresql
  - ✅ ☸️ Esecuzione su Kubernetes/Minikube locale con yaml dedicati
- ✅ ☁️ Esecuzione con docker-compose della versione AWS su sistema locale con Mysql e DynamoDB 
  - ✅ 🐳 Deploy su AWS usando EC2 per eseguire il container docker, script scritto in AWS-CLI per il provisioning delle risorse necessarie (Aurora-RDS-Mysql e DynamoDB ) e la creazione della EC2 con lancio del docker con `user_data`
  - ✅ 🐳 Deploy su AWS usando ECS, Fargate e repository ECR (senza DockerHub), script scritto in AWS-CLI per il provisioning delle risorse necessarie (Aurora-RDS-Mysql e DynamoDB ) e lancio del task su ECS. Non previsto sistema di scaling up e/o bilanciatore ALB.
- ✅ 🔒 Autenticazione e autorizzazione (Spring Security) e token Jwt
  - ✅ 👥 introduzione sistema di verifica degli utenti e validazione richieste con tabella utenti
  - ✅ 📝 Gestione multiutente e modifica annotazioni con utente diverso dal creatore, test nell'applicazione web
  - ✅ 🛡️ Centralità dei service JwtService e UserService nel core senza `adapter-security`
- ✅ ⚙️ Evoluzione adapter con integrazione con altri sistemi
  - ✅ 🧬 Gestione delle annotazioni in stato INVIATA
  - ✅ 📚 Export annotazioni: creazione service che permetta di inviare notifiche via coda (kafka o sqs) con creazione `adapter-kafka` e che con frequenza invii delle annotazioni concluse con cambio di stato
  - ✅ ☁️ Configurazione del servizio SQS nell'adapter AWS e test nelle versioni EC2 e ECS
- ✅ 🏁 Test finale di tutti i punti precedenti e tag della versione 0.0.1 e inizio versione 0.0.2
  - ✅ 🐳 Cambio nome profilo da *OnPrem* ad *Kube*
  - ✅ 📡 Rilascio immagine 0.0.1 su DockerHub
      ```bash
      docker tag alnao/gestioneannotazioni:latest alnao/gestioneannotazioni:0.0.1
      docker push alnao/gestioneannotazioni:0.0.1
      ```
- ✅ ☁️ Integrazione con Azure
  - ✅ 🔩 Creazione del adapter Azure e sviluppo implementazioni per cosmos e ms-sql server.
  - ✅ 🖥️ Prima esecuzione in locale adapter azure *che non funziona*
  - ✅ ▶️ Script deploy su Azure della versione con cosmos e sqlserver con run in locale
  - ✅ 🎯 Script deploy su Azure della versione con cosmos-mongodb e postgresql con run in locale
  - ✅ 📖 Export annotazioni verso servizio Azure service dockerbus
  - ✅ 🔧 Verifica inserimento in storico stati quando una annotazione viene inviata
  - ✅ 📝 Esportazione delle annotazioni su Azure in sistema code 
  - ✅ 🚀 Script deploy su Azure della versione con cosmos e sqlserver con run in VM-azure
  - ✅ 🛠️ Script deploy su Azure con Azure Container Instances (ACI)
- ✅ 📝 Divisione dei file README in più parti, migrazione a Debian13 con Java21 e test competo di non regressione! 
  - ✅ ⚙️ Migrazione da Java 17 a Java 21 ed esecuzione su Debian 13
  - ✅ 🛠️ Gestione errore esecuzione test locale con `mkdir -p /mnt/Dati4/Workspace/JavaSpringBootExample/application/data`
- 🚧 🗃️ Sistema evoluto di gestione annotazioni
  - ✅ 🧑‍🤝‍🧑 Gestione modifica annotazione con annotazione `@Version` di JPA (vedi Entity AnnotazioneMetadataEntity di Postgresql). *Non funziona perchè il Service esegue un refresh della versione all'interno del metodo aggiornaAnnotazione quindi non andrebbe in errore in caso di contesa*
  - ✅ 👥 Sistema di lock con Redis che impedisce che due utenti modifichino la stessa annotazione allo stesso momento
    - ✅ 🔒 Implementazione Redis con Redisson per profili kube, aws, azure
    - ✅ 💾 Implementazione in-memory per profilo sqlite
    - ✅ 🎯 Gestione eccezioni con HTTP 409 CONFLICT quando annotazione è già in modifica
    - ✅ 🔄 Api per bloccare una annotazione da un utente specifico
    - ✅ 🛠️ Creazione script test specifo per il blocci di annotazioni e integrazione degli script di test 
    - ✅ ☁️ Modifica script profilo AWS per servizio redis on Cloud
    - ✅ ☁️ Modifica script profilo Azure per servizio redis on Cloud
    - ✅ ⚙️ Modifica al frontend per gestire le prenotazioni di una annotazione quando si entra nel dettaglio
    - ✅ ⚙️ Modifica al frontend per visualizzare l'errore specifico se qualcun'altro ha bloccato quella annotazione
    - 🚧 🤖 Test finali del frontend e conclusione processo di prenotazione delle annotazioni!
  - 🚧 🕸️ Gestione invio notifiche singolo se ci sono più istanze dell'applicazione in esecuzione (esempio minikube)
    - ✅ 🛠️ Rinominare l'attuale sistema di lock in "AnnotazioneRedisLockService"
    - ✅ ⚙️ Aggiunta libreria shedlock nel core e creazione classe config nel adapter redis
    - ✅ 🕸️ Gestione con lock schedulazione nei profili cloud
    - ✅ 🔧 Gestione senza lock schedulazione nei profili sqlite
    - ✅ 🎯 Test con profilo sqlite e kube
    - 🚧 🤖 Test con profilo AWS e Azure
  - 🚧 🛠️ Refactor e rimozione del `@Autowired` a favore del injectiont tramite costruttore!
  - 🚧 🔄 Import annotazioni: sistemi di import dati
    - 🚧 📖 Consumer Kafka che legge da un topic e inserisce annotazioni
    - 🚧 📝 Nuovo stato annotazioni "Importata"
    - 🚧 ⚙️ Import annotazioni da un CSV
    - 🚧 🛠️ Import annotazioni da un Json 
    - 🚧 🔧 Sistema di backup and restore tramite export json (che salva in mongo? o db postgres)
    - 🚧 🎯 Test con profilo sqlite e kube
    - 🚧 🤖 Test con profilo AWS e Azure
  - 🚧 🏁 Test finale di tutti i punti precedenti e tag della versione 0.3.0
  - 🚧 🎯 Notifiche real-time (WebSocket): creazione `adapter-notifier` che permetta ad utenti di registrarsi su WebSocket e ricevere notifiche su cambio stato delle proprie annotazioni
    - 🚧 👥 Social Reminders: Notifiche quando qualcuno interagisce con annotazioni modificate
  - 🚧 🧭 Sistema che gestisce la scadenza di una annotazione con spring-batch che elabora tutte le annotazioni rifiutate o scadute, con nuovo stato scadute.
  - 🚧 💾 Backup & Disaster Recovery: Cross-region backup, point-in-time recovery, RTO/RPO compliance
  - 🚧 🔐 OAuth2/OIDC Provider: Integrazione con provider esterni (Google, Microsoft, GitHub) + SSO enterprise
- 🚧 🛡️ Gestione password via secret
  - 🚧 🔒 Gestione password tramite setret di Kubernetes nel profilo Kube
  - 🚧 🔒 Gestione password tramite AWS Secret manager nel profilo aws
  - 🚧 🔒 Gestione password tramite Azure key vault nel profilo azure
  - 🚧 🔒 Gestione password tramite File statici nel profilo sqlite
- 🚧 🏁 Test finale di tutti i punti precedenti e tag della versione 0.3.0
- 🚧 ☸️ Esecuzione su Cloud in infrastruttura Kubernetes
  - 🚧 🤖 Deploy su AWS su EKS del profilo Kube
  - 🚧 📦 Deploy su Azure con Azure Container Apps (ACA non è Kubernetes *ma quasi*)
  - 🚧 ⚙️ Deploy su Azure su AKS del profilo Kube
  - 🚧 🎡 Script deploy su Azure della versione con cosmos-mongo e postgres con run in VM-azure
  - 🚧 🔧 Sistem di Deploy con Kubernetes Helm charts del profilo Kube
  - 🚧 📈 Auto-Scaling Policies: Horizontal Pod Autoscaler (HPA) e Vertical Pod Autoscaler (VPA) per Kubernetes
- 🚧 🎯 Sistema di caricamento annotazioni avanzato, caricare annotazioni tramite stream dati
- 🚧 🗃️ Idee per il futuro
  - 🚧 🏗️ GitOps Workflow: ArgoCD/Flux per deployment automatici, configuration drift detection
  - 🚧 🧪 Testing Pyramid: Unit + Integration + E2E + Performance + Security testing automatizzati
  - 🚧 📎 File Attachments: Supporto allegati (immagini, PDF, documenti) con preview e versioning
  - 🚧 ⚡ Redis Caching Layer: Cache multi-livello (L1: in-memory, L2: Redis) con invalidation strategies e cache warming
  - 🚧 📊 Read Replicas: Separazione read/write con eventual consistency e load balancing intelligente
  - 🚧 🔍 Elasticsearch Integration: Ricerca full-text avanzata con highlighting, auto-complete, ricerca semantica
- Fantasie dell'IA
  - 🚧 📦 Container Security: Vulnerability scanning (Trivy/Snyk), distroless images, rootless containers
  - 🚧 🎯 Feature Flags: LaunchDarkly/ConfigCat integration per feature toggling, A/B testing, gradual
  - 🚧 💬 Comment Threads: Sistema di commenti su singole annotazioni con threading e notifications
  - 🚧 📝 Templates & Forms: Template predefiniti per annotazioni (meeting notes, bug reports, ideas) con campi strutturati
  - 🚧 🔄 Annotation Workflows: Stati delle annotazioni (draft→review→approved→published) con approval process e notifiche
  - 🚧 📅 Smart Date Recognition: NLP per riconoscere date naturali ("domani", "la prossima settimana", "tra 3 giorni") e convertirle in deadline
  - 🚧 ⏰ Time Boxing: Stima automatica del tempo necessario per task basata su annotazioni simili completate
  - 🚧 📈 Progress Tracking: Visualizzazione progresso con barre, percentuali, streak counters
  - 🚧 🔗 Task Dependencies: Link tra annotazioni per gestire sequenze e blocchi
  - 🚧 ⏰ Context-Aware Reminders: Promemoria basati su location, tempo, altre attività ("Ricorda quando arrivi in ufficio")
  - 🚧 Weekly Digest: Riassunto settimanale con achievement, todo completati, annotazioni più accedute
  - 🚧 🎤 Voice Notes: Registrazione audio con trascrizione automatica e timestamp
  - 🚧 📋 Recommendation Engine: Sistema di raccomandazioni basato su ML per suggerire annotazioni correlate, utenti simili, contenuti rilevanti
  - 🚧 🤖 AI-Powered Insights: Integrazione OpenAI/Bedrock per suggerimenti automatici di categorizzazione, sentiment analysis delle note, auto-completamento intelligente
  - 🚧 📱 Mobile-First PWA: Progressive Web App con offline-sync, push notifications, gesture navigation
  - 🚧 🎨 Rich Text Editor: Editor WYSIWYG con markdown, syntax highlighting, embed multimedia, link preview
  - 🚧 ☁️ Serverless Functions: AWS Lambda/Azure Functions per task asincroni (email, reporting, cleanup)
  - 🚧 🔧 Admin Panel: Interfaccia amministrativa per configurazione sistema, user management, monitoring
  - 🚧 📚 Developer Portal: API documentation interattiva, SDK multi-linguaggio, code examples
  - 🚧 📖 Migration Tools: Import/export da altri sistemi, data transformation, migration assistants
  - 🚧 🎤 Voice-to-Text Advanced: Transcription multilingua con speaker identification e emotion detection
  - 🚧 🤖 Conversational Annotation: Chatbot intelligente per creare annotazioni tramite dialogo naturale
  - 🚧 🌱 Carbon Footprint Tracking: Monitoring dell'impatto ambientale dell'infrastruttura
  - 🚧 ♻️ Green Computing Optimization: Automatic migration a data centers con energia rinnovabile
  - 🚧 📊 Sustainability Metrics: KPI per misurare efficienza energetica e carbon impact
  - 🚧 🌿 Eco-Friendly Features: Dark mode per battery saving, compression algorithms, lazy loading




# Test di non regressione
Per ogni modifica, prima del rilascio, *bisognerebbe* eseguire un test di non regressione su tutta l'applicazione. I test da eseguire sono:
- Compilazione e upload/push dell'immagine
  ```bash
  ./script/docker-build.sh 
  ./script/push-image-docker-hub.sh 
  ```
  risultato atteso: nessun errore
- Pulizia globale prima di partire (meglio partire da situazione pulita con volumi vuoti!)
  ```bash
  docker volume rm $(docker volume ls -q)
  ```
- Script generale per eseguire tutti i gli script di test *automatici* su profili sqlite, kube e aws in locale
  ```
  ./script/automatic-test/test-all.sh
  ```
- Profilo `aws` in Cloud AWS con MySql e MySql ed esecuzione su EC2
  ```bash
  ./script/aws-ec2/start-all.sh

  # Recupero indirizzo IP ed esecuzione test sistema prenotazione
  EC2_PUBLIC_IP=$(aws ec2 describe-instances --region "eu-central-1" \
    --filters "Name=tag:gestioneannotazioni-app,Values=true" \
              "Name=instance-state-name,Values=running" \
    --query 'Reservations[0].Instances[0].PublicIpAddress' \
    --output text)
  EC2_PUBLIC_URL="$EC2_PUBLIC_IP:8080"
  ./script/automatic-test/test-prenotazione-annotazione.sh $EC2_PUBLIC_URL

  # Verifica coda SQS
  SQS_QUEUE_NAME=gestioneannotazioni-annotazioni
  SQS_QUEUE_URL=$(aws sqs get-queue-url --queue-name $SQS_QUEUE_NAME --region eu-central-1 --query 'QueueUrl' --output text)
  aws sqs receive-message --queue-url "$SQS_QUEUE_URL" --region eu-central-1 --attribute-names All --message-attribute-names All

  # Eliminazione di tutte le risorse!
  ./script/aws-ec2/stop-all.sh
  ```
  - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- Profilo `azure` in Cloud Azure con MySql e MySql ed esecuzione in locale
  ```bash
  ./script/azure-dbremoti-cosmos-runlocale/start-all.sh

  ./script/azure-dbremoti-cosmos-runlocale/test.sh

  ./script/azure-dbremoti-cosmos-runlocale/stop-all.sh
  ```
  - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- Profilo `azure` in Cloud Azure con MySql e MySql ed esecuzione in VirtualMachine su azure
  ```bash
  ./script/azure-dbremoti-cosmos-vm/start-all.sh

  # Recupero indirizzo IP ed esecuzione test sistema prenotazione
  SERVICE_URL=$(az vm show -d -g gestioneannotazioni-rg-cosmos-mssql \
    -n gestioneannotazioni-vm --query publicIps --output tsv)
  SERVICE_PUBLIC_URL="$SERVICE_URL:8082"
  ./script/automatic-test/test-prenotazione-annotazione.sh $SERVICE_PUBLIC_URL

  ./script/azure-dbremoti-cosmos-aci/test.sh

  ./script/azure-dbremoti-cosmos-vm/stop-all.sh
  ```
  - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- Profilo `kube` in cloud Azure con Postgresql e MongoDB ed esecuzione in locale
  ```bash
  ./script/azure-dbremoti-mongo-runlocale/start-all.sh

  ./script/automatic-test/test-prenotazione-annotazione.sh 
  ./script/azure-dbremoti-mongo-runlocale/test.sh

  ./script/azure-dbremoti-mongo-runlocale/stop-all.sh
  ```
  - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- Profilo `azure` in CLoud Azure con Azure Container Images
  ```
  ./script/azure-dbremoti-cosmos-aci/start-all.sh

  CONTAINER_IP=$(az container show --resource-group "gestioneannotazioni-aci-rg" \
    --name "gestioneannotazioni-aci" --query "ipAddress.ip" --output tsv 2>/dev/null || echo "")
  SERVICE_URL="$CONTAINER_IP:8080"
  ./script/automatic-test/test-prenotazione-annotazione.sh $SERVICE_URL

  ./script/azure-dbremoti-cosmos-aci/test.sh

  ./script/azure-dbremoti-cosmos-aci/stop-all.sh
  ```
  - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- Pulizia finale del sistema docker locale
  ```
  minikube delete
  docker-compose down --remove-orphans
  docker network prune -f
  docker volume rm $(docker volume ls -q)
  docker rmi $(docker images -q)
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



