## Context

Motivazione in `proposal.md`; requisiti nuovi in
`specs/provisioning-aws-tag/spec.md`, requisiti esistenti nella spec
principale `provisioning-aws-tag`.

Censimento eseguito prima della change sugli 8 script sh che creano risorse
(circa 50 comandi):

| Script | Esito |
|---|---|
| `aws-ec2/start-all.sh` | conforme, tranne l'Elastic IP (mai taggato) |
| `aws-ecs/start-all.sh`, `aws-ecs/run-ecs-mysql-insert.sh`, `aws-ecs/deprecato-launch-ec2-mysql-client.sh` | conformi |
| `aws-eks/start-all.sh` | conforme (cluster con `--tags`, load balancer con annotation) |
| `aws-terraform-ecs/start-all.sh` | conforme (`run-task` con i tag da `terraform output`) |
| `sqlite-ec2/start-all.sh` | security group e key pair conformi; istanza con due tag scritti a mano, volumi senza tag, Elastic IP mai taggato |
| `init-database/init-dynamodb.sh` | tabelle senza tag (DynamoDB locale di `aws-onprem`) |

Forme di tagging già in uso, tutte basate su `script/aws-tags.sh`:

- nel comando: `--tags $(aws_tags_kv ...)`, `--tags "$(aws_tags_map ...)"`,
  `--tags "$(aws_tags_json ...)"`, `--tag-specifications "$(aws_tags_spec ...)"`;
- comando dedicato subito dopo la creazione: `aws ec2 create-tags`,
  `aws sqs tag-queue`, `aws iam tag-role`;
- file JSON generato dallo script con heredoc e passato con
  `--cli-input-json file://...` (`"tags": $(aws_tags_json ...)`);
- `run-task` del wrapper Terraform: `--tags "$(terraform output -raw init_task_tags_json)"`,
  tag generati dai `locals` Terraform (`ManagedBy=Terraform`).

Alcuni comandi sono dentro una stringa passata a `safe_run "..." "aws ..."`
(`aws-ecs`), molti sono spezzati su più righe con `\`.

Verificato prima della change: DynamoDB Local accetta `Tags` in
`CreateTable` e crea la tabella, ma `ListTagsOfResource` e `TagResource`
rispondono "Tagging is not currently supported in DynamoDB Local".

## Goals / Non-Goals

**Goals:**

- Un controllo che, se fosse esistito, avrebbe segnalato le lacune di
  `sqlite-ec2` al momento della change `tag-risorse-aws`.
- Correzioni allineate allo stile degli script esistenti, senza nuove
  funzioni in `aws-tags.sh` se non servono.

**Non-Goals:**

- Il controllo non verifica che il *valore* di `Name` sia quello giusto per
  ogni risorsa: verifica che i tag arrivino dalle funzioni condivise, che
  garantiscono i sei tag. Il valore di `Name` resta compito della review e
  della spec.
- Nessun parser shell completo: il controllo riconosce le forme usate nel
  repository e documenta le regole.

## Decisions

### D1. Controllo in bash + awk, nella cartella dei test automatici

Nuovo `script/automatic-test/test-tag-aws-script.sh`. Un passo awk legge ogni
script, unisce le righe continuate con `\`, salta le righe di commento e
individua i comandi di creazione; il resto è bash. Registra l'esito con
`test_ok`/`test_ko` di `lib-report.sh` (un esito per ogni script analizzato,
con il dettaglio file:riga dei comandi non conformi) ed esce con codice
diverso da zero se trova violazioni. Accetta come argomenti opzionali i file o
le cartelle da analizzare (default `script/`), così si può provare su una
cartella temporanea con casi costruiti a mano.

`test-all.sh` lo lancia con `esegui_profilo`, come gli altri test, prima dei
profili: è il test più veloce e non richiede container.

Alternative scartate:
- *Python*: parsing più comodo, ma negli script del repository python compare
  in un solo file, mentre awk e jq sono già usati in molti; bash + awk non
  aggiunge strumenti da installare.
- *grep riga per riga*: non regge i comandi spezzati con `\` né i
  comandi dentro `safe_run "..."`.

### D2. Regole di conformità

Un comando di creazione (elenco nel requisito della spec) è conforme se vale
almeno una delle condizioni:

1. **Nel comando**: il comando, dopo l'unione delle righe, contiene una
   chiamata `aws_tags_kv|aws_tags_map|aws_tags_json|aws_tags_spec`, oppure
   `terraform output -raw <nome>_tags_json` (tag standard generati da
   Terraform).
2. **File JSON**: il comando usa `--cli-input-json file://<percorso>` e lo
   stesso script scrive quel percorso con un heredoc che contiene
   `aws_tags_json`.
3. **Comando dedicato**: il comando non ha tag propri e lo stesso script
   contiene il comando di tagging previsto per quel tipo di creazione, con le
   funzioni condivise. La tabella è nel controllo stesso; per esempio
   `ec2 create-security-group` → `ec2 create-tags`, `sqs create-queue` →
   `sqs tag-queue`, `iam create-role` → `iam tag-role`, `dynamodb
   create-table` → `dynamodb tag-resource`, `rds create-db-*` → `rds
   add-tags-to-resource`.

Tag presenti ma scritti a mano (nessuna funzione condivisa) → sempre non
conforme, anche se la regola 3 sarebbe soddisfatta: è proprio il caso
dell'istanza di `sqlite-ec2`.

Precisazione emersa in apply: la regola 3 associa la creazione al *tipo* di
comando di tagging e non a un servizio qualsiasi. Con "un `ec2 create-tags`
qualunque nello stesso file" l'istanza di `sqlite-ec2` sarebbe risultata
conforme grazie al tag del security group. L'associazione resta per file e non
per singola risorsa: è un'approssimazione voluta (vedi Risks).

### D3. sqlite-ec2: istanza e volumi con `aws_tags_spec`

`run-instances` passa
`--tag-specifications "$(aws_tags_spec instance gestioneannotazioni-sqlite-ec2 $TAG_MARKER_KEY)" "$(aws_tags_spec volume gestioneannotazioni-sqlite-ec2-volume)"`,
identico nello stile ad `aws-ec2`. Il `Name` dell'istanza passa da
`gestioneannotazioni-sqlite-ec2-app` a `gestioneannotazioni-sqlite-ec2`, come
chiede la spec; il marcatore `gestioneannotazioni-sqlite-ec2-app=true` resta,
quindi `stop-all.sh` e `rotate-secret.sh`, che filtrano sul marcatore,
continuano a funzionare. Nessuno script filtra sul `Name`.

### D4. Elastic IP: blocco best effort dopo l'avvio dell'istanza

In `aws-ec2` (dopo il blocco istanza esistente/nuova, così copre entrambi i
rami) e in `sqlite-ec2` (dopo `wait instance-running`):

```
EIP_ALLOC_ID=$(aws ec2 describe-addresses --filters Name=instance-id,Values=$INSTANCE_ID \
  --query 'Addresses[0].AllocationId' --output text --region $REGION 2>/dev/null || true)
if [ -n "$EIP_ALLOC_ID" ] && [ "$EIP_ALLOC_ID" != "None" ]; then
  aws ec2 create-tags --resources $EIP_ALLOC_ID --tags $(aws_tags_kv gestioneannotazioni-ec2-eip) ... \
    || echo "Avviso: tag dell'Elastic IP non applicati"
fi
```

Con `Name` `gestioneannotazioni-ec2-eip` o `gestioneannotazioni-sqlite-ec2-eip`.
Nessuna allocazione: se l'Elastic IP non c'è il blocco non fa nulla. Un errore
di tagging non ferma lo script, perché lo stack funziona anche senza.

### D5. init-dynamodb: `aws-tags.sh` montato nel container

`init-dynamodb.sh` fa `source "$(dirname "$0")/../aws-tags.sh" init-database`
e passa `--tags $(aws_tags_kv <tabella>)` ai tre `create-table`. Il container
`init-dynamodb` di `script/aws-onprem/docker-compose.yml` monta in più
`../aws-tags.sh:/aws-tags.sh:ro`: da `/init-database/init-dynamodb.sh` il
percorso `$(dirname "$0")/../aws-tags.sh` risolve proprio in `/aws-tags.sh`,
e lo stesso percorso funziona lanciando lo script dal repository.
`Project` vale `Annotazioni.init-database`, secondo la regola della spec
(cartella dello script eseguito).

## Risks / Trade-offs

- [Regola 3 per servizio e non per risorsa: uno script che tagga una coda e
  ne crea un'altra senza tag passerebbe il controllo] → accettato: negli
  script attuali ogni creazione ha il suo tagging accanto; il controllo è una
  rete contro la dimenticanza totale e contro i tag scritti a mano, non una
  prova formale.
- [Falsi positivi su forme nuove di tagging non previste da D2] → il
  messaggio di errore indica file e riga; la forma nuova va aggiunta alle
  regole del controllo nella stessa change che la introduce.
- [DynamoDB Local ignora i tag: se una versione futura li rifiutasse,
  `init-dynamodb.sh` maschera l'errore con `|| echo` e `aws-onprem` resterebbe
  senza tabelle] → la verifica passa da `test-aws-onprem.sh`, che fallisce se
  le tabelle mancano; l'immagine `amazon/dynamodb-local` usata oggi è stata
  provata prima della change.
- [La verifica passa da `test-aws-onprem.sh`, che usa aws-cli nei container di
  init (verso DynamoDB Local) e anche sull'host (`aws sqs ...` con
  `--endpoint-url=http://localhost:4566`, verso LocalStack)] → nessuna chiamata
  raggiunge AWS; poiché CLAUDE.md vieta aws-cli, l'esecuzione da parte
  dell'agente richiede un'autorizzazione esplicita (data in apply).
- [Le correzioni di `aws-ec2` e `sqlite-ec2` non sono provate su cloud dalla
  change] → dry-run con stub `aws` che registrano gli argomenti; il test su
  cloud resta un task manuale dell'utente, come in `secret-sqlite`.

## Migration Plan

Nessuna migrazione: le risorse già create non vengono ritaggate (requisito
"Rilancio idempotente"). Le istanze `sqlite-ec2` create da qui in avanti hanno
il nuovo `Name`. Rollback: ripristinare le righe modificate negli script.
