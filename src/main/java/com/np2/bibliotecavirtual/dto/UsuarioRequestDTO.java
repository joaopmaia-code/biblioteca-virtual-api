package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos para cadastrar ou atualizar um usuário.
 */
@Schema(description = "Dados para cadastrar ou atualizar um usuário")
public record UsuarioRequestDTO(

        @Schema(description = "Nome do aluno", example = "Ana Souza")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Schema(description = "E-mail (único)", example = "ana.souza@escola.edu.br")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String email,

        @Schema(description = "Matrícula (única)", example = "2026001")
        @NotBlank(message = "A matrícula é obrigatória")
        @Size(max = 30, message = "A matrícula deve ter no máximo 30 caracteres")
        String matricula
) {
}
