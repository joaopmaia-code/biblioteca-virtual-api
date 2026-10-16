package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.CategoriaRequestDTO;
import com.np2.bibliotecavirtual.dto.CategoriaResponseDTO;
import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.service.CategoriaService;
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
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Cadastro das categorias (assuntos) dos materiais")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @PostMapping
    @Operation(summary = "Cadastrar categoria", description = "O nome não pode repetir o de outra categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Categoria cadastrada",
                    content = @Content(schema = @Schema(implementation = CategoriaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Já existe uma categoria com esse nome",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<CategoriaResponseDTO> cadastrar(@Valid @RequestBody CategoriaRequestDTO dto) {
        CategoriaResponseDTO criada = categoriaService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criada.id())
                .toUri();
        return ResponseEntity.created(location).body(criada);
    }

    @GetMapping
    @Operation(summary = "Listar categorias", description = "Retorna todas as categorias em ordem alfabética.")
    @ApiResponse(responseCode = "200", description = "Lista de categorias",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = CategoriaResponseDTO.class))))
    public ResponseEntity<List<CategoriaResponseDTO>> listar() {
        return ResponseEntity.ok(categoriaService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar categoria por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada",
                    content = @Content(schema = @Schema(implementation = CategoriaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<CategoriaResponseDTO> buscarPorId(
            @Parameter(description = "ID da categoria", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Substitui todos os dados da categoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada",
                    content = @Content(schema = @Schema(implementation = CategoriaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Já existe outra categoria com esse nome",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<CategoriaResponseDTO> atualizar(
            @Parameter(description = "ID da categoria", example = "1") @PathVariable Long id,
            @Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.ok(categoriaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria excluída"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da categoria", example = "1") @PathVariable Long id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
