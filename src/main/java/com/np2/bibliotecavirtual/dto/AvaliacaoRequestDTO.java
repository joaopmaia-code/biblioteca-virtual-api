package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos para avaliar um material. A data é gerada pelo sistema.
 */
@Schema(description = "Dados para avaliar um material")
public record AvaliacaoRequestDTO(

        @Schema(description = "ID do aluno que avalia (precisa já ter pegado o material emprestado)", example = "1")
        @NotNull(message = "O usuário é obrigatório")
        Long usuarioId,

        @Schema(description = "ID do material avaliado", example = "1")
        @NotNull(message = "O material é obrigatório")
        Long materialId,

        @Schema(description = "Nota de 1 a 5", example = "5")
        @NotNull(message = "A nota é obrigatória")
        @Min(value = 1, message = "A nota deve ser de 1 a 5")
        @Max(value = 5, message = "A nota deve ser de 1 a 5")
        Integer nota,

        @Schema(description = "Comentário (opcional)", example = "Ótimo para estudar para o ENEM.")
        @Size(max = 1000, message = "O comentário deve ter no máximo 1000 caracteres")
        String comentario
) {
}
