package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.PerfilUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos para cadastrar ou atualizar um usuário (só o bibliotecário cadastra e edita).
 */
@Schema(description = "Dados para cadastrar ou atualizar um usuário")
public record UsuarioRequestDTO(

        @Schema(description = "Nome do usuário", example = "Ana Souza")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Schema(description = "E-mail (único)", example = "ana.souza@escola.edu.br")
        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
        String email,

        @Schema(description = "Matrícula (única, usada para entrar)", example = "2026001")
        @NotBlank(message = "A matrícula é obrigatória")
        @Size(max = 30, message = "A matrícula deve ter no máximo 30 caracteres")
        String matricula,

        @Schema(description = "Perfil de acesso (opcional; padrão ALUNO no cadastro, mantém o atual na edição)",
                example = "ALUNO")
        PerfilUsuario perfil,

        @Schema(description = "Senha (obrigatória no cadastro; na edição, só se for trocar)", example = "senha123")
        @Size(min = 6, max = 72, message = "A senha deve ter de 6 a 72 caracteres")
        String senha
) {
}
