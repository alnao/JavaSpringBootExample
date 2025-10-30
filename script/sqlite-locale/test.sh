#!/bin/bash
# Script per eseguire tutti i test dell'applicazione con il profilo "sqlite"
# set -e 

#cd ..
#echo "Costruzione immagine Docker..."
#./script/docker-build.sh 
#./script/push-image-docker-hub.sh 

echo "Posizione script: $(dirname "$0")"
cd "$(dirname "$0")/.."
echo "Directory di lavoro: $(pwd)"

echo "Preparazione database SQLite in /tmp/database.sqlite..."
# Rimuovi il database esistente e crea la directory con permessi corretti
rm -f /tmp/database.sqlite
rm -f /tmp/database.sqlite-journal
rm -f /tmp/database.sqlite-wal
rm -f /tmp/database.sqlite-shm

# Crea il file database vuoto con permessi di scrittura
touch /tmp/database.sqlite
chmod 666 /tmp/database.sqlite
# Assicurati che la directory /tmp sia scrivibile
chmod 777 /tmp 2>/dev/null || true

echo "Esecuzione test con profilo 'sqlite'..."
mvn clean package -DskipTests

# Avvia l'applicazione in background e salva il PID
java -jar application/target/application-*.jar \
    --spring.profiles.active=sqlite \
    --spring.datasource.url=jdbc:sqlite:/tmp/database.sqlite \
    --server.port=8082 \
    > /tmp/app-sqlite.log 2>&1 &
APP_PID=$!
echo "Applicazione avviata con PID: $APP_PID"

# Funzione per terminare l'applicazione in caso di errore
cleanup() {
    echo "Terminazione applicazione (PID: $APP_PID)..."
    kill $APP_PID 2>/dev/null || true
    wait $APP_PID 2>/dev/null || true
}
trap cleanup EXIT

echo "Attesa avvio applicazione (max 60 secondi)..."
for i in {1..30}; do
    if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
        echo "Applicazione pronta dopo $((i*2)) secondi"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "ERRORE: Timeout - L'applicazione non si è avviata in 60 secondi"
        cat /tmp/app-sqlite.log
        exit 1
    fi
    sleep 2
done

# Prendo il campo status e verifico se è UP
status=$(curl -s http://localhost:8082/actuator/health | jq -r .status)
if [ "$status" == "UP" ]; then
    echo "L'applicazione è in esecuzione correttamente."
else
    echo "L'applicazione non è in esecuzione."
    exit 1
fi

sqlite3 /tmp/database.sqlite < script/init-database/init-sqlite.sql

#exit 0

# Login e ottenimento token
echo "Esecuzione login..."
token_response=$(curl -s -X POST http://localhost:8082/api/auth/login -H "Content-Type: application/json" \
    -d '{"username":"admin","password":"admin"}')
token=$(echo $token_response | jq -r .token)
echo "Token ottenuto: $token"

if [ -z "$token" ] || [ "$token" == "null" ]; then
    echo "ERRORE: Login fallito. Risposta: $token_response"
    exit 1
else
    echo "Login eseguito correttamente."
fi

curl -s http://localhost:8082/api/annotazioni -H "Authorization: Bearer $token" | jq .  > /dev/null
if [ $? -eq 0 ]; then
    echo "Chiamata API /api/annotazioni eseguita correttamente."
else
    echo "Chiamata API /api/annotazioni fallita."
    exit 1
fi


echo "Creazione annotazione..."
RISPOSTA=$(curl -s -X POST http://localhost:8082/api/annotazioni \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"titolo":"Test Annotazione","descrizione":"Descrizione di test","valoreNota":"Valore di test","stato":"INSERITA","dataCreazione":"2024-06-01T12:00:00Z","utente":"admin"}' \
         | jq .)
echo "Risposta POST annotazione: $RISPOSTA"
# Verifica che la risposta contenga un ID (segno di successo)
id_creato=$(echo $RISPOSTA | jq -r .id 2>/dev/null)
if [ -n "$id_creato" ] && [ "$id_creato" != "null" ]; then
    echo "✅ Creazione annotazione eseguita correttamente. ID: $id_creato"
else
    echo "❌ Creazione annotazione fallita."
    echo "   Risposta completa: $RISPOSTA"
    exit 1
fi

# da risposta prendo l'id e provo a inviare l'annotazione
echo "Invio annotazione creata (ID: $id_creato)..."

RISPOSTA_INVIO1=$(curl -s -X PATCH http://localhost:8082/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"vecchioStato":"INSERITA","utente":"admin","nuovoStato":"CONFERMATA"}')
echo "Risposta conferma annotazione: $RISPOSTA_INVIO1"

RISPOSTA_INVIO2=$(curl -s -X PATCH http://localhost:8082/api/annotazioni/$id_creato/stato \
    -H "Authorization: Bearer $token" \
    -H "Content-Type: application/json" \
    -d '{"vecchioStato":"CONFERMATA","utente":"admin","nuovoStato":"DAINVIARE"}')
echo "Risposta invio annotazione: $RISPOSTA_INVIO2"

echo "Verifica invio annotazione nel database SQLite (max 10 minuti)..."
max_attempts=40  # 40 tentativi x 15 secondi = 600 secondi (10 minuti)
attempt=0
row_count=0

while [ $attempt -lt $max_attempts ]; do
    attempt=$((attempt + 1))
    row_count=$(sqlite3 /tmp/database.sqlite "SELECT COUNT(*) FROM annotazioni_inviate;" 2>/dev/null || echo "0")
    
    if [ "$row_count" -ge 1 ]; then
        echo "✅ Verifica database SQLite riuscita al tentativo $attempt/$max_attempts"
        echo "   Numero di righe in annotazioni_inviate: $row_count"
        break
    else
        elapsed=$((attempt * 15))
        echo "⏳ Tentativo $attempt/$max_attempts: nessuna annotazione trovata (attesa ${elapsed}s/120s)"
        
        if [ $attempt -lt $max_attempts ]; then
            sleep 15
        fi
    fi
done

if [ "$row_count" -lt 1 ]; then
    echo "❌ Verifica database SQLite fallita dopo 2 minuti"
    echo "   Righe trovate: $row_count"
    echo "   Tabelle presenti nel database:"
    sqlite3 /tmp/database.sqlite ".tables"
    echo "   Contenuto tabella annotazioni_inviate:"
    sqlite3 /tmp/database.sqlite "SELECT * FROM annotazioni_inviate LIMIT 5;" 2>/dev/null || echo "Errore nella query"
    exit 1
fi

# Terminazione applicazione (gestita da trap cleanup)
echo "Terminazione applicazione (PID: $APP_PID)..."
kill $APP_PID 2>/dev/null || true

echo "Test con profilo 'sqlite' superati!"

echo "Tutti i test sono stati eseguiti con successo!"
exit 0