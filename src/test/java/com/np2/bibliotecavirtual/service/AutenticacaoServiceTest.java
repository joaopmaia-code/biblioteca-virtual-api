package com.np2.bibliotecavirtual.service;

import com.np2.bibliotecavirtual.dto.LoginRequestDTO;
import com.np2.bibliotecavirtual.dto.LoginResponseDTO;
import com.np2.bibliotecavirtual.dto.TrocaSenhaRequestDTO;
import com.np2.bibliotecavirtual.exception.CredenciaisInvalidasException;
import com.np2.bibliotecavirtual.exception.RequisicaoInvalidaException;
import com.np2.bibliotecavirtual.model.PerfilUsuario;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import com.np2.bibliotecavirtual.seguranca.TokenService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Testa o login e a troca de senha com BCrypt e JWT de verdade (só o repositório é simulado).
 */
@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    private static final SecretKey CHAVE = new SecretKeySpec(
            "chave-de-teste-com-mais-de-32-caracteres!!".getBytes(StandardCharsets.UTF_8), "HmacSHA256");

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private AutenticacaoService autenticacaoService;
    private Usuario ana;

    @BeforeEach
    void montar() {
        TokenService tokenService = new TokenService(new NimbusJwtEncoder(new ImmutableSecret<>(CHAVE)), 8);
        autenticacaoService = new AutenticacaoService(usuarioRepository, passwordEncoder, tokenService);

        ana = new Usuario();
        ana.setId(7L);
        ana.setNome("Ana Souza");
        ana.setMatricula("2026A0142");
        ana.setPerfil(PerfilUsuario.ALUNO);
        ana.setSenhaHash(passwordEncoder.encode("senha123"));
    }

    @Test
    void loginCertoDevolveTokenAssinadoComIdEPerfil() {
        when(usuarioRepository.findByMatricula("2026A0142")).thenReturn(Optional.of(ana));

        LoginResponseDTO resposta = autenticacaoService.entrar(new LoginRequestDTO(" 2026A0142 ", "senha123"));

        Jwt token = NimbusJwtDecoder.withSecretKey(CHAVE).macAlgorithm(MacAlgorithm.HS256).build().decode(resposta.token());
        assertThat(token.getSubject()).isEqualTo("7");
        assertThat(token.getClaimAsString("perfil")).isEqualTo("ALUNO");
        assertThat(resposta.tipo()).isEqualTo("Bearer");
        assertThat(resposta.expiraEmSegundos()).isEqualTo(8 * 3600);
        assertThat(resposta.usuario().perfil()).isEqualTo(PerfilUsuario.ALUNO);
    }

    @Test
    void senhaErradaEMatriculaInexistenteDaoAMesmaMensagem() {
        when(usuarioRepository.findByMatricula("2026A0142")).thenReturn(Optional.of(ana));
        when(usuarioRepository.findByMatricula("nao-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> autenticacaoService.entrar(new LoginRequestDTO("2026A0142", "errada")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("Matrícula ou senha inválidas");
        assertThatThrownBy(() -> autenticacaoService.entrar(new LoginRequestDTO("nao-existe", "senha123")))
                .isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("Matrícula ou senha inválidas");
    }

    @Test
    void usuarioSemSenhaNaoEntra() {
        ana.setSenhaHash(null);
        when(usuarioRepository.findByMatricula("2026A0142")).thenReturn(Optional.of(ana));

        assertThatThrownBy(() -> autenticacaoService.entrar(new LoginRequestDTO("2026A0142", "senha123")))
                .isInstanceOf(CredenciaisInvalidasException.class);
    }

    @Test
    void trocarSenhaExigeASenhaAtualCorreta() {
        when(usuarioRepository.findById(7L)).thenReturn(Optional.of(ana));

        assertThatThrownBy(() -> autenticacaoService.trocarSenha(7L, new TrocaSenhaRequestDTO("errada", "novaSenha1")))
                .isInstanceOf(RequisicaoInvalidaException.class);

        autenticacaoService.trocarSenha(7L, new TrocaSenhaRequestDTO("senha123", "novaSenha1"));
        assertThat(passwordEncoder.matches("novaSenha1", ana.getSenhaHash())).isTrue();
    }
}
