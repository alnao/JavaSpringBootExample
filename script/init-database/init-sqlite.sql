
DELETE FROM users where username IN ('alnao', 'admin', 'moderatore');
-- Inserimento utenti di esempio per Sqlite
INSERT INTO users (
    id, username, email, password, first_name, last_name, userRole, account_type, 
    enabled, email_verified
) VALUES 
(
    '1a2b3c4d-5e6f-7g8h-9i0j-1k2l3m4n5o6p', 'alnao', 'alnao@example.com', 
    '$2b$12$hFoVfPak5m77PJD0cIIe8u1Yo5out7B.h8PWvwfbaloys/ndX9Zpi', 
    'Alberto', 'Nao', 'USER', 'LOCAL', true, true
), -- bellissimo
(
    '2b3c4d5e-6f7g-8h9i-0j1k-2l3m4n5o6p7q', 'admin', 'admin@example.com', 
    '$2b$12$TUQyZEAT4R.5nsyGJYm6Z.HQMiD.Z8dRs8nc6k1fHZf31sKt4lUOa', 
    'Admin', 'User', 'ADMIN', 'LOCAL', true, true
), -- admin 
(
    '3c4d5e6f-7g8h-9i0j-1k2l-3m4n5o6p7q8r', 'moderatore', 'moderatore@example.com', 
    '$2b$12$hLQdX/4p6la8/CnpGtvB0uBVOzUOVZkIRFxV4BySpfn6Sn9f1wKFm', 
    'Moderatore', 'User', 'MODERATOR', 'LOCAL', true, true
); -- cattivo