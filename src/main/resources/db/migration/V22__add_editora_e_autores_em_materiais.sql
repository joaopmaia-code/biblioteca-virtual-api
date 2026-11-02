-- Pessoa 2: liga materiais a editoras (N:1 opcional) e a autores (N:N) (faixa V20+)
-- Depende das tabelas editoras (V11) e autores (V10) da Pessoa 1.
ALTER TABLE materiais
    ADD COLUMN editora_id BIGINT NULL,
    ADD CONSTRAINT fk_materiais_editora FOREIGN KEY (editora_id) REFERENCES editoras (id);

-- Tabela de ligação N:N: a chave primária composta impede o mesmo autor duas vezes no mesmo material.
CREATE TABLE material_autor (
    material_id BIGINT NOT NULL,
    autor_id    BIGINT NOT NULL,
    PRIMARY KEY (material_id, autor_id),
    CONSTRAINT fk_material_autor_material FOREIGN KEY (material_id) REFERENCES materiais (id),
    CONSTRAINT fk_material_autor_autor FOREIGN KEY (autor_id) REFERENCES autores (id)
);
