package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.MaterialFiltroDTO;
import com.np2.bibliotecavirtual.dto.MaterialRequestDTO;
import com.np2.bibliotecavirtual.dto.MaterialResponseDTO;
import com.np2.bibliotecavirtual.dto.OrdenacaoMaterial;
import com.np2.bibliotecavirtual.dto.PaginaResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.model.Autor;
import com.np2.bibliotecavirtual.model.Categoria;
import com.np2.bibliotecavirtual.model.Editora;
import com.np2.bibliotecavirtual.model.Material;
import com.np2.bibliotecavirtual.repository.AutorRepository;
import com.np2.bibliotecavirtual.repository.CategoriaRepository;
import com.np2.bibliotecavirtual.repository.EditoraRepository;
import com.np2.bibliotecavirtual.repository.MaterialFiltros;
import com.np2.bibliotecavirtual.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final CategoriaRepository categoriaRepository;
    private final EditoraRepository editoraRepository;
    private final AutorRepository autorRepository;

    @Transactional
    public MaterialResponseDTO cadastrar(MaterialRequestDTO dto) {
        Material material = new Material();
        preencherDados(material, dto);
        return MaterialResponseDTO.de(materialRepository.save(material));
    }

    @Transactional(readOnly = true)
    public List<MaterialResponseDTO> listar() {
        return materialRepository.findAllByOrderByTituloAsc().stream()
                .map(MaterialResponseDTO::de)
                .toList();
    }

    /** Busca no acervo: filtros opcionais combinados, ordenação e paginação. */
    @Transactional(readOnly = true)
    public PaginaResponseDTO<MaterialResponseDTO> buscar(MaterialFiltroDTO filtro, OrdenacaoMaterial ordenacao,
                                                         int pagina, int tamanho) {
        Page<Material> resultado = materialRepository.findAll(MaterialFiltros.de(filtro),
                PageRequest.of(pagina, tamanho, ordenacao.getSort()));
        return PaginaResponseDTO.de(resultado, MaterialResponseDTO::de);
    }

    @Transactional(readOnly = true)
    public MaterialResponseDTO buscarPorId(Long id) {
        return MaterialResponseDTO.de(buscarMaterial(id));
    }

    @Transactional
    public MaterialResponseDTO atualizar(Long id, MaterialRequestDTO dto) {
        Material material = buscarMaterial(id);
        preencherDados(material, dto);
        return MaterialResponseDTO.de(materialRepository.save(material));
    }

    // Ao excluir, o Hibernate apaga antes as linhas do material em material_autor e o banco apaga as
    // avaliações (ON DELETE CASCADE). Material com empréstimos é recusado pela chave estrangeira (409).
    @Transactional
    public void excluir(Long id) {
        Material material = buscarMaterial(id);
        materialRepository.delete(material);
    }

    private Material buscarMaterial(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material não encontrado com id " + id));
    }

    private void preencherDados(Material material, MaterialRequestDTO dto) {
        material.setTitulo(dto.titulo().trim());
        material.setTipo(dto.tipo());
        material.setAnoPublicacao(dto.anoPublicacao());
        material.setSinopse(dto.sinopse() == null || dto.sinopse().isBlank() ? null : dto.sinopse().trim());
        material.setUrlArquivo(dto.urlArquivo().trim());
        material.setNumeroPaginas(dto.numeroPaginas());
        material.setQuantidadeLicencas(dto.quantidadeLicencas());
        material.setCategoria(buscarCategoria(dto.categoriaId()));
        material.setEditora(dto.editoraId() == null ? null : buscarEditora(dto.editoraId()));
        material.setAutores(buscarAutores(dto.autoresIds()));
    }

    private Categoria buscarCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id " + id));
    }

    private Editora buscarEditora(Long id) {
        return editoraRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Editora não encontrada com id " + id));
    }

    /** Busca todos os autores numa consulta só; se algum ID não existir, avisa quais faltaram. */
    private Set<Autor> buscarAutores(List<Long> ids) {
        Set<Long> idsSemRepeticao = new LinkedHashSet<>(ids);
        List<Autor> encontrados = autorRepository.findAllById(idsSemRepeticao);

        if (encontrados.size() != idsSemRepeticao.size()) {
            encontrados.forEach(autor -> idsSemRepeticao.remove(autor.getId()));
            String faltando = idsSemRepeticao.stream().map(String::valueOf).collect(Collectors.joining(", "));
            throw new RecursoNaoEncontradoException("Autor não encontrado com id " + faltando);
        }
        return new HashSet<>(encontrados);
    }
}
