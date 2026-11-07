package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Material sugerido a um usuário, com a pontuação e o motivo da sugestão.
 */
@Schema(description = "Material recomendado com base no histórico de empréstimos")
public record RecomendacaoResponseDTO(

        MaterialResponseDTO material,

        @Schema(description = "Quanto maior, mais parecido com o que o usuário já pegou")
        int pontuacao,

        @Schema(description = "Por que o material foi recomendado")
        String motivo
) {
}
