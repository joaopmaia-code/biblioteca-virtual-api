-- Pessoa 2: avaliações de materiais (faixa V40+, funcionalidades novas)
-- Fica depois da V31 porque depende de usuarios (V30); numa V2x o Flyway rodaria antes da tabela existir.
-- Cada aluno avalia um material uma vez só (nota de 1 a 5 e comentário opcional).
-- ON DELETE CASCADE: ao excluir o material ou o usuário, as avaliações dele saem junto.
CREATE TABLE avaliacoes (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    usuario_id     BIGINT        NOT NULL,
    material_id    BIGINT        NOT NULL,
    nota           INT           NOT NULL,
    comentario     VARCHAR(1000) NULL,
    data_avaliacao DATETIME(6)   NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_avaliacoes_usuario_material UNIQUE (usuario_id, material_id),
    CONSTRAINT ck_avaliacoes_nota CHECK (nota BETWEEN 1 AND 5),
    CONSTRAINT fk_avaliacoes_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios (id)  ON DELETE CASCADE,
    CONSTRAINT fk_avaliacoes_material FOREIGN KEY (material_id) REFERENCES materiais (id) ON DELETE CASCADE
);

-- Média e total de avaliações por material (calculados a cada consulta de material).
CREATE INDEX idx_avaliacoes_material ON avaliacoes (material_id);
