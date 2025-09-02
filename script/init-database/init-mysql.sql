-- Schema per MySQL (AWS)
CREATE DATABASE IF NOT EXISTS annotazioni_aws;
USE annotazioni_aws;

CREATE TABLE IF NOT EXISTS annotazione_metadata (
    id VARCHAR(255) PRIMARY KEY,
    versione_nota VARCHAR(50),
    utente_creazione VARCHAR(100),
    data_inserimento DATETIME,
    data_ultima_modifica DATETIME,
    utente_ultima_modifica VARCHAR(100),
    descrizione TEXT,
    categoria VARCHAR(100),
    tags TEXT,
    pubblica BOOLEAN DEFAULT FALSE,
    priorita INTEGER DEFAULT 1
);

-- Indici per migliorare le performance
CREATE INDEX idx_annotazione_metadata_categoria ON annotazione_metadata(categoria);
CREATE INDEX idx_annotazione_metadata_pubblica ON annotazione_metadata(pubblica);
CREATE INDEX idx_annotazione_metadata_priorita ON annotazione_metadata(priorita);
CREATE INDEX idx_annotazione_metadata_utente_creazione ON annotazione_metadata(utente_creazione);
CREATE INDEX idx_annotazione_metadata_data_inserimento ON annotazione_metadata(data_inserimento);

-- Dati di esempio
--INSERT IGNORE INTO annotazione_metadata (
--    id, versione_nota, utente_creazione, data_inserimento, data_ultima_modifica,
--    utente_ultima_modifica, descrizione, categoria, tags, pubblica, priorita
--) VALUES 
--(
--    '3a2b7c91-9e5f-4f0e-8b69-d0e989f0b2f6', 'v1.0', 'admin', NOW(), NOW(),
--    'admin', 'Annotazione AWS di esempio per test', 'Cloud', 'aws,esempio,test', true, 3
--),
--(
--    '6d1c2a40-1f90-4b8b-9f5e-4e014a6da2bb', 'v1.1', 'user1', NOW(), NOW(),
--    'user1', 'Seconda annotazione AWS', 'Sviluppo', 'aws,sviluppo', false, 2
--);