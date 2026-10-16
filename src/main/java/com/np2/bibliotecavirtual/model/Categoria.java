package com.np2.bibliotecavirtual.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Categoria (assunto) que agrupa os materiais do acervo, ex.: Matemática, Literatura.
 */
@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(name = "area_conhecimento", nullable = false, length = 100)
    private String areaConhecimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "serie_recomendada", nullable = false, length = 20)
    private SerieRecomendada serieRecomendada;
}
