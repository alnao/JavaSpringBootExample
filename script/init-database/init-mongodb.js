// Script di inizializzazione per MongoDB (OnPrem)
db = db.getSiblingDB('gestionepersonale_db');

// Crea collezione annotazioni se non esiste
db.createCollection('annotazioni');

// Crea indici per migliorare le performance
db.annotazioni.createIndex({ "versioneNota": 1 });
db.annotazioni.createIndex({ "valoreNota": "text" });

// Inserisci dati di esempio
db.annotazioni.insertMany([
    {
        "_id": "3a2b7c91-9e5f-4f0e-8b69-d0e989f0b2f6",
        "versioneNota": "v1.0",
        "valoreNota": "Questa è una nota di esempio per testare l'applicazione. Contiene del testo significativo per i test di ricerca e filtraggio."
    },
    {
        "_id": "6d1c2a40-1f90-4b8b-9f5e-4e014a6da2bb",
        "versioneNota": "v1.1",
        "valoreNota": "Seconda nota di esempio con contenuto diverso. Utile per verificare la funzionalità di ricerca nel contenuto delle note."
    }
]);

print("Database MongoDB inizializzato con successo!");
