## Why

Le risorse AWS create dagli script di provisioning in `script/` sono taggate in
modo incoerente (`Name=gestioneannotazioni-app` ovunque, `Project` e
`Environment` solo su ECS, con valori diversi fra loro) e diverse risorse non
hanno alcun tag (ruoli IAM, instance profile, key pair, volumi EBS, log group
CloudWatch, task definition ECS, load balancer creato da Kubernetes su EKS).
Senza un set di tag standard non è possibile attribuire i costi per progetto e
centro di costo in Cost Explorer né distinguere l'ambiente di riferimento; la
voce "Gestione tag e script terraform su AWS" è aperta (🚧) in `Roadmap.md`.

## What Changes

- Ogni risorsa AWS creata dagli script `sh` riceve sei tag standard:
  `Name`, `Environment`, `Project`, `Owner`, `CostCenter`, `ManagedBy`.
  - `Name` = nome proprio della risorsa quando esiste (identificatore, nome del
    gruppo, nome della tabella/coda/ruolo); per le risorse senza nome
    (istanza EC2, volumi EBS, task ECS, load balancer) `gestioneannotazioni-<servizio>`.
  - `Environment` = `dev` di default, sovrascrivibile con la variabile di
    shell `ENVIRONMENT` (valori ammessi `dev`, `test`, `production`).
  - `Project` = `Annotazioni.<cartella dello script>` (`Annotazioni.aws-ec2`,
    `Annotazioni.aws-ecs`, `Annotazioni.aws-eks`, `Annotazioni.sqlite-ec2`).
  - `Owner=AlNao`, `CostCenter=Annotazioni`, `ManagedBy=Sh` (valori fissi).
- Nuovo file condiviso `script/aws-tags.sh`, sorgente dagli script di
  provisioning: definisce i valori e le funzioni che producono i tag nei quattro
  formati richiesti da `aws-cli` (`Key=,Value=`; mappa `k=v,k=v`; JSON
  `key/value` per ECS; `--tag-specifications` per EC2).
- Vengono taggate anche le risorse oggi prive di tag: ruolo IAM e instance
  profile (aws-ec2), key pair e volumi EBS (aws-ec2, sqlite-ec2), log group
  CloudWatch e task definition (aws-ecs), task Fargate (propagazione dei tag
  del service e `run-task` in `run-ecs-mysql-insert.sh`), cluster EKS via
  `eksctl --tags`, load balancer del `Service` Kubernetes via annotation.
- I tag marcatori esistenti (`gestioneannotazioni-app=true`,
  `gestioneannotazioni-sqlite-ec2-app=true`) restano: sono usati come filtro
  dagli script `stop-all.sh`, `test-aws-ec2.sh` e dallo stesso `start-all.sh`
  per ritrovare le istanze EC2. I tag `Project=gestioneannotazioni-app` e
  `Environment=production` presenti solo su ECS vengono sostituiti dai valori
  standard.
- Documentazione: nuova sezione "Tag delle risorse" in `PlatformAws.md`
  (tabella dei tag, variabile `ENVIRONMENT`, attivazione dei cost allocation
  tag, come ritaggare risorse create prima della change); voce in `Roadmap.md`.

## Capabilities

### New Capabilities
- `provisioning-aws-tag`: ogni risorsa AWS creata dagli script di provisioning
  del repository porta i sei tag standard con i valori previsti; i tag
  marcatori usati per la ricerca delle istanze restano invariati.

### Modified Capabilities
<!-- nessuna: le capability applicative annotazioni-stati e annotazioni-lock non cambiano -->

## Impact

- **Profili coinvolti**: solo gli script di deploy del profilo `aws`
  (`script/aws-ec2`, `script/aws-ecs`, `script/aws-eks`) e `script/sqlite-ec2`,
  che pubblica il profilo `sqlite` su una EC2 reale. Nessuna modifica al
  runtime dell'applicazione: il comportamento dei quattro profili resta
  identico.
- **Core e adapter**: non toccati. La change riguarda soltanto `script/` e la
  documentazione; nessun file Java, nessun `pom.xml`, nessuna dipendenza nuova.
- **File modificati**: `script/aws-ec2/start-all.sh`,
  `script/aws-ecs/start-all.sh`, `script/aws-ecs/run-ecs-mysql-insert.sh`,
  `script/aws-ecs/deprecato-launch-ec2-mysql-client.sh`,
  `script/aws-eks/start-all.sh`, `script/sqlite-ec2/start-all.sh`,
  `PlatformAws.md`, `Roadmap.md`. Nuovo: `script/aws-tags.sh`.
- **Compatibilità**: gli script `stop-all.sh` non cambiano perché ritrovano le
  risorse per nome o per tag marcatore, non per `Name`. Le risorse già
  esistenti su AWS al momento del lancio non vengono ritaggate (i tag sono
  applicati alla creazione, salvo dove lo script già li applica a ogni
  esecuzione): per allinearle serve un ciclo `stop-all.sh`/`start-all.sh` o un
  tagging manuale, documentato in `PlatformAws.md`.
- **Costi**: nessun costo aggiuntivo; i tag non hanno prezzo. La verifica
  reale richiede di lanciare gli script su AWS, che creano risorse a pagamento:
  è a carico dell'utente.

## Non-goals

- **Terraform / CloudFormation**: nel repository non esistono template; la voce
  di Roadmap resta aperta per la parte "script terraform". `ManagedBy` vale
  sempre `Sh` in questa change; i valori `Terraform` e `CloudFormation` sono
  riservati per quando esisteranno.
- **Profilo `azure`** (`script/azure-*`): richiesta limitata ad AWS; Azure ha un
  proprio modello di tag e sarà oggetto di una change separata.
- **Profilo `kube`** (`script/minikube`) e `script/aws-onprem` (LocalStack):
  girano in locale, non creano risorse cloud.
- **Oggetti Kubernetes** (namespace, deployment, service): non sono risorse
  AWS taggabili; si tagga solo il load balancer che il `Service` provoca.
- **Ritaggare a ogni esecuzione** le risorse già esistenti con chiamate
  `tag-resource` per ogni servizio: aumenterebbe molto la lunghezza degli
  script didattici; documentato il ciclo stop/start come alternativa.
