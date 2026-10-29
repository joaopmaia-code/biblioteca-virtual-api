-- Pessoa 1: tabela de editoras (faixa V10+)
-- O CNPJ é guardado só com os 14 dígitos (sem pontos, barra e hífen), por isso CHAR(14).
CREATE TABLE editoras (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    nome   VARCHAR(150) NOT NULL,
    cnpj   CHAR(14)     NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    site   VARCHAR(255) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_editoras_cnpj UNIQUE (cnpj)
);
