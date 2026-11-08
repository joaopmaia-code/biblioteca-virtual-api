package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.LoginRequestDTO;
import com.np2.bibliotecavirtual.dto.LoginResponseDTO;
import com.np2.bibliotecavirtual.dto.TrocaSenhaRequestDTO;
import com.np2.bibliotecavirtual.dto.UsuarioResponseDTO;
import com.np2.bibliotecavirtual.exception.CredenciaisInvalidasException;
import com.np2.bibliotecavirtual.exception.RecursoNaoEncontradoException;
import com.np2.bibliotecavirtual.exception.RequisicaoInvalidaException;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import com.np2.bibliotecavirtual.seguranca.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login (RN18) e troca da própria senha.
 */
@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private static final String CREDENCIAIS_INVALIDAS = "Matrícula ou senha inválidas";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    /**
     * Confere a senha com o hash BCrypt guardado e devolve um token JWT. Matrícula inexistente, usuário
     * sem senha e senha errada dão a mesma mensagem, para não revelar quais matrículas existem.
     */
    @Transactional(readOnly = true)
    public LoginResponseDTO entrar(LoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByMatricula(dto.matricula().trim())
                .filter(u -> u.getSenhaHash() != null && passwordEncoder.matches(dto.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new CredenciaisInvalidasException(CREDENCIAIS_INVALIDAS));

        return new LoginResponseDTO(tokenService.gerar(usuario), "Bearer", tokenService.validadeEmSegundos(),
                UsuarioResponseDTO.de(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO dadosDe(Long usuarioId) {
        return UsuarioResponseDTO.de(buscar(usuarioId));
    }

    @Transactional
    public void trocarSenha(Long usuarioId, TrocaSenhaRequestDTO dto) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.getSenhaHash() == null || !passwordEncoder.matches(dto.senhaAtual(), usuario.getSenhaHash())) {
            throw new RequisicaoInvalidaException("A senha atual está incorreta");
        }
        usuario.setSenhaHash(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado com id " + id));
    }
}
