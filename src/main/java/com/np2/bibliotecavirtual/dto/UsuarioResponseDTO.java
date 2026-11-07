package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dados de um usuário devolvidos pela API.
 */
@Schema(description = "Usuário cadastrado")
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String matricula
) {

    public static UsuarioResponseDTO de(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getMatricula());
    }
}
