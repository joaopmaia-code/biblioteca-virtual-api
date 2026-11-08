package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.Avaliacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Dados de uma avaliação devolvidos pela API, com aluno e material resumidos (ID e nome).
 */
@Schema(description = "Avaliação de um material")
public record AvaliacaoResponseDTO(

        @Schema(example = "1")
        Long id,

        ResumoDTO usuario,

        @Schema(description = "Material (o nome é o título)")
        ResumoDTO material,

        @Schema(example = "5")
        Integer nota,

        @Schema(description = "Comentário (null quando não há)", example = "Ótimo para estudar para o ENEM.")
        String comentario,

        @Schema(description = "Data da avaliação ou da última edição", example = "2026-11-08T14:30:00")
        LocalDateTime dataAvaliacao
) {

    public static AvaliacaoResponseDTO de(Avaliacao avaliacao) {
        return new AvaliacaoResponseDTO(
                avaliacao.getId(),
                new ResumoDTO(avaliacao.getUsuario().getId(), avaliacao.getUsuario().getNome()),
                new ResumoDTO(avaliacao.getMaterial().getId(), avaliacao.getMaterial().getTitulo()),
                avaliacao.getNota(),
                avaliacao.getComentario(),
                avaliacao.getDataAvaliacao());
    }
}
