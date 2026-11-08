package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Dados recebidos para registrar em que página o aluno está.
 */
@Schema(description = "Página em que o aluno parou")
public record ProgressoLeituraRequestDTO(

        @Schema(description = "Página atual (de 1 até o número de páginas do material)", example = "120")
        @NotNull(message = "A página atual é obrigatória")
        @Min(value = 1, message = "A página atual deve ser 1 ou maior")
        Integer paginaAtual
) {
}
