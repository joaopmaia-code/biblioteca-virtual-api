package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.dto.RecomendacaoResponseDTO;
import com.np2.bibliotecavirtual.service.RecomendacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Recomendações", description = "Funcionalidade extra: sugestão de materiais pelo histórico de empréstimos")
public class RecomendacaoController {

    private final RecomendacaoService recomendacaoService;

    @GetMapping("/{id}/recomendacoes")
    @Operation(summary = "Recomendar materiais para um usuário",
            description = "Sugere até 10 materiais que o usuário ainda não pegou, da mesma categoria ou dos mesmos "
                    + "autores dos materiais do seu histórico de empréstimos. Lista vazia se ele nunca pegou nada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Materiais recomendados, do mais ao menos parecido",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RecomendacaoResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<List<RecomendacaoResponseDTO>> recomendar(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        return ResponseEntity.ok(recomendacaoService.recomendar(id));
    }
}
