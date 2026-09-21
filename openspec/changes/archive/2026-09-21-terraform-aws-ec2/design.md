## Context

Vedi `proposal.md` - Why. Stato attuale rilevante:

- Lo stack di riferimento è `script/aws-ec2/start-all.sh` (445 righe, letto
  per intero): IAM → SG → Aurora (attesa `available`) → DynamoDB → SQS →
  ElastiCache (attesa) → key pair → EC2 con user data che aspetta Aurora, esegue
  `init-mysql.sql` (7,4 KB, incorporato nello user data) e avvia il container.
  `stop-all.sh` cancella per nome, l'istanza per tag marcatore.
- Nomi delle tabelle: `annotazioni` e `annotazioni_storico` sono configurabili
  via env, `annotazioni_storicoStati` è fissa nell'adapter → i nomi vanno
  mantenuti, i due stack non coesistono.
- `test-aws-ec2.sh` trova l'istanza con `tag:gestioneannotazioni-app=true`,
  non usa `lib-report.sh`; `test-prenotazione-annotazione.sh` (che richiama)
  sì. `openspec/project.md` chiede che un test nuovo registri gli esiti con
  `lib-report.sh`.
- Sulla macchina di sviluppo: Terraform 1.16.2, nessuna credenziale AWS
  utilizzabile dall'agente. `terraform init` scarica i provider dal registry
  (rete, non cloud): consentito. `plan`/`apply` restano all'utente.
- Il bucket `alnao-dev-terraform` (eu-central-1) esiste già; Terraform ≥ 1.10
  supporta il lock nativo S3 (`use_lockfile = true`), quindi niente tabella
  DynamoDB per il lock.
- Spec vincolanti: `provisioning-aws-tag` (sei tag, `Name` per risorsa,
  marcatori) e la nuova `provisioning-aws-terraform`.

## Goals / Non-Goals

**Goals:**
- Un file per "capitolo" dello stack, nell'ordine in cui lo script bash crea
  le risorse, con commenti in italiano: chi conosce lo script si orienta
  subito.
- Nessun valore duplicato: nomi in `locals.tf`, tag comuni in un solo
  `default_tags`, endpoint passati allo user data dalle risorse stesse.
- Wrapper che si usano come gli altri (`./script/aws-terraform-ec2/start-all.sh`
  dalla root) e che non richiedono di conoscere Terraform per il caso base.
- Validazione statica ripetibile senza credenziali.

**Non-Goals:**
- Moduli riusabili o workspace Terraform: una sola configurazione piatta.
- Import dello stack bash esistente nello state (`terraform import`): si parte
  da account vuoto.
- Riprodurre gli `sleep`/retry dello script: Terraform aspetta da solo che
  Aurora e Redis siano `available`.

## Decisions

### D1. Layout dei file

```
script/aws-terraform-ec2/
  versions.tf           terraform {} con required_version >= 1.10, provider aws ~> 5.0,
                        tls ~> 4.0, local ~> 2.5, http ~> 3.4; backend "s3" {} (parziale)
  variables.tf          region, environment (validation), instance_type, db_instance_class,
                        db_username, db_password (sensitive, senza default), db_name,
                        ssh_allowed_cidr (default null = IP del chiamante), key_name
  locals.tf             nomi delle risorse (identici allo stack bash), common_tags, marker
  data.tf               aws_vpc default, aws_subnets della VPC, aws_ami Amazon Linux 2,
                        http checkip.amazonaws.com
  iam.tf                aws_iam_role + 4 aws_iam_role_policy_attachment + aws_iam_instance_profile
  network.tf            aws_security_group + aws_vpc_security_group_ingress_rule (for_each) + egress
  database.tf           aws_rds_cluster + aws_rds_cluster_instance
  dynamodb.tf           3 aws_dynamodb_table (storicoStati con GSI)
  sqs.tf                2 aws_sqs_queue
  cache.tf              aws_elasticache_subnet_group + aws_elasticache_cluster
  compute.tf            tls_private_key + aws_key_pair + local_sensitive_file + aws_instance
  outputs.tf            ip, url, ssh, endpoint, code, percorso .pem
  user_data.sh.tftpl    template dello user data (stesso contenuto del heredoc bash)
  terraform.tfvars.example
  .terraform.lock.hcl   versionato (generato da terraform init)
  ../aws-tf-init.sh     (condiviso) configura il backend e fa terraform init
  start-all.sh / stop-all.sh / test-aws-terraform-ec2.sh
  README.md             uso rapido, variabili, backend, differenze dallo stack bash
```

*Alternativa scartata*: un solo `main.tf`. Più corto ma illeggibile per uno
stack di ~15 risorse; il progetto è didattico.

### D2. Tag: `default_tags` del provider + `Name` per risorsa

```hcl
provider "aws" {
  region = var.region
  default_tags { tags = local.common_tags }   # Environment, Project, Owner, CostCenter, ManagedBy
}
resource "aws_security_group" "app" { tags = { Name = local.sg_name } }
```

- `local.common_tags` = `{ Environment = var.environment, Project =
  "Annotazioni.aws-terraform-ec2", Owner = "AlNao", CostCenter =
  "Annotazioni", ManagedBy = "Terraform" }`.
- `Name` segue la mappa della spec dei tag (nome proprio della risorsa);
  istanza e volumi: `gestioneannotazioni-terraform-ec2` /
  `gestioneannotazioni-terraform-ec2-volume`.
- Volumi: `root_block_device { tags = merge(local.common_tags, { Name = ... }) }`
  con merge esplicito, perché i `default_tags` non si applicano in modo
  affidabile ai tag dei block device in tutte le versioni del provider 5.x.
- Marcatore `gestioneannotazioni-terraform-app = "true"` solo nei `tags`
  dell'istanza (`local.marker_key`).
- `aws_key_pair`, `aws_iam_role`, `aws_iam_instance_profile`, `aws_sqs_queue`,
  `aws_elasticache_*`, `aws_rds_*`, `aws_dynamodb_table` supportano `tags` e
  ricevono i default: coperte tutte le risorse dello scenario "Stack EC2 via
  Terraform".

### D3. Backend S3 parziale + override per lo state locale

`versions.tf` dichiara `backend "s3" {}` vuoto (configurazione parziale). Il
file sorgente `tf-init.sh` (caricato da `start-all.sh` e `stop-all.sh`)
espone `terraform_init_backend`:

```bash
TF_STATE_BUCKET="${TF_STATE_BUCKET-alnao-dev-terraform}"   # "-" e non ":-": vuoto = locale
TF_STATE_REGION="${TF_STATE_REGION:-eu-central-1}"
TF_STATE_KEY="${TF_STATE_KEY:-annotazioni/aws-terraform-ec2/terraform.tfstate}"
if [ -z "$TF_STATE_BUCKET" ]; then
  printf 'terraform {\n  backend "local" {\n    path = "terraform.tfstate"\n  }\n}\n' > backend_override.tf
  terraform init -input=false -reconfigure
else
  rm -f backend_override.tf
  terraform init -input=false -reconfigure \
    -backend-config="bucket=$TF_STATE_BUCKET" -backend-config="key=$TF_STATE_KEY" \
    -backend-config="region=$TF_STATE_REGION" -backend-config="use_lockfile=true"
fi
```

- Un file `*_override.tf` sostituisce il blocco `backend` (regola degli
  override di Terraform): niente modifiche ai file versionati, il file è
  ignorato da git.
- `-reconfigure` evita che Terraform provi a migrare lo state fra backend
  quando cambia bucket: chi vuole spostare uno state lo fa a mano con
  `-migrate-state` (documentato nel README).
- `use_lockfile=true` passato come backend-config: lock `.tflock` accanto allo
  state, nessuna tabella DynamoDB (Terraform ≥ 1.10).

*Alternativa scartata*: `backend.hcl` da compilare a mano: meno comodo dei
default via env e non copre il caso locale. Scartato anche un `backend.tf`
generato per intero dal wrapper: i file versionati non dichiarerebbero il
backend, e `terraform init` lanciato a mano userebbe lo state locale senza
avvisare.

### D4. IP del chiamante e regole del security group

`data "http" "my_ip"` su `https://checkip.amazonaws.com` (come lo script) →
`local.ssh_cidr = coalesce(var.ssh_allowed_cidr, "${chomp(data.http.my_ip.response_body)}/32")`.
Regole con `aws_vpc_security_group_ingress_rule` in `for_each` su una mappa
`{ mysql = 3306, redis = 6379, app = 8080, adminer = 8086, dynamodb_admin = 8087 }`
verso `0.0.0.0/0` più la regola SSH; egress totale esplicito (Terraform non
crea l'egress di default come fa la console).

### D5. Key pair generata da Terraform

`tls_private_key` (RSA 4096) → `aws_key_pair` (`gestioneannotazioni-terraform-key`)
→ `local_sensitive_file` in `${path.module}/gestioneannotazioni-terraform-key.pem`
con `file_permission = "0400"`. La chiave privata è nello state: accettato
per uno stack demo con bucket privato; README spiega l'alternativa
(`var.key_name` verso una key pair esistente, disattivando la generazione con
un `count`). `stop-all.sh` cancella il `.pem` residuo.

### D6. Aurora, DynamoDB, SQS, ElastiCache

- Aurora: `aws_rds_cluster` (`engine = "aurora-mysql"`, `skip_final_snapshot`,
  `apply_immediately`, `vpc_security_group_ids`) + `aws_rds_cluster_instance`
  (`db.t3.medium`, `publicly_accessible = true`). Nessuna `engine_version`
  pinnata, come lo script. Non si passa `database_name`: la crea
  `init-mysql.sql` (`CREATE DATABASE IF NOT EXISTS`).
- DynamoDB: `annotazioni` e `annotazioni_storico` `PAY_PER_REQUEST` hash `id`;
  `annotazioni_storicoStati` `PROVISIONED` 5/5, hash `idOperazione`, GSI
  `idAnnotazione-index` (hash `idAnnotazione`, range `dataModifica`,
  `projection_type = "ALL"`, 5/5). Attributi dichiarati solo per le chiavi.
- SQS: due `aws_sqs_queue` standard con i default (come `create-queue` senza
  attributi).
- ElastiCache: subnet group sulle subnet della VPC di default;
  `aws_elasticache_cluster` `engine = "redis"`, `cache.t3.micro`, 1 nodo, porta
  6379, `security_group_ids`.

### D7. EC2 e ordine di creazione

`aws_instance` con `ami = data.aws_ami.amazon_linux_2.id`, `iam_instance_profile`,
`vpc_security_group_ids`, `key_name`, `user_data = templatefile(...)`,
`user_data_replace_on_change = true` (cambia lo user data → istanza ricreata,
coerente con "lo stack è lo script"). Dipendenze:
- implicite: lo user data referenzia `aws_rds_cluster.this.endpoint`,
  `aws_elasticache_cluster.this.cache_nodes[0].address/port`,
  `aws_sqs_queue.*.url`;
- esplicita: `depends_on = [aws_rds_cluster_instance.this]`, perché l'endpoint
  del cluster esiste prima che l'istanza DB sia utilizzabile e lo user data
  ha solo 3 tentativi da 30 s. Terraform attende `available` sull'istanza
  Aurora e sul cluster Redis, sostituendo i cicli di attesa dello script.

### D8. User data

`user_data.sh.tftpl` è il heredoc dello script bash trasposto: variabili
Terraform `${aurora_endpoint}`, `${db_user}`, `${db_pass}`, `${db_name}`,
`${region}`, `${sqs_export_url}`, `${sqs_import_url}`, `${redis_host}`,
`${redis_port}`, `${init_sql}` (contenuto di `../init-database/init-mysql.sql`
via `file()`); i `$` di shell che nello script erano `\$` diventano `$` puri
(nel template solo `${` è interpolazione; `$${` dove serve il letterale).
Il contenuto SQL inserito non viene ri-templatizzato. Dimensione ben sotto i
16 KB dello user data.

### D9. Wrapper

- `start-all.sh`: `cd` nella propria cartella; `export TF_VAR_environment=${ENVIRONMENT:-dev}`,
  `TF_VAR_db_password=${DB_PASS:-gestioneannotazioni_pass}`,
  `TF_VAR_region=${AWS_REGION:-eu-central-1}`; `terraform_init_backend`;
  `terraform apply -auto-approve -input=false`; `terraform output`.
  Non valida `ENVIRONMENT` in bash: lo fa il blocco `validation` della
  variabile, con lo stesso messaggio.
- `stop-all.sh`: stesso init; `terraform destroy -auto-approve -input=false`;
  `rm -f *.pem`. Senza stack: destroy "No changes", exit 0.
- `test-aws-terraform-ec2.sh`: copia di `test-aws-ec2.sh` con filtro
  `tag:gestioneannotazioni-terraform-app`, `set -e` rimosso a favore di
  `test_ok`/`test_ko` di `lib-report.sh` (`report_run_inizio "aws
  (terraform-ec2)"` / `report_chiusura`), come chiede `project.md`; richiama
  `test-prenotazione-annotazione.sh` come l'originale. Esce con errore se non
  trova l'istanza.
- Il default della password nel wrapper è lo stesso letterale già presente in
  `script/aws-ec2/start-all.sh`: non introduce una credenziale nuova nei file
  versionati; i file `.tf` non ne contengono.

### D10. Validazione senza cloud

`terraform fmt -check -recursive`, `terraform init -backend=false` (scarica i
provider e genera `.terraform.lock.hcl`, che viene versionato) e
`terraform validate`. `validate` non contatta AWS. Il `plan` richiede
credenziali per i data source: resta all'utente, insieme ad apply/test/destroy.
Dry-run dei wrapper con uno stub di `terraform` che logga gli argomenti, per
verificare la composizione di `init` nei tre casi (default, bucket custom,
locale).

### D11. Spec dei tag

Delta MODIFIED sul requisito "Tag standard su ogni risorsa creata": `ManagedBy`
`Sh` per gli script, `Terraform` per le configurazioni Terraform, scenario
"Stack EC2 via Terraform". Il marcatore distinto e i `Name` distinti stanno
nella capability nuova, non nel requisito "Tag marcatori conservati", che
resta sui marcatori bash.

## Risks / Trade-offs

- [Stack bash attivo durante l'apply] → l'apply fallisce sulla prima risorsa
  con nome duplicato (IAM role, il primo creato) senza toccare le altre;
  documentato: prima `script/aws-ec2/stop-all.sh`.
- [`script/aws-ec2/stop-all.sh` lanciato con lo stack Terraform attivo] →
  cancella per nome Aurora, DynamoDB, SQS, Redis, SG, IAM fuori dallo state
  (non la EC2, marcatore diverso). Rimedio: `terraform apply` ricrea ciò che
  manca (rileva il drift con `refresh`); documentato come errore da evitare.
- [Bucket dello state non raggiungibile o inesistente] → `terraform init`
  fallisce subito con l'errore S3, prima di creare risorse; con
  `TF_STATE_BUCKET=` si lavora in locale.
- [Chiave privata nello state remoto] → bucket privato, state cifrato
  server-side di default; alternativa key pair esistente nel README.
- [Aurora `engine_version` non pinnata] → come lo script; un cambio di default
  AWS può cambiare la versione fra un apply e l'altro; pinnabile con
  `var.db_engine_version` senza cambiare la spec.
- [Ricreazione della EC2 a ogni modifica dello user data] → voluto
  (`user_data_replace_on_change`); il dato persistente sta in Aurora/DynamoDB,
  non sull'istanza.
- [`data "http"` verso checkip fallisce senza rete] → `var.ssh_allowed_cidr`
  esplicito evita la chiamata.
- [Provider 5.x e `default_tags` sui volumi] → merge esplicito nel
  `root_block_device`, verificato nel `plan` dall'utente.

## Migration Plan

1. Creare la cartella e i file; `fmt`/`init -backend=false`/`validate`.
2. `.gitignore`, documentazione, Roadmap, project.md.
3. Utente: `./script/aws-ec2/stop-all.sh` se lo stack bash è attivo;
   `./script/aws-terraform-ec2/start-all.sh`; `test-aws-terraform-ec2.sh`;
   controllo tag (`get-resources --tag-filters Key=Project,Values=Annotazioni.aws-terraform-ec2`);
   `stop-all.sh`.
4. Rollback: cancellare la cartella e le righe di `.gitignore`/docs; nessuna
   risorsa resta se il destroy è stato eseguito. Uno state remoto orfano si
   elimina con `aws s3 rm`.

## Open Questions

Nessuna: bucket, region, chiave dello state, marcatore e gestione della key
pair sono stati decisi con l'utente.
