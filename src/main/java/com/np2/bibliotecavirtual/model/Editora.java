package com.np2.bibliotecavirtual.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Editora que publica materiais do acervo (um material tem no máximo uma editora).
 */
@Entity
@Table(name = "editoras")
@Getter
@Setter
@NoArgsConstructor
public class Editora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    /** Só os 14 dígitos, sem máscara. */
    @Column(nullable = false, unique = true, length = 14, columnDefinition = "CHAR(14)")
    private String cnpj;

    @Column(nullable = false, length = 100)
    private String cidade;

    @Column(length = 255)
    private String site;
}
