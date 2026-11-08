package com.np2.bibliotecavirtual.config;

import com.np2.bibliotecavirtual.model.PerfilUsuario;
import com.np2.bibliotecavirtual.model.Usuario;
import com.np2.bibliotecavirtual.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Só o bibliotecário cadastra usuários. Para o primeiro acesso não ficar impossível, se o banco não tem
 * nenhum bibliotecário, este cadastra um ao subir a aplicação (matrícula e senha das variáveis
 * ADMIN_MATRICULA e ADMIN_SENHA; padrão admin / admin123). Roda depois do seed do perfil demo.
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class BibliotecarioInicial implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${biblioteca.admin.matricula}")
    private String matricula;

    @Value("${biblioteca.admin.senha}")
    private String senha;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByPerfil(PerfilUsuario.BIBLIOTECARIO)) {
            return;
        }
        if (usuarioRepository.existsByMatricula(matricula)) {
            log.warn("Não há bibliotecário e a matrícula '{}' já é de outro usuário; defina ADMIN_MATRICULA", matricula);
            return;
        }

        Usuario bibliotecario = new Usuario();
        bibliotecario.setNome("Bibliotecário(a)");
        bibliotecario.setEmail(matricula.toLowerCase() + "@biblioteca.local");
        bibliotecario.setMatricula(matricula);
        bibliotecario.setPerfil(PerfilUsuario.BIBLIOTECARIO);
        bibliotecario.setSenhaHash(passwordEncoder.encode(senha));
        usuarioRepository.save(bibliotecario);
        log.warn("Criado o bibliotecário inicial (matrícula '{}'). Troque a senha em PUT /auth/senha.", matricula);
    }
}
