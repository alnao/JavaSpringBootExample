-- Script per creare le tabelle di gestioneannotazioni in PostgreSQL
-- Script per creare le tabelle di gestioneannotazioni in PostgreSQL

-- Tabella degli utenti
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(255) PRIMARY KEY,
    username VARCHAR(100) UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    avatar_url TEXT,
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    account_type VARCHAR(50) NOT NULL,
    external_id VARCHAR(255),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    last_login TIMESTAMP
);

-- Tabella dei provider OAuth2 collegati agli utenti
CREATE TABLE IF NOT EXISTS user_providers (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    provider_username VARCHAR(255),
    access_token_hash VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (user_id, provider),
    UNIQUE (provider, external_id)
);

-- Tabella dei refresh token
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Indici per migliorare le performance
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_external_id ON users(external_id);
CREATE INDEX IF NOT EXISTS idx_users_account_type ON users(account_type);
CREATE INDEX IF NOT EXISTS idx_user_providers_user_id ON user_providers(user_id);
CREATE INDEX IF NOT EXISTS idx_user_providers_provider ON user_providers(provider);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_tokens_expiry ON refresh_tokens(expiry_date);


--- Sistema di annotazioni metadata
-- Schema per PostgreSQL (OnPrem)
CREATE TABLE IF NOT EXISTS annotazioni_metadata (
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
CREATE INDEX IF NOT EXISTS idx_annotazioni_metadata_categoria ON annotazioni_metadata(categoria);
CREATE INDEX IF NOT EXISTS idx_annotazioni_metadata_pubblica ON annotazioni_metadata(pubblica);
CREATE INDEX IF NOT EXISTS idx_annotazioni_metadata_priorita ON annotazioni_metadata(priorita);
CREATE INDEX IF NOT EXISTS idx_annotazioni_metadata_utente_creazione ON annotazioni_metadata(utente_creazione);
CREATE INDEX IF NOT EXISTS idx_annotazioni_metadata_utente_ultima_modifica ON annotazioni_metadata(utente_ultima_modifica);
CREATE INDEX IF NOT EXISTS idx_annotazioni_metadata_data_inserimento ON annotazioni_metadata(data_inserimento);

-- Dati di esempio
INSERT INTO annotazioni_metadata (
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

-- Inserimento utenti di esempio
INSERT INTO users (
    id, username, email, password, first_name, last_name, role, account_type, 
    enabled, email_verified, created_at
) VALUES 
(
    '1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p', 'alnao', 'alnao@example.com', 
    '$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi', 
    'Alberto', 'Nao', 'USER', 'LOCAL', true, true, NOW()
), -- bellissimo
(
    '2b3c4d5e-6f7g-8h9i-0j1k-2l3m4n5o6p7q', 'admin', 'admin@example.com', 
    '$2b$12$TUQyZEAT4R.5nsyGJYm6Z.HQMiD.Z8dRs8nc6k1fHZf31sKt4lUOa', 
    'Admin', 'User', 'ADMIN', 'LOCAL', true, true, NOW()
), -- admin
(
    '3c4d5e6f-7g8h-9i0j-1k2l-3m4n5o6p7q8r', 'moderatore', 'moderatore@example.com', 
    '$2b$12$hLQdX/4p6la8/CnpGtvB0uBVOzUOVZkIRFxV4BySpfn6Sn9f1wKFm', 
    'Moderatore', 'User', 'MODERATOR', 'LOCAL', true, true, NOW() -- cattivo
) ON CONFLICT (username) DO NOTHING;


--python3 -c "
--import bcrypt
--passwords = {    'bellissimo': 'bellissimo',    'admin': 'admin',     'cattivo': 'cattivo'}
--for name, pwd in passwords.items():
--    hashed = bcrypt.hashpw(pwd.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')
--    print(f'{name}: {hashed}')
--"