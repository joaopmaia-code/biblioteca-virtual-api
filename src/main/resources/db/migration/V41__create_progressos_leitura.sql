-- Pessoa 2: progresso de leitura de um material emprestado (faixa V40+, funcionalidades novas)
-- Depende de emprestimos (V31). Um progresso por empréstimo (UNIQUE), sem mexer na tabela emprestimos.
-- ON DELETE CASCADE: ao excluir o empréstimo, o progresso dele sai junto.
CREATE TABLE progressos_leitura (
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    emprestimo_id BIGINT      NOT NULL,
    pagina_atual  INT         NOT NULL,
    atualizado_em DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_progressos_leitura_emprestimo UNIQUE (emprestimo_id),
    CONSTRAINT fk_progressos_leitura_emprestimo FOREIGN KEY (emprestimo_id) REFERENCES emprestimos (id) ON DELETE CASCADE
);
