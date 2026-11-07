package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.RecomendacaoResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.model.Autor;
import com.np2.bibliotecavirtual.model.Categoria;
import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.MaterialRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testa a recomendação pelo histórico de empréstimos com os repositórios simulados (Mockito), sem banco.
 */
@ExtendWith(MockitoExtension.class)
class RecomendacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private RecomendacaoService recomendacaoService;

    private final Categoria literatura = categoria(1L, "Literatura");
    private final Categoria matematica = categoria(2L, "Matemática");
    private final Autor machado = autor(1L, "Machado de Assis");
    private final Autor aluisio = autor(2L, "Aluísio Azevedo");
    private final Autor iezzi = autor(3L, "Gelson Iezzi");

    private final Material domCasmurro = material(1L, "Dom Casmurro", literatura, machado);
    private final Material memorias = material(2L, "Memórias Póstumas", literatura, machado);
    private final Material quincas = material(3L, "Quincas Borba", literatura, machado);
    private final Material cortico = material(4L, "O Cortiço", literatura, aluisio);
    private final Material algebra = material(5L, "Álgebra I", matematica, iezzi);

    @Test
    void recomendaPrimeiroOMesmoAutorDepoisAMesmaCategoriaESemOQueJaPegou() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(emprestimoRepository.findByUsuarioId(1L)).thenReturn(List.of(emprestimo(domCasmurro), emprestimo(memorias)));
        when(materialRepository.findAllByOrderByTituloAsc())
                .thenReturn(List.of(algebra, domCasmurro, memorias, cortico, quincas));

        List<RecomendacaoResponseDTO> resposta = recomendacaoService.recomendar(1L);

        // Quincas Borba: 2 da categoria + 2×2 do autor = 6; O Cortiço: só a categoria = 2; Álgebra não tem afinidade.
        assertThat(resposta).extracting(recomendacao -> recomendacao.material().titulo())
                .containsExactly("Quincas Borba", "O Cortiço");
        assertThat(resposta).extracting(RecomendacaoResponseDTO::pontuacao).containsExactly(6, 2);
        assertThat(resposta.get(0).motivo())
                .isEqualTo("Mesma categoria (Literatura) e mesmo autor (Machado de Assis) de materiais que você já pegou");
        assertThat(resposta.get(1).motivo())
                .isEqualTo("Mesma categoria (Literatura) de materiais que você já pegou");
    }

    @Test
    void usuarioSemEmprestimosRecebeListaVazia() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(emprestimoRepository.findByUsuarioId(1L)).thenReturn(List.of());

        assertThat(recomendacaoService.recomendar(1L)).isEmpty();
        verify(materialRepository, never()).findAllByOrderByTituloAsc();
    }

    @Test
    void usuarioInexistenteLancaRecursoNaoEncontrado() {
        when(usuarioRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> recomendacaoService.recomendar(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessage("Usuário não encontrado com id 99");
    }

    @Test
    void limitaAQuantidadeDeRecomendacoes() {
        when(usuarioRepository.existsById(1L)).thenReturn(true);
        when(emprestimoRepository.findByUsuarioId(1L)).thenReturn(List.of(emprestimo(domCasmurro)));
        List<Material> acervo = new ArrayList<>();
        for (long id = 100; id < 100 + RecomendacaoService.LIMITE + 5; id++) {
            acervo.add(material(id, "Livro " + id, literatura, aluisio));
        }
        when(materialRepository.findAllByOrderByTituloAsc()).thenReturn(acervo);

        assertThat(recomendacaoService.recomendar(1L)).hasSize(RecomendacaoService.LIMITE);
    }

    private static Emprestimo emprestimo(Material material) {
        Emprestimo emprestimo = new Emprestimo();
        emprestimo.setMaterial(material);
        return emprestimo;
    }

    private static Material material(Long id, String titulo, Categoria categoria, Autor autor) {
        Material material = new Material();
        material.setId(id);
        material.setTitulo(titulo);
        material.setCategoria(categoria);
        material.setAutores(Set.of(autor));
        return material;
    }

    private static Categoria categoria(Long id, String nome) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNome(nome);
        return categoria;
    }

    private static Autor autor(Long id, String nome) {
        Autor autor = new Autor();
        autor.setId(id);
        autor.setNome(nome);
        return autor;
    }
}
