package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.AutorRequestDTO;
import com.np2.bibliotecavirtual.dto.AutorResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.model.Autor;
import com.np2.bibliotecavirtual.repository.AutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AutorService {

    private final AutorRepository autorRepository;

    @Transactional
    public AutorResponseDTO cadastrar(AutorRequestDTO dto) {
        Autor autor = new Autor();
        preencherDados(autor, dto);
        return AutorResponseDTO.de(autorRepository.save(autor));
    }

    @Transactional(readOnly = true)
    public List<AutorResponseDTO> listar() {
        return autorRepository.findAll(Sort.by("nome")).stream()
                .map(AutorResponseDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public AutorResponseDTO buscarPorId(Long id) {
        return AutorResponseDTO.de(buscarAutor(id));
    }

    @Transactional
    public AutorResponseDTO atualizar(Long id, AutorRequestDTO dto) {
        Autor autor = buscarAutor(id);
        preencherDados(autor, dto);
        return AutorResponseDTO.de(autorRepository.save(autor));
    }

    // RN06 (não excluir autor vinculado a material) entra quando a tabela material_autor existir (V22).
    @Transactional
    public void excluir(Long id) {
        Autor autor = buscarAutor(id);
        autorRepository.delete(autor);
    }

    private Autor buscarAutor(Long id) {
        return autorRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Autor não encontrado com id " + id));
    }

    private void preencherDados(Autor autor, AutorRequestDTO dto) {
        autor.setNome(dto.nome().trim());
        autor.setNacionalidade(dto.nacionalidade());
        autor.setDataNascimento(dto.dataNascimento());
        autor.setBiografia(dto.biografia());
    }
}
