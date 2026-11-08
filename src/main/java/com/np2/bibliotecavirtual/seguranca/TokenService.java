package com.np2.bibliotecavirtual.seguranca;

import com.np2.bibliotecavirtual.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Gera o token JWT devolvido no login. O token leva o ID do usuário (subject), o nome, a matrícula
 * e o perfil, e é assinado com a chave secreta da aplicação (HS256): quem altera o token invalida a assinatura.
 */
@Service
public class TokenService {

    public static final String CLAIM_PERFIL = "perfil";

    private final JwtEncoder jwtEncoder;
    private final Duration validade;

    public TokenService(JwtEncoder jwtEncoder, @Value("${biblioteca.jwt.validade-horas:8}") long validadeHoras) {
        this.jwtEncoder = jwtEncoder;
        this.validade = Duration.ofHours(validadeHoras);
    }

    public String gerar(Usuario usuario) {
        Instant agora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("biblioteca-virtual")
                .issuedAt(agora)
                .expiresAt(agora.plus(validade))
                .subject(String.valueOf(usuario.getId()))
                .claim("nome", usuario.getNome())
                .claim("matricula", usuario.getMatricula())
                .claim(CLAIM_PERFIL, usuario.getPerfil().name())
                .build();
        JwsHeader cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(cabecalho, claims)).getTokenValue();
    }

    public long validadeEmSegundos() {
        return validade.toSeconds();
    }
}
