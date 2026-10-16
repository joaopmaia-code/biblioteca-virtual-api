package com.np2.bibliotecavirtual;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Sobe o contexto completo do Spring: confirma que o Flyway roda as migrations e que o
 * Hibernate (ddl-auto=validate) aceita o schema. Precisa do MySQL do docker-compose rodando.
 */
@SpringBootTest
class BibliotecaVirtualApplicationTests {

    @Test
    void contextLoads() {
    }
}
