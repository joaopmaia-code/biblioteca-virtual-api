package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.EditoraRequestDTO;
import com.np2.bibliotecavirtual.dto.EditoraResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Editora;
import com.np2.bibliotecavirtual.repository.EditoraRepository;
import com.np2.bibliotecavirtual.repository.MaterialRepository;
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
 * Testa as regras do EditoraService com o repositório simulado (Mockito), sem banco.
 */
@ExtendWith(MockitoExtension.class)
class EditoraServiceTest {

    @Mock
    private EditoraRepository editoraRepository;

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private EditoraService editoraService;

    private final EditoraRequestDTO dto = new EditoraRequestDTO(
            "  Editora Moderna  ", "11.222.333/0001-81", " São Paulo ", "");

    @Test
    void cadastrarGuardaCnpjSoComDigitosESiteVazioComoNulo() {
        when(editoraRepository.existsByCnpj("11222333000181")).thenReturn(false);
        when(editoraRepository.save(any(Editora.class))).thenAnswer(invocacao -> {
            Editora salva = invocacao.getArgument(0);
            salva.setId(1L);
            return salva;
        });

        EditoraResponseDTO resposta = editoraService.cadastrar(dto);

        assertThat(resposta.id()).isEqualTo(1L);
        assertThat(resposta.nome()).isEqualTo("Editora Moderna");
        assertThat(resposta.cnpj()).isEqualTo("11222333000181");
        assertThat(resposta.cidade()).isEqualTo("São Paulo");
        assertThat(resposta.site()).isNull();
    }

    @Test
    void cadastrarComCnpjDuplicadoLancaRegraNegocioENaoSalva() {
        when(editoraRepository.existsByCnpj("11222333000181")).thenReturn(true);

        assertThatThrownBy(() -> editoraService.cadastrar(dto))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Já existe uma editora com o CNPJ 11222333000181");
        verify(editoraRepository, never()).save(any());
    }

    @Test
    void buscarPorIdInexistenteLancaRecursoNaoEncontrado() {
        when(editoraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> editoraService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Editora não encontrada com id 99");
    }

    @Test
    void atualizarMantendoOProprioCnpjNaoAcusaDuplicidade() {
        Editora existente = editoraComId(1L, "11222333000181");
        when(editoraRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(editoraRepository.existsByCnpjAndIdNot("11222333000181", 1L)).thenReturn(false);
        when(editoraRepository.save(existente)).thenReturn(existente);

        EditoraResponseDTO resposta = editoraService.atualizar(1L, dto);

        assertThat(resposta.nome()).isEqualTo("Editora Moderna");
    }

    @Test
    void atualizarComCnpjDeOutraEditoraLancaRegraNegocio() {
        when(editoraRepository.findById(2L)).thenReturn(Optional.of(editoraComId(2L, "99888777000166")));
        when(editoraRepository.existsByCnpjAndIdNot("11222333000181", 2L)).thenReturn(true);

        assertThatThrownBy(() -> editoraService.atualizar(2L, dto))
                .isInstanceOf(RegraNegocioException.class);
        verify(editoraRepository, never()).save(any());
    }

    @Test
    void excluirEditoraComMateriaisLancaRegraNegocioENaoExclui() {
        when(editoraRepository.findById(1L)).thenReturn(Optional.of(editoraComId(1L, "11222333000181")));
        when(materialRepository.existsByEditoraId(1L)).thenReturn(true);

        assertThatThrownBy(() -> editoraService.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessage("Não é possível excluir a editora 'Editora Antiga': existem materiais vinculados a ela");
        verify(editoraRepository, never()).delete(any());
    }

    @Test
    void excluirEditoraSemMateriaisRemove() {
        Editora existente = editoraComId(1L, "11222333000181");
        when(editoraRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(materialRepository.existsByEditoraId(1L)).thenReturn(false);

        editoraService.excluir(1L);

        verify(editoraRepository).delete(existente);
    }

    @Test
    void excluirInexistenteLancaRecursoNaoEncontrado() {
        when(editoraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> editoraService.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(editoraRepository, never()).delete(any());
    }

    private Editora editoraComId(Long id, String cnpj) {
        Editora editora = new Editora();
        editora.setId(id);
        editora.setNome("Editora Antiga");
        editora.setCnpj(cnpj);
        editora.setCidade("Campinas");
        return editora;
    }
}
