package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.model.Avaliacao;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    /** Uma avaliação por aluno e material: ele já avaliou este material? */
    boolean existsByUsuarioIdAndMaterialId(Long usuarioId, Long materialId);

    /**
     * Listagem com filtros opcionais (null não filtra), mais recentes primeiro.
     * Já traz aluno e material na mesma consulta.
     */
    @EntityGraph(attributePaths = {"usuario", "material"})
    @Query("""
            select a from Avaliacao a
            where (:materialId is null or a.material.id = :materialId)
              and (:usuarioId is null or a.usuario.id = :usuarioId)
            order by a.dataAvaliacao desc, a.id desc
            """)
    List<Avaliacao> buscar(@Param("materialId") Long materialId, @Param("usuarioId") Long usuarioId);
}
