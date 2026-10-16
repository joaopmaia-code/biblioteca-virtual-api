package com.np2.bibliotecavirtual.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Formato único de erro devolvido por toda a API (montado pelo GlobalExceptionHandler).
 * O campo "campos" só aparece quando há erros de validação de campos.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta padrão de erro da API")
public record ErroResponseDTO(

        @Schema(description = "Momento em que o erro aconteceu", example = "2026-10-16T15:30:00")
        LocalDateTime timestamp,

        @Schema(description = "Código HTTP", example = "404")
        int status,

        @Schema(description = "Nome do status HTTP", example = "Not Found")
        String erro,

        @Schema(description = "Explicação do erro", example = "Categoria não encontrada com id 99")
        String mensagem,

        @Schema(description = "Erros por campo (apenas em erros de validação)")
        List<CampoErroDTO> campos
) {

    public static ErroResponseDTO de(HttpStatus status, String mensagem) {
        return de(status, mensagem, null);
    }

    public static ErroResponseDTO de(HttpStatus status, String mensagem, List<CampoErroDTO> campos) {
        return new ErroResponseDTO(LocalDateTime.now(), status.value(), status.getReasonPhrase(), mensagem, campos);
    }
}
