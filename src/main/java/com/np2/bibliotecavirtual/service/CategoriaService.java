package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.CategoriaRequestDTO;
import com.np2.bibliotecavirtual.dto.CategoriaResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Categoria;
import com.np2.bibliotecavirtual.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    @Transactional
    public CategoriaResponseDTO cadastrar(CategoriaRequestDTO dto) {
        String nome = dto.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new RegraNegocioException("Já existe uma categoria com o nome '" + nome + "'");
        }

        Categoria categoria = new Categoria();
        preencherDados(categoria, dto);
        return CategoriaResponseDTO.de(categoriaRepository.save(categoria));
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listar() {
        return categoriaRepository.findAll(Sort.by("nome")).stream()
                .map(CategoriaResponseDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {
        return CategoriaResponseDTO.de(buscarCategoria(id));
    }

    @Transactional
    public CategoriaResponseDTO atualizar(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = buscarCategoria(id);

        String nome = dto.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new RegraNegocioException("Já existe uma categoria com o nome '" + nome + "'");
        }

        preencherDados(categoria, dto);
        return CategoriaResponseDTO.de(categoriaRepository.save(categoria));
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarCategoria(id);
        categoriaRepository.delete(categoria);
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id " + id));
    }

    private void preencherDados(Categoria categoria, CategoriaRequestDTO dto) {
        categoria.setNome(dto.nome().trim());
        categoria.setDescricao(dto.descricao());
        categoria.setAreaConhecimento(dto.areaConhecimento().trim());
        categoria.setSerieRecomendada(dto.serieRecomendada());
    }
}
