package com.np2.bibliotecavirtual.repository;

import com.np2.bibliotecavirtual.dto.MaterialFiltroDTO;
import com.np2.bibliotecavirtual.model.Material;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

/**
 * Monta o WHERE da busca no acervo a partir dos filtros informados.
 * Filtro não informado (null) não restringe nada; os informados são combinados com E.
 */
public final class MaterialFiltros {

    private MaterialFiltros() {
    }

    public static Specification<Material> de(MaterialFiltroDTO filtro) {
        return (root, query, cb) -> {
            List<Predicate> condicoes = new ArrayList<>();

            if (filtro.titulo() != null && !filtro.titulo().isBlank()) {
                condicoes.add(cb.like(cb.lower(root.get("titulo")), "%" + filtro.titulo().trim().toLowerCase() + "%"));
            }
            if (filtro.categoriaId() != null) {
                condicoes.add(cb.equal(root.get("categoria").get("id"), filtro.categoriaId()));
            }
            if (filtro.editoraId() != null) {
                condicoes.add(cb.equal(root.get("editora").get("id"), filtro.editoraId()));
            }
            if (filtro.autorId() != null) {
                // Subconsulta em vez de join: com join, um material de 2 autores apareceria 2 vezes na página.
                Subquery<Long> doAutor = query.subquery(Long.class);
                Root<Material> material = doAutor.from(Material.class);
                Join<Object, Object> autor = material.join("autores");
                doAutor.select(material.get("id"))
                        .where(cb.equal(material.get("id"), root.get("id")), cb.equal(autor.get("id"), filtro.autorId()));
                condicoes.add(cb.exists(doAutor));
            }
            if (filtro.tipo() != null) {
                condicoes.add(cb.equal(root.get("tipo"), filtro.tipo()));
            }
            if (filtro.serie() != null) {
                condicoes.add(cb.equal(root.get("categoria").get("serieRecomendada"), filtro.serie()));
            }
            if (filtro.anoMin() != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.get("anoPublicacao"), filtro.anoMin()));
            }
            if (filtro.anoMax() != null) {
                condicoes.add(cb.lessThanOrEqualTo(root.get("anoPublicacao"), filtro.anoMax()));
            }
            if (filtro.notaMinima() != null) {
                condicoes.add(cb.greaterThanOrEqualTo(root.get("mediaAvaliacoes"), filtro.notaMinima().doubleValue()));
            }
            if (Boolean.TRUE.equals(filtro.disponivel())) {
                condicoes.add(cb.lessThan(root.get("licencasEmUso"), root.get("quantidadeLicencas")));
            } else if (Boolean.FALSE.equals(filtro.disponivel())) {
                condicoes.add(cb.greaterThanOrEqualTo(root.get("licencasEmUso"), root.get("quantidadeLicencas")));
            }

            return cb.and(condicoes.toArray(Predicate[]::new));
        };
    }
}
