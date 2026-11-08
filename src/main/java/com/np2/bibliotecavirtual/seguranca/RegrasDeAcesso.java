package com.np2.bibliotecavirtual.seguranca;

import com.np2.bibliotecavirtual.repository.AvaliacaoRepository;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static com.np2.bibliotecavirtual.model.PerfilUsuario.BIBLIOTECARIO;
import static com.np2.bibliotecavirtual.model.PerfilUsuario.PROFESSOR;

/**
 * Regras de "dono do recurso", usadas no SegurancaConfig junto com as regras por perfil.
 * Cada regra olha o {id} da URL e responde se quem está logado pode seguir.
 * <p>
 * "Equipe" = professor ou bibliotecário (podem consultar dados dos alunos); só o bibliotecário altera
 * dados de outro usuário. Se o ID não é número ou o registro não existe, a regra deixa passar, e o
 * controller responde 400 ou 404 como sempre.
 */
@Component
@RequiredArgsConstructor
public class RegrasDeAcesso {

    private final EmprestimoRepository emprestimoRepository;
    private final AvaliacaoRepository avaliacaoRepository;

    /** /usuarios/{id}/...: o próprio usuário ou a equipe. */
    public AuthorizationManager<RequestAuthorizationContext> proprioUsuarioOuEquipe() {
        return (autenticacao, contexto) -> decidir(autenticacao.get(), idDaUrl(contexto)
                .map(id -> ehOProprio(autenticacao.get(), id) || ehEquipe(autenticacao.get())));
    }

    /** GET /emprestimos: a equipe vê todos; aluno só com ?usuarioId= dele mesmo. */
    public AuthorizationManager<RequestAuthorizationContext> listaDeEmprestimos() {
        return (autenticacao, contexto) -> {
            Authentication quem = autenticacao.get();
            if (ehEquipe(quem)) {
                return new AuthorizationDecision(true);
            }
            String usuarioId = contexto.getRequest().getParameter("usuarioId");
            return new AuthorizationDecision(usuarioId != null
                    && Objects.equals(usuarioId, String.valueOf(UsuarioLogado.idDe(quem))));
        };
    }

    /** Consultar um empréstimo (e o progresso dele): o dono ou a equipe. */
    public AuthorizationManager<RequestAuthorizationContext> donoDoEmprestimoOuEquipe() {
        return (autenticacao, contexto) -> decidir(autenticacao.get(), idDaUrl(contexto)
                .map(id -> ehEquipe(autenticacao.get()) || ehDonoDoEmprestimo(autenticacao.get(), id)));
    }

    /** Renovar, devolver ou registrar progresso: o dono ou o bibliotecário. */
    public AuthorizationManager<RequestAuthorizationContext> donoDoEmprestimoOuBibliotecario() {
        return (autenticacao, contexto) -> decidir(autenticacao.get(), idDaUrl(contexto)
                .map(id -> UsuarioLogado.temPerfil(autenticacao.get(), BIBLIOTECARIO)
                        || ehDonoDoEmprestimo(autenticacao.get(), id)));
    }

    /** Editar uma avaliação: só o autor. Excluir: o autor ou o bibliotecário (moderação). */
    public AuthorizationManager<RequestAuthorizationContext> autorDaAvaliacao(boolean bibliotecarioTambemPode) {
        return (autenticacao, contexto) -> decidir(autenticacao.get(), idDaUrl(contexto)
                .map(id -> (bibliotecarioTambemPode && UsuarioLogado.temPerfil(autenticacao.get(), BIBLIOTECARIO))
                        || avaliacaoRepository.findById(id)
                        .map(a -> Objects.equals(a.getUsuario().getId(), UsuarioLogado.idDe(autenticacao.get())))
                        .orElse(true)));
    }

    // ----- apoio -----

    /** Sem login: nega (vira 401). ID inválido na URL (Optional vazio): deixa o controller responder 400. */
    private AuthorizationDecision decidir(Authentication quem, Optional<Boolean> permitido) {
        if (UsuarioLogado.idDe(quem) == null) {
            return new AuthorizationDecision(false);
        }
        return new AuthorizationDecision(permitido.orElse(true));
    }

    private Optional<Long> idDaUrl(RequestAuthorizationContext contexto) {
        try {
            return Optional.of(Long.valueOf(contexto.getVariables().get("id")));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private boolean ehEquipe(Authentication quem) {
        return UsuarioLogado.temPerfil(quem, PROFESSOR, BIBLIOTECARIO);
    }

    private boolean ehOProprio(Authentication quem, Long usuarioId) {
        return Objects.equals(UsuarioLogado.idDe(quem), usuarioId);
    }

    private boolean ehDonoDoEmprestimo(Authentication quem, Long emprestimoId) {
        return emprestimoRepository.findById(emprestimoId)
                .map(e -> ehOProprio(quem, e.getUsuario().getId()))
                .orElse(true);
    }
}
