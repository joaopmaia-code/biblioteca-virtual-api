package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.SerieRecomendada;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Dados recebidos para cadastrar ou atualizar uma categoria.
 */
@Schema(description = "Dados para cadastrar ou atualizar uma categoria")
public record CategoriaRequestDTO(

        @Schema(description = "Nome da categoria (único)", example = "Matemática")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Schema(description = "Descrição da categoria (opcional)", example = "Álgebra, geometria e funções")
        @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
        String descricao,

        @Schema(description = "Área do conhecimento", example = "Ciências Exatas")
        @NotBlank(message = "A área de conhecimento é obrigatória")
        @Size(max = 100, message = "A área de conhecimento deve ter no máximo 100 caracteres")
        String areaConhecimento,

        @Schema(description = "Série recomendada", example = "PRIMEIRO_ANO")
        @NotNull(message = "A série recomendada é obrigatória")
        SerieRecomendada serieRecomendada
) {
}
