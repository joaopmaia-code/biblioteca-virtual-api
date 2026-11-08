package com.np2.bibliotecavirtual.model;

/**
 * Perfil de acesso do usuário. Gravado no banco como texto e enviado no token JWT.
 * <ul>
 *   <li>ALUNO: pega materiais emprestados, registra a leitura, avalia e vê só os próprios dados.</li>
 *   <li>PROFESSOR: o mesmo que o aluno, mais cadastrar o acervo e acompanhar os alunos (só leitura).</li>
 *   <li>BIBLIOTECARIO: tudo, inclusive usuários e empréstimos de qualquer aluno.</li>
 * </ul>
 */
public enum PerfilUsuario {
    ALUNO,
    PROFESSOR,
    BIBLIOTECARIO
}
