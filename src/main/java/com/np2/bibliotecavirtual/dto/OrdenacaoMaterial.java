package com.np2.bibliotecavirtual.dto;

import org.springframework.data.domain.Sort;

/**
 * Ordens aceitas na busca no acervo. O desempate é sempre pelo título.
 */
public enum OrdenacaoMaterial {

    /** Título de A a Z. */
    TITULO(Sort.by("titulo")),

    /** Publicados mais recentemente primeiro. */
    ANO(Sort.by(Sort.Order.desc("anoPublicacao"), Sort.Order.asc("titulo"))),

    /**
     * Maior média primeiro; com a mesma média, o mais avaliado. Os sem avaliação (média null) vão
     * para o fim porque, no MySQL, NULL é o menor valor e fica por último em ordem decrescente.
     * (Sort.Order.nullsLast() não funciona aqui: o Spring Data não aplica nullsLast em Criteria.)
     */
    AVALIACAO(Sort.by(Sort.Order.desc("mediaAvaliacoes"), Sort.Order.desc("totalAvaliacoes"),
            Sort.Order.asc("titulo")));

    private final Sort sort;

    OrdenacaoMaterial(Sort sort) {
        this.sort = sort;
    }

    public Sort getSort() {
        return sort;
    }
}
