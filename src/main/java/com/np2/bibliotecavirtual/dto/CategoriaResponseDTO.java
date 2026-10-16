package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.Categoria;
import com.np2.bibliotecavirtual.model.SerieRecomendada;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dados de uma categoria devolvidos pela API.
 */
@Schema(description = "Categoria cadastrada")
public record CategoriaResponseDTO(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Matemática")
        String nome,

        @Schema(example = "Álgebra, geometria e funções")
        String descricao,

        @Schema(example = "Ciências Exatas")
        String areaConhecimento,

        @Schema(example = "PRIMEIRO_ANO")
        SerieRecomendada serieRecomendada
) {

    public static CategoriaResponseDTO de(Categoria categoria) {
        return new CategoriaResponseDTO(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getAreaConhecimento(),
                categoria.getSerieRecomendada());
    }
}
