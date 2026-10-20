## 1. Controllo statico (design D3)

Nessun modulo Java viene modificato: il task di coverage previsto per i moduli
non si applica. Al suo posto il controllo statico viene provato su casi
costruiti a mano (1.1).

- [x] 1.1 `script/automatic-test/test-tag-aws-script.sh`: per `ec2 run-instances` il comando è conforme solo se contiene `aws_tags_spec instance`, `aws_tags_spec volume` e `aws_tags_spec network-interface`; il motivo elenca i tipi mancanti. Verifica: `bash -n`; casi costruiti a mano in una cartella temporanea → `run-instances` con i tre tipi conforme; con solo `instance` e `volume` non conforme con "network-interface" nel motivo; con tag scritti a mano ancora non conforme; i casi di prova della change `verifica-tag-script-aws` (comando senza tag, tagging dedicato, JSON con heredoc, `safe_run` su più righe, commenti) danno lo stesso esito di prima; stesso risultato con `mawk`.
- [x] 1.2 Prima esecuzione sul repository, prima delle correzioni. Verifica: il controllo fallisce segnalando esattamente i tre `run-instances` di `script/aws-ec2/start-all.sh`, `script/sqlite-ec2/start-all.sh` e `script/aws-ecs/deprecato-launch-ec2-mysql-client.sh`, e nient'altro.

## 2. Correzioni (design D1, D2)

Nessun task di questo gruppo esegue comandi verso AWS.

- [x] 2.1 `script/aws-ec2/start-all.sh`, `script/sqlite-ec2/start-all.sh`, `script/aws-ecs/deprecato-launch-ec2-mysql-client.sh`: terza `--tag-specifications` con `aws_tags_spec network-interface` e nomi `gestioneannotazioni-ec2-eni`, `gestioneannotazioni-sqlite-ec2-eni`, `gestioneannotazioni-mysql-client-eni`; commento di `script/aws-tags.sh` aggiornato con i tipi usati. Verifica: `bash -n` sui quattro file; dry-run completo di `sqlite-ec2/start-all.sh` con stub `aws` → `run-instances` riceve tre tag specification (istanza con marcatore, volume, interfaccia di rete) con i sei tag e i `Name` attesi; per gli altri due script, valutazione delle tre `--tag-specifications` del comando con `aws-tags.sh` caricato → stessi controlli.
- [x] 2.2 `script/aws-terraform-ec2/compute.tf`: risorsa `aws_ec2_tag` con `for_each` sui tag comuni più `Name = "${local.ec2_name}-eni"`, su `aws_instance.app.primary_network_interface_id`. Verifica: `terraform fmt -check` e `terraform init -backend=false` + `terraform validate` nella cartella (nessun plan né apply); `terraform console` o lettura del codice per confermare le sei chiavi.
- [x] 2.3 Seconda esecuzione del controllo statico sul repository. Verifica: `./script/automatic-test/test-tag-aws-script.sh` termina con 0 e nessun comando non conforme.

## 3. Documentazione

- [x] 3.1 `PlatformAws.md`, sezione "Tag delle risorse AWS": ENI delle istanze taggate (`Name` = `<istanza>-eni`); IPv4 pubblico automatico non taggabile e comando `aws ec2 describe-network-interfaces` con filtro sul tag `Project` per ritrovarlo; ENI dei task Fargate e dei nodi EKS create da AWS; regola nuova del controllo statico. Verifica: i comandi riportati corrispondono agli script.

## 4. Verifica finale

- [x] 4.1 (Manuale, su cloud, solo su richiesta esplicita: genera costi) Eseguito dall'utente, esito positivo (2026-10-20). Avvio di uno stack EC2 (per esempio `script/sqlite-ec2`), verifica in console o con `aws ec2 describe-network-interfaces` dei sei tag sull'ENI dell'istanza e dell'IPv4 pubblico associato, poi `stop-all.sh`. Verifica: esiti annotati in questo task da chi lo esegue.
- [x] 4.2 Regressione: `./script/automatic-test/test-tag-aws-script.sh` dalla root, lo script di `script/automatic-test` che copre questa change (la change non tocca il runtime dell'applicazione, quindi i test dei profili non cambiano esito). Verifica: uscita 0 e riepilogo senza `test_ko`.
