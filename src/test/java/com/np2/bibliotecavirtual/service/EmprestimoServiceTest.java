package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.EmprestimoAtualizacaoDTO;
import com.np2.bibliotecavirtual.dto.EmprestimoRequestDTO;
import com.np2.bibliotecavirtual.dto.EmprestimoResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
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
 * Testa as regras de empréstimo (RN01–RN05) com o repositório simulado (Mockito), sem banco.
 * Usa o EmprestimoRegras de verdade (prazo 14 dias, máximo 3), só o repositório é simulado.
 */
@ExtendWith(MockitoExtension.class)
class EmprestimoServiceTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private UsuarioService usuarioService;

    private EmprestimoService emprestimoService;

    private final Usuario usuario = usuario(1L);
    private final Material material = material(10L, 2);

    @BeforeEach
    void montarService() {
        EmprestimoRegras regras = new EmprestimoRegras(emprestimoRepository, 14, 3);
        emprestimoService = new EmprestimoService(emprestimoRepository, usuarioService, regras);
    }

    @Test
    void registrarSemDataUsaPrazoPadraoDe14DiasEStatusAtivo() {
        prepararUsuarioEMaterial();
        when(emprestimoRepository.countByMaterialIdAndStatusIn(any(), anyCollection())).thenReturn(0L);
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocacao -> {
            Emprestimo salvo = invocacao.getArgument(0);
            salvo.setId(5L);
            return salvo;
        });

        EmprestimoResponseDTO resposta = emprestimoService.registrar(new EmprestimoRequestDTO(1L, 10L, null));

        assertThat(resposta.id()).isEqualTo(5L);
        assertThat(resposta.status()).isEqualTo(StatusEmprestimo.ATIVO);
        assertThat(resposta.dataPrevistaDevolucao()).isEqualTo(LocalDate.now().plusDays(14));
        assertThat(resposta.dataRetirada()).isNotNull();
        assertThat(resposta.usuario().id()).isEqualTo(1L);
        assertThat(resposta.material().nome()).isEqualTo("Dom Casmurro");
    }

    @Test
    void registrarSemLicencaLivreLancaRegraNegocio() {
        prepararUsuarioEMaterial();
        when(emprestimoRepository.countByMaterialIdAndStatusIn(any(), anyCollection())).thenReturn(2L);

        assertThatThrownBy(() -> emprestimoService.registrar(new EmprestimoRequestDTO(1L, 10L, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Sem licenças disponíveis no momento. Tente de novo quando alguém devolver.");
        verify(emprestimoRepository, never()).save(any());
    }

    @Test
    void registrarMaterialQueOUsuarioJaTemLancaRegraNegocio() {
        prepararUsuarioEMaterial();
        when(emprestimoRepository.countByMaterialIdAndStatusIn(any(), anyCollection())).thenReturn(1L);
        when(emprestimoRepository.existsByUsuarioIdAndMaterialIdAndStatusIn(any(), any(), anyCollection()))
                .thenReturn(true);

        assertThatThrownBy(() -> emprestimoService.registrar(new EmprestimoRequestDTO(1L, 10L, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O usuário já está com este material emprestado");
        verify(emprestimoRepository, never()).save(any());
    }

    @Test
    void registrarComTresEmprestimosEmUsoLancaRegraNegocio() {
        prepararUsuarioEMaterial();
        when(emprestimoRepository.countByMaterialIdAndStatusIn(any(), anyCollection())).thenReturn(0L);
        when(emprestimoRepository.countByUsuarioIdAndStatusIn(any(), anyCollection())).thenReturn(3L);

        assertThatThrownBy(() -> emprestimoService.registrar(new EmprestimoRequestDTO(1L, 10L, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("O usuário atingiu o limite de 3 empréstimos ativos");
        verify(emprestimoRepository, never()).save(any());
    }

    @Test
    void registrarComMaterialInexistenteLancaRecursoNaoEncontrado() {
        when(usuarioService.buscarUsuario(1L)).thenReturn(usuario);
        when(emprestimoRepository.buscarMaterialComTrava(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emprestimoService.registrar(new EmprestimoRequestDTO(1L, 99L, null)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Material não encontrado com id 99");
    }

    @Test
    void devolverMarcaDevolvidoComData() {
        Emprestimo emprestimo = emprestimo(StatusEmprestimo.ATIVO);
        when(emprestimoRepository.findById(5L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(emprestimo)).thenReturn(emprestimo);

        EmprestimoResponseDTO resposta = emprestimoService.devolver(5L);

        assertThat(resposta.status()).isEqualTo(StatusEmprestimo.DEVOLVIDO);
        assertThat(resposta.dataDevolucao()).isNotNull();
    }

    @Test
    void devolverDuasVezesLancaRegraNegocio() {
        when(emprestimoRepository.findById(5L)).thenReturn(Optional.of(emprestimo(StatusEmprestimo.DEVOLVIDO)));

        assertThatThrownBy(() -> emprestimoService.devolver(5L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Este empréstimo já foi devolvido");
        verify(emprestimoRepository, never()).save(any());
    }

    @Test
    void renovarEmprestimoAtrasadoVoltaParaAtivo() {
        Emprestimo emprestimo = emprestimo(StatusEmprestimo.ATRASADO);
        when(emprestimoRepository.findById(5L)).thenReturn(Optional.of(emprestimo));
        when(emprestimoRepository.save(emprestimo)).thenReturn(emprestimo);
        LocalDate novaData = LocalDate.now().plusDays(7);

        EmprestimoResponseDTO resposta = emprestimoService.atualizar(5L, new EmprestimoAtualizacaoDTO(novaData));

        assertThat(resposta.status()).isEqualTo(StatusEmprestimo.ATIVO);
        assertThat(resposta.dataPrevistaDevolucao()).isEqualTo(novaData);
    }

    @Test
    void atualizarAtrasadosMarcaOsVencidosComoAtrasado() {
        Emprestimo vencido = emprestimo(StatusEmprestimo.ATIVO);
        when(emprestimoRepository.findByStatusAndDataPrevistaDevolucaoBefore(StatusEmprestimo.ATIVO, LocalDate.now()))
                .thenReturn(List.of(vencido));

        int atualizados = emprestimoService.atualizarAtrasados();

        assertThat(atualizados).isEqualTo(1);
        assertThat(vencido.getStatus()).isEqualTo(StatusEmprestimo.ATRASADO);
    }

    private void prepararUsuarioEMaterial() {
        when(usuarioService.buscarUsuario(1L)).thenReturn(usuario);
        when(emprestimoRepository.buscarMaterialComTrava(10L)).thenReturn(Optional.of(material));
    }

    private Emprestimo emprestimo(StatusEmprestimo status) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setId(5L);
        emprestimo.setUsuario(usuario);
        emprestimo.setMaterial(material);
        emprestimo.setDataPrevistaDevolucao(LocalDate.now().minusDays(1));
        emprestimo.setStatus(status);
        return emprestimo;
    }

    private static Usuario usuario(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Ana Souza");
        usuario.setEmail("ana@escola.edu.br");
        usuario.setMatricula("2026001");
        return usuario;
    }

    private static Material material(Long id, int licencas) {
        Material material = new Material();
        material.setId(id);
        material.setTitulo("Dom Casmurro");
        material.setQuantidadeLicencas(licencas);
        return material;
    }
}
