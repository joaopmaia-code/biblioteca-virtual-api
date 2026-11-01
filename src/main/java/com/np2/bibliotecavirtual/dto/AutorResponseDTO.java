package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.Autor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * Dados de um autor devolvidos pela API.
 */
@Schema(description = "Autor cadastrado")
public record AutorResponseDTO(
        Long id,
        String nome,
        String nacionalidade,
        LocalDate dataNascimento,
        String biografia
) {

    public static AutorResponseDTO de(Autor autor) {
        return new AutorResponseDTO(
                autor.getId(),
                autor.getNome(),
                autor.getNacionalidade(),
                autor.getDataNascimento(),
                autor.getBiografia());
    }
}
