-- Pessoa 3: tabela de empréstimos (faixa V30+)
-- Status: ATIVO, DEVOLVIDO ou ATRASADO. ATIVO e ATRASADO ocupam uma licença do material.
CREATE TABLE emprestimos (
    id                      BIGINT      NOT NULL AUTO_INCREMENT,
    usuario_id              BIGINT      NOT NULL,
    material_id             BIGINT      NOT NULL,
    data_retirada           DATETIME(6) NOT NULL,
    data_prevista_devolucao DATE        NOT NULL,
    data_devolucao          DATETIME(6) NULL,
    status                  VARCHAR(20) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_emprestimos_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuarios (id),
    CONSTRAINT fk_emprestimos_material FOREIGN KEY (material_id) REFERENCES materiais (id)
);

-- Contagem de licenças em uso por material (RN01) e de empréstimos ativos por usuário (RN04).
CREATE INDEX idx_emprestimos_material_status ON emprestimos (material_id, status);
CREATE INDEX idx_emprestimos_usuario_status ON emprestimos (usuario_id, status);
