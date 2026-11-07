package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.AutorRequestDTO;
import com.np2.bibliotecavirtual.dto.AutorResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Autor;
import com.np2.bibliotecavirtual.repository.AutorRepository;
import com.np2.bibliotecavirtual.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa as regras do AutorService com o repositório simulado (Mockito), sem banco.
 */
@ExtendWith(MockitoExtension.class)
class AutorServiceTest {

    @Mock
    private AutorRepository autorRepository;

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private AutorService autorService;

    private final AutorRequestDTO dto = new AutorRequestDTO(
            "  Machado de Assis  ", "Brasileira", LocalDate.of(1839, 6, 21), "Fundador da ABL.");

    @Test
    void cadastrarSalvaComNomeSemEspacosNasPontas() {
        when(autorRepository.save(any(Autor.class))).thenAnswer(invocacao -> {
            Autor salvo = invocacao.getArgument(0);
            salvo.setId(1L);
            return salvo;
        });

        AutorResponseDTO resposta = autorService.cadastrar(dto);

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.nome()).isEqualTo("Machado de Assis");
        assertThat(resposta.dataNascimento()).isEqualTo(LocalDate.of(1839, 6, 21));
    }

    @Test
    void listarDevolveEmOrdemAlfabetica() {
        when(autorRepository.findAll(Sort.by("nome")))
                .thenReturn(List.of(autorComId(1L, "Cecília Meireles"), autorComId(2L, "Machado de Assis")));

        List<AutorResponseDTO> resposta = autorService.listar();

        assertThat(resposta).extracting(AutorResponseDTO::nome)
                .containsExactly("Cecília Meireles", "Machado de Assis");
    }

    @Test
    void buscarPorIdInexistenteLancaRecursoNaoEncontrado() {
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autorService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Autor não encontrado com id 99");
    }

    @Test
    void atualizarSubstituiOsDados() {
        Autor existente = autorComId(1L, "Machado");
        when(autorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(autorRepository.save(existente)).thenReturn(existente);

        AutorResponseDTO resposta = autorService.atualizar(1L, dto);

        assertThat(resposta.nome()).isEqualTo("Machado de Assis");
        assertThat(resposta.nacionalidade()).isEqualTo("Brasileira");
    }

    @Test
    void atualizarInexistenteLancaRecursoNaoEncontradoENaoSalva() {
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autorService.atualizar(99L, dto))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(autorRepository, never()).save(any());
    }

    @Test
    void excluirRemoveOAutor() {
        Autor existente = autorComId(1L, "Machado de Assis");
        when(autorRepository.findById(1L)).thenReturn(Optional.of(existente));

        autorService.excluir(1L);

        verify(autorRepository).delete(existente);
    }

    @Test
    void excluirAutorComMateriaisLancaRegraNegocioENaoExclui() {
        when(autorRepository.findById(1L)).thenReturn(Optional.of(autorComId(1L, "Machado de Assis")));
        when(materialRepository.existsByAutoresId(1L)).thenReturn(true);

        assertThatThrownBy(() -> autorService.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível excluir o autor 'Machado de Assis': existem materiais vinculados a ele");
        verify(autorRepository, never()).delete(any());
    }

    @Test
    void excluirInexistenteLancaRecursoNaoEncontrado() {
        when(autorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autorService.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(autorRepository, never()).delete(any());
    }

    private Autor autorComId(Long id, String nome) {
        Autor autor = new Autor();
        autor.setId(id);
        autor.setNome(nome);
        return autor;
    }
}
