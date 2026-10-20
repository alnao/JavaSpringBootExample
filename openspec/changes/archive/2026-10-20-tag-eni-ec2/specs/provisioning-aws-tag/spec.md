## MODIFIED Requirements

### Requirement: Tag standard su ogni risorsa creata

Ogni risorsa AWS creata da uno script di provisioning del repository SHALL
portare, appena creata, i sei tag `Name`, `Environment`, `Project`, `Owner`,
`CostCenter` e `ManagedBy`, verificabili dalla console o dalla CLI AWS. I tag
`Owner` e `CostCenter` SHALL valere rispettivamente `AlNao` e `Annotazioni`;
`ManagedBy` SHALL valere `Sh` per le risorse create da script di shell e
`Terraform` per le risorse create da una configurazione Terraform.

Le risorse taggabili coinvolte SHALL comprendere, dove lo script le crea:
security group, istanze EC2 con i relativi volumi e le relative interfacce di
rete (ENI), key pair, ruoli IAM e instance profile, cluster e istanze Aurora,
tabelle DynamoDB, code SQS, subnet group e cluster ElastiCache, repository
ECR, cluster, task definition, service e task ECS, log group CloudWatch,
cluster EKS e load balancer generato dal `Service` Kubernetes.

L'indirizzo IPv4 pubblico assegnato automaticamente (dalla subnet a
un'istanza EC2, da ECS a un task Fargate) non è una risorsa AWS e non può
ricevere tag: per le istanze EC2 SHALL essere riconducibile al progetto
tramite i tag dell'ENI a cui è associato. Le interfacce di rete create e
gestite da AWS per i task Fargate e per i nodi EKS non sono create dagli
script e sono escluse da questo requisito.

#### Scenario: Stack EC2

- **WHEN** viene eseguito lo script di avvio dello stack in `script/aws-ec2`
  su un account privo delle risorse
- **THEN** security group, istanza EC2 con i suoi volumi e la sua interfaccia
  di rete, key pair, ruolo IAM, instance profile, cluster e istanza Aurora, le
  tre tabelle DynamoDB, le due code SQS, subnet group e cluster ElastiCache
  riportano tutti e sei i tag
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
- **THEN** security group, key pair, istanza EC2 con i suoi volumi e la sua
  interfaccia di rete riportano tutti e sei i tag
- **AND** `Project` vale `Annotazioni.sqlite-ec2`

#### Scenario: Task Fargate di servizio

- **WHEN** viene eseguito lo script `run-ecs-mysql-insert.sh` in
  `script/aws-ecs`
- **THEN** la task definition temporanea e il task avviato riportano tutti e
  sei i tag con `Project` uguale a `Annotazioni.aws-ecs`

#### Scenario: EC2 di servizio per MySQL

- **WHEN** viene eseguito lo script `deprecato-launch-ec2-mysql-client.sh` in
  `script/aws-ecs`
- **THEN** l'istanza, i suoi volumi e la sua interfaccia di rete riportano
  tutti e sei i tag con `Project` uguale a `Annotazioni.aws-ecs`

#### Scenario: Stack EC2 via Terraform

- **WHEN** viene applicata la configurazione Terraform in
  `script/aws-terraform-ec2` su un account privo delle risorse
- **THEN** security group, istanza EC2 con i suoi volumi e la sua interfaccia
  di rete primaria, key pair, ruolo IAM, instance profile, cluster e istanza
  Aurora, le tre tabelle DynamoDB, le due code SQS, subnet group e cluster
  ElastiCache riportano tutti e sei i tag
- **AND** `ManagedBy` vale `Terraform` e `Project` vale
  `Annotazioni.aws-terraform-ec2`

#### Scenario: Stack ECS Fargate via Terraform

- **WHEN** viene applicata la configurazione Terraform in
  `script/aws-terraform-ecs` su un account privo delle risorse
- **THEN** repository ECR, i due ruoli IAM, security group, cluster e istanza
  Aurora, tabelle DynamoDB, code SQS, subnet group e cluster ElastiCache,
  cluster ECS, log group CloudWatch, le due task definition e il service ECS
  riportano tutti e sei i tag
- **AND** i task Fargate avviati dal service ereditano gli stessi tag
- **AND** `ManagedBy` vale `Terraform` e `Project` vale
  `Annotazioni.aws-terraform-ecs`

#### Scenario: IPv4 pubblico automatico di un'istanza EC2

- **WHEN** un'istanza creata da uno script ha un IPv4 pubblico assegnato
  automaticamente e non un Elastic IP
- **THEN** cercando le interfacce di rete con il tag `Project` dello stack,
  l'interfaccia dell'istanza risulta associata a quell'IPv4 pubblico
- **AND** nessuno script tenta di taggare l'indirizzo in sé

### Requirement: Valore del tag Name

Il tag `Name` SHALL valere il nome proprio della risorsa quando la risorsa ne
ha uno (identificatore del cluster o dell'istanza, nome del gruppo, della
tabella, della coda, del ruolo, del repository, del log group, della key pair,
della task definition, del service). Per le risorse senza un nome proprio
(istanze EC2, volumi, interfacce di rete, task ECS, load balancer) `Name`
SHALL seguire lo schema `gestioneannotazioni-<servizio>`; per volumi e
interfacce di rete di un'istanza il nome è quello dell'istanza seguito da
`-volume` o `-eni`.

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
- **AND** le interfacce di rete delle due istanze valgono
  `gestioneannotazioni-ec2-eni` e `gestioneannotazioni-sqlite-ec2-eni`

### Requirement: Verifica statica dei tag negli script

Il repository SHALL fornire un controllo statico che analizza gli script sh
di `script/` (esclusi `script/azure-onprem-non-funziona` e le cartelle
`.terraform`) e, per ogni comando che crea una risorsa (`aws <servizio>
create-*` diverso da `create-tags`, `aws ec2 run-instances`, `aws ec2
allocate-address`, `aws ecs run-task`, `aws ecs register-task-definition`,
`eksctl create`), verifica che i sei tag standard siano applicati tramite le
funzioni condivise di `script/aws-tags.sh`: nel comando stesso, nel file JSON
passato al comando oppure con un comando di tagging della stessa risorsa nello
stesso script. Per `aws ec2 run-instances` i tag SHALL coprire l'istanza, i
volumi e l'interfaccia di rete. Tag scritti a mano non soddisfano il
controllo, perché non garantiscono i sei tag. Il controllo MUST NOT eseguire
chiamate verso AWS. In caso di violazione SHALL terminare con errore elencando
file e riga di ogni comando non conforme. Il controllo SHALL essere lanciabile
da solo e SHALL far parte della regressione completa `test-all.sh`, con
l'esito registrato nel riepilogo.

#### Scenario: Repository conforme

- **WHEN** il controllo statico viene eseguito sul repository
- **THEN** termina con successo
- **AND** il riepilogo della regressione riporta il test come superato

#### Scenario: Comando di creazione senza tag

- **WHEN** uno script analizzato contiene `aws sqs create-queue --queue-name
  prova` senza alcun tagging della coda
- **THEN** il controllo termina con errore
- **AND** l'elenco riporta file e riga di quel comando

#### Scenario: Tag scritti a mano

- **WHEN** uno script analizzato crea un'istanza con
  `--tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=prova}]'`
- **THEN** il controllo termina con errore indicando file e riga

#### Scenario: Istanza senza tag sull'interfaccia di rete

- **WHEN** uno script analizzato crea un'istanza con `aws ec2 run-instances`
  e `--tag-specifications` costruite con le funzioni condivise solo per
  `instance` e `volume`
- **THEN** il controllo termina con errore indicando file e riga e la mancanza
  del tag sull'interfaccia di rete

#### Scenario: Tagging con comando dedicato

- **WHEN** uno script analizzato crea una coda con `aws sqs create-queue` e
  subito dopo la tagga con `aws sqs tag-queue` usando le funzioni di
  `script/aws-tags.sh`
- **THEN** il controllo considera il comando conforme

#### Scenario: Comando commentato

- **WHEN** un comando di creazione senza tag compare solo in una riga di
  commento
- **THEN** il controllo lo ignora
