package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Um campo inválido e a mensagem de validação correspondente.
 */
@Schema(description = "Erro de validação de um campo")
public record CampoErroDTO(

        @Schema(description = "Nome do campo", example = "nome")
        String campo,

        @Schema(description = "Mensagem de validação", example = "O nome é obrigatório")
        String mensagem
) {
}
