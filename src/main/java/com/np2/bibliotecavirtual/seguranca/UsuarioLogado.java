package com.np2.bibliotecavirtual.seguranca;

import com.np2.bibliotecavirtual.model.PerfilUsuario;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

/**
 * Quem fez a requisição, lido do token JWT já validado pelo Spring Security.
 * Usado nas regras que dependem do corpo da requisição (ex.: o usuarioId de um novo empréstimo).
 */
@Component
public class UsuarioLogado {

    /** ID do usuário do token, ou null se a requisição não tem token. */
    public Long id() {
        return idDe(SecurityContextHolder.getContext().getAuthentication());
    }

    public boolean temPerfil(PerfilUsuario... perfis) {
        return temPerfil(SecurityContextHolder.getContext().getAuthentication(), perfis);
    }

    /** Aluno e professor só agem em nome próprio; o bibliotecário pode agir por qualquer usuário. */
    public void garantirQueEhOProprioOuBibliotecario(Long usuarioId) {
        if (!Objects.equals(id(), usuarioId) && !temPerfil(PerfilUsuario.BIBLIOTECARIO)) {
            throw new AccessDeniedException("Você só pode fazer isso em seu próprio nome");
        }
    }

    /** Ninguém age em nome de outro (ex.: avaliar um material). */
    public void garantirQueEhOProprio(Long usuarioId) {
        if (!Objects.equals(id(), usuarioId)) {
            throw new AccessDeniedException("Você só pode fazer isso em seu próprio nome");
        }
    }

    static Long idDe(Authentication autenticacao) {
        if (autenticacao != null && autenticacao.getPrincipal() instanceof Jwt jwt) {
            return Long.valueOf(jwt.getSubject());
        }
        return null;
    }

    static boolean temPerfil(Authentication autenticacao, PerfilUsuario... perfis) {
        if (autenticacao == null) {
            return false;
        }
        return autenticacao.getAuthorities().stream()
                .anyMatch(autoridade -> Arrays.stream(perfis)
                        .anyMatch(perfil -> autoridade.getAuthority().equals("ROLE_" + perfil.name())));
    }
}
