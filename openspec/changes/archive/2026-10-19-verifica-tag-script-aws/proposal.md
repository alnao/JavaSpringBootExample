## Why

La spec `provisioning-aws-tag` richiede che ogni risorsa AWS creata dagli
script del repository porti i sei tag standard (`Name`, `Environment`,
`Project`, `Owner`, `CostCenter`, `ManagedBy`), ma oggi non è così: lo script
`script/sqlite-ec2/start-all.sh` crea l'istanza EC2 con due tag scritti a mano
e lascia i volumi senza tag, e nessuno dei due stack EC2 tagga l'Elastic IP
associato. I task 5.1 e 5.2 della change archiviata `tag-risorse-aws`
risultano completati, ma nel codice quelle parti non ci sono. Senza i tag le
risorse sfuggono ai filtri di costo e di inventario per `Project`, `Owner` e
`CostCenter`.

Manca anche un controllo che impedisca di ricadere nello stesso errore: oggi
l'unico modo di accorgersene è guardare le risorse in console dopo averle
create, e pagate.

## What Changes

- Censimento completo dei comandi che creano risorse negli script sh del
  repository (`aws ... create-*`, `run-instances`, `run-task`,
  `register-task-definition`, `eksctl create`, ...), con l'esito per ciascuno
  riportato nel design.
- `script/sqlite-ec2/start-all.sh`: istanza con i sei tag standard
  (`Name=gestioneannotazioni-sqlite-ec2`) più il tag marcatore, volumi con
  `Name=gestioneannotazioni-sqlite-ec2-volume`, usando le funzioni di
  `script/aws-tags.sh` come fa già `script/aws-ec2/start-all.sh`.
- `script/aws-ec2/start-all.sh` e `script/sqlite-ec2/start-all.sh`: dopo
  l'avvio dell'istanza, tag dell'Elastic IP eventualmente associato, come
  richiede la spec (nessun Elastic IP allocato dagli script).
- `script/init-database/init-dynamodb.sh` (tabelle DynamoDB **locali** di
  `aws-onprem`): stessi tag delle tabelle create su AWS, solo per uniformità
  fra gli script. Verificato prima della change: DynamoDB Local accetta i tag
  in `create-table` ma non li salva (`ListTagsOfResource` e `TagResource`
  rispondono "Tagging is not currently supported in DynamoDB Local"), quindi
  sulle tabelle locali i tag non sono leggibili; conta che la creazione non
  fallisca.
- Nuovo controllo statico in `script/automatic-test/`: analizza gli script sh
  e fallisce, elencando file e riga, se un comando che crea una risorsa non
  porta i tag standard nel comando stesso o con un comando di tagging dedicato.
  Non fa chiamate AWS e gira in locale.

## Capabilities

### New Capabilities

Nessuna.

### Modified Capabilities

- `provisioning-aws-tag`: si aggiungono due requisiti, la verifica statica dei
  tag negli script e i tag nei comandi che creano le tabelle DynamoDB locali
  di `aws-onprem`. I requisiti esistenti (sei tag, `Name`, marcatori, Elastic
  IP) non cambiano: la change porta il codice a rispettarli.

## Profili coinvolti

- `aws`: coinvolto (stack `aws-ec2`, `aws-ecs`, `aws-eks`, `aws-onprem` e
  script di servizio in `script/aws-ecs`).
- `sqlite`: coinvolto solo per il deploy su AWS `script/sqlite-ec2`.
- `kube`, `azure`: non coinvolti, vedi Non-goals.

Tocca soltanto script di provisioning e di test: né il `core` né gli adapter.

## Non-goals

- **Profili `kube` e `azure`**: non creano risorse AWS. I tag delle risorse
  Azure sono un tema separato, non coperto dalla spec `provisioning-aws-tag`.
- **Stack Terraform** (`script/aws-terraform-ec2`, `script/aws-terraform-ecs`):
  i tag arrivano da `default_tags` del provider e sono già verificati dalle
  rispettive spec. Il controllo statico copre solo i comandi aws/eksctl negli
  script sh, compresi i wrapper Terraform.
- **Verifica su cloud** (lettura dei tag delle risorse reali con le API AWS):
  scartata per questa change. Le risorse create "a cascata" da `eksctl` (VPC,
  NAT gateway, nodi) ereditano i tag dallo stack CloudFormation e restano
  verificabili solo in console.
- **Ritaggare risorse già esistenti**: come prevede la spec (rilancio
  idempotente), gli script non ritaggano risorse create prima della change.
- **Cartella `script/azure-onprem-non-funziona`**: fuori perimetro come nelle
  change precedenti.

## Impact

- **Core e adapter**: non toccati.
- **Script**: `script/sqlite-ec2/start-all.sh`, `script/aws-ec2/start-all.sh`,
  `script/init-database/init-dynamodb.sh`, `script/aws-onprem/docker-compose.yml`
  (il container di init deve poter leggere `script/aws-tags.sh`), nuovo script
  di controllo in `script/automatic-test/` e suo aggancio alla regressione.
- **Documentazione**: `PlatformAws.md` (Elastic IP e controllo statico),
  Roadmap.
- **Dipendenze**: nessuna nuova.
- **Costi**: nessuno. Nessun comando AWS viene eseguito durante la change: le
  verifiche sono dry-run con stub e il controllo statico; il test su cloud
  resta manuale e a cura dell'utente.
