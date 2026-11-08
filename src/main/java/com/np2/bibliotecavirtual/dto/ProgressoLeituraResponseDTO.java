package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.ProgressoLeitura;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Progresso de leitura de um empréstimo: página atual, total de páginas e percentual lido.
 */
@Schema(description = "Progresso de leitura de um material emprestado")
public record ProgressoLeituraResponseDTO(

        @Schema(example = "1")
        Long emprestimoId,

        ResumoDTO usuario,

        @Schema(description = "Material (o nome é o título)")
        ResumoDTO material,

        @Schema(example = "ATIVO")
        StatusEmprestimo statusEmprestimo,

        @Schema(example = "2026-11-19")
        LocalDate dataPrevistaDevolucao,

        @Schema(description = "Página atual (0 enquanto o aluno não registrou nenhuma)", example = "120")
        Integer paginaAtual,

        @Schema(description = "Número de páginas do material (null quando o material não tem páginas, ex.: vídeo)",
                example = "256")
        Integer numeroPaginas,

        @Schema(description = "Percentual lido, com uma casa decimal (null quando o material não tem páginas)",
                example = "46.9")
        Double percentual,

        @Schema(description = "true quando o aluno chegou à última página", example = "false")
        boolean concluido,

        @Schema(description = "Quando o progresso foi registrado pela última vez (null se nunca)",
                example = "2026-11-08T20:15:00")
        LocalDateTime atualizadoEm
) {

    /** progresso pode ser null: o aluno ainda não registrou nenhuma página. */
    public static ProgressoLeituraResponseDTO de(Emprestimo emprestimo, ProgressoLeitura progresso) {
        Material material = emprestimo.getMaterial();
        Integer total = material.getNumeroPaginas();
        int pagina = progresso == null ? 0 : progresso.getPaginaAtual();

        return new ProgressoLeituraResponseDTO(
                emprestimo.getId(),
                new ResumoDTO(emprestimo.getUsuario().getId(), emprestimo.getUsuario().getNome()),
                new ResumoDTO(material.getId(), material.getTitulo()),
                emprestimo.getStatus(),
                emprestimo.getDataPrevistaDevolucao(),
                pagina,
                total,
                total == null || total == 0 ? null : Math.round(pagina * 1000.0 / total) / 10.0,
                total != null && pagina >= total,
                progresso == null ? null : progresso.getAtualizadoEm());
    }
}
