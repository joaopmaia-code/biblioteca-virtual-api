-- Pessoa 1: tabela de autores (faixa V10+)
CREATE TABLE autores (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    nome            VARCHAR(150) NOT NULL,
    nacionalidade   VARCHAR(100) NULL,
    data_nascimento DATE         NULL,
    biografia       TEXT         NULL,
    PRIMARY KEY (id)
);
