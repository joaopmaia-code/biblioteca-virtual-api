package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.AutorRequestDTO;
import com.np2.bibliotecavirtual.dto.AutorResponseDTO;
import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.service.AutorService;
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
@RequestMapping("/autores")
@RequiredArgsConstructor
@Tag(name = "Autores", description = "Cadastro dos autores dos materiais")
public class AutorController {

    private final AutorService autorService;

    @PostMapping
    @Operation(summary = "Cadastrar autor")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Autor cadastrado",
                    content = @Content(schema = @Schema(implementation = AutorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<AutorResponseDTO> cadastrar(@Valid @RequestBody AutorRequestDTO dto) {
        AutorResponseDTO criado = autorService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar autores", description = "Retorna todos os autores em ordem alfabética.")
    @ApiResponse(responseCode = "200", description = "Lista de autores",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = AutorResponseDTO.class))))
    public ResponseEntity<List<AutorResponseDTO>> listar() {
        return ResponseEntity.ok(autorService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar autor por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autor encontrado",
                    content = @Content(schema = @Schema(implementation = AutorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<AutorResponseDTO> buscarPorId(
            @Parameter(description = "ID do autor") @PathVariable Long id) {
        return ResponseEntity.ok(autorService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar autor", description = "Substitui todos os dados do autor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autor atualizado",
                    content = @Content(schema = @Schema(implementation = AutorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<AutorResponseDTO> atualizar(
            @Parameter(description = "ID do autor") @PathVariable Long id,
            @Valid @RequestBody AutorRequestDTO dto) {
        return ResponseEntity.ok(autorService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir autor")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Autor excluído"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Autor não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do autor") @PathVariable Long id) {
        autorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
