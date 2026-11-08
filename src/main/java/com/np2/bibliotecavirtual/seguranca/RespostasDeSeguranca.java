package com.np2.bibliotecavirtual.seguranca;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Respostas 401 e 403 dadas pelo Spring Security antes de chegar ao controller, no mesmo formato
 * de erro do GlobalExceptionHandler (ErroResponseDTO).
 */
@Component
@RequiredArgsConstructor
public class RespostasDeSeguranca implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /** Sem token, ou token inválido/expirado → 401. */
    @Override
    public void commence(HttpServletRequest requisicao, HttpServletResponse resposta, AuthenticationException ex)
            throws IOException {
        String mensagem = requisicao.getHeader("Authorization") != null
                ? "Token inválido ou expirado. Entre de novo em POST /auth/login"
                : "É preciso entrar (POST /auth/login) para acessar esta rota";
        escrever(resposta, ErroResponseDTO.de(HttpStatus.UNAUTHORIZED, mensagem));
    }

    /** Entrou, mas o perfil não permite → 403. */
    @Override
    public void handle(HttpServletRequest requisicao, HttpServletResponse resposta, AccessDeniedException ex)
            throws IOException {
        escrever(resposta, ErroResponseDTO.de(HttpStatus.FORBIDDEN, "Você não tem permissão para esta ação"));
    }

    private void escrever(HttpServletResponse resposta, ErroResponseDTO erro) throws IOException {
        resposta.setStatus(erro.status());
        resposta.setContentType(MediaType.APPLICATION_JSON_VALUE);
        resposta.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(resposta.getOutputStream(), erro);
    }
}
