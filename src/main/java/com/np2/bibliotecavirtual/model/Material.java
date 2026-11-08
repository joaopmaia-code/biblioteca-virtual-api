package com.np2.bibliotecavirtual.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Item do acervo digital (livro, apostila, artigo ou vídeo).
 * <p>
 * Pertence a uma categoria (obrigatória), pode ter uma editora (opcional) e tem um ou
 * mais autores, ligados pela tabela material_autor (V22).
 * Licenças em uso e a média das avaliações são calculadas pelo banco (@Formula).
 */
@Entity
@Table(name = "materiais")
@Getter
@Setter
@NoArgsConstructor
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMaterial tipo;

    @Column(name = "ano_publicacao", nullable = false)
    private Integer anoPublicacao;

    @Column(columnDefinition = "TEXT")
    private String sinopse;

    @Column(name = "url_arquivo", nullable = false, length = 500)
    private String urlArquivo;

    /** Imagem de capa (opcional, V43). */
    @Column(name = "url_capa", length = 500)
    private String urlCapa;

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    @Column(name = "quantidade_licencas", nullable = false)
    private Integer quantidadeLicencas;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "editora_id")
    private Editora editora;

    /**
     * Material é o lado dono da relação: salvar o material grava as linhas de material_autor.
     * BatchSize: na busca paginada, carrega os autores de vários materiais numa consulta só.
     */
    @ManyToMany
    @BatchSize(size = 50)
    @JoinTable(name = "material_autor",
            joinColumns = @JoinColumn(name = "material_id"),
            inverseJoinColumns = @JoinColumn(name = "autor_id"))
    private Set<Autor> autores = new HashSet<>();

    // Campos calculados pelo banco a cada consulta (@Formula): não são colunas, não entram no INSERT/UPDATE
    // e ficam null num material recém-cadastrado (o MaterialResponseDTO trata o null).

    /** Licenças ocupadas agora: empréstimos ATIVOS ou ATRASADOS deste material (mesma conta da RN01). */
    @Formula("(select count(*) from emprestimos e where e.material_id = id and e.status in ('ATIVO', 'ATRASADO'))")
    private Long licencasEmUso;

    /** Média das notas das avaliações (null se ninguém avaliou). */
    @Formula("(select avg(a.nota) from avaliacoes a where a.material_id = id)")
    private Double mediaAvaliacoes;

    @Formula("(select count(*) from avaliacoes a where a.material_id = id)")
    private Long totalAvaliacoes;
}
