package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.PerfilUsuario;
import com.np2.bibliotecavirtual.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Dados de um usuário devolvidos pela API (a senha nunca é devolvida).
 */
@Schema(description = "Usuário cadastrado")
public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        String matricula,
        @Schema(example = "ALUNO")
        PerfilUsuario perfil
) {

    public static UsuarioResponseDTO de(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getMatricula(),
                usuario.getPerfil());
    }
}
