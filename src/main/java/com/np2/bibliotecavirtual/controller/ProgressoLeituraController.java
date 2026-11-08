package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.dto.ProgressoLeituraRequestDTO;
import com.np2.bibliotecavirtual.dto.ProgressoLeituraResponseDTO;
import com.np2.bibliotecavirtual.service.ProgressoLeituraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Rotas do progresso de leitura. Ficam neste controller (e não no EmprestimoController)
 * para a funcionalidade nova não alterar o código de Empréstimo.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Progresso de leitura", description = "Página em que o aluno parou em cada material emprestado e o percentual lido")
public class ProgressoLeituraController {

    private final ProgressoLeituraService progressoService;

    @PutMapping("/emprestimos/{id}/progresso")
    @Operation(summary = "Registrar progresso de leitura",
            description = "Grava a página em que o aluno parou (cria na primeira vez, atualiza nas seguintes) "
                    + "e devolve o percentual lido. Só vale para empréstimos ATIVOS ou ATRASADOS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progresso registrado",
                    content = @Content(schema = @Schema(implementation = ProgressoLeituraResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Página ausente ou menor que 1",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Empréstimo já devolvido, material sem número de páginas "
                    + "ou página maior que o total",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<ProgressoLeituraResponseDTO> registrar(
            @Parameter(description = "ID do empréstimo", example = "1") @PathVariable Long id,
            @Valid @RequestBody ProgressoLeituraRequestDTO dto) {
        return ResponseEntity.ok(progressoService.registrar(id, dto));
    }

    @GetMapping("/emprestimos/{id}/progresso")
    @Operation(summary = "Consultar progresso de leitura",
            description = "Página atual e percentual lido do empréstimo; página 0 se o aluno ainda não registrou nenhuma.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progresso do empréstimo",
                    content = @Content(schema = @Schema(implementation = ProgressoLeituraResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<ProgressoLeituraResponseDTO> consultar(
            @Parameter(description = "ID do empréstimo", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(progressoService.consultar(id));
    }

    @GetMapping("/usuarios/{id}/leituras")
    @Operation(summary = "Leituras em andamento do usuário",
            description = "Os materiais que estão com o usuário agora (empréstimos ATIVOS ou ATRASADOS), "
                    + "com o progresso de cada um. Lista vazia se ele não está com nenhum.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Leituras em andamento, retiradas mais recentemente primeiro",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProgressoLeituraResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<List<ProgressoLeituraResponseDTO>> leiturasDoUsuario(
            @Parameter(description = "ID do usuário", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(progressoService.leiturasDoUsuario(id));
    }
}
