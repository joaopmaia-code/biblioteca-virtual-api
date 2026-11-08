package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Uma linha do extrato de pontos: de onde vieram os pontos do aluno.
 */
@Schema(description = "Linha do extrato de pontos")
public record PontuacaoDTO(

        @Schema(example = "Leituras concluídas")
        String motivo,

        @Schema(example = "3")
        int quantidade,

        @Schema(description = "Pontos por ocorrência", example = "10")
        int pontosCada,

        @Schema(example = "30")
        int total
) {

    public static PontuacaoDTO de(String motivo, int quantidade, int pontosCada) {
        return new PontuacaoDTO(motivo, quantidade, pontosCada, quantidade * pontosCada);
    }
}
