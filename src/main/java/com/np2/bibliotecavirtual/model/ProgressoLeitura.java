package com.np2.bibliotecavirtual.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Página em que o aluno está num material emprestado (V41).
 * Fica numa tabela própria, ligada 1:1 ao empréstimo, para não alterar a entidade Emprestimo.
 */
@Entity
@Table(name = "progressos_leitura")
@Getter
@Setter
@NoArgsConstructor
public class ProgressoLeitura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "emprestimo_id", nullable = false, unique = true)
    private Emprestimo emprestimo;

    @Column(name = "pagina_atual", nullable = false)
    private Integer paginaAtual;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}
