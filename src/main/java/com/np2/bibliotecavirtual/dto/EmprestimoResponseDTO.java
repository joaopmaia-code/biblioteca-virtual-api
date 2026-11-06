package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Dados de um empréstimo devolvidos pela API, com usuário e material resumidos (ID e nome).
 */
@Schema(description = "Empréstimo registrado")
public record EmprestimoResponseDTO(

        @Schema(example = "1")
        Long id,

        ResumoDTO usuario,

        @Schema(description = "Material (o nome é o título)")
        ResumoDTO material,

        @Schema(example = "2026-11-05T10:30:00")
        LocalDateTime dataRetirada,

        @Schema(example = "2026-11-19")
        LocalDate dataPrevistaDevolucao,

        @Schema(description = "Preenchida ao devolver (null enquanto não devolvido)")
        LocalDateTime dataDevolucao,

        @Schema(example = "ATIVO")
        StatusEmprestimo status
) {

    public static EmprestimoResponseDTO de(Emprestimo emprestimo) {
        return new EmprestimoResponseDTO(
                emprestimo.getId(),
                new ResumoDTO(emprestimo.getUsuario().getId(), emprestimo.getUsuario().getNome()),
                new ResumoDTO(emprestimo.getMaterial().getId(), emprestimo.getMaterial().getTitulo()),
                emprestimo.getDataRetirada(),
                emprestimo.getDataPrevistaDevolucao(),
                emprestimo.getDataDevolucao(),
                emprestimo.getStatus());
    }
}
