package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.Autor;
import com.np2.bibliotecavirtual.model.Editora;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.TipoMaterial;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Comparator;
import java.util.List;

/**
 * Dados de um material devolvidos pela API, com categoria, editora e autores resumidos (ID e nome).
 */
@Schema(description = "Material cadastrado")
public record MaterialResponseDTO(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Dom Casmurro")
        String titulo,

        @Schema(example = "LIVRO")
        TipoMaterial tipo,

        @Schema(example = "1899")
        Integer anoPublicacao,

        @Schema(example = "Bentinho narra sua história com Capitu.")
        String sinopse,

        @Schema(example = "https://acervo.escola.edu.br/livros/dom-casmurro.pdf")
        String urlArquivo,

        @Schema(example = "256")
        Integer numeroPaginas,

        @Schema(example = "3")
        Integer quantidadeLicencas,

        ResumoDTO categoria,

        @Schema(description = "Editora (null quando o material não tem editora)")
        ResumoDTO editora,

        @Schema(description = "Autores em ordem alfabética")
        List<ResumoDTO> autores
) {

    public static MaterialResponseDTO de(Material material) {
        Editora editora = material.getEditora();

        return new MaterialResponseDTO(
                material.getId(),
                material.getTitulo(),
                material.getTipo(),
                material.getAnoPublicacao(),
                material.getSinopse(),
                material.getUrlArquivo(),
                material.getNumeroPaginas(),
                material.getQuantidadeLicencas(),
                new ResumoDTO(material.getCategoria().getId(), material.getCategoria().getNome()),
                editora == null ? null : new ResumoDTO(editora.getId(), editora.getNome()),
                material.getAutores().stream()
                        .sorted(Comparator.comparing(Autor::getNome))
                        .map(autor -> new ResumoDTO(autor.getId(), autor.getNome()))
                        .toList());
    }
}
