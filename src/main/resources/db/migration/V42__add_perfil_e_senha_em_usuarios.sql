-- Pessoa 2: autenticação (faixa V40+, depende de usuarios da V30)
-- perfil: ALUNO, PROFESSOR ou BIBLIOTECARIO. Usuários que já existiam viram ALUNO.
-- senha_hash: senha criptografada com BCrypt (nunca a senha em texto). Fica NULL para os usuários antigos,
-- que só conseguem entrar depois que um bibliotecário definir uma senha para eles.
ALTER TABLE usuarios
    ADD COLUMN perfil     VARCHAR(20)  NOT NULL DEFAULT 'ALUNO',
    ADD COLUMN senha_hash VARCHAR(100) NULL;
