# Adapter Kafka

Questo modulo implementa l'invio delle annotazioni tramite Apache Kafka per il profilo `onprem`.

## Funzionalità

- **Invio asincrono**: Le annotazioni in stato `DAINVIARE` vengono inviate a un topic Kafka
- **Configurazione flessibile**: Broker URL e nome del topic configurabili tramite properties
- **Gestione errori**: In caso di errore, lo stato dell'annotazione viene impostato su `ERRORE`
- **Schedulazione**: Esecuzione automatica tramite job schedulato configurabile

## Configurazione

Nel file `application-onprem.yml`:

```yaml
annotazione:
  invio:
    enabled: true
    cron-expression: "0 */10 * * * *"  # ogni 10 minuti
    kafka:
      broker-url: "localhost:9092"
      topic-name: "annotazioni-export"
```

## Variabili d'ambiente

- `KAFKA_BROKER_URL`: URL del broker Kafka (default: localhost:9092)
- `KAFKA_TOPIC_NAME`: Nome del topic Kafka (default: annotazioni-export)

## Profilo di attivazione

Questo adapter viene attivato automaticamente quando:
- Il profilo Spring attivo è `onprem`
- La configurazione `annotazione.invio.enabled=true`

## Dipendenze principali

- Spring Kafka
- Jackson per la serializzazione JSON
- Core module per le interfaces e domini

## Formato messaggio

I messaggi inviati a Kafka contengono l'annotazione completa (annotazione + metadata) serializzata in JSON:

```json
{
  "annotazione": {
    "id": "uuid",
    "versioneNota": "1.0",
    "valoreNota": "contenuto annotazione"
  },
  "metadata": {
    "id": "uuid",
    "stato": "INVIATA",
    "dataInserimento": "2025-09-25T10:00:00",
    "descrizione": "descrizione",
    // ... altri campi metadata
  }
}
```

## Test

Eseguire i test con:
```bash
mvn test
```
