package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.ProgressoLeituraRequestDTO;
import com.np2.bibliotecavirtual.dto.ProgressoLeituraResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.ProgressoLeitura;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.ProgressoLeituraRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa as regras do progresso de leitura (RN13 e RN14) com os repositórios simulados (Mockito), sem banco.
 */
@ExtendWith(MockitoExtension.class)
class ProgressoLeituraServiceTest {

    @Mock
    private ProgressoLeituraRepository progressoRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ProgressoLeituraService progressoService;

    @Test
    void registrarPrimeiraVezCriaProgressoECalculaPercentual() {
        Emprestimo emprestimo = emprestimo(1L, StatusEmprestimo.ATIVO, 256);
        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));
        when(progressoRepository.findByEmprestimoId(1L)).thenReturn(Optional.empty());
        when(progressoRepository.save(any(ProgressoLeitura.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        ProgressoLeituraResponseDTO resposta = progressoService.registrar(1L, new ProgressoLeituraRequestDTO(120));

        assertThat(resposta.paginaAtual()).isEqualTo(120);
        assertThat(resposta.numeroPaginas()).isEqualTo(256);
        assertThat(resposta.percentual()).isEqualTo(46.9);
        assertThat(resposta.concluido()).isFalse();
        assertThat(resposta.atualizadoEm()).isNotNull();
    }

    @Test
    void registrarDeNovoAtualizaOMesmoProgressoEMarcaConcluidoNaUltimaPagina() {
        Emprestimo emprestimo = emprestimo(1L, StatusEmprestimo.ATRASADO, 256);
        ProgressoLeitura existente = progresso(emprestimo, 120);
        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo));
        when(progressoRepository.findByEmprestimoId(1L)).thenReturn(Optional.of(existente));
        when(progressoRepository.save(existente)).thenReturn(existente);

        ProgressoLeituraResponseDTO resposta = progressoService.registrar(1L, new ProgressoLeituraRequestDTO(256));

        assertThat(existente.getPaginaAtual()).isEqualTo(256);
        assertThat(resposta.percentual()).isEqualTo(100.0);
        assertThat(resposta.concluido()).isTrue();
    }

    @Test
    void registrarEmEmprestimoDevolvidoLancaRegraNegocio() {
        when(emprestimoRepository.findById(1L))
                .thenReturn(Optional.of(emprestimo(1L, StatusEmprestimo.DEVOLVIDO, 256)));

        assertThatThrownBy(() -> progressoService.registrar(1L, new ProgressoLeituraRequestDTO(10)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já foi devolvido");
        verify(progressoRepository, never()).save(any());
    }

    @Test
    void registrarPaginaMaiorQueOTotalLancaRegraNegocio() {
        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo(1L, StatusEmprestimo.ATIVO, 256)));

        assertThatThrownBy(() -> progressoService.registrar(1L, new ProgressoLeituraRequestDTO(300)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("A página 300 passa do total de 256 páginas do material");
        verify(progressoRepository, never()).save(any());
    }

    @Test
    void registrarEmMaterialSemNumeroDePaginasLancaRegraNegocio() {
        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo(1L, StatusEmprestimo.ATIVO, null)));

        assertThatThrownBy(() -> progressoService.registrar(1L, new ProgressoLeituraRequestDTO(1)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não tem número de páginas");
    }

    @Test
    void consultarSemProgressoRegistradoDevolvePaginaZero() {
        when(emprestimoRepository.findById(1L)).thenReturn(Optional.of(emprestimo(1L, StatusEmprestimo.ATIVO, 256)));
        when(progressoRepository.findByEmprestimoId(1L)).thenReturn(Optional.empty());

        ProgressoLeituraResponseDTO resposta = progressoService.consultar(1L);

        assertThat(resposta.paginaAtual()).isZero();
        assertThat(resposta.percentual()).isEqualTo(0.0);
        assertThat(resposta.atualizadoEm()).isNull();
    }

    @Test
    void leiturasDoUsuarioTrazSoEmprestimosEmUsoComOProgressoDeCada() {
        Emprestimo ativoComProgresso = emprestimo(1L, StatusEmprestimo.ATIVO, 200);
        Emprestimo atrasadoSemProgresso = emprestimo(2L, StatusEmprestimo.ATRASADO, 100);
        Emprestimo devolvido = emprestimo(3L, StatusEmprestimo.DEVOLVIDO, 100);
        ativoComProgresso.setDataRetirada(LocalDateTime.now().minusDays(1));
        atrasadoSemProgresso.setDataRetirada(LocalDateTime.now().minusDays(20));
        when(usuarioRepository.existsById(5L)).thenReturn(true);
        when(emprestimoRepository.findByUsuarioId(5L))
                .thenReturn(List.of(atrasadoSemProgresso, devolvido, ativoComProgresso));
        when(progressoRepository.findByEmprestimoIdIn(anyCollection()))
                .thenReturn(List.of(progresso(ativoComProgresso, 50)));

        List<ProgressoLeituraResponseDTO> leituras = progressoService.leiturasDoUsuario(5L);

        assertThat(leituras).extracting(ProgressoLeituraResponseDTO::emprestimoId).containsExactly(1L, 2L);
        assertThat(leituras.get(0).percentual()).isEqualTo(25.0);
        assertThat(leituras.get(1).paginaAtual()).isZero();
    }

    @Test
    void leiturasDeUsuarioInexistenteLanca404() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> progressoService.leiturasDoUsuario(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado com id 99");
    }

    private static Emprestimo emprestimo(Long id, StatusEmprestimo status, Integer paginas) {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNome("Ana");

        Material material = new Material();
        material.setId(10L + id);
        material.setTitulo("Material " + id);
        material.setNumeroPaginas(paginas);

        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(id);
        emprestimo.setUsuario(usuario);
        emprestimo.setMaterial(material);
        emprestimo.setStatus(status);
        emprestimo.setDataRetirada(LocalDateTime.now());
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().plusDays(14));
        return emprestimo;
    }

    private static ProgressoLeitura progresso(Emprestimo emprestimo, int pagina) {
        ProgressoLeitura progresso = new ProgressoLeitura();
        progresso.setEmprestimo(emprestimo);
        progresso.setPaginaAtual(pagina);
        progresso.setAtualizadoEm(LocalDateTime.now());
        return progresso;
    }
}
