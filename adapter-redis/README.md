# Adapter Redis - Sistema di Lock Distribuiti

Questo modulo fornisce un sistema di lock distribuiti per prevenire modifiche concorrenti alle annotazioni.

## Funzionalità

- **Lock distribuiti con Redis**: Utilizza Redis e Redisson per lock distribuiti su annotazioni
- **Fallback in-memory**: Implementazione locale per il profilo sqlite
- **Timeout automatico**: I lock vengono rilasciati automaticamente dopo il timeout
- **Multi-profilo**: Supporta profili kube, aws, azure (Redis) e sqlite (in-memory)

## Utilizzo

Il sistema è completamente trasparente per l'utente finale. Quando un utente tenta di modificare un'annotazione:

1. Il sistema acquisisce un lock sulla risorsa
2. Se il lock è già posseduto da un altro utente, viene restituito un errore 409 CONFLICT
3. Se il lock viene acquisito, l'operazione procede
4. Il lock viene sempre rilasciato alla fine dell'operazione

## Configurazione

### Profilo Kube
```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
```

### Profilo AWS
```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    ssl: ${REDIS_SSL:false}
```

### Profilo Azure
```yaml
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6380}
    password: ${REDIS_PASSWORD:}
    ssl: ${REDIS_SSL:true}
```

### Profilo SQLite
Non richiede configurazione Redis, usa implementazione in-memory.

## API

### Esempio di errore quando annotazione è bloccata

```http
PUT /api/annotazioni/{id}
Content-Type: application/json

{
  "valoreNota": "Nuovo valore",
  "descrizione": "Nuova descrizione",
  "utente": "user2"
}
```

Risposta quando l'annotazione è già in modifica:
```http
HTTP/1.1 409 Conflict
Content-Type: application/json

{
  "message": "Annotazione xxx-xxx-xxx è in modifica da: user1",
  "errorCode": "ANNOTATION_LOCKED"
}
```

## Deployment

### Docker Compose
Il servizio Redis è già incluso nel `docker-compose.yml`:
```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
```

### Kubernetes/Minikube
Applica il manifest:
```bash
kubectl apply -f script/minikube/redis-deployment.yaml
```

## Note Tecniche

- **Timeout lock**: 30 secondi di default
- **Timeout auto-release (in-memory)**: 5 minuti
- **Persistenza Redis**: Usa AOF (Append Only File) per persistenza
- **Scalabilità**: Supporta deployment multi-istanza con Redis come coordinatore

## Sicurezza

- I lock sono associati all'utente che li acquisisce
- Solo il proprietario del lock può rilasciarlo
- Lock scaduti vengono automaticamente rilasciati
