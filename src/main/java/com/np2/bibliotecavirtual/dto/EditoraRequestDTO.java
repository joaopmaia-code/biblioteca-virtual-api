package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;
import org.hibernate.validator.constraints.br.CNPJ;

/**
 * Dados recebidos para cadastrar ou atualizar uma editora.
 */
@Schema(description = "Dados para cadastrar ou atualizar uma editora")
public record EditoraRequestDTO(

        @Schema(description = "Nome da editora")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Schema(description = "CNPJ (único), com ou sem pontuação")
        @NotBlank(message = "O CNPJ é obrigatório")
        @CNPJ(message = "CNPJ inválido")
        String cnpj,

        @Schema(description = "Cidade da editora")
        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 100, message = "A cidade deve ter no máximo 100 caracteres")
        String cidade,

        @Schema(description = "Site da editora (opcional)")
        @URL(message = "O site deve ser uma URL válida")
        @Size(max = 255, message = "O site deve ter no máximo 255 caracteres")
        String site
) {
}
