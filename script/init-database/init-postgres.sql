-- Schema per PostgreSQL (OnPrem)
CREATE TABLE IF NOT EXISTS annotazione_metadata (
    id VARCHAR(255) PRIMARY KEY,
    versione_nota VARCHAR(50),
    utente_creazione VARCHAR(100),
    data_inserimento TIMESTAMP,
    data_ultima_modifica TIMESTAMP,
    utente_ultima_modifica VARCHAR(100),
    descrizione TEXT,
    categoria VARCHAR(100),
    tags TEXT,
    pubblica BOOLEAN DEFAULT FALSE,
    priorita INTEGER DEFAULT 1
);

-- Indici per migliorare le performance
CREATE INDEX IF NOT EXISTS idx_annotazione_metadata_categoria ON annotazione_metadata(categoria);
CREATE INDEX IF NOT EXISTS idx_annotazione_metadata_pubblica ON annotazione_metadata(pubblica);
CREATE INDEX IF NOT EXISTS idx_annotazione_metadata_priorita ON annotazione_metadata(priorita);
CREATE INDEX IF NOT EXISTS idx_annotazione_metadata_utente_creazione ON annotazione_metadata(utente_creazione);
CREATE INDEX IF NOT EXISTS idx_annotazione_metadata_data_inserimento ON annotazione_metadata(data_inserimento);

-- Dati di esempio
INSERT INTO annotazione_metadata (
    id, versione_nota, utente_creazione, data_inserimento, data_ultima_modifica,
    utente_ultima_modifica, descrizione, categoria, tags, pubblica, priorita
) VALUES 
(
    '3a2b7c91-9e5f-4f0e-8b69-d0e989f0b2f6', 'v1.0', 'admin', NOW(), NOW(),
    'admin', 'Annotazione di esempio per test', 'Documentazione', 'esempio,test,demo', true, 2
),
(
    '6d1c2a40-1f90-4b8b-9f5e-4e014a6da2bb', 'v1.1', 'user1', NOW(), NOW(),
    'user1', 'Seconda annotazione di esempio', 'Note', 'esempio,privato', false, 1
) ON CONFLICT (id) DO NOTHING;
