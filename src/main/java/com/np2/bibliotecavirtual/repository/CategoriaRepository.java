package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    /** Usado no cadastro: já existe categoria com esse nome? */
    boolean existsByNomeIgnoreCase(String nome);

    /** Usado na atualização: outra categoria (id diferente) já usa esse nome? */
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}
