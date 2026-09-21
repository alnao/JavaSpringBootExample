## Context

Vedi `proposal.md` - Why. Stato attuale rilevante per l'approccio:

- Le risorse AWS sono create esclusivamente da script bash con `aws-cli`
  (v2) ed `eksctl`; non esistono Terraform né CloudFormation. Ogni script è
  autonomo: nessuno fa `source` di un file comune, tranne quelli di test che
  usano `script/automatic-test/lib-report.sh`.
- Gli script vanno lanciati dalla root del progetto (usano percorsi relativi
  come `./script/init-database/init-mysql.sql` e
  `file://script/aws-ecs/task-def.json`).
- `aws-cli` accetta i tag in **quattro formati diversi** a seconda del servizio:

  | Formato | Servizi / comandi |
  |---|---|
  | `Key=k,Value=v Key=k,Value=v` (lista shorthand) | `rds`, `dynamodb`, `elasticache`, `ecr`, `iam`, `ec2 create-tags` |
  | `k=v,k=v` (mappa) | `sqs tag-queue`, `logs create-log-group`, `eksctl --tags`, annotation ELB di Kubernetes |
  | `[{"key":k,"value":v}]` (JSON minuscolo) | `ecs create-cluster/create-service/register-task-definition/run-task` |
  | `ResourceType=t,Tags=[{Key=k,Value=v},...]` | `ec2 run-instances`, `ec2 create-key-pair` (`--tag-specifications`) |

- `aws-ec2/start-all.sh` e `sqlite-ec2/start-all.sh` girano con `set -e`;
  `run-ecs-mysql-insert.sh` con `set -euo pipefail` (quindi variabili non
  definite fanno fallire lo script).
- Gli script `stop-all.sh` e `test-aws-ec2.sh` ritrovano le istanze EC2 con il
  filtro `tag:gestioneannotazioni-app=true` (o
  `tag:gestioneannotazioni-sqlite-ec2-app=true`); tutto il resto per nome.
- Vincolo operativo: l'agente non può lanciare `aws` né gli script di
  provisioning (costi reali). La verifica statica è `bash -n`; quella reale è
  a carico dell'utente.

## Goals / Non-Goals

**Goals:**
- Un solo punto in cui vivono valori e formati dei tag (`script/aws-tags.sh`),
  così che aggiungere un tag in futuro sia una modifica in un file solo.
- Zero cambiamenti di comportamento negli script oltre ai tag: stessi nomi di
  risorse, stessa idempotenza, stessi filtri di ricerca.
- Script leggibili: ogni chiamata `aws` mostra chiaramente quale `Name` riceve
  la risorsa.

**Non-Goals:**
- Riscrivere o uniformare gli script (helper `run_aws_command`/`safe_run`
  diversi, gestione errori diversa): si tocca solo la riga dei tag.
- Ritaggare a ogni esecuzione le risorse già esistenti (vedi proposal -
  Non-goals).
- Introdurre `shellcheck` come dipendenza di build (non è installato sulla
  macchina di sviluppo); resta un controllo facoltativo.

## Decisions

### D1. File condiviso `script/aws-tags.sh` sorgente con la cartella come argomento

```bash
# in testa a ogni start-all.sh, dopo la configurazione della regione:
source "$(dirname "$0")/../aws-tags.sh" aws-ec2
```

Il file:
- valida `ENVIRONMENT` (`dev` default; ammessi `dev|test|production`; altro →
  `echo` dell'errore ed `exit 1`, prima di qualsiasi chiamata `aws`);
- imposta `TAG_ENVIRONMENT`, `TAG_PROJECT="Annotazioni.$1"`, `TAG_OWNER`,
  `TAG_COSTCENTER`, `TAG_MANAGEDBY`, `TAG_MARKER_KEY` (default
  `gestioneannotazioni-app`, sovrascrivibile dallo script prima del `source`:
  `sqlite-ec2` usa `gestioneannotazioni-sqlite-ec2-app`);
- espone quattro funzioni, una per formato, che ricevono il `Name`:
  `aws_tags_kv NAME`, `aws_tags_map NAME`, `aws_tags_json NAME`,
  `aws_tags_spec RESOURCE_TYPE NAME [MARKER_KEY]`. Il tag marcatore è
  aggiunto solo dove lo script lo passa esplicitamente: terzo argomento di
  `aws_tags_spec` per le istanze EC2 (non per key pair, volumi né per la EC2
  dello script deprecato, che altrimenti verrebbe terminata da
  `aws-ec2/stop-all.sh`) e `Key=$TAG_MARKER_KEY,Value=true` accodato al
  `create-tags` dei security group, dove già c'è oggi.

*Perché la cartella come argomento e non `basename "$(dirname "$0")"` dentro
al file*: quando un file è sorgente, `$0` resta lo script chiamante ma
`BASH_SOURCE` no; passare il nome esplicitamente evita ambiguità e rende
evidente il valore di `Project` leggendo lo script.

*Alternativa scartata*: blocco inline in ogni script. Scelta dall'utente la
versione condivisa; la duplicazione (~30 righe × 6 file) avrebbe reso facile
far divergere i valori.

*Compatibilità con `set -u`*: usare `${1:-}` e `${ENVIRONMENT:-dev}`; nessuna
variabile letta senza default.

### D2. Tag alla creazione, non a ogni esecuzione

Si aggiunge `--tags` (o `--tag-specifications`) al comando di creazione. Dove
lo script già tagga con una chiamata separata eseguita a ogni giro (`ec2
create-tags` sui security group in tutti gli script, `sqs tag-queue`, `iam
tag-role` in `aws-ecs`) si mantiene quel punto e si cambia solo il contenuto:
è già il comportamento attuale e non cambia la logica di idempotenza.

*Alternativa scartata*: chiamare `*-tag-resource` per ogni risorsa a ogni
esecuzione, che avrebbe ritaggato anche risorse preesistenti. Servono ARN
diversi per ogni servizio (`rds add-tags-to-resource`, `dynamodb tag-resource`,
`elasticache add-tags-to-resource`, `ecr tag-resource`, `ecs tag-resource`,
`logs tag-log-group`...): troppo codice per script didattici. L'allineamento
delle risorse preesistenti si ottiene con `stop-all.sh`/`start-all.sh` ed è
documentato.

### D3. Mappa dei `Name`

| Script | Risorsa | `Name` |
|---|---|---|
| aws-ec2 | ruolo IAM / instance profile | `gestioneannotazioni-ec2-role` / `gestioneannotazioni-ec2-profile` |
| aws-ec2 | security group | `gestioneannotazioni-sg` |
| aws-ec2 | Aurora cluster / istanza | `gestioneannotazioni-cluster` / `gestioneannotazioni-instance` |
| aws-ec2, aws-ecs, aws-eks | DynamoDB | `annotazioni`, `annotazioni_storico`, `annotazioni_storicoStati` |
| aws-ec2, aws-ecs, aws-eks | SQS | `gestioneannotazioni-annotazioni-export` / `-import` |
| aws-ec2, aws-ecs, aws-eks | ElastiCache subnet group / cluster | `gestioneannotazioni-redis-subnet-group` / `gestioneannotazioni-redis` |
| aws-ec2 | key pair | valore di `$PARAM_KEY_NAME` (default `gestioneannotazioni-key`) |
| aws-ec2 | istanza EC2 / volumi | `gestioneannotazioni-ec2` / `gestioneannotazioni-ec2-volume` |
| aws-ec2 | Elastic IP, solo se già associato all'istanza | `gestioneannotazioni-ec2-eip` |
| aws-ecs, aws-eks | ECR | `gestioneannotazioni` |
| aws-ecs | ruoli IAM | `gestioneannotazioni-ecs-task-role` / `gestioneannotazioni-ecs-execution-role` |
| aws-ecs | security group | `gestioneannotazioni-sg` |
| aws-ecs, aws-eks | Aurora cluster / istanza | `gestioneannotazioni-aurora-cluster` / `gestioneannotazioni-aurora-instance` |
| aws-ecs | cluster ECS / service / task definition | `gestioneannotazioni-cluster` / `gestioneannotazioni-service` / `gestioneannotazioni-task` |
| aws-ecs | task Fargate | ereditano dal service (`--propagate-tags SERVICE`) |
| aws-ecs | log group | `/ecs/gestioneannotazioni-app` |
| aws-ecs (run-ecs-mysql-insert) | task definition e task | `gestioneannotazioni-mysql-client-task` |
| aws-ecs (deprecato) | istanza EC2 | `gestioneannotazioni-mysql-client` (invariato) |
| aws-eks | cluster EKS | `gestioneannotazioni-eks-cluster` |
| aws-eks | security group | `gestioneannotazioni-eks-sg` |
| aws-eks | load balancer del Service | `gestioneannotazioni-eks-lb` |
| sqlite-ec2 | security group / key pair | `gestioneannotazioni-sqlite-ec2-sg` / `gestioneannotazioni-sqlite-ec2-key` |
| sqlite-ec2 | istanza EC2 / volumi | `gestioneannotazioni-sqlite-ec2` / `gestioneannotazioni-sqlite-ec2-volume` |
| sqlite-ec2 | Elastic IP, solo se già associato all'istanza | `gestioneannotazioni-sqlite-ec2-eip` |

I `Name` delle istanze EC2 cambiano da `gestioneannotazioni-app` /
`gestioneannotazioni-sqlite-ec2-app` ai valori sopra: nessuno script li usa
come filtro (usano il tag marcatore), quindi non c'è impatto.

### D4. Risorse taggate per la prima volta: come

- **IAM** (`aws-ec2`): `--tags` su `create-role` e `create-instance-profile`.
  In `aws-ecs` i ruoli sono già taggati con `tag-role` a ogni giro: si cambia
  solo il contenuto.
- **Key pair**: `create-key-pair --tag-specifications
  "$(aws_tags_spec key-pair "$PARAM_KEY_NAME")"`.
- **Volumi EBS**: secondo elemento di `--tag-specifications` in
  `run-instances` (`ResourceType=volume`), stesso comando dell'istanza.
- **Log group**: `logs create-log-group --tags "$(aws_tags_map ...)"`.
- **Task definition ECS**: chiave `"tags": [...]` nel JSON generato con
  heredoc (`task-def.json`, `mysql-task-def.json`), popolata con
  `aws_tags_json`; evita un'opzione in più sulla riga di comando.
- **Task ECS**: `create-service --propagate-tags SERVICE`; in
  `run-ecs-mysql-insert.sh` `run-task --tags "$(aws_tags_json ...)"`.
- **EKS**: `eksctl create cluster --tags "$(aws_tags_map ...)"` propaga i tag
  agli stack CloudFormation e alle risorse che generano (VPC, nodi). Il tag
  `ManagedBy` resta `Sh`: il punto d'ingresso è lo script.
- **Elastic IP**: nessuno script lo alloca; dopo `wait instance-running`,
  `describe-addresses --filters Name=instance-id` e, se c'è un
  `AllocationId`, `create-tags` su di esso (best effort, `|| true` per non
  fermare lo script). Copre l'EIP associato a mano che altrimenti resterebbe
  senza tag; il `ssm:managed-instance` che compare in Tag Editor non è
  taggabile via API per le EC2 e viene solo documentato.
- **Load balancer EKS**: annotation
  `service.beta.kubernetes.io/aws-load-balancer-additional-resource-tags`
  con valore `"$(aws_tags_map gestioneannotazioni-eks-lb)"` nel manifest del
  `Service` (heredoc già presente).

### D5. Sostituzione dei tag ECS non standard

`Project=gestioneannotazioni-app` (cluster e service) ed
`Environment=production` (service) vengono sostituiti dai valori standard: era
l'unico punto con `Environment` e il valore contraddiceva l'uso di
test/demo degli stack.

### D6. Validazione senza accesso al cloud

- `bash -n` su tutti i file toccati;
- test manuale locale di `aws-tags.sh`: `source` in una shell e stampa delle
  quattro funzioni con `ENVIRONMENT` non impostato, valido e non valido;
- l'utente, quando lancia gli stack, controlla i tag con
  `aws resourcegroupstaggingapi get-resources --tag-filters Key=CostCenter,Values=Annotazioni`
  (comando documentato in `PlatformAws.md`).

## Risks / Trade-offs

- [Un comando `aws` di una versione vecchia non accetta `--tags`] → gli script
  richiedono già aws-cli v2; ogni opzione usata esiste da almeno il 2021. In
  caso di errore la risorsa non viene creata e lo script si ferma (`set -e`)
  o segnala (`safe_run`), come già oggi.
- [Valori con virgole o `=` rompono il formato mappa/shorthand] → nessun
  valore previsto li contiene; `aws-tags.sh` lo dichiara nel commento di testa.
- [Tag su `eksctl` non propagati a tutte le risorse dei nodi] → verificare
  sulla console dopo il primo lancio; i nodi ricevono comunque i tag dello
  stack CloudFormation. Se mancano, fallback documentato:
  `eksctl create nodegroup --tags`.
- [Annotation ELB ignorata dal controller] → con il cloud provider in-tree e
  con AWS Load Balancer Controller l'annotation è supportata; se il LB nasce
  senza tag lo si tagga con `elb add-tags`, documentato.
- [Risorse condivise fra ECS ed EKS con `Project` del primo creatore] →
  accettato ed esplicitato nella spec; i costi restano attribuiti a
  `CostCenter=Annotazioni`.
- [`source` con percorso relativo a `$0`] → funziona sia da root
  (`./script/aws-ec2/start-all.sh`) sia con `bash script/...`; con `sh` non
  funziona (`$(dirname "$0")` sì, ma gli script sono già `#!/bin/bash`).

## Migration Plan

1. Aggiungere `script/aws-tags.sh` e le righe di `source`; modificare le
   chiamate `aws`/`eksctl`/manifest.
2. Verifica statica (`bash -n`, test delle funzioni in shell).
3. Documentazione e Roadmap.
4. A cura dell'utente: `stop-all.sh` + `start-all.sh` su `aws-ec2` (stack più
   economico), verifica tag in console, `test-aws-ec2.sh`, `stop-all.sh`.
5. Rollback: gli script non cambiano nomi di risorse né filtri; ripristinare i
   file precedenti basta, le risorse già taggate restano valide.
