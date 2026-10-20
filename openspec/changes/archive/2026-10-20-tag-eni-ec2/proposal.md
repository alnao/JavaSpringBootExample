## Why

La spec `provisioning-aws-tag` chiede i sei tag standard su "istanze EC2 e
relativi volumi", ma un'istanza porta con sé anche la sua interfaccia di rete
primaria (ENI), che oggi nessuno script tagga: in console e in Tag Editor
compare senza `Name`, `Project`, `Owner` e `CostCenter`. È anche la risorsa a
cui è legato l'IPv4 pubblico dell'istanza, che quindi oggi non è riconducibile
al progetto.

L'IPv4 pubblico assegnato automaticamente dalla subnet (EC2 nella VPC di
default) o da ECS (`assignPublicIp` dei task Fargate) non è una risorsa AWS:
non ha un identificativo e non si può taggare. La spec oggi non lo dice, e
lascia credere che sia una lacuna degli script.

## What Changes

- Spec: le ENI create insieme alle istanze EC2 entrano fra le risorse che
  ricevono i sei tag, con `Name` = `gestioneannotazioni-<servizio>-eni`; la
  spec dichiara che l'IPv4 pubblico automatico non è taggabile e resta
  riconducibile tramite l'ENI che lo porta; un Elastic IP associato continua a
  essere taggato come oggi.
- `script/aws-ec2/start-all.sh`, `script/sqlite-ec2/start-all.sh`,
  `script/aws-ecs/deprecato-launch-ec2-mysql-client.sh`: `run-instances` tagga
  anche `network-interface`.
- `script/aws-terraform-ec2`: tag dell'ENI primaria dell'istanza (la risorsa
  `aws_instance` non la tagga, nemmeno con `default_tags`).
- Controllo statico `script/automatic-test/test-tag-aws-script.sh`: un
  `run-instances` è conforme solo se tagga istanza, volumi e interfaccia di
  rete.
- `PlatformAws.md`: ENI taggate, IPv4 pubblici automatici e ENI dei task
  Fargate non taggabili.

## Capabilities

### New Capabilities

Nessuna.

### Modified Capabilities

- `provisioning-aws-tag`: i requisiti "Tag standard su ogni risorsa creata",
  "Valore del tag Name" e "Verifica statica dei tag negli script" includono le
  ENI delle istanze EC2 e dichiarano il caso degli IPv4 pubblici automatici.

## Profili coinvolti

- `aws`: coinvolto (stack `aws-ec2`, `aws-terraform-ec2`, script di servizio
  in `script/aws-ecs`).
- `sqlite`: coinvolto solo per il deploy su AWS `script/sqlite-ec2`.
- `kube`, `azure`: non coinvolti, vedi Non-goals.

Tocca soltanto script di provisioning, Terraform e test: né il `core` né gli
adapter.

## Non-goals

- **ENI dei task Fargate** (`aws-ecs`, `aws-terraform-ecs`) e **nodi EKS**:
  le loro interfacce di rete le crea e gestisce AWS (ECS, eksctl con
  CloudFormation), non gli script; la spec lo dichiara come eccezione.
- **Taggare l'IPv4 pubblico automatico**: non è possibile. L'alternativa
  (allocare un Elastic IP per ogni istanza e rilasciarlo allo stop) è stata
  scartata: cambia l'infrastruttura e lascia costi se lo stop non avviene.
- **Ritaggare risorse già esistenti**: come per gli altri tag, valgono per le
  risorse create da qui in avanti.
- **Profili `kube` e `azure`**: non creano risorse AWS.

## Impact

- **Core e adapter**: non toccati.
- **Script**: `script/aws-ec2/start-all.sh`, `script/sqlite-ec2/start-all.sh`,
  `script/aws-ecs/deprecato-launch-ec2-mysql-client.sh`,
  `script/aws-terraform-ec2/compute.tf`,
  `script/automatic-test/test-tag-aws-script.sh`.
- **Spec**: `openspec/specs/provisioning-aws-tag/spec.md` (tre requisiti
  modificati).
- **Documentazione**: `PlatformAws.md`, Roadmap.
- **Costi**: nessuno durante la change; verifiche in dry-run, con `terraform
  validate` e con il controllo statico. Il test su cloud resta manuale e a cura
  dell'utente.
