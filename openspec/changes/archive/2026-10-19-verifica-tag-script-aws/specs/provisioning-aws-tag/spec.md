## ADDED Requirements

### Requirement: Verifica statica dei tag negli script

Il repository SHALL fornire un controllo statico che analizza gli script sh
di `script/` (esclusi `script/azure-onprem-non-funziona` e le cartelle
`.terraform`) e, per ogni comando che crea una risorsa (`aws <servizio>
create-*` diverso da `create-tags`, `aws ec2 run-instances`, `aws ec2
allocate-address`, `aws ecs run-task`, `aws ecs register-task-definition`,
`eksctl create`), verifica che i sei tag standard siano applicati tramite le
funzioni condivise di `script/aws-tags.sh`: nel comando stesso, nel file JSON
passato al comando oppure con un comando di tagging della stessa risorsa nello
stesso script. Tag scritti a mano non soddisfano il controllo, perché non
garantiscono i sei tag. Il controllo MUST NOT eseguire chiamate verso AWS. In
caso di violazione SHALL terminare con errore elencando file e riga di ogni
comando non conforme. Il controllo SHALL essere lanciabile da solo e SHALL far
parte della regressione completa `test-all.sh`, con l'esito registrato nel
riepilogo.

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

#### Scenario: Tagging con comando dedicato

- **WHEN** uno script analizzato crea una coda con `aws sqs create-queue` e
  subito dopo la tagga con `aws sqs tag-queue` usando le funzioni di
  `script/aws-tags.sh`
- **THEN** il controllo considera il comando conforme

#### Scenario: Comando commentato

- **WHEN** un comando di creazione senza tag compare solo in una riga di
  commento
- **THEN** il controllo lo ignora

### Requirement: Tag nelle tabelle DynamoDB locali

Lo script che inizializza le tabelle DynamoDB locali per l'ambiente
`aws-onprem` SHALL passare i sei tag standard nei comandi di creazione delle
tabelle, con `Name` uguale al nome della tabella, come fanno gli script che
creano le stesse tabelle su AWS. DynamoDB Local accetta i tag ma non li
conserva, quindi il requisito riguarda i comandi e non la lettura dei tag;
l'aggiunta dei tag MUST NOT impedire la creazione delle tabelle.

#### Scenario: Avvio di aws-onprem

- **WHEN** viene avviato l'ambiente `aws-onprem` con docker-compose
- **THEN** il servizio di inizializzazione crea le tre tabelle
  `annotazioni`, `annotazioni_storico` e `annotazioni_storicoStati`
- **AND** l'applicazione del profilo `aws` in locale le usa senza errori

#### Scenario: Controllo statico

- **WHEN** il controllo statico analizza lo script di inizializzazione delle
  tabelle locali
- **THEN** i tre comandi di creazione risultano conformi
