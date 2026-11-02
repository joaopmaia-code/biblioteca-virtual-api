package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.model.Material;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    /** Listagem: já traz categoria, editora e autores na mesma consulta (evita uma consulta por material). */
    @EntityGraph(attributePaths = {"categoria", "editora", "autores"})
    List<Material> findAllByOrderByTituloAsc();

    /** RN06: existe material nesta categoria? */
    boolean existsByCategoriaId(Long categoriaId);

    /** RN06: existe material desta editora? */
    boolean existsByEditoraId(Long editoraId);

    /** RN06: existe material deste autor? */
    boolean existsByAutoresId(Long autorId);
}
