package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.EditoraRequestDTO;
import com.np2.bibliotecavirtual.dto.EditoraResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Editora;
import com.np2.bibliotecavirtual.repository.EditoraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EditoraService {

    private final EditoraRepository editoraRepository;

    @Transactional
    public EditoraResponseDTO cadastrar(EditoraRequestDTO dto) {
        String cnpj = somenteDigitos(dto.cnpj());
        if (editoraRepository.existsByCnpj(cnpj)) {
            throw new RegraNegocioException("Já existe uma editora com o CNPJ " + cnpj);
        }

        Editora editora = new Editora();
        preencherDados(editora, dto, cnpj);
        return EditoraResponseDTO.de(editoraRepository.save(editora));
    }

    @Transactional(readOnly = true)
    public List<EditoraResponseDTO> listar() {
        return editoraRepository.findAll(Sort.by("nome")).stream()
                .map(EditoraResponseDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public EditoraResponseDTO buscarPorId(Long id) {
        return EditoraResponseDTO.de(buscarEditora(id));
    }

    @Transactional
    public EditoraResponseDTO atualizar(Long id, EditoraRequestDTO dto) {
        Editora editora = buscarEditora(id);

        String cnpj = somenteDigitos(dto.cnpj());
        if (editoraRepository.existsByCnpjAndIdNot(cnpj, id)) {
            throw new RegraNegocioException("Já existe outra editora com o CNPJ " + cnpj);
        }

        preencherDados(editora, dto, cnpj);
        return EditoraResponseDTO.de(editoraRepository.save(editora));
    }

    // RN06 (não excluir editora vinculada a material) entra quando materiais.editora_id existir (V22).
    @Transactional
    public void excluir(Long id) {
        Editora editora = buscarEditora(id);
        editoraRepository.delete(editora);
    }

    private Editora buscarEditora(Long id) {
        return editoraRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Editora não encontrada com id " + id));
    }

    private void preencherDados(Editora editora, EditoraRequestDTO dto, String cnpj) {
        editora.setNome(dto.nome().trim());
        editora.setCnpj(cnpj);
        editora.setCidade(dto.cidade().trim());
        editora.setSite(dto.site() == null || dto.site().isBlank() ? null : dto.site().trim());
    }

    /** "12.345.678/0001-95" e "12345678000195" viram o mesmo valor, para a checagem de CNPJ único valer. */
    private String somenteDigitos(String cnpj) {
        return cnpj.replaceAll("\\D", "");
    }
}
