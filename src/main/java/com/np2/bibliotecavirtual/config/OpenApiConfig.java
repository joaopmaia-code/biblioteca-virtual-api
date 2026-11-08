package com.np2.bibliotecavirtual.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Informações do Swagger UI (/swagger-ui.html) e o botão "Authorize" para usar o token do login.
 */
@Configuration
public class OpenApiConfig {

    private static final String ESQUEMA_TOKEN = "token";

    @Bean
    public OpenAPI bibliotecaVirtualOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Biblioteca Virtual API")
                        .version("v1")
                        .description("API REST da biblioteca virtual de uma escola de ensino médio (NP2 - Tópicos Web).\n\n"
                                + "**Como usar:** faça POST /auth/login, copie o `token` da resposta, clique em "
                                + "**Authorize** e cole o token. Consultar o acervo e as avaliações não exige login. "
                                + "Perfis: ALUNO, PROFESSOR e BIBLIOTECARIO."))
                .components(new Components().addSecuritySchemes(ESQUEMA_TOKEN, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token devolvido por POST /auth/login (cole só o token, sem 'Bearer')")))
                .addSecurityItem(new SecurityRequirement().addList(ESQUEMA_TOKEN));
    }

    /** Documenta 401 e 403 em todas as rotas que exigem token, com o formato padrão de erro. */
    @Bean
    public OpenApiCustomizer documentarErros401e403() {
        return openApi -> openApi.getPaths().forEach((caminho, item) -> item.readOperations().forEach(operacao -> {
            if (operacao.getSecurity() != null && operacao.getSecurity().isEmpty()) {
                return; // rota pública (ex.: login)
            }
            operacao.getResponses().putIfAbsent("401", erro("Sem token, ou token inválido ou expirado"));
            operacao.getResponses().putIfAbsent("403", erro("O perfil do usuário não permite esta ação"));
        }));
    }

    private ApiResponse erro(String descricao) {
        return new ApiResponse().description(descricao).content(new Content().addMediaType("application/json",
                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErroResponseDTO"))));
    }
}
