package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.model.Editora;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EditoraRepository extends JpaRepository<Editora, Long> {

    /** Usado no cadastro: já existe editora com esse CNPJ? */
    boolean existsByCnpj(String cnpj);

    /** Usado na atualização: outra editora (id diferente) já usa esse CNPJ? */
    boolean existsByCnpjAndIdNot(String cnpj, Long id);
}
