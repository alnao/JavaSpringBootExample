# Task 3 of plan Annotazioni import from SQS
AlNaoAIRunner runned this on 26/05/2026 13:12:57
Model: copilot:claude-sonnet-4.6 - Agent: DefaultAgent
Commit message: Scripts


Prompt:
Header: DefaultAgent

Context: Leggi lo stato attuale per sapere cosa hanno fatto i predecessori dai files.
/mnt/Dati4/todel/workspace/.AlNaoAIRunners/plan-f67b94e7-annotazioni-import-from-sqs/task1-aws-sqs.md
 - /mnt/Dati4/todel/workspace/.AlNaoAIRunners/plan-f67b94e7-annotazioni-import-from-sqs/task2-azure.md

Important: Hai il permesso e gli strumenti per modificare direttamente i file nel workspace corrente se necessario.
Important: Non eseguire mai comandi nel repository come commit, push, pull, merge o altri comandi di git. Non uscire mai dalla cartella di lavoro "workspace".

Constraint: Se finisci i tentativi di fix, scrivi 'STOP_FAILURE' e non fare il commit.

Task: Dato quanto fatto dai punti precedenti e leggi i file md di platform, voglio che mi aggiorni tutti gli script su aws e azure per aggiungere le code sqs , soprattutto il docker compose ma alla fine aggiorna anche gli md platform. Ricorda che non devi usare le stesse code già presenti che sono usate nell'export



Log content:
Now I have a full picture. Let me look at the AWS and Azure deployment scripts to understand queue creation patterns:

Now I have a complete picture. Let me plan the work:

Now fix `SqsAnnotazioneImportService` to use the import queue URL:

Also fix `isEnabled()` to check importQueueUrl:

Now update `application-aws.yml` to add the import queue URL:

Now update the docker-compose.yml to add the import SQS queue:

Now add `SQS_IMPORT_QUEUE_URL` to the app environment in docker-compose:

Now let me update the EC2 script to create the import queue. Let me see the relevant section:

Now add import queue creation after the export queue creation in EC2 script:

Now add `SQS_IMPORT_QUEUE_URL` to docker run in EC2 script. Let me find the exact lines:

Now update the ECS start-all.sh. Let me see the relevant sections:

Now add `SQS_IMPORT_QUEUE_URL` to the ECS task definition. Let me find the relevant line:

Also update the final echo at the end:

Now implement Azure import service. First, add `importQueueName` to `AzureProperties`:

Now look at application.yml to see what Azure ServiceBus config is there:

Now create the Azure import service. Let me look at the existing invio service and the AnnotazioneImportService interface:

Now create the Azure ServiceBus import service:

Now let's check if AnnotazioneImportScheduler has a conditional that might prevent it from loading without a bean:

Good - Kafka import is only on `kube` profile, no conflict. Now let me update the Azure scripts to create the import queue. Let me look at the azure scripts:

Now add `AZURE_SERVICEBUS_IMPORT_QUEUE_NAME` to the docker run command in Azure runlocale:

Now update the Azure VM script:

Now find the docker run / VM docker command in the VM script and add the import queue env var:

Now update the Azure ACI script:

Now add `AZURE_SERVICEBUS_IMPORT_QUEUE_NAME` to the ACI docker/container creation:

Now update PlatformAws.md to add import SQS queue documentation:

Now update the EC2 section of PlatformAws.md:

Now add CLI commands for reading import queue in EC2 and ECS sections of PlatformAws.md:

Now update the ECS SQS command section in PlatformAws.md:

Also update the ECS script step 5 description:

Now update PlatformAzure.md to add import queue documentation:

Now let's run a quick build to validate the Java changes:

All tests pass. Let me verify there are no build errors:

No errors. Let me do a final check of the key changes:

All 11 tasks completed. Here's a summary of what was done:

**AWS changes:**
- `AwsProperties.SqsConfig`: Added `importQueueUrl` field (separate from export `queueUrl`)
- `SqsAnnotazioneImportService`: Now uses `importQueueUrl` instead of the export queue URL
- `application-aws.yml`: Added `import-queue-url: ${SQS_IMPORT_QUEUE_URL:}`
- `script/aws-onprem/docker-compose.yml`: Added `init-sqs-import` service creating queue `annotazioni-import`, and added `SQS_IMPORT_QUEUE_URL` env var to the app
- `script/aws-ec2/start-all.sh`: Added creation of `gestioneannotazioni-annotazioni-import` queue and passes `SQS_IMPORT_QUEUE_URL` to Docker
- `script/aws-ecs/start-all.sh`: Same as EC2, plus added env var to ECS task definition

**Azure changes:**
- `AzureProperties.ServiceBusProperties`: Added `importQueueName` field
- Created `ServiceBusAnnotazioneImportService`: Full Azure import service reading from the ServiceBus import queue, similar to the AWS implementation
- `application-azure.yaml`: Added `import-queue-name: ${AZURE_SERVICEBUS_IMPORT_QUEUE_NAME:annotazioni-import}`
- Updated all 3 Azure scripts (runlocale, vm, aci) to create `annotazioni-import` queue and pass `AZURE_SERVICEBUS_IMPORT_QUEUE_NAME` env var

**Documentation:** Updated `PlatformAws.md` and `PlatformAzure.md` with info about the new import queues.





Status: COMPLETED
