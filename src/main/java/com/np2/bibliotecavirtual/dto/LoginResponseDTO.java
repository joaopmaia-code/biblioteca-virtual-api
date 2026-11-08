package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token de acesso e dados de quem entrou")
public record LoginResponseDTO(

        @Schema(description = "Token JWT: envie em Authorization: Bearer <token>")
        String token,

        @Schema(example = "Bearer")
        String tipo,

        @Schema(description = "Validade do token, em segundos", example = "28800")
        long expiraEmSegundos,

        UsuarioResponseDTO usuario
) {
}
