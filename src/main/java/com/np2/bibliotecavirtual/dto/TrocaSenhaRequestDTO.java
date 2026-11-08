package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Troca da própria senha")
public record TrocaSenhaRequestDTO(

        @Schema(example = "senha123")
        @NotBlank(message = "A senha atual é obrigatória")
        String senhaAtual,

        @Schema(example = "novaSenha456")
        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 6, max = 72, message = "A nova senha deve ter de 6 a 72 caracteres")
        String novaSenha
) {
}
