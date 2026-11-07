package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.UsuarioRequestDTO;
import com.np2.bibliotecavirtual.dto.UsuarioResponseDTO;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RegraNegocioException;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.EmprestimoRepository;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EmprestimoRepository emprestimoRepository;

    @Transactional
    public UsuarioResponseDTO cadastrar(UsuarioRequestDTO dto) {
        String email = normalizarEmail(dto.email());
        String matricula = dto.matricula().trim();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Já existe um usuário com o e-mail " + email);
        }
        if (usuarioRepository.existsByMatricula(matricula)) {
            throw new RegraNegocioException("Já existe um usuário com a matrícula " + matricula);
        }

        Usuario usuario = new Usuario();
        preencherDados(usuario, dto, email, matricula);
        return UsuarioResponseDTO.de(usuarioRepository.save(usuario));
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll(Sort.by("nome")).stream()
                .map(UsuarioResponseDTO::de)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return UsuarioResponseDTO.de(buscarUsuario(id));
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = buscarUsuario(id);

        String email = normalizarEmail(dto.email());
        String matricula = dto.matricula().trim();
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new RegraNegocioException("Já existe outro usuário com o e-mail " + email);
        }
        if (usuarioRepository.existsByMatriculaAndIdNot(matricula, id)) {
            throw new RegraNegocioException("Já existe outro usuário com a matrícula " + matricula);
        }

        preencherDados(usuario, dto, email, matricula);
        return UsuarioResponseDTO.de(usuarioRepository.save(usuario));
    }

    /** Não exclui usuário com histórico de empréstimos, para não perder o registro. */
    @Transactional
    public void excluir(Long id) {
        Usuario usuario = buscarUsuario(id);
        if (emprestimoRepository.existsByUsuarioId(id)) {
            throw new RegraNegocioException("Não é possível excluir o usuário porque ele tem empréstimos registrados");
        }
        usuarioRepository.delete(usuario);
    }

    /** Usado pelo EmprestimoService (e pela Reserva) para buscar o usuário ou devolver 404. */
    public Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com id " + id));
    }

    private void preencherDados(Usuario usuario, UsuarioRequestDTO dto, String email, String matricula) {
        usuario.setNome(dto.nome().trim());
        usuario.setEmail(email);
        usuario.setMatricula(matricula);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
