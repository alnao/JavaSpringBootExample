## Purpose

Definisce i tag che ogni risorsa AWS creata dagli script di provisioning del
repository deve portare, così che le risorse siano riconoscibili, attribuibili a
un ambiente e a un centro di costo, e ritrovabili dagli script di test e di
pulizia.

## ADDED Requirements

### Requirement: Tag standard su ogni risorsa creata

Ogni risorsa AWS creata da uno script di provisioning del repository SHALL
portare, appena creata, i sei tag `Name`, `Environment`, `Project`, `Owner`,
`CostCenter` e `ManagedBy`, verificabili dalla console o dalla CLI AWS. I tag
`Owner`, `CostCenter` e `ManagedBy` SHALL valere rispettivamente `AlNao`,
`Annotazioni` e `Sh` per tutte le risorse create da script di shell.

Le risorse taggabili coinvolte SHALL comprendere, dove lo script le crea:
security group, istanze EC2 e relativi volumi, key pair, ruoli IAM e instance
profile, cluster e istanze Aurora, tabelle DynamoDB, code SQS, subnet group e
cluster ElastiCache, repository ECR, cluster, task definition, service e task
ECS, log group CloudWatch, cluster EKS e load balancer generato dal `Service`
Kubernetes.

#### Scenario: Stack EC2

- **WHEN** viene eseguito lo script di avvio dello stack in `script/aws-ec2`
  su un account privo delle risorse
- **THEN** security group, istanza EC2 e i suoi volumi, key pair, ruolo IAM,
  instance profile, cluster e istanza Aurora, le tre tabelle DynamoDB, le due
  code SQS, subnet group e cluster ElastiCache riportano tutti e sei i tag
- **AND** `Project` vale `Annotazioni.aws-ec2`

#### Scenario: Stack ECS Fargate

- **WHEN** viene eseguito lo script di avvio dello stack in `script/aws-ecs`
  su un account privo delle risorse
- **THEN** repository ECR, i due ruoli IAM, security group, cluster e istanza
  Aurora, tabelle DynamoDB, code SQS, subnet group e cluster ElastiCache,
  cluster ECS, log group CloudWatch, task definition e service ECS riportano
  tutti e sei i tag
- **AND** i task Fargate avviati dal service ereditano gli stessi tag
- **AND** `Project` vale `Annotazioni.aws-ecs`

#### Scenario: Stack EKS

- **WHEN** viene eseguito lo script di avvio dello stack in `script/aws-eks`
  su un account privo delle risorse
- **THEN** repository ECR, cluster EKS, security group, cluster e istanza
  Aurora, tabelle DynamoDB, code SQS, subnet group e cluster ElastiCache
  riportano tutti e sei i tag
- **AND** il load balancer creato dal `Service` Kubernetes riporta gli stessi
  tag
- **AND** `Project` vale `Annotazioni.aws-eks`

#### Scenario: EC2 con profilo sqlite

- **WHEN** viene eseguito lo script di avvio in `script/sqlite-ec2`
- **THEN** security group, key pair, istanza EC2 e i suoi volumi riportano
  tutti e sei i tag
- **AND** `Project` vale `Annotazioni.sqlite-ec2`

#### Scenario: Task Fargate di servizio

- **WHEN** viene eseguito lo script `run-ecs-mysql-insert.sh` in
  `script/aws-ecs`
- **THEN** la task definition temporanea e il task avviato riportano tutti e
  sei i tag con `Project` uguale a `Annotazioni.aws-ecs`

### Requirement: Valore del tag Name

Il tag `Name` SHALL valere il nome proprio della risorsa quando la risorsa ne
ha uno (identificatore del cluster o dell'istanza, nome del gruppo, della
tabella, della coda, del ruolo, del repository, del log group, della key pair,
della task definition, del service). Per le risorse senza un nome proprio
(istanze EC2, volumi, task ECS, load balancer) `Name` SHALL seguire lo schema
`gestioneannotazioni-<servizio>`.

#### Scenario: Risorsa con nome proprio

- **WHEN** lo script crea la tabella DynamoDB `annotazioni_storico` e il
  security group `gestioneannotazioni-sg`
- **THEN** i due `Name` valgono rispettivamente `annotazioni_storico` e
  `gestioneannotazioni-sg`

#### Scenario: Risorsa senza nome proprio

- **WHEN** lo script di `script/aws-ec2` crea l'istanza EC2 e lo script di
  `script/sqlite-ec2` crea la propria
- **THEN** i `Name` valgono rispettivamente `gestioneannotazioni-ec2` e
  `gestioneannotazioni-sqlite-ec2`
- **AND** i volumi delle due istanze valgono `gestioneannotazioni-ec2-volume`
  e `gestioneannotazioni-sqlite-ec2-volume`

### Requirement: Ambiente configurabile

Il tag `Environment` SHALL valere `dev` se chi lancia lo script non indica
nulla, e SHALL essere sovrascrivibile tramite la variabile di shell
`ENVIRONMENT`. I valori ammessi SHALL essere `dev`, `test` e `production`; con
un valore diverso lo script SHALL fermarsi prima di creare qualsiasi risorsa,
segnalando il valore errato.

#### Scenario: Valore di default

- **WHEN** lo script viene lanciato senza la variabile `ENVIRONMENT`
- **THEN** tutte le risorse create riportano `Environment=dev`

#### Scenario: Valore esplicito

- **WHEN** lo script viene lanciato con `ENVIRONMENT=test`
- **THEN** tutte le risorse create riportano `Environment=test`

#### Scenario: Valore non ammesso

- **WHEN** lo script viene lanciato con `ENVIRONMENT=collaudo`
- **THEN** lo script termina con errore senza creare risorse
- **AND** il messaggio riporta i valori ammessi

### Requirement: Progetto derivato dalla cartella dello script

Il tag `Project` SHALL valere `Annotazioni.<cartella>`, dove `<cartella>` è il
nome della cartella di `script/` che contiene lo script eseguito. Una risorsa
condivisa fra più stack (stesso nome Aurora, DynamoDB, SQS, ElastiCache o ECR
usato da `aws-ecs` e `aws-eks`) SHALL conservare il `Project` dello script che
l'ha creata per primo.

#### Scenario: Risorsa condivisa fra due stack

- **WHEN** lo stack `aws-ecs` ha già creato la coda
  `gestioneannotazioni-annotazioni-export` e viene lanciato lo script di
  `aws-eks`
- **THEN** la coda esiste una sola volta e riporta `Project=Annotazioni.aws-ecs`

### Requirement: Tag marcatori conservati

Le istanze EC2 create dagli script SHALL continuare a portare il tag marcatore
già usato per ritrovarle (`gestioneannotazioni-app=true` per `aws-ec2`,
`gestioneannotazioni-sqlite-ec2-app=true` per `sqlite-ec2`), in aggiunta ai sei
tag standard. Gli script di test e di pulizia SHALL ritrovare le risorse come
prima della change.

#### Scenario: Pulizia dopo l'avvio

- **WHEN** lo stack `aws-ec2` è stato avviato con i nuovi tag e viene lanciato
  lo script `stop-all.sh` della stessa cartella
- **THEN** l'istanza EC2 viene individuata e terminata
- **AND** le altre risorse dello stack vengono eliminate come prima della change

#### Scenario: Test automatico dopo l'avvio

- **WHEN** lo stack `aws-ec2` è stato avviato con i nuovi tag e viene lanciato
  `test-aws-ec2.sh`
- **THEN** lo script individua l'istanza e completa i test senza modifiche

### Requirement: Rilancio idempotente

Il rilancio di uno script di avvio su risorse già esistenti SHALL completare
senza errori dovuti ai tag e senza duplicare risorse; le risorse già esistenti
SHALL conservare i tag che avevano (non è richiesto che vengano ritaggate).

#### Scenario: Secondo lancio consecutivo

- **WHEN** lo script di avvio viene lanciato una seconda volta senza aver
  eseguito la pulizia
- **THEN** lo script termina con lo stesso esito del primo lancio
- **AND** nessuna risorsa risulta duplicata

### Requirement: Elastic IP associato all'istanza

Gli script di avvio non allocano Elastic IP. Se però, al momento in cui
l'istanza EC2 risulta avviata, le è associato un Elastic IP (allocato dalla
console o da un'altra automazione), lo script SHALL applicargli i sei tag
standard con `Name` uguale a `gestioneannotazioni-ec2-eip` (stack `aws-ec2`) o
`gestioneannotazioni-sqlite-ec2-eip` (stack `sqlite-ec2`). Se nessun Elastic
IP è associato, lo script SHALL proseguire senza errori e senza allocarne uno.

#### Scenario: Elastic IP associato da fuori

- **WHEN** un Elastic IP è stato associato all'istanza dello stack `aws-ec2` e
  lo script di avvio viene rilanciato
- **THEN** l'Elastic IP riporta i sei tag con `Name=gestioneannotazioni-ec2-eip`
  e `Project=Annotazioni.aws-ec2`

#### Scenario: Nessun Elastic IP

- **WHEN** all'istanza non è associato alcun Elastic IP
- **THEN** lo script termina con lo stesso esito di prima
- **AND** nessun Elastic IP risulta allocato nell'account per effetto dello script
