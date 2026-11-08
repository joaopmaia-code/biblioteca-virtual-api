package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.model.PerfilUsuario;
import com.np2.bibliotecavirtual.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Cadastro: já existe usuário com esse e-mail? */
    boolean existsByEmailIgnoreCase(String email);

    /** Atualização: outro usuário (id diferente) já usa esse e-mail? */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);

    /** Login: busca o usuário pela matrícula. */
    Optional<Usuario> findByMatricula(String matricula);

    /** Bibliotecário inicial: já existe algum usuário com este perfil? */
    boolean existsByPerfil(PerfilUsuario perfil);
}
