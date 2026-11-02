package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.Editora;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dados de uma editora devolvidos pela API.
 */
@Schema(description = "Editora cadastrada")
public record EditoraResponseDTO(
        Long id,
        String nome,
        String cnpj,
        String cidade,
        String site
) {

    public static EditoraResponseDTO de(Editora editora) {
        return new EditoraResponseDTO(
                editora.getId(),
                editora.getNome(),
                editora.getCnpj(),
                editora.getCidade(),
                editora.getSite());
    }
}
