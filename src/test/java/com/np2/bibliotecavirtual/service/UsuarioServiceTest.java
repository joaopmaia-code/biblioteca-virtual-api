package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.UsuarioRequestDTO;
import com.np2.bibliotecavirtual.dto.UsuarioResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa as regras do UsuarioService com os repositórios simulados (Mockito), sem banco.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private final UsuarioRequestDTO dto = new UsuarioRequestDTO("  Ana Souza ", " Ana@Escola.edu.br ", " 2026001 ");

    @Test
    void cadastrarGuardaEmailEmMinusculasESemEspacos() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@escola.edu.br")).thenReturn(false);
        when(usuarioRepository.existsByMatricula("2026001")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> {
            Usuario salvo = invocacao.getArgument(0);
            salvo.setId(1L);
            return salvo;
        });

        UsuarioResponseDTO resposta = usuarioService.cadastrar(dto);

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.nome()).isEqualTo("Ana Souza");
        assertThat(resposta.email()).isEqualTo("ana@escola.edu.br");
        assertThat(resposta.matricula()).isEqualTo("2026001");
    }

    @Test
    void cadastrarComEmailDuplicadoLancaRegraNegocioENaoSalva() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@escola.edu.br")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe um usuário com o e-mail ana@escola.edu.br");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cadastrarComMatriculaDuplicadaLancaRegraNegocio() {
        when(usuarioRepository.existsByEmailIgnoreCase("ana@escola.edu.br")).thenReturn(false);
        when(usuarioRepository.existsByMatricula("2026001")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cadastrar(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe um usuário com a matrícula 2026001");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void buscarPorIdInexistenteLancaRecursoNaoEncontrado() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado com id 99");
    }

    @Test
    void excluirUsuarioComEmprestimosLancaRegraNegocio() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(emprestimoRepository.existsByUsuarioId(1L)).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.excluir(1L))
                .isInstanceOf(RegraNegocioException.class);
        verify(usuarioRepository, never()).delete(any());
    }
}
