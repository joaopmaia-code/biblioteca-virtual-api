package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos para editar uma avaliação: só a nota e o comentário mudam.
 * Aluno e material não mudam; para isso, exclua e faça outra avaliação.
 */
@Schema(description = "Dados para editar uma avaliação")
public record AvaliacaoAtualizacaoDTO(

        @Schema(description = "Nota de 1 a 5", example = "4")
        @NotNull(message = "A nota é obrigatória")
        @Min(value = 1, message = "A nota deve ser de 1 a 5")
        @Max(value = 5, message = "A nota deve ser de 1 a 5")
        Integer nota,

        @Schema(description = "Comentário (opcional; vazio ou ausente apaga o comentário)",
                example = "Reli e gostei menos do final.")
        @Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
        String comentario
) {
}
