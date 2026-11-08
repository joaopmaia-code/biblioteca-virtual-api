package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

/**
 * Uma página de resultados. Formato próprio (em vez do Page do Spring) para o JSON ficar
 * estável e com nomes em português.
 */
@Schema(description = "Página de resultados")
public record PaginaResponseDTO<T>(

        @Schema(description = "Itens desta página")
        List<T> conteudo,

        @Schema(description = "Número da página, começando em 0", example = "0")
        int pagina,

        @Schema(description = "Itens por página", example = "10")
        int tamanho,

        @Schema(description = "Total de itens encontrados em todas as páginas", example = "23")
        long totalElementos,

        @Schema(description = "Total de páginas", example = "3")
        int totalPaginas
) {

    public static <E, T> PaginaResponseDTO<T> de(Page<E> pagina, Function<E, T> conversor) {
        return new PaginaResponseDTO<>(
                pagina.getContent().stream().map(conversor).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }
}
