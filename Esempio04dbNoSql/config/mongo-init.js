// scripts/mongo-init.js
// Script per inizializzare MongoDB con dati di esempio
db = db.getSiblingDB('microservice_db');

// Crea collezione users se non exists
db.createCollection('users');

// Inserisci alcuni dati di esempio
db.users.insertMany([
    {
        name: "Mario Rossi",
        email: "mario.rossi@example.com",
        createdAt: new Date(),
        updatedAt: new Date()
    },
    {
        name: "Giulia Verdi",
        email: "giulia.verdi@example.com",
        createdAt: new Date(),
        updatedAt: new Date()
    }
]);

print('MongoDB initialized with sample data');