package com.np2.bibliotecavirtual.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Informações gerais exibidas no topo do Swagger UI (/swagger-ui.html).
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bibliotecaVirtualOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca Virtual API")
                        .version("v1")
                        .description("API REST da biblioteca virtual de uma escola de ensino médio (NP2 - Tópicos Web)."));
    }
}
