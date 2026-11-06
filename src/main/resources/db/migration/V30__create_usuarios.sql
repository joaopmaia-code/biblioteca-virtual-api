-- Pessoa 3: tabela de usuários (faixa V30+)
-- Usuário das aulas, reaproveitado para empréstimos e reservas (não conta como CRUD da NP2).
CREATE TABLE usuarios (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    nome      VARCHAR(150) NOT NULL,
    email     VARCHAR(150) NOT NULL,
    matricula VARCHAR(30)  NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuarios_email UNIQUE (email),
    CONSTRAINT uk_usuarios_matricula UNIQUE (matricula)
);
