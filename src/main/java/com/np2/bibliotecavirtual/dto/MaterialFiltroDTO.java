package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.SerieRecomendada;
import com.np2.bibliotecavirtual.model.TipoMaterial;

/**
 * Filtros da busca no acervo (GET /materiais/busca). Todos são opcionais: o que vier null não filtra.
 *
 * @param titulo      parte do título, sem diferenciar maiúsculas
 * @param serie       série recomendada da categoria do material
 * @param anoMin      ano de publicação a partir de (inclusive)
 * @param anoMax      ano de publicação até (inclusive)
 * @param notaMinima  média das avaliações maior ou igual a (materiais sem avaliação ficam de fora)
 * @param disponivel  true: só com licença livre; false: só os esgotados
 */
public record MaterialFiltroDTO(
        String titulo,
        Long autorId,
        Long categoriaId,
        Long editoraId,
        TipoMaterial tipo,
        SerieRecomendada serie,
        Integer anoMin,
        Integer anoMax,
        Integer notaMinima,
        Boolean disponivel
) {
}
