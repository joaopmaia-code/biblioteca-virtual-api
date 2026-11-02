package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.MaterialRequestDTO;
import com.np2.bibliotecavirtual.dto.MaterialResponseDTO;
import com.np2.bibliotecavirtual.dto.ResumoDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.model.Autor;
import com.np2.bibliotecavirtual.model.Categoria;
import com.np2.bibliotecavirtual.model.Editora;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.model.TipoMaterial;
import com.np2.bibliotecavirtual.repository.AutorRepository;
import com.np2.bibliotecavirtual.repository.CategoriaRepository;
import com.np2.bibliotecavirtual.repository.EditoraRepository;
import com.np2.bibliotecavirtual.repository.MaterialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa as regras do MaterialService com os repositórios simulados (Mockito), sem banco.
 */
@ExtendWith(MockitoExtension.class)
class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private EditoraRepository editoraRepository;

    @Mock
    private AutorRepository autorRepository;

    @InjectMocks
    private MaterialService materialService;

    private final Categoria literatura = categoria(1L, "Literatura");
    private final Editora moderna = editora(1L, "Moderna");
    private final Autor machado = autor(1L, "Machado de Assis");
    private final Autor alencar = autor(2L, "José de Alencar");

    @Test
    void cadastrarLigaCategoriaEditoraEAutores() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(literatura));
        when(editoraRepository.findById(1L)).thenReturn(Optional.of(moderna));
        when(autorRepository.findAllById(Set.of(1L, 2L))).thenReturn(List.of(machado, alencar));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocacao -> {
            Material salvo = invocacao.getArgument(0);
            salvo.setId(10L);
            return salvo;
        });

        MaterialResponseDTO resposta = materialService.cadastrar(dto(1L, 1L, List.of(1L, 2L)));

        assertThat(resposta.id()).isEqualTo(10L);
        assertThat(resposta.titulo()).isEqualTo("Dom Casmurro");
        assertThat(resposta.categoria()).isEqualTo(new ResumoDTO(1L, "Literatura"));
        assertThat(resposta.editora()).isEqualTo(new ResumoDTO(1L, "Moderna"));
        // Autores voltam em ordem alfabética.
        assertThat(resposta.autores()).extracting(ResumoDTO::nome)
                .containsExactly("José de Alencar", "Machado de Assis");
    }

    @Test
    void cadastrarSemEditoraDeixaEditoraNula() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(literatura));
        when(autorRepository.findAllById(Set.of(1L))).thenReturn(List.of(machado));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        MaterialResponseDTO resposta = materialService.cadastrar(dto(1L, null, List.of(1L)));

        assertThat(resposta.editora()).isNull();
        verify(editoraRepository, never()).findById(any());
    }

    @Test
    void cadastrarComCategoriaInexistenteLancaRecursoNaoEncontradoENaoSalva() {
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.cadastrar(dto(99L, null, List.of(1L))))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Categoria não encontrada com id 99");
        verify(materialRepository, never()).save(any());
    }

    @Test
    void cadastrarComEditoraInexistenteLancaRecursoNaoEncontrado() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(literatura));
        when(editoraRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.cadastrar(dto(1L, 99L, List.of(1L))))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Editora não encontrada com id 99");
        verify(materialRepository, never()).save(any());
    }

    @Test
    void cadastrarComAutorInexistenteInformaQualIdFaltou() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(literatura));
        when(autorRepository.findAllById(Set.of(1L, 7L))).thenReturn(List.of(machado));

        assertThatThrownBy(() -> materialService.cadastrar(dto(1L, null, List.of(1L, 7L))))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Autor não encontrado com id 7");
        verify(materialRepository, never()).save(any());
    }

    @Test
    void cadastrarComAutorRepetidoNaListaNaoAcusaErro() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(literatura));
        when(autorRepository.findAllById(Set.of(1L))).thenReturn(List.of(machado));
        when(materialRepository.save(any(Material.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        MaterialResponseDTO resposta = materialService.cadastrar(dto(1L, null, List.of(1L, 1L)));

        assertThat(resposta.autores()).hasSize(1);
    }

    @Test
    void atualizarSubstituiOsAutores() {
        Material existente = new Material();
        existente.setId(10L);
        existente.setAutores(new HashSet<>(Set.of(machado)));
        when(materialRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(literatura));
        when(autorRepository.findAllById(Set.of(2L))).thenReturn(List.of(alencar));
        when(materialRepository.save(existente)).thenReturn(existente);

        MaterialResponseDTO resposta = materialService.atualizar(10L, dto(1L, null, List.of(2L)));

        assertThat(resposta.autores()).extracting(ResumoDTO::nome).containsExactly("José de Alencar");
    }

    @Test
    void buscarPorIdInexistenteLancaRecursoNaoEncontrado() {
        when(materialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Material não encontrado com id 99");
    }

    @Test
    void excluirInexistenteLancaRecursoNaoEncontrado() {
        when(materialRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> materialService.excluir(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(materialRepository, never()).delete(any());
    }

    private MaterialRequestDTO dto(Long categoriaId, Long editoraId, List<Long> autoresIds) {
        return new MaterialRequestDTO("  Dom Casmurro  ", TipoMaterial.LIVRO, 1899, "Bentinho e Capitu.",
                "https://acervo.escola.edu.br/dom-casmurro.pdf", 256, 3, categoriaId, editoraId, autoresIds);
    }

    private static Categoria categoria(Long id, String nome) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome(nome);
        return categoria;
    }

    private static Editora editora(Long id, String nome) {
        Editora editora = new Editora();
        editora.setId(id);
        editora.setNome(nome);
        return editora;
    }

    private static Autor autor(Long id, String nome) {
        Autor autor = new Autor();
        autor.setId(id);
        autor.setNome(nome);
        return autor;
    }
}
