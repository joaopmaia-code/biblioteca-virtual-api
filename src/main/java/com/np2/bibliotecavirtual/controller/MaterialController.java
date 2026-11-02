package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.dto.MaterialRequestDTO;
import com.np2.bibliotecavirtual.dto.MaterialResponseDTO;
import com.np2.bibliotecavirtual.service.MaterialService;
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
@RequestMapping("/materiais")
@RequiredArgsConstructor
@Tag(name = "Materiais", description = "Cadastro do acervo (livros, apostilas, artigos e vídeos) com categoria, editora e autores")
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping
    @Operation(summary = "Cadastrar material",
            description = "Categoria e autores são obrigatórios e a editora é opcional; todos são informados pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Material cadastrado",
                    content = @Content(schema = @Schema(implementation = MaterialResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Categoria, editora ou autor informado não existe",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<MaterialResponseDTO> cadastrar(@Valid @RequestBody MaterialRequestDTO dto) {
        MaterialResponseDTO criado = materialService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar materiais", description = "Retorna todos os materiais em ordem alfabética de título.")
    @ApiResponse(responseCode = "200", description = "Lista de materiais",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MaterialResponseDTO.class))))
    public ResponseEntity<List<MaterialResponseDTO>> listar() {
        return ResponseEntity.ok(materialService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar material por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Material encontrado",
                    content = @Content(schema = @Schema(implementation = MaterialResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Material não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<MaterialResponseDTO> buscarPorId(
            @Parameter(description = "ID do material", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(materialService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar material",
            description = "Substitui todos os dados do material, inclusive a lista de autores.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Material atualizado",
                    content = @Content(schema = @Schema(implementation = MaterialResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Material, categoria, editora ou autor não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<MaterialResponseDTO> atualizar(
            @Parameter(description = "ID do material", example = "1") @PathVariable Long id,
            @Valid @RequestBody MaterialRequestDTO dto) {
        return ResponseEntity.ok(materialService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir material")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Material excluído"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Material não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do material", example = "1") @PathVariable Long id) {
        materialService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
