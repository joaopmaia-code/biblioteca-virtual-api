package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.AvaliacaoAtualizacaoDTO;
import com.np2.bibliotecavirtual.dto.AvaliacaoRequestDTO;
import com.np2.bibliotecavirtual.dto.AvaliacaoResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Avaliacao;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.AvaliacaoRepository;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.MaterialRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa as regras de avaliação (RN11 e RN12) com os repositórios simulados (Mockito), sem banco.
 */
@ExtendWith(MockitoExtension.class)
class AvaliacaoServiceTest {

    @Mock
    private AvaliacaoRepository avaliacaoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @InjectMocks
    private AvaliacaoService avaliacaoService;

    private final Usuario usuario = usuario();
    private final Material material = material();

    @Test
    void cadastrarDeQuemJaPegouOMaterialSalvaComComentarioSemEspacos() {
        prepararUsuarioEMaterial();
        when(emprestimoRepository.existsByUsuarioIdAndMaterialIdAndStatusIn(eq(1L), eq(10L), anyCollection()))
                .thenReturn(true);
        when(avaliacaoRepository.existsByUsuarioIdAndMaterialId(1L, 10L)).thenReturn(false);
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocacao -> {
            Avaliacao salva = invocacao.getArgument(0);
            salva.setId(7L);
            return salva;
        });

        AvaliacaoResponseDTO resposta = avaliacaoService.cadastrar(
                new AvaliacaoRequestDTO(1L, 10L, 5, "  Ótimo para o ENEM  "));

        assertThat(resposta.id()).isEqualTo(7L);
        assertThat(resposta.nota()).isEqualTo(5);
        assertThat(resposta.comentario()).isEqualTo("Ótimo para o ENEM");
        assertThat(resposta.usuario().nome()).isEqualTo("Ana");
        assertThat(resposta.material().nome()).isEqualTo("Dom Casmurro");
        assertThat(resposta.dataAvaliacao()).isNotNull();
    }

    @Test
    void cadastrarComComentarioEmBrancoGuardaNull() {
        prepararUsuarioEMaterial();
        when(emprestimoRepository.existsByUsuarioIdAndMaterialIdAndStatusIn(eq(1L), eq(10L), anyCollection()))
                .thenReturn(true);
        when(avaliacaoRepository.save(any(Avaliacao.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        AvaliacaoResponseDTO resposta = avaliacaoService.cadastrar(new AvaliacaoRequestDTO(1L, 10L, 3, "   "));

        assertThat(resposta.comentario()).isNull();
    }

    @Test
    void cadastrarDeQuemNuncaPegouOMaterialLancaRegraNegocio() {
        prepararUsuarioEMaterial();
        when(emprestimoRepository.existsByUsuarioIdAndMaterialIdAndStatusIn(eq(1L), eq(10L), anyCollection()))
                .thenReturn(false);

        assertThatThrownBy(() -> avaliacaoService.cadastrar(new AvaliacaoRequestDTO(1L, 10L, 5, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Só pode avaliar o material quem já o pegou emprestado");
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void cadastrarSegundaAvaliacaoDoMesmoMaterialLancaRegraNegocio() {
        prepararUsuarioEMaterial();
        when(emprestimoRepository.existsByUsuarioIdAndMaterialIdAndStatusIn(eq(1L), eq(10L), anyCollection()))
                .thenReturn(true);
        when(avaliacaoRepository.existsByUsuarioIdAndMaterialId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> avaliacaoService.cadastrar(new AvaliacaoRequestDTO(1L, 10L, 4, null)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("já avaliou");
        verify(avaliacaoRepository, never()).save(any());
    }

    @Test
    void cadastrarComMaterialInexistenteLanca404() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(materialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.cadastrar(new AvaliacaoRequestDTO(1L, 99L, 5, null)))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Material não encontrado com id 99");
    }

    @Test
    void atualizarTrocaNotaEComentario() {
        Avaliacao existente = new Avaliacao();
        existente.setId(7L);
        existente.setUsuario(usuario);
        existente.setMaterial(material);
        existente.setNota(5);
        existente.setComentario("Gostei");
        existente.setDataAvaliacao(LocalDateTime.now().minusDays(3));
        when(avaliacaoRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(avaliacaoRepository.save(existente)).thenReturn(existente);

        AvaliacaoResponseDTO resposta = avaliacaoService.atualizar(7L, new AvaliacaoAtualizacaoDTO(2, "Mudei de ideia"));

        assertThat(resposta.nota()).isEqualTo(2);
        assertThat(resposta.comentario()).isEqualTo("Mudei de ideia");
        assertThat(resposta.dataAvaliacao()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void excluirAvaliacaoInexistenteLanca404() {
        when(avaliacaoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> avaliacaoService.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Avaliação não encontrada com id 99");
    }

    private void prepararUsuarioEMaterial() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(materialRepository.findById(10L)).thenReturn(Optional.of(material));
    }

    private static Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Ana");
        return usuario;
    }

    private static Material material() {
        Material material = new Material();
        material.setId(10L);
        material.setTitulo("Dom Casmurro");
        return material;
    }
}
