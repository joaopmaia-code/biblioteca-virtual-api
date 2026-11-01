package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Dados recebidos para cadastrar ou atualizar um autor.
 */
@Schema(description = "Dados para cadastrar ou atualizar um autor")
public record AutorRequestDTO(

        @Schema(description = "Nome do autor")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Schema(description = "Nacionalidade (opcional)")
        @Size(max = 100, message = "A nacionalidade deve ter no máximo 100 caracteres")
        String nacionalidade,

        @Schema(description = "Data de nascimento (opcional, no passado)")
        @Past(message = "A data de nascimento deve estar no passado")
        LocalDate dataNascimento,

        @Schema(description = "Biografia (opcional)")
        String biografia
) {
}
