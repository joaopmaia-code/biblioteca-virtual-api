package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.MaterialResponseDTO;
import com.np2.bibliotecavirtual.dto.RecomendacaoResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.model.Autor;
import com.np2.bibliotecavirtual.model.Emprestimo;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.MaterialRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Funcionalidade extra: recomenda materiais parecidos com os que o usuário já pegou emprestado.
 * <p>
 * Cada material que o usuário ainda não pegou ganha pontos:
 * +1 para cada empréstimo anterior da mesma categoria e +2 para cada empréstimo anterior
 * de um mesmo autor (autor pesa mais porque é uma afinidade mais específica).
 */
@Service
@RequiredArgsConstructor
public class RecomendacaoService {

    static final int LIMITE = 10;
    private static final int PESO_CATEGORIA = 1;
    private static final int PESO_AUTOR = 2;

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final MaterialRepository materialRepository;

    @Transactional(readOnly = true)
    public List<RecomendacaoResponseDTO> recomendar(Long usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com id " + usuarioId);
        }

        List<Material> historico = emprestimoRepository.findByUsuarioId(usuarioId).stream()
                .map(Emprestimo::getMaterial)
                .toList();
        if (historico.isEmpty()) {
            return List.of();
        }

        // Quantas vezes o usuário pegou cada categoria e cada autor.
        Map<Long, Long> vezesPorCategoria = historico.stream()
                .collect(Collectors.groupingBy(material -> material.getCategoria().getId(), Collectors.counting()));
        Map<Long, Long> vezesPorAutor = historico.stream()
                .flatMap(material -> material.getAutores().stream())
                .collect(Collectors.groupingBy(Autor::getId, Collectors.counting()));
        Set<Long> jaPegou = historico.stream().map(Material::getId).collect(Collectors.toSet());

        return materialRepository.findAllByOrderByTituloAsc().stream()
                .filter(material -> !jaPegou.contains(material.getId()))
                .map(material -> pontuar(material, vezesPorCategoria, vezesPorAutor))
                .filter(recomendacao -> recomendacao.pontuacao() > 0)
                .sorted(Comparator.comparingInt(RecomendacaoResponseDTO::pontuacao).reversed()
                        .thenComparing(recomendacao -> recomendacao.material().titulo()))
                .limit(LIMITE)
                .toList();
    }

    private RecomendacaoResponseDTO pontuar(Material material, Map<Long, Long> vezesPorCategoria,
                                            Map<Long, Long> vezesPorAutor) {
        long pontos = 0;
        List<String> motivos = new ArrayList<>();

        long vezesCategoria = vezesPorCategoria.getOrDefault(material.getCategoria().getId(), 0L);
        if (vezesCategoria > 0) {
            pontos += vezesCategoria * PESO_CATEGORIA;
            motivos.add("mesma categoria (" + material.getCategoria().getNome() + ")");
        }

        List<Autor> autoresConhecidos = material.getAutores().stream()
                .filter(autor -> vezesPorAutor.containsKey(autor.getId()))
                .sorted(Comparator.comparing(Autor::getNome))
                .toList();
        for (Autor autor : autoresConhecidos) {
            pontos += vezesPorAutor.get(autor.getId()) * PESO_AUTOR;
        }
        if (!autoresConhecidos.isEmpty()) {
            motivos.add((autoresConhecidos.size() == 1 ? "mesmo autor (" : "mesmos autores (")
                    + autoresConhecidos.stream().map(Autor::getNome).collect(Collectors.joining(", ")) + ")");
        }

        String motivo = motivos.isEmpty() ? "" : capitalizar(String.join(" e ", motivos))
                + " de materiais que você já pegou";
        return new RecomendacaoResponseDTO(MaterialResponseDTO.de(material), (int) pontos, motivo);
    }

    private String capitalizar(String texto) {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
