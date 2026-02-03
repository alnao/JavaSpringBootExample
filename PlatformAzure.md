# Sistema di Gestione annotazioni - Azure

  <p align="center">
    <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=black"  height=60/>
    <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white"  height=60/>
  </p>

Progetto realizzato da `< AlNao />` come esempio pratico con Java Spring Boot: consente di creare, modificare e visualizzare annotazioni, utenti con privilegi da moderatore possono confermare le annotazioni e utenti con privilegi da amministratori possono confermare e *inviare* annotazioni a sistemi esterni.


## ☁️ Esecuzione locale profilo Azure

Ho perso molte ore a capire come far funzionare CosmosDB in locale usando l'immagine ufficiale
```
mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:mongodb
```
ma alla fine ⚠️ **non funziona** ⚠️

C'è una cartella `azure-onprem-non-funziona` dentro alla cartella `script` così come promemoria delle prove fatte e dei comandi eseguiti. Se in futuro la situazione cambierà sistemerò questo esempio.


L’emulatore Linux in Docker è pensato solo per test container-to-container, e non supporta SDK dal host o a volte neanche da container separati se non dal container ufficiale che lo avvia. **CosmosDB Emulator Linux in Docker non implementa correttamente tutte le API richieste dagli SDK esterni.** Funziona solo con richieste dal container stesso o dalla rete interna Docker dove gira l’emulatore.


Microsoft documenta che il Linux Emulator è destinato a test container-to-container, non per SDK dal host o container separati. Alcune porte interne (10250+) devono essere esposte e certificate corrette mappate, cosa che il container ufficiale Linux non fa completamente.


⚠️ Limiti importanti
- Python / Java / C# SDK esterni → non funzionano correttamente su Linux Emulator fuori dal container ufficiale.
- curl funziona solo perché fa richieste HTTP molto semplici.
- MongoDB endpoint funziona meglio perché l’emulatore implementa almeno un protocollo minimale Mongo compatibile, ma anche qui bisogna fare tutto container-to-container.
- Non esistono workaround affidabili per usare l’SDK dal host Linux/macOS o da container separati senza problemi di TLS/JSON RPC.


### 🚀 Esecuzione locale profilo Azure con db remoti su Azure
Script bash per la creazione automatica di risorse Azure (CosmosDB + SQL Server + ServiceBus) ed esecuzione dell'applicazione Spring Boot in locale con Docker.
- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- 📋 **Prerequisiti**
  - Azure CLI installato e autenticato (`az login`)
  - Docker installato e in esecuzione
  - Immagine Docker `alnao/gestioneannotazioni:latest`
- Componenti creati dallo script
  0. **Login**: per essere eseguito necessita della login eseguita con il comando `az login`
  1. **Crea Resource Group** su Azure nella regione North Europe
  2. **Provisiona CosmosDB** (tier Free) con database e container per annotazioni
  3. **Provisiona SQL Server** (tier Basic) con database per metadati e autenticazione
  4. **Configura Firewall** per accesso locale e servizi Azure
  5. **Inizializza Database** con tabelle (`users`, `annotazione_metadata`, `storico_stati`) e utenti di test
  6. **Provisiona ServiceBus** come servizio per la gestione delle code di invio annotazioni
  7. **Azure Cache for Redis** come servizio di gestione delle prenotazioni delle annotazioni
  8. **Avvia Container Docker** nel sistema locale (non nel cloud) con configurazione automatica
- ▶️ Esecuzione
  ```bash
  ./script/azure-dbremoti-cosmos-runlocale/start-all.sh
  ```
  - ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- Esecuzione dei due script di test (in locale si può provare anche il sistema di prenotazione!)
  ```bash
  ./script/azure-dbremoti-cosmos-runlocale/test.sh
  ./script/automatic-test/test-prenotazione-annotazione.sh
  ```
- Rimozione completa
  ```bash
  ./script/azure-dbremoti-cosmos-runlocale/stop-all.sh
  ```
- ⚠️ Note importanti
  - CosmosDB Free Tier: Limitato a 1000 RU/s e 25GB storage. Solo 1 account Free per subscription.
  - SQL Server Basic: 5 DTU e 2GB storage. Costo stimato: ~5€/mese.
  - Firewall: Lo script configura l'accesso dal tuo IP. Aggiorna la regola se l'IP cambia.
  - Il tier Basic C0 è il minimo disponibile per Azure Cache for Redis
  - Connection String: Salva le connection string restituite dai comandi 8 e 13 in modo sicuro.
    - Password: Cambia P@ssw0rd123! con una password sicura prima di eseguire.
  - La tabella non include costi di bandwidth in/out (inclusi nei limiti Azure)
  - ⚠️ Costi: Anche con tier Free/Basic, SQL Server ha costi mensili. Monitorare sempre i costi ⚠️
    | Risorsa | Tier/SKU | Costo Orario | Costo Giornaliero | Costo Settimanale | Costo Mensile |
    |---------|----------|--------------|-------------------|-------------------|---------------|
    | **Cosmos DB SQL API** | Free Tier (1000 RU/s, 25GB) | €0.00 | €0.00 | €0.00 | **€0.00** |
    | **SQL Server Basic** | 5 DTU, 2GB | €0.0068 | €0.16 | €1.14 | **~€5.00** |
    | **Storage** | 2GB incluso | €0.00 | €0.00 | €0.00 | €0.00 |
    | **Azure Cache for Redis** | Basic C0 (250MB) | €0.0168 | €0.40 | €2.82 | **~€12.10** |
    | **Egress Data** | <100GB/mese | ~€0.00 | ~€0.01 | ~€0.07 | **~€0.30** |
    | **TOTALE 24/7** | | **€0.0236/h** | **€0.57/day** | **€4.03/week** | **~€17.40/mese** |


### 🚀 Esecuzione locale profilo Kube con db remoti su Azure
Script bash per la creazione automatica di risorse Azure con profilo *kube* (Cosmos con compatibilità Mongo e Postgresql) ed esecuzione dell'applicazione Spring Boot in locale con Docker.
- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- 📋 **Prerequisiti**
  - Azure CLI installato e autenticato (`az login`)
  - Docker installato e in esecuzione
  - Immagine Docker `alnao/gestioneannotazioni:latest`
- Componenti creati dallo script
  0. **Login**: per essere eseguito necessita della login eseguita con il comando `az login`
  1. **Crea Resource Group** su Azure nella regione North Europe
  2. **Provisiona CosmosDB con compatibilità MongoDb** (tier Free) con database e container per annotazioni
  3. **Provisiona Postgresql** con database per metadati e autenticazione
  4. **Configura Firewall** per accesso locale e servizi Azure
  5. **Inizializza Database** con tabelle (`users`, `annotazione_metadata`, `storico_stati`) e utenti di test
  6. **Azure Cache for Redis** come servizio di gestione delle prenotazioni delle annotazioni
  7. **Eventhubs Kafka**  come servizio per la gestione delle code di invio annotazioni
    - tier Basic: non supporta Kafka, max 1 Consumer Group, retention 1 giorno
    - tier Standard: supporta Kafka, max 20 Consumer Groups, retention fino a 7 giorni
  8. **Avvia Container Docker** con configurazione automatica
- ▶️ Esecuzione
  ```bash
  ./script/azure-dbremoti-mongo-runlocale/start-all.sh
  ```
  - Esecuzione dei due script di test (in locale si può provare anche il sistema di prenotazione!)
    ```bash
    ./script/azure-dbremoti-mongo-runlocale/test.sh
    ./script/automatic-test/test-prenotazione-annotazione.sh
    ```
    - Nota: questi script necessita `kcat` installato perchè azure-cli non ha un comando specifico per leggere dal eventhubs-kafka. Per esempio si può lanciare
      ```
      # Verificare la coda
      kcat -b gestioneannotazioni-eventhubs.servicebus.windows.net:9093 \
        -X security.protocol=SASL_SSL \
        -X sasl.mechanisms=PLAIN \
        -X sasl.username='$ConnectionString' \
        -X sasl.password='xxxxxxxxxxxxxxxxxxxxxxxxxx' \
        -L
      # Consumare il primo messaggio dalla coda!
      kcat -b gestioneannotazioni-eventhubs.servicebus.windows.net:9093 \
        -X security.protocol=SASL_SSL \
        -X sasl.mechanisms=PLAIN \
        -X sasl.username='$ConnectionString' \
        -X sasl.password='xxxxxxxxxxxxxxxxxxxxxxxxxx' \
        -C -t "annotazioni-export" \
        -o beginning
        -c 1
        -L
      # Produrre un messaggio
      echo "your message here" | kcat -b gestioneannotazioni-eventhubs.servicebus.windows.net:9093 \
        -t annotazioni-export \
        -X security.protocol=SASL_SSL \
        -X sasl.mechanisms=PLAIN \
        -X sasl.username='$ConnectionString' \
        -X sasl.password='xxxxxxxxxxxxxxxxxxxxxxxxxx' \
        -P 
      ```
  - Verifica che la coda kafka sia correttamente creata
    ```
    az eventhubs eventhub list --resource-group "gestioneannotazioni-rg-mongo-postgres" --namespace-name "gestioneannotazioni-eventhubs" --query "[].name"
    ```
- Rimozione completa
  ```bash
  ./script/azure-dbremoti-mongo-runlocale/stop-all.sh
  ```

- ⚠️ Note importanti
  - ⚠️ Costi: Anche con tier Free/Basic, le risorse potrebbero avere dei costi. Monitorare sempre i costi ⚠️
    | Risorsa                   | Tier/SKU                | Costo Orario | Costo Giornaliero | Costo Settimanale | Costo Mensile |
    |---------------------------|-------------------------|--------------|-------------------|-------------------|---------------|
    | **Cosmos DB MongoDB API** | Free Tier (1000 RU/s)   | €0.00   | €0.00            | €0.00            | **€0.00**     |
    | **PostgreSQL Flexible**   | Standard_B1ms           | €0.0165      | €0.40             | €2.77            | **~€12.00**   |
    | **Event Hubs (Kafka)**    | Standard (1 TU)         | €0.0337      | €0.81             | €5.65            | **~€24.30**   |
    | **Azure Cache for Redis** | Basic C0 (250MB)        | €0.0168      | €0.40             | €2.82            | **~€12.10**   |
    | **Storage PostgreSQL**    | 32GB                    | €0.0048      | €0.12             | €0.81            | **~€3.50**    |
    | **Backup**                | 32GB (7 giorni)         | €0.0014      | €0.03             | €0.24            | **~€1.00**    |
    | **Egress Data**           | <100GB/mese             | ~€0.00       | ~€0.01            | ~€0.07           | **~€0.30**    |
    | **TOTALE 24/7**           |            | **€0.0732/h**| **€1.77/day**     | **€12.36/week**  | **~€53.20/mese** |


### 🚀 Esecuzione su VirtualMachine Azure del profilo Azure
Script bash per la creazione automatica di risorse Azure (CosmosDB + SQL Server + ServiceBus) ed esecuzione dell'applicazione Spring Boot in una Virtual Machine su Azure
- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- 📋 **Prerequisiti**
  - Azure CLI installato e autenticato (`az login`)
  - Docker installato e in esecuzione
  - Immagine Docker `alnao/gestioneannotazioni:latest`
- Componenti creati dallo script
  0. **Login**: per essere eseguito necessita della login eseguita con il comando `az login`
  1. **Crea Resource Group** su Azure nella regione North Europe
  2. **Provisiona CosmosDB** (tier Free) con database e container per annotazioni
  3. **Provisiona SQL Server** (tier Basic) con database per metadati e autenticazione
  4. **Configura Firewall** per accesso locale e servizi Azure
  5. **Inizializza Database** con tabelle (`users`, `annotazione_metadata`, `storico_stati`) e utenti di test
  6. **Provisiona ServiceBus** come servizio di code per la gestione delle code di invio annotazioni
  7. **Azure Cache for Redis** come servizio di cache per la gestione del sistema di prenotazioni delle annotazioni 
  8. **Virtual Machine** come macchine virtuale dove viene eseguito l'immagine docker del servizio
  9. **Configurazione rete** per accesso della VM verso il database SQL

- ▶️ Esecuzione
  ```bash
  ./script/azure-dbremoti-cosmos-vm/start-all.sh
  ```
  - Esecuzione del test automatico
    ```bash
    ./script/azure-dbremoti-cosmos-aci/test.sh
    ```
- Rimozione completa
  ```bash
  ./script/azure-dbremoti-cosmos-vm/stop-all.sh
  ```
- ⚠️ Note importanti
  - CosmosDB Free Tier: Limitato a 1000 RU/s e 25GB storage. Solo 1 account Free per subscription.
  - SQL Server Basic: 5 DTU e 2GB storage. Costo stimato: ~5€/mese.
  - VM Standard_B1s: 1 vCPU, 1GB RAM. Ideal per carichi leggeri con burstable performance.
  - Firewall: Lo script configura l'accesso dal tuo IP e dalla VM. Aggiorna le regole se l'IP cambia.
  - Password: Cambia P@ssw0rd123! con una password sicura prima di eseguire.
  - Connection String: Salva le connection string restituite dai comandi in modo sicuro.
  - SSH Key: La chiave SSH viene generata automaticamente in `$HOME/.ssh/azure-vm-key.pub`
  - ⚠️ Costi: Con l'aggiunta della VM, i costi aumentano. Spegni la VM quando non la usi per risparmiare. ⚠️
    | Risorsa | Tier/SKU | Costo Orario | Costo Giornaliero | Costo Settimanale | Costo Mensile |
    |---------|----------|--------------|-------------------|-------------------|---------------|
    | **Cosmos DB SQL API** | Free Tier (1000 RU/s, 25GB) | €0.00 | €0.00 | €0.00 | **€0.00** |
    | **SQL Server Basic** | 5 DTU, 2GB | €0.0068 | €0.16 | €1.14 | **~€5.00** |
    | **VM Standard_B1s** | 1 vCPU, 1GB RAM | €0.0105 | €0.25 | €1.76 | **~€7.60** |
    | **VM IP Pubblico Standard** | Static IP | €0.0043 | €0.10 | €0.72 | **~€3.10** |
    | **VM OS Disk** | 32GB Standard HDD | €0.0006 | €0.01 | €0.10 | **~€0.40** |
    | **Storage SQL** | 2GB incluso | €0.00 | €0.00 | €0.00 | €0.00 |
    | **Service Bus Standard** | 12.5M ops/mese | €0.0118 | €0.28 | €1.99 | **~€8.50** |
    | **Azure Cache for Redis** | Basic C0 (250MB) | €0.0168 | €0.40 | €2.82 | **~€12.10** |
    | **Egress Data** | <100GB/mese | ~€0.00 | ~€0.01 | ~€0.07 | **~€0.30** |
    | **TOTALE 24/7** | | **€0.0508/h** | **€1.21/day** | **€8.60/week** | **~€37.00/mese** |


### 🚀 Esecuzione su Azure Container Instances del profilo Azure

Script bash per la creazione automatica di risorse Azure (CosmosDB + SQL Server) ed esecuzione dell'applicazione Spring Boot in un **Azure Container Instance (ACI)**

- ⚠️ L'esecuzione di questo profilo on cloud potrebbe causare costi indesiderati ⚠️
- 📋 **Prerequisiti**
  - Azure CLI installato e autenticato (`az login`)
  - Immagine Docker `alnao/gestioneannotazioni:latest`. Sostituito con Amazon Container Registry perchè: 
    - Please be aware that Docker Hub has recently introduced a pull rate limit on Docker images. When specifying an image from the Docker Hub registry, this may impact your container instance. [Learn more](https://docs.docker.com/docker-hub/download-rate-limit).
- Le risorse create da questo esempio sono:
  | Name | Type |
  | -----|------|
  | gestioneannotazioni-logs | Microsoft.OperationalInsights/workspaces |
  | gestioneannotazioni-cosmos | Microsoft.DocumentDB/databaseAccounts |
  | gestioneannotazioni-sql | Microsoft.Sql/servers |
  | gestioneannotazioni-sql/gestioneannotazioni | Microsoft.Sql/servers/databases |
  | gestioneannotazioni-sql/master | Microsoft.Sql/servers/databases |
  | gestioneannotazioniacisa | Microsoft.Storage/storageAccounts |
  | gestioneannotazioniacr | Microsoft.ContainerRegistry/registries |
  | gestioneannotazioni-servicebus | Microsoft.ServiceBus/namespaces |
  | gestioneannotazioni-redis | Microsoft.Cache/Redis |
  | gestioneannotazioni-aci | Microsoft.ContainerInstance/containerGroups |

- ▶️ **Esecuzione**
  ```bash
  ./script/azure-dbremoti-cosmos-aci/start-all.sh
  ```
  - Esecuzione del test automatico
    ```bash
    ./script/azure-dbremoti-cosmos-aci/test.sh
    ```
- Rimozione completa
  ```bash
  ./script/azure-dbremoti-cosmos-aci/stop-all.sh
  ```
- ⚠️ **Note importanti**
  - CosmosDB Free Tier: Limitato a 1000 RU/s e 25GB storage. Solo 1 account Free per subscription.
  - SQL Server Basic: 5 DTU e 2GB storage. Costo stimato: ~5€/mese.
  - ACI: Container eseguito in subnet privata, accessibile solo dal tuo IP (configurato automaticamente).
  - Firewall: Lo script configura l'accesso dal tuo IP. Aggiorna la regola se l'IP cambia.
  - Password: Cambia P@ssw0rd123! con una password sicura prima di eseguire.
  - Connection String: Salva le connection string restituite dai comandi in modo sicuro.
  - ⚠️ Costi: Anche con tier Free/Basic, SQL Server e ACI hanno costi mensili. Monitorare sempre i costi ⚠️
    | Servizio | SKU/Tier | Free Tier | Costo Giornaliero | Costo Settimanale | Costo Mensile | Note |
    |----------|----------|-----------|-------------------|-------------------|---------------|------|
    | **Resource Group** | Standard | ✅ Sempre gratuito | €0.00 | €0.00 | €0.00 | Container logico |
    | **Azure Cosmos DB** | Free Tier | ✅ Sì (400 RU/s + 25GB) | €0.00 | €0.00 | €0.00 | 2 container x 400 RU/s = 800 RU/s totali |
    | **Azure SQL Server** | Server | ✅ Solo server | €0.00 | €0.00 | €0.00 | Il server stesso è gratuito |
    | **Azure SQL Database** | Basic (2GB) | ❌ No | €0.17 | €1.19 | €5.10 | 5 DTU, max 2GB storage |
    | **Azure Container Instances** | 1 vCPU, 2GB RAM | ⚠️ Parziale | €1.03 | €7.21 | €30.90 | ~€0.043/ora (24h/giorno) |
    | **Azure Container Registry** | Basic | ⚠️ Primi 12 mesi | €0.17 | €1.19 | €5.10 | 10GB storage inclusi |
    | **Storage Account** | Standard LRS | ⚠️ Primi 12 mesi | €0.01 | €0.07 | €0.30 | Quota minima per share |
    | **Log Analytics Workspace** | Pay-as-you-go | ⚠️ 5GB/mese free | €0.03 | €0.21 | €0.90 | ~100MB/giorno stimati |
    | **Service Bus** | Standard | ❌ No | €0.28 | €1.99 | €8.50 | 12.5M operazioni base incluse |
    | **Azure Cache for Redis** | Basic C0 (250MB) | ❌ No | €0.40 | €2.82 | €12.10 | Cache distribuita, single node |
    | **Virtual Network** | Standard | ✅ Sempre gratuito | €0.00 | €0.00 | €0.00 | Solo se `CREATE_VNET=SI` |
    | **Network Security Group** | Standard | ✅ Sempre gratuito | €0.00 | €0.00 | €0.00 | Solo se `CREATE_VNET=SI` |
    | **Bandwidth (Egress)** | Zone 1 (EU) | ⚠️ 100GB/mese free | €0.05 | €0.35 | €1.50 | Stima 2GB/mese oltre free tier |
    | **TOTALE STIMATO** | | | **€2.17/giorno** | **€15.31/settimana** | **~€65.40/mese** | |





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



