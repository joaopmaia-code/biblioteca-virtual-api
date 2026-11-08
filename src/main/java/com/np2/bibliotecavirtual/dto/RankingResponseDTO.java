package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.NivelLeitor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Uma linha do ranking de leitores.
 */
@Schema(description = "Posição no ranking de leitores")
public record RankingResponseDTO(

        @Schema(description = "Posição (empatados ficam na mesma posição)", example = "1")
        int posicao,

        @Schema(description = "Aluno, com o nome abreviado (primeiro nome e inicial do último)")
        ResumoDTO usuario,

        @Schema(example = "68")
        int pontos,

        @Schema(example = "LEITOR")
        NivelLeitor nivel,

        @Schema(description = "Quantas conquistas o aluno já desbloqueou", example = "2")
        int conquistasDesbloqueadas
) {
}
