CREATE DATABASE IF NOT EXISTS informazioni;
USE informazioni;

CREATE TABLE IF NOT EXISTS persone (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(255),
    cognome VARCHAR(255),
    eta INT
);