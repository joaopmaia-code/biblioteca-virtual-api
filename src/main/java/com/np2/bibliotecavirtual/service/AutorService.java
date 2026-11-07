package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.AutorRequestDTO;
import com.np2.bibliotecavirtual.dto.AutorResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Autor;
import com.np2.bibliotecavirtual.repository.AutorRepository;
import com.np2.bibliotecavirtual.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AutorService {

    private final AutorRepository autorRepository;
    private final MaterialRepository materialRepository;

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

    @Transactional
    public void excluir(Long id) {
        Autor autor = buscarAutor(id);

        // RN06: não excluir autor que ainda assina materiais.
        if (materialRepository.existsByAutoresId(id)) {
            throw new RegraNegocioException("Não é possível excluir o autor '" + autor.getNome()
                    + "': existem materiais vinculados a ele");
        }

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
