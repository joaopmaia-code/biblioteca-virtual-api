-- Pessoa 2: tabela de materiais (faixa V20+)
-- editora_id (FK opcional para editoras) e a tabela material_autor (N:N com autores)
-- entram numa V22, quando as tabelas editoras e autores da Pessoa 1 (faixa V10+) existirem.
CREATE TABLE materiais (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    titulo              VARCHAR(200) NOT NULL,
    tipo                VARCHAR(20)  NOT NULL,
    ano_publicacao      INT          NOT NULL,
    sinopse             TEXT         NULL,
    url_arquivo         VARCHAR(500) NOT NULL,
    numero_paginas      INT          NULL,
    quantidade_licencas INT          NOT NULL,
    categoria_id        BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_materiais_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id)
);
