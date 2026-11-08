package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.model.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long>, JpaSpecificationExecutor<Material> {

    /** Listagem: já traz categoria, editora e autores na mesma consulta (evita uma consulta por material). */
    @EntityGraph(attributePaths = {"categoria", "editora", "autores"})
    List<Material> findAllByOrderByTituloAsc();

    /**
     * Busca no acervo com filtros (MaterialFiltros) e paginação. Traz categoria e editora junto;
     * os autores não, porque buscar uma coleção junto obrigaria o Hibernate a paginar em memória
     * (eles vêm em lote, pelo @BatchSize do Material).
     */
    @Override
    @EntityGraph(attributePaths = {"categoria", "editora"})
    Page<Material> findAll(Specification<Material> filtros, Pageable paginacao);

    /** RN06: existe material nesta categoria? */
    boolean existsByCategoriaId(Long categoriaId);

    /** RN06: existe material desta editora? */
    boolean existsByEditoraId(Long editoraId);

    /** RN06: existe material deste autor? */
    boolean existsByAutoresId(Long autorId);
}
