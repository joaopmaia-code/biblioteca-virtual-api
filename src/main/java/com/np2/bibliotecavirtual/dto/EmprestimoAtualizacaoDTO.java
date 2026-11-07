package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Dados recebidos para editar um empréstimo: só o prazo pode mudar (renovação).
 * Usuário e material não mudam; para isso, exclua e registre outro empréstimo.
 */
@Schema(description = "Dados para editar (renovar) um empréstimo")
public record EmprestimoAtualizacaoDTO(

        @Schema(description = "Nova data prevista de devolução", example = "2026-11-30")
        @NotNull(message = "A data prevista de devolução é obrigatória")
        @Future(message = "A data prevista de devolução deve ser depois de hoje")
        LocalDate dataPrevistaDevolucao
) {
}
