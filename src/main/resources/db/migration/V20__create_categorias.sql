-- Pessoa 2: tabela de categorias (faixa V20+)
CREATE TABLE categorias (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    nome              VARCHAR(100) NOT NULL,
    descricao         VARCHAR(500) NULL,
    area_conhecimento VARCHAR(100) NOT NULL,
    serie_recomendada VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_categorias_nome UNIQUE (nome)
);
