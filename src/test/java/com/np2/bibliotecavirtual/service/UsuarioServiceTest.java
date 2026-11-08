package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.UsuarioRequestDTO;
import com.np2.bibliotecavirtual.dto.UsuarioResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.exception.RequisicaoInvalidaException;
import com.np2.bibliotecavirtual.model.PerfilUsuario;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    /** BCrypt de verdade, com custo baixo para o teste ser rápido. */
    @Spy
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    @InjectMocks
    private UsuarioService usuarioService;

    private final UsuarioRequestDTO dto = new UsuarioRequestDTO("  Ana Souza ", " Ana@Escola.edu.br ", " 2026001 ",
            null, "senha123");

    @Test
    void cadastrarGuardaASenhaCriptografadaEPerfilAlunoPorPadrao() {
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        usuarioService.cadastrar(dto);

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(salvo.capture());
        assertThat(salvo.getValue().getPerfil()).isEqualTo(PerfilUsuario.ALUNO);
        assertThat(salvo.getValue().getSenhaHash()).isNotEqualTo("senha123").startsWith("$2");
        assertThat(passwordEncoder.matches("senha123", salvo.getValue().getSenhaHash())).isTrue();
    }

    @Test
    void cadastrarSemSenhaLancaRequisicaoInvalida() {
        UsuarioRequestDTO semSenha = new UsuarioRequestDTO("Ana", "ana@escola.edu.br", "2026001", null, null);

        assertThatThrownBy(() -> usuarioService.cadastrar(semSenha))
                .isInstanceOf(RequisicaoInvalidaException.class)
                .hasMessage("A senha é obrigatória no cadastro");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void atualizarSemSenhaNemPerfilMantemOsAtuais() {
        Usuario existente = new Usuario();
        existente.setId(1L);
        existente.setPerfil(PerfilUsuario.PROFESSOR);
        existente.setSenhaHash("hash-antigo");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.save(existente)).thenReturn(existente);

        usuarioService.atualizar(1L, new UsuarioRequestDTO("Ana", "ana@escola.edu.br", "2026001", null, null));

        assertThat(existente.getPerfil()).isEqualTo(PerfilUsuario.PROFESSOR);
        assertThat(existente.getSenhaHash()).isEqualTo("hash-antigo");
    }

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
