package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Referência curta (ID e nome) a outro cadastro, usada dentro de outras respostas.
 * Ex.: a categoria, a editora e os autores dentro de MaterialResponseDTO.
 */
@Schema(description = "Referência resumida a outro cadastro")
public record ResumoDTO(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Machado de Assis")
        String nome
) {
}
