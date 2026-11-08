package com.np2.bibliotecavirtual.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.np2.bibliotecavirtual.seguranca.RegrasDeAcesso;
import com.np2.bibliotecavirtual.seguranca.RespostasDeSeguranca;
import com.np2.bibliotecavirtual.seguranca.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Autenticação e permissões.
 * <p>
 * Login com matrícula e senha (POST /auth/login) devolve um token JWT; as outras rotas recebem o token no
 * cabeçalho "Authorization: Bearer ...". Não há sessão no servidor (stateless).
 * <p>
 * Quem pode o quê (a ordem importa: vale a primeira regra que bater com a requisição):
 * <ul>
 *   <li>Sem login: login, Swagger e consulta do catálogo (materiais, categorias, autores, editoras,
 *       avaliações e conquistas).</li>
 *   <li>ALUNO: os próprios empréstimos, leituras, avaliações, perfil de leitor e recomendações; ranking.</li>
 *   <li>PROFESSOR: o mesmo, mais cadastrar o acervo e consultar os dados de qualquer aluno.</li>
 *   <li>BIBLIOTECARIO: tudo, inclusive usuários e empréstimos de qualquer aluno.</li>
 * </ul>
 */
@Configuration
public class SegurancaConfig {

    private static final String PROFESSOR = "PROFESSOR";
    private static final String BIBLIOTECARIO = "BIBLIOTECARIO";
    private static final String[] ACERVO = {
            "/materiais", "/materiais/**", "/categorias", "/categorias/**",
            "/autores", "/autores/**", "/editoras", "/editoras/**"};

    @Bean
    public SecurityFilterChain filtroDeSeguranca(HttpSecurity http, RegrasDeAcesso regras,
                                                 RespostasDeSeguranca respostas) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // API sem cookies: o token vai no cabeçalho
                .sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(rotas -> rotas
                        // Público
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, ACERVO).permitAll()
                        .requestMatchers(HttpMethod.GET, "/avaliacoes", "/avaliacoes/**", "/conquistas").permitAll()

                        // Acervo: cadastrar, editar e excluir é da equipe
                        .requestMatchers(ACERVO).hasAnyRole(PROFESSOR, BIBLIOTECARIO)

                        // Usuários
                        .requestMatchers(HttpMethod.GET, "/usuarios").hasAnyRole(PROFESSOR, BIBLIOTECARIO)
                        .requestMatchers(HttpMethod.GET, "/usuarios/{id}", "/usuarios/{id}/**")
                        .access(regras.proprioUsuarioOuEquipe())
                        .requestMatchers("/usuarios", "/usuarios/**").hasRole(BIBLIOTECARIO)

                        // Empréstimos (o usuarioId do POST é checado no EmprestimoController)
                        .requestMatchers("/emprestimos/atualizar-atrasados").hasRole(BIBLIOTECARIO)
                        .requestMatchers(HttpMethod.GET, "/emprestimos").access(regras.listaDeEmprestimos())
                        .requestMatchers(HttpMethod.POST, "/emprestimos").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/emprestimos/{id}").hasRole(BIBLIOTECARIO)
                        .requestMatchers(HttpMethod.GET, "/emprestimos/{id}", "/emprestimos/{id}/progresso")
                        .access(regras.donoDoEmprestimoOuEquipe())
                        .requestMatchers("/emprestimos/{id}", "/emprestimos/{id}/**")
                        .access(regras.donoDoEmprestimoOuBibliotecario())

                        // Avaliações (consultar já é público; o usuarioId do POST é checado no AvaliacaoController)
                        .requestMatchers(HttpMethod.PUT, "/avaliacoes/{id}").access(regras.autorDaAvaliacao(false))
                        .requestMatchers(HttpMethod.DELETE, "/avaliacoes/{id}").access(regras.autorDaAvaliacao(true))

                        // Todo o resto (ranking, /auth/eu, POST /avaliacoes...): qualquer usuário que entrou
                        .anyRequest().authenticated())
                .oauth2ResourceServer(recurso -> recurso
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(conversorDePerfil()))
                        .authenticationEntryPoint(respostas))
                .exceptionHandling(erros -> erros
                        .authenticationEntryPoint(respostas)
                        .accessDeniedHandler(respostas));
        return http.build();
    }

    /** O claim "perfil" do token vira a autoridade ROLE_ALUNO, ROLE_PROFESSOR ou ROLE_BIBLIOTECARIO. */
    private JwtAuthenticationConverter conversorDePerfil() {
        JwtGrantedAuthoritiesConverter perfis = new JwtGrantedAuthoritiesConverter();
        perfis.setAuthoritiesClaimName(TokenService.CLAIM_PERFIL);
        perfis.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter conversor = new JwtAuthenticationConverter();
        conversor.setJwtGrantedAuthoritiesConverter(perfis);
        return conversor;
    }

    /** Chave secreta do token (HS256 exige pelo menos 32 bytes). Em produção, vem da variável JWT_SECRET. */
    @Bean
    public SecretKey chaveDoToken(@Value("${biblioteca.jwt.segredo}") String segredo) {
        byte[] bytes = segredo.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalStateException("biblioteca.jwt.segredo (JWT_SECRET) precisa ter pelo menos 32 caracteres");
        }
        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey chave) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(chave));
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey chave) {
        return NimbusJwtDecoder.withSecretKey(chave).macAlgorithm(MacAlgorithm.HS256).build();
    }

    /** Senhas guardadas com BCrypt (hash com "sal"; não dá para voltar à senha original). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
