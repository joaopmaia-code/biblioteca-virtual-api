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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Item do acervo digital (livro, apostila, artigo ou vídeo).
 * <p>
 * Editora (N:1, opcional) e autores (N:N via material_autor) entram numa próxima
 * migration (V22), quando as tabelas editoras e autores da Pessoa 1 existirem.
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

    @Column(name = "numero_paginas")
    private Integer numeroPaginas;

    @Column(name = "quantidade_licencas", nullable = false)
    private Integer quantidadeLicencas;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
}
