package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.CategoriaRequestDTO;
import com.np2.bibliotecavirtual.dto.CategoriaResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Categoria;
import com.np2.bibliotecavirtual.model.SerieRecomendada;
import com.np2.bibliotecavirtual.repository.CategoriaRepository;
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
 * Testa as regras do CategoriaService com o repositório simulado (Mockito), sem banco.
 */
@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private final CategoriaRequestDTO dto = new CategoriaRequestDTO(
            "  Matemática  ", "Álgebra e geometria", "Ciências Exatas", SerieRecomendada.PRIMEIRO_ANO);

    @Test
    void cadastrarSalvaComNomeSemEspacosNasPontas() {
        when(categoriaRepository.existsByNomeIgnoreCase("Matemática")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocacao -> {
            Categoria salva = invocacao.getArgument(0);
            salva.setId(1L);
            return salva;
        });

        CategoriaResponseDTO resposta = categoriaService.cadastrar(dto);

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.nome()).isEqualTo("Matemática");
        assertThat(resposta.serieRecomendada()).isEqualTo(SerieRecomendada.PRIMEIRO_ANO);
    }

    @Test
    void cadastrarComNomeDuplicadoLancaRegraNegocioENaoSalva() {
        when(categoriaRepository.existsByNomeIgnoreCase("Matemática")).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.cadastrar(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe uma categoria com o nome 'Matemática'");
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void buscarPorIdInexistenteLancaRecursoNaoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Categoria não encontrada com id 99");
    }

    @Test
    void atualizarMantendoOProprioNomeNaoAcusaDuplicidade() {
        Categoria existente = categoriaComId(1L, "Matemática");
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Matemática", 1L)).thenReturn(false);
        when(categoriaRepository.save(existente)).thenReturn(existente);

        CategoriaResponseDTO resposta = categoriaService.atualizar(1L, dto);

        assertThat(resposta.descricao()).isEqualTo("Álgebra e geometria");
    }

    @Test
    void atualizarComNomeDeOutraCategoriaLancaRegraNegocio() {
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoriaComId(2L, "Física")));
        when(categoriaRepository.existsByNomeIgnoreCaseAndIdNot("Matemática", 2L)).thenReturn(true);

        assertThatThrownBy(() -> categoriaService.atualizar(2L, dto))
                .isInstanceOf(RegraNegocioException.class);
        verify(categoriaRepository, never()).save(any());
    }

    @Test
    void excluirInexistenteLancaRecursoNaoEncontrado() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoriaService.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(categoriaRepository, never()).delete(any());
    }

    private Categoria categoriaComId(Long id, String nome) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome(nome);
        categoria.setAreaConhecimento("Ciências Exatas");
        categoria.setSerieRecomendada(SerieRecomendada.PRIMEIRO_ANO);
        return categoria;
    }
}
