-- Pessoa 2: imagem de capa do material (faixa V40+)
-- Opcional: sem capa, o front mostra um bloco colorido com o título.
ALTER TABLE materiais
    ADD COLUMN url_capa VARCHAR(500) NULL;
