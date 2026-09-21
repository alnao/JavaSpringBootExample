## 1. File condiviso dei tag

- [x] 1.1 Creare `script/aws-tags.sh` con validazione di `ENVIRONMENT` (default `dev`, ammessi `dev|test|production`, altrimenti messaggio ed `exit 1`), variabili `TAG_ENVIRONMENT`, `TAG_PROJECT="Annotazioni.$1"`, `TAG_OWNER=AlNao`, `TAG_COSTCENTER=Annotazioni`, `TAG_MANAGEDBY=Sh`, `TAG_MARKER_KEY` (default `gestioneannotazioni-app`) e le funzioni `aws_tags_kv`, `aws_tags_map`, `aws_tags_json`, `aws_tags_spec` (design D1); commento di testa con uso e vincolo "niente virgole o `=` nei valori". Verifica: `bash -n script/aws-tags.sh`.
- [x] 1.2 Provare il file in una shell: `source script/aws-tags.sh aws-ec2` e stampare le quattro funzioni con `ENVIRONMENT` non impostato (atteso `dev`), `ENVIRONMENT=test`, `ENVIRONMENT=collaudo` (atteso errore con elenco dei valori ammessi) e con `set -u` attivo (nessuna variabile non definita). Verifica: output dei quattro formati corretto per ogni caso.

## 2. Stack aws-ec2

- [x] 2.1 In `script/aws-ec2/start-all.sh` aggiungere il `source` di `aws-tags.sh` con argomento `aws-ec2` dopo la configurazione della regione; aggiungere `--tags "$(aws_tags_kv ...)"` a `iam create-role` (Name `gestioneannotazioni-ec2-role`) e `iam create-instance-profile` (Name `gestioneannotazioni-ec2-profile`). Verifica: `bash -n` e rilettura del diff.
- [x] 2.2 Sostituire i tag di security group (`create-tags`, incluso il marcatore), Aurora cluster e istanza, tre tabelle DynamoDB, due code SQS (`tag-queue` con `aws_tags_map`), subnet group e cluster ElastiCache con i valori della mappa in design D3. Verifica: `grep -n "gestioneannotazioni-app" script/aws-ec2/start-all.sh` restituisce solo il filtro `tag:gestioneannotazioni-app` e il marcatore, nessun `Name=gestioneannotazioni-app`.
- [x] 2.3 Taggare key pair (`--tag-specifications "$(aws_tags_spec key-pair "$PARAM_KEY_NAME")"`) e in `run-instances` passare due specifiche: `instance` con Name `gestioneannotazioni-ec2` più marcatore, `volume` con Name `gestioneannotazioni-ec2-volume`. Verifica: `bash -n`; il messaggio finale dello script cita ancora il tag marcatore per il cleanup.

## 3. Stack aws-ecs

- [x] 3.1 In `script/aws-ecs/start-all.sh` aggiungere il `source` (argomento `aws-ecs`); sostituire i tag di ECR, `tag-role` dei due ruoli, security group, Aurora, DynamoDB, SQS, ElastiCache con le funzioni e i Name della mappa D3. Verifica: `bash -n`; nessuna occorrenza residua di `Key=Name,Value=gestioneannotazioni-app`.
- [x] 3.2 Cluster ECS e service: usare `aws_tags_json` (Name `gestioneannotazioni-cluster` / `gestioneannotazioni-service`), rimuovere `Project=gestioneannotazioni-app` ed `Environment=production`, aggiungere `--propagate-tags SERVICE` a `create-service`. Verifica: `bash -n`; `grep -n "production" script/aws-ecs/start-all.sh` vuoto.
- [x] 3.3 Taggare log group (`logs create-log-group --tags "$(aws_tags_map "/ecs/gestioneannotazioni-app")"`) e task definition (chiave `"tags"` nel heredoc di `task-def.json`, Name `gestioneannotazioni-task`). Verifica: generare il JSON in locale con valori finti delle variabili e validarlo con `python3 -m json.tool`.
- [x] 3.4 In `script/aws-ecs/run-ecs-mysql-insert.sh` (attivo `set -euo pipefail`) aggiungere il `source`, la chiave `"tags"` nel heredoc di `mysql-task-def.json` e `--tags "$(aws_tags_json gestioneannotazioni-mysql-client-task)"` a `run-task`. In `deprecato-launch-ec2-mysql-client.sh` sostituire la `--tag-specifications` con `aws_tags_spec instance gestioneannotazioni-mysql-client`. Verifica: `bash -n` su entrambi; JSON validato come in 3.3.

## 4. Stack aws-eks

- [x] 4.1 In `script/aws-eks/start-all.sh` aggiungere il `source` (argomento `aws-eks`); sostituire i tag di ECR, security group (`gestioneannotazioni-eks-sg`), Aurora, DynamoDB, SQS, ElastiCache con funzioni e Name della mappa D3. Verifica: `bash -n`; nessuna occorrenza residua di `gestioneannotazioni-app` tranne nei nomi Kubernetes.
- [x] 4.2 `eksctl create cluster --tags "$(aws_tags_map gestioneannotazioni-eks-cluster)"` e annotation `service.beta.kubernetes.io/aws-load-balancer-additional-resource-tags: "$(aws_tags_map gestioneannotazioni-eks-lb)"` nel manifest del `Service`. Verifica: estrarre il manifest con valori finti e validarlo con `kubectl apply --dry-run=client -f -` se `kubectl` è disponibile, altrimenti con `python3 -c "import yaml"` sul testo generato.

## 5. Stack sqlite-ec2

- [x] 5.1 In `script/sqlite-ec2/start-all.sh` impostare `TAG_MARKER_KEY=gestioneannotazioni-sqlite-ec2-app` prima del `source` (argomento `sqlite-ec2`); sostituire i tag del security group, taggare key pair (`gestioneannotazioni-sqlite-ec2-key`), istanza (`gestioneannotazioni-sqlite-ec2` più marcatore) e volumi (`gestioneannotazioni-sqlite-ec2-volume`). Verifica: `bash -n`; `stop-all.sh` della cartella continua a filtrare per `tag:gestioneannotazioni-sqlite-ec2-app` (nessuna modifica necessaria).
- [x] 5.2 In `aws-ec2/start-all.sh` e `sqlite-ec2/start-all.sh`, dopo `wait instance-running`, taggare l'eventuale Elastic IP già associato all'istanza (`describe-addresses` + `create-tags`, best effort) e documentare in `PlatformAws.md` EIP e `ssm:managed-instance`. Verifica: dry-run con stub nei due casi (EIP presente → `create-tags` su `eipalloc-…`; assente → blocco saltato, exit 0).

## 6. Documentazione

- [x] 6.1 In `PlatformAws.md` aggiungere la sezione "Tag delle risorse": tabella dei sei tag con valori e regole per `Name`, variabile `ENVIRONMENT` con esempio `ENVIRONMENT=test ./script/aws-ec2/start-all.sh`, nota sull'attivazione dei cost allocation tag in Billing, comando `aws resourcegroupstaggingapi get-resources --tag-filters Key=CostCenter,Values=Annotazioni` per la verifica, come allineare risorse create prima della change (ciclo stop/start) e i fallback per EKS (`eksctl create nodegroup --tags`, `elb add-tags`). Aggiornare le due frasi "Tutte le risorse sono taggate..." con il rimando alla sezione. Verifica: rilettura; i link interni funzionano.
- [x] 6.2 In `Roadmap.md` trasformare la voce "🚧 ☁️ Gestione tag e script terraform su AWS" in due sotto-voci: "✅ 🏷️ Tag standard su tutte le risorse create dagli script sh (change `tag-risorse-aws`)" e "🚧 📜 Script terraform". Verifica: rilettura della sezione.

## 7. Verifica finale

- [x] 7.1 `bash -n` su tutti i file toccati (`script/aws-tags.sh` e i sei script) e `grep -rn "gestioneannotazioni-app" script/aws-ec2 script/aws-ecs script/aws-eks script/sqlite-ec2` per confermare che restano solo filtri, marcatori e nomi Kubernetes/log group. Verifica: nessun errore di sintassi, grep coerente con l'atteso.
- [x] 7.2 Regressione: `./script/automatic-test/test-aws-onprem.sh` (LocalStack, nessun costo) per confermare che il runtime del profilo `aws` è invariato; poi, **a cura dell'utente perché genera costi**, ciclo `./script/aws-ec2/stop-all.sh` → `./script/aws-ec2/start-all.sh` → controllo dei sei tag in console o con `get-resources` → `./script/aws-ec2/test-aws-ec2.sh` → `./script/aws-ec2/stop-all.sh`. Verifica: regressione locale verde; sullo stack reale tutte le risorse dello scenario "Stack EC2" della spec riportano i sei tag e la pulizia le elimina tutte.
