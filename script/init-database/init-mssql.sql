-- Inizializzazione database SQL Server per adapter Azure
-- USE master;
-- GO
-- Creazione database se non esiste
-- IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'gestioneannotazioni')
-- BEGIN
--    CREATE DATABASE gestioneannotazioni;
--END
--GO
--USE gestioneannotazioni;
--GO

-- Elimina tabella se esiste (solo per sviluppo)
IF EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
BEGIN
    DROP TABLE [users];
END
GO

-- Creazione tabella utenti (allineata con UserSqlServerEntity.java)
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='users' AND xtype='U')
BEGIN  
    CREATE TABLE [users] (
        id BIGINT PRIMARY KEY IDENTITY(1,1),
        username NVARCHAR(50) UNIQUE NOT NULL,
        password NVARCHAR(255) NOT NULL,
        email NVARCHAR(100) UNIQUE NOT NULL,
        role NVARCHAR(20) NOT NULL DEFAULT 'USER',
        first_name NVARCHAR(100),
        last_name NVARCHAR(100),
        avatar_url NVARCHAR(255),
        enabled BIT NOT NULL DEFAULT 1,
        email_verified BIT NOT NULL DEFAULT 0,
        account_type NVARCHAR(20),
        external_id NVARCHAR(255),
        created_at DATETIME2 DEFAULT GETDATE(),
        updated_at DATETIME2 DEFAULT GETDATE(),
        last_login DATETIME2 NULL
    );
END
GO

IF EXISTS (SELECT * FROM sysobjects WHERE name='annotazione_metadata' AND xtype='U')
BEGIN
    DROP TABLE [annotazione_metadata];
END
GO

-- Creazione tabella metadata annotazioni
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='annotazione_metadata' AND xtype='U')
BEGIN
    CREATE TABLE [annotazione_metadata] (
        id NVARCHAR(255) PRIMARY KEY,
        categoria NVARCHAR(50),
        dataInserimento DATETIME2 DEFAULT GETDATE(),
        dataUltimaModifica DATETIME2 DEFAULT GETDATE(),
        descrizione NVARCHAR(MAX),
        priorita NVARCHAR(20),
        pubblica BIT DEFAULT 0,
        stato NVARCHAR(50),
        tags NVARCHAR(MAX),
        utenteCreazione NVARCHAR(50),
        utenteUltimaModifica NVARCHAR(50),
        versioneNota NVARCHAR(50)
    );
END
GO

-- Creazione tabella storico stati
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='storico_stati' AND xtype='U')
BEGIN
    CREATE TABLE storico_stati (
        id UNIQUEIDENTIFIER PRIMARY KEY DEFAULT NEWID(),
        annotazione_id NVARCHAR(255) NOT NULL,
        stato_precedente NVARCHAR(50),
        stato_nuovo NVARCHAR(50) NOT NULL,
        utente NVARCHAR(50) NOT NULL,
        timestamp_modifica DATETIME2 DEFAULT GETDATE(),
        note NVARCHAR(MAX)
    );
END
GO

-- Inserimento utenti di test --password123
INSERT INTO [users] (username, password, email, role, first_name, last_name, enabled, email_verified, account_type) 
VALUES 
('admin', '$2b$12$TUQyZEAT4R.5nsyGJYm6Z.HQMiD.Z8dRs8nc6k1fHZf31sKt4lUOa', 'admin@example.com', 'ADMIN', 'Admin', 'User', 1, 1, 'LOCAL'),
('moderatore', '$2b$12$hLQdX/4p6la8/CnpGtvB0uBVOzUOVZkIRFxV4BySpfn6Sn9f1wKFm', 'mod@example.com', 'MODERATORE', 'Moderatore', 'User', 1, 1, 'LOCAL'),
('alnao', '$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi', 'user@example.com', 'USER', 'Normal', 'User', 1, 1, 'LOCAL');
GO

PRINT 'Database SQL Server inizializzato con successo!';
PRINT 'Utenti creati: admin (admin), moderatore (cattivo), alnao (bellissimo)';
GO