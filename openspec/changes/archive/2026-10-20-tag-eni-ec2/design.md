## Context

Motivazione in `proposal.md`, requisiti in `specs/provisioning-aws-tag/spec.md`.

Le istanze EC2 vengono create in quattro punti:

| Dove | Come | Istanza | Volumi | ENI primaria |
|---|---|---|---|---|
| `script/aws-ec2/start-all.sh` | `aws ec2 run-instances` | `aws_tags_spec instance` | `aws_tags_spec volume` | no |
| `script/sqlite-ec2/start-all.sh` | `aws ec2 run-instances` | `aws_tags_spec instance` | `aws_tags_spec volume` | no |
| `script/aws-ecs/deprecato-launch-ec2-mysql-client.sh` | `aws ec2 run-instances` | `aws_tags_spec instance` | `aws_tags_spec volume` | no |
| `script/aws-terraform-ec2/compute.tf` | `aws_instance` | `default_tags` + `tags` | `root_block_device.tags` (merge esplicito) | no |

`aws ec2 run-instances` accetta `--tag-specifications` anche con
`ResourceType=network-interface`, che tagga l'interfaccia primaria creata
insieme all'istanza. In Terraform la risorsa `aws_instance` non tagga
l'interfaccia primaria (né con `tags` né con `default_tags`), ma ne espone
l'identificativo in `primary_network_interface_id`.

Il controllo statico (`test-tag-aws-script.sh`) oggi considera conforme un
`run-instances` se nel comando compare una qualsiasi funzione `aws_tags_*`.

## Goals / Non-Goals

**Goals:**

- ENI primaria taggata alla creazione in tutti e quattro i punti, con lo
  stesso stile già usato per istanze e volumi.
- Il controllo statico deve accorgersi di un `run-instances` futuro che
  dimentica l'interfaccia di rete.

**Non-Goals:**

- Nessuna allocazione di Elastic IP (soluzione B scartata, vedi proposal).
- Nessun tagging a posteriori di ENI create da AWS (Fargate, EKS).

## Decisions

### D1. Script sh: terza `--tag-specifications` per l'interfaccia di rete

Ai tre `run-instances` si aggiunge
`"$(aws_tags_spec network-interface <nome-istanza>-eni)"` accanto alle due
esistenti. `aws_tags_spec` costruisce già il formato
`ResourceType=...,Tags=[...]` per qualunque tipo di risorsa: nessuna funzione
nuova in `aws-tags.sh`, solo il commento che elenca i tipi usati. Il tag
marcatore resta solo sull'istanza, come oggi: gli script ritrovano le
istanze, non le ENI.

Nomi: `gestioneannotazioni-ec2-eni`, `gestioneannotazioni-sqlite-ec2-eni`,
`gestioneannotazioni-mysql-client-eni`.

Alternativa scartata: `aws ec2 create-tags` sull'ENI dopo `wait
instance-running`. Comando in più, una finestra in cui l'ENI esiste senza
tag, e un caso nuovo da insegnare al controllo statico.

### D2. Terraform: `aws_ec2_tag` sull'ENI primaria

In `compute.tf`:

```hcl
resource "aws_ec2_tag" "eni" {
  for_each    = merge(local.common_tags, { Name = "${local.ec2_name}-eni" })
  resource_id = aws_instance.app.primary_network_interface_id
  key         = each.key
  value       = each.value
}
```

Un tag per risorsa, perché `aws_ec2_tag` gestisce una coppia chiave/valore.
`aws_ec2_tag` non riceve i `default_tags` del provider, quindi i tag comuni
vanno passati esplicitamente, come già per i volumi. Il `Name` segue lo
schema della spec: `gestioneannotazioni-terraform-ec2-eni`.

Alternativa scartata: `network_interface` esplicito dentro `aws_instance` con
una `aws_network_interface` creata a parte. Cambia la struttura dell'istanza
e il modo in cui riceve l'IP pubblico automatico, per ottenere solo i tag.

### D3. Controllo statico: `run-instances` richiede i tre tipi

Nel parser awk, per `ec2 run-instances` la regola "funzioni nel comando"
diventa più stretta: il comando, dopo l'unione delle righe, deve contenere
`aws_tags_spec instance`, `aws_tags_spec volume` e `aws_tags_spec
network-interface`. Se mancano uno o più tipi il comando è non conforme e il
motivo li elenca (es. "manca aws_tags_spec network-interface"). Le altre
regole non cambiano.

Il controllo si verifica in due tempi, come nella change
`verifica-tag-script-aws`: prima della correzione degli script deve
segnalare esattamente i tre `run-instances`, dopo deve uscire con 0.

### D4. Documentazione

`PlatformAws.md`, sezione "Tag delle risorse AWS": ENI delle istanze
taggate con `Name` = `<istanza>-eni`; IPv4 pubblico automatico non taggabile
e come ritrovarlo (`describe-network-interfaces` filtrando per tag
`Project`); ENI dei task Fargate e dei nodi EKS create da AWS e senza tag;
regola nuova del controllo statico.

## Risks / Trade-offs

- [`ResourceType=network-interface` rifiutato da `run-instances` in un caso
  particolare] → è un tipo documentato per `run-instances`; la verifica
  locale è un dry-run con stub, il test reale è il task manuale su cloud.
- [`aws_ec2_tag` e un futuro `tags` sull'ENI gestito altrove potrebbero
  contendersi le stesse chiavi] → oggi nessun'altra risorsa Terraform tocca
  l'ENI; commento nel file.
- [ENI di istanze già esistenti restano senza tag] → requisito "Rilancio
  idempotente": nessun ritagging; un ciclo `stop-all.sh` / `start-all.sh`
  le ricrea taggate.

## Migration Plan

Nessuna migrazione. Le istanze create da qui in avanti hanno l'ENI taggata.
Rollback: togliere la terza `--tag-specifications`, la risorsa `aws_ec2_tag`
e la regola nel controllo statico.
