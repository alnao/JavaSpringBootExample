## MODIFIED Requirements

### Requirement: Tag standard su ogni risorsa creata

Ogni risorsa AWS creata da uno script di provisioning del repository SHALL
portare, appena creata, i sei tag `Name`, `Environment`, `Project`, `Owner`,
`CostCenter` e `ManagedBy`, verificabili dalla console o dalla CLI AWS. I tag
`Owner` e `CostCenter` SHALL valere rispettivamente `AlNao` e `Annotazioni`;
`ManagedBy` SHALL valere `Sh` per le risorse create da script di shell e
`Terraform` per le risorse create da una configurazione Terraform.

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

#### Scenario: Stack EC2 via Terraform

- **WHEN** viene applicata la configurazione Terraform in
  `script/aws-terraform-ec2` su un account privo delle risorse
- **THEN** security group, istanza EC2 e i suoi volumi, key pair, ruolo IAM,
  instance profile, cluster e istanza Aurora, le tre tabelle DynamoDB, le due
  code SQS, subnet group e cluster ElastiCache riportano tutti e sei i tag
- **AND** `ManagedBy` vale `Terraform` e `Project` vale
  `Annotazioni.aws-terraform-ec2`

