package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.AvaliacaoAtualizacaoDTO;
import com.np2.bibliotecavirtual.dto.AvaliacaoRequestDTO;
import com.np2.bibliotecavirtual.dto.AvaliacaoResponseDTO;
import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.seguranca.UsuarioLogado;
import com.np2.bibliotecavirtual.service.AvaliacaoService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/avaliacoes")
@RequiredArgsConstructor
@Tag(name = "Avaliações", description = "Notas de 1 a 5 e comentários dos alunos sobre os materiais que já pegaram emprestado")
public class AvaliacaoController {

    private final AvaliacaoService avaliacaoService;
    private final UsuarioLogado usuarioLogado;

    @PostMapping
    @Operation(summary = "Avaliar material",
            description = "Só avalia quem já pegou o material emprestado, e uma vez só por material. "
                    + "A média aparece em GET /materiais/{id}.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Avaliação registrada",
                    content = @Content(schema = @Schema(implementation = AvaliacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos (ex.: nota fora de 1 a 5)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário ou material não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "O usuário nunca pegou o material ou já o avaliou",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<AvaliacaoResponseDTO> cadastrar(@Valid @RequestBody AvaliacaoRequestDTO dto) {
        // Ninguém avalia em nome de outro, nem o bibliotecário (403).
        usuarioLogado.garantirQueEhOProprio(dto.usuarioId());
        AvaliacaoResponseDTO criada = avaliacaoService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    @Operation(summary = "Listar avaliações",
            description = "Mais recentes primeiro. Filtros opcionais por material e por usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de avaliações",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = AvaliacaoResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Filtro em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<List<AvaliacaoResponseDTO>> listar(
            @Parameter(description = "Só as avaliações deste material", example = "1")
            @RequestParam(required = false) Long materialId,
            @Parameter(description = "Só as avaliações deste usuário", example = "1")
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(avaliacaoService.listar(materialId, usuarioId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar avaliação por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação encontrada",
                    content = @Content(schema = @Schema(implementation = AvaliacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<AvaliacaoResponseDTO> buscarPorId(
            @Parameter(description = "ID da avaliação", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(avaliacaoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar avaliação", description = "Troca a nota e o comentário; aluno e material não mudam.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Avaliação atualizada",
                    content = @Content(schema = @Schema(implementation = AvaliacaoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<AvaliacaoResponseDTO> atualizar(
            @Parameter(description = "ID da avaliação", example = "1") @PathVariable Long id,
            @Valid @RequestBody AvaliacaoAtualizacaoDTO dto) {
        return ResponseEntity.ok(avaliacaoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir avaliação")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Avaliação excluída"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Avaliação não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da avaliação", example = "1") @PathVariable Long id) {
        avaliacaoService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
