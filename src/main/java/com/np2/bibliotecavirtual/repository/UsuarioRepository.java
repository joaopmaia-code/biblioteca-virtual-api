package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Cadastro: já existe usuário com esse e-mail? */
    boolean existsByEmailIgnoreCase(String email);

    /** Atualização: outro usuário (id diferente) já usa esse e-mail? */
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByMatricula(String matricula);

    boolean existsByMatriculaAndIdNot(String matricula, Long id);
}
