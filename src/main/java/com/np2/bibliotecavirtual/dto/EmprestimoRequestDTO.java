package com.np2.bibliotecavirtual.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Dados recebidos para registrar um empréstimo. A data de retirada é gerada pelo sistema.
 */
@Schema(description = "Dados para registrar um empréstimo")
public record EmprestimoRequestDTO(

        @Schema(description = "ID do usuário", example = "1")
        @NotNull(message = "O usuário é obrigatório")
        Long usuarioId,

        @Schema(description = "ID do material", example = "1")
        @NotNull(message = "O material é obrigatório")
        Long materialId,

        @Schema(description = "Data prevista de devolução (opcional; se omitida, usa o prazo padrão de 14 dias)",
                example = "2026-11-20")
        @Future(message = "A data prevista de devolução deve ser depois de hoje")
        LocalDate dataPrevistaDevolucao
) {
}
