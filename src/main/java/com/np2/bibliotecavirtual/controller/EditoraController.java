package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.EditoraRequestDTO;
import com.np2.bibliotecavirtual.dto.EditoraResponseDTO;
import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.service.EditoraService;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/editoras")
@RequiredArgsConstructor
@Tag(name = "Editoras", description = "Cadastro das editoras dos materiais")
public class EditoraController {

    private final EditoraService editoraService;

    @PostMapping
    @Operation(summary = "Cadastrar editora", description = "O CNPJ não pode repetir o de outra editora.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Editora cadastrada",
                    content = @Content(schema = @Schema(implementation = EditoraResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Já existe uma editora com esse CNPJ",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<EditoraResponseDTO> cadastrar(@Valid @RequestBody EditoraRequestDTO dto) {
        EditoraResponseDTO criada = editoraService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    @Operation(summary = "Listar editoras", description = "Retorna todas as editoras em ordem alfabética.")
    @ApiResponse(responseCode = "200", description = "Lista de editoras",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = EditoraResponseDTO.class))))
    public ResponseEntity<List<EditoraResponseDTO>> listar() {
        return ResponseEntity.ok(editoraService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar editora por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Editora encontrada",
                    content = @Content(schema = @Schema(implementation = EditoraResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Editora não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<EditoraResponseDTO> buscarPorId(
            @Parameter(description = "ID da editora") @PathVariable Long id) {
        return ResponseEntity.ok(editoraService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar editora", description = "Substitui todos os dados da editora.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Editora atualizada",
                    content = @Content(schema = @Schema(implementation = EditoraResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Editora não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Já existe outra editora com esse CNPJ",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<EditoraResponseDTO> atualizar(
            @Parameter(description = "ID da editora") @PathVariable Long id,
            @Valid @RequestBody EditoraRequestDTO dto) {
        return ResponseEntity.ok(editoraService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir editora", description = "Não exclui editora vinculada a materiais.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Editora excluída"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Editora não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Editora vinculada a materiais",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da editora") @PathVariable Long id) {
        editoraService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
