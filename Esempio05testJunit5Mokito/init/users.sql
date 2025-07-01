
CREATE DATABASE IF NOT EXISTS Applicazione;

USE Applicazione;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nome VARCHAR(100) NOT NULL,
  password VARCHAR(100) NOT NULL
);

-- Dati di test
INSERT INTO users (nome, password) VALUES ('admin', 'adminpass');
INSERT INTO users (nome, password) VALUES ('alnao', 'bellissimo');
