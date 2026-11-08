package com.np2.bibliotecavirtual.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Usuário da biblioteca: aluno, professor ou bibliotecário (perfil), que entra com matrícula e senha.
 * Usuário das aulas, reaproveitado (não conta como um dos 5 CRUDs).
 */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    /** Guardado em minúsculas, para a checagem de e-mail único valer. */
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false, unique = true, length = 30)
    private String matricula;

    /** Perfil de acesso (V42). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerfilUsuario perfil = PerfilUsuario.ALUNO;

    /** Senha criptografada com BCrypt (V42). Nunca sai da API. */
    @Column(name = "senha_hash", length = 100)
    private String senhaHash;
}
