-- Schema per MySQL (AWS)
-- nota IMPORTANTE: in questo script NON mettere nessun carattere speciale e nemmeno lettere accentate
-- perche usato dal  script/aws-ecs/start-all.sh che si arrabbia tantissimo se presenti lettere non ascii standard
-- nota: questo file usato da alcuni script docker-compose
-- nota: questo file usato anche da script/aws-ecs/run-ecs-mysql-insert.sh

-- Creazione del database
CREATE DATABASE IF NOT EXISTS gestioneannotazioni;
USE gestioneannotazioni;

-- Script per creare le tabelle di autenticazione in MySQL

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
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
    last_used TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_provider (user_id, provider),
    UNIQUE KEY unique_provider_external (provider, external_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabella dei refresh token
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Indici per migliorare le performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_external_id ON users(external_id);
CREATE INDEX idx_users_account_type ON users(account_type);
CREATE INDEX idx_user_providers_user_id ON user_providers(user_id);
CREATE INDEX idx_user_providers_provider ON user_providers(provider);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_expiry ON refresh_tokens(expiry_date);


-- Sistema di annotazioni metadata
CREATE TABLE IF NOT EXISTS annotazioni_metadata (
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
    priorita INTEGER DEFAULT 1,
    stato VARCHAR(50) DEFAULT 'ERROR'
);

-- Indici per migliorare le performance
CREATE INDEX idx_annotazioni_metadata_categoria ON annotazioni_metadata(categoria);
CREATE INDEX idx_annotazioni_metadata_pubblica ON annotazioni_metadata(pubblica);
CREATE INDEX idx_annotazioni_metadata_priorita ON annotazioni_metadata(priorita);
CREATE INDEX idx_annotazioni_metadata_utente_creazione ON annotazioni_metadata(utente_creazione);
CREATE INDEX idx_annotazioni_metadata_utente_ultima_modifica ON annotazioni_metadata(utente_ultima_modifica);
CREATE INDEX idx_annotazioni_metadata_data_inserimento ON annotazioni_metadata(data_inserimento);

-- Tabelle di autenticazione
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
    updated_at TIMESTAMP NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabella dei provider OAuth2 collegati agli utenti
CREATE TABLE IF NOT EXISTS user_providers (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    provider_email VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_provider (user_id, provider),
    UNIQUE KEY unique_provider_external (provider, external_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tabella dei refresh token
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used TIMESTAMP NULL DEFAULT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Dati di esempio
 INSERT IGNORE INTO annotazioni_metadata (
    id, versione_nota, utente_creazione, data_inserimento, data_ultima_modifica,
    utente_ultima_modifica, descrizione, categoria, tags, pubblica, priorita, stato
 ) VALUES 
 (
    '3a2b7c91-9e5f-4f0e-8b69-d0e989f0b2f6', 'v1.0', 'admin', NOW(), NOW(),
    'admin', 'Annotazione AWS di esempio per test', 'Cloud', 'aws,esempio,test', true, 3, 'INSERITA'
 ),
 (
    '6d1c2a40-1f90-4b8b-9f5e-4e014a6da2bb', 'v1.1', 'user1', NOW(), NOW(),
    'user1', 'Seconda annotazione AWS', 'Sviluppo', 'aws,sviluppo', false, 2, 'INSERITA'
 );

-- Inserimento utenti di esempio per MySQL/AWS
INSERT IGNORE INTO users (
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
    'Moderatore', 'User', 'MODERATOR', 'LOCAL', true, true, NOW()
); -- cattivo