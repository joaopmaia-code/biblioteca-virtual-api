package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.TipoMaterial;
import com.np2.bibliotecavirtual.validacao.AnoNaoFuturo;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * Dados recebidos para cadastrar ou atualizar um material.
 * Categoria, editora e autores chegam só pelo ID; o Service busca cada um e devolve 404 se não existir.
 */
@Schema(description = "Dados para cadastrar ou atualizar um material")
public record MaterialRequestDTO(

        @Schema(description = "Título do material", example = "Dom Casmurro")
        @NotBlank(message = "O título é obrigatório")
        @Size(max = 200, message = "O título deve ter no máximo 200 caracteres")
        String titulo,

        @Schema(description = "Tipo do material", example = "LIVRO")
        @NotNull(message = "O tipo é obrigatório")
        TipoMaterial tipo,

        @Schema(description = "Ano de publicação (de 1500 até o ano atual)", example = "1899")
        @NotNull(message = "O ano de publicação é obrigatório")
        @Min(value = 1500, message = "O ano de publicação deve ser 1500 ou depois")
        @AnoNaoFuturo(message = "O ano de publicação não pode ser maior que o ano atual")
        Integer anoPublicacao,

        @Schema(description = "Sinopse (opcional)", example = "Bentinho narra sua história com Capitu.")
        @Size(max = 5000, message = "A sinopse deve ter no máximo 5000 caracteres")
        String sinopse,

        @Schema(description = "Link do arquivo digital", example = "https://acervo.escola.edu.br/livros/dom-casmurro.pdf")
        @NotBlank(message = "A URL do arquivo é obrigatória")
        @URL(message = "A URL do arquivo deve ser uma URL válida")
        @Size(max = 500, message = "A URL do arquivo deve ter no máximo 500 caracteres")
        String urlArquivo,

        @Schema(description = "Número de páginas (opcional)", example = "256")
        @Positive(message = "O número de páginas deve ser positivo")
        Integer numeroPaginas,

        @Schema(description = "Quantas pessoas podem ter o material emprestado ao mesmo tempo", example = "3")
        @NotNull(message = "A quantidade de licenças é obrigatória")
        @Min(value = 1, message = "A quantidade de licenças deve ser pelo menos 1")
        Integer quantidadeLicencas,

        @Schema(description = "ID da categoria", example = "1")
        @NotNull(message = "A categoria é obrigatória")
        Long categoriaId,

        @Schema(description = "ID da editora (opcional)", example = "1")
        Long editoraId,

        @ArraySchema(arraySchema = @Schema(description = "IDs dos autores (pelo menos um)"),
                schema = @Schema(example = "1"))
        @NotEmpty(message = "Informe pelo menos um autor")
        List<@NotNull(message = "O ID do autor não pode ser nulo") Long> autoresIds
) {
}
