package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Matrícula e senha para entrar")
public record LoginRequestDTO(

        @Schema(example = "2026A0142")
        @NotBlank(message = "A matrícula é obrigatória")
        String matricula,

        @Schema(example = "senha123")
        @NotBlank(message = "A senha é obrigatória")
        String senha
) {
}
