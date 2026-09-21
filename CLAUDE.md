# CLAUDE.md

Istruzioni per Claude Code su questo repository. Rispondi e documenta **in italiano**.

Note importanti che NON DEVI MAI disobbedire:
- non sei MAI autorizzato a lanciare comandi in cloud , per esempio aws-cli `aws` e azure-cli `az`
- non sei MAI autorizzato a fare operazioni con git come commit/push/pull
- sei autorizzato a lanciare sh SOLO se sono contenuti in questo workspace, per lanciare sh fuori dal repository chiedi conferma
- sei autorizzato a fare operazioni su docker locale come le pull ma MAI fare push delle immagini
- per ogni modifica che ti viene chiesta, usa openspec come framework prima delle modifiche


## Il progetto in tre righe

"Sistema di Gestione Annotazioni" (`it.alnao.springbootexample`, v0.0.2): Java 21
+ Spring Boot 3.3.5, Maven multi-modulo, **architettura esagonale**. Gestisce
annotazioni con workflow di stati, import/export su coda e lock in modifica.
Nato come esempio pratico e didattico: leggibilita' e coerenza fra i moduli
contano quanto la funzionalita'.

**Il contesto completo sta in [openspec/project.md](openspec/project.md)**:
moduli, port, stati, comandi, script. Leggilo prima di interventi non banali,
invece di ricostruire tutto dal codice.

## Regole non negoziabili

1. **La logica sta nel `core`.** Gli adapter implementano soltanto i port
   (`core/repository`, `core/portService`). I controller di `adapter-api`
   delegano, non decidono.
2. **Mai un `if` sul profilo Spring dentro al `core`.** Un backend tecnologico
   nuovo e' un adapter nuovo con `@Profile`.
3. **Constructor injection.** Niente `@Autowired` sui campi: il refactor e' gia'
   stato fatto, non reintrodurlo.
4. **I quattro profili sono un contratto.** `sqlite`, `kube`, `aws`, `azure`
   devono comportarsi allo stesso modo. Una modifica che tocca persistenza, code
   o lock e' incompleta finche' non li copre tutti e quattro: dillo
   esplicitamente se ne lasci indietro uno.
5. **Niente credenziali in chiaro** nei file versionati.

## Comandi

```bash
mvn clean package                          # build completo con test
mvn clean package -DskipTests              # build veloce
mvn -q -pl adapter-aws,core -am test       # test di alcuni moduli
./script/automatic-test/test-all.sh        # regressione completa (dalla root)
mkdir -p application/data                  # serve prima dei test locali
```

Script di test per singolo profilo in [script/automatic-test/](script/automatic-test/).
Prima di dichiarare finito un lavoro che tocca il runtime: build pulita + lo
script del profilo interessato.

## Cosa non fare senza che te lo chieda

- **Comandi git**: niente `commit`, `push`, `pull`, `merge`.
- **Script di provisioning cloud** in `script/aws-*` e `script/azure-*`: creano
  risorse reali su AWS e Azure e **generano costi**.
- Riformattare o "ripulire" file che non c'entrano con il lavoro in corso.

## Come si governano le modifiche

Il progetto usa **OpenSpec** ([openspec/](openspec/)):

- `openspec/specs/` — cosa il sistema garantisce oggi (contratto)
- `openspec/changes/` — proposte in corso: proposal, spec delta, design, task
- `/opsx:explore` → `/opsx:propose` → *approvazione* → `/opsx:apply` → `/opsx:archive`

Per una modifica non banale, proponi di passare da li' invece di partire subito
a scrivere codice. [Roadmap.md](Roadmap.md) racconta cosa e' stato fatto
(✅/🚧); le spec dichiarano cosa deve valere. Quando una change viene
archiviata, si aggiornano le spec **e** si spunta la voce in Roadmap.

## Documentazione

[README.md](README.md) (struttura, API, esecuzione, Swagger, Sonar, auth) ·
[Roadmap.md](Roadmap.md) · [PlatformDockerHub.md](PlatformDockerHub.md) ·
[PlatformAws.md](PlatformAws.md) · [PlatformAzure.md](PlatformAzure.md)
