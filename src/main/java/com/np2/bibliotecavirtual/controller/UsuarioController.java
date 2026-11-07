package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.dto.UsuarioRequestDTO;
import com.np2.bibliotecavirtual.dto.UsuarioResponseDTO;
import com.np2.bibliotecavirtual.service.UsuarioService;
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
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Alunos que fazem empréstimos e reservas")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @Operation(summary = "Cadastrar usuário", description = "E-mail e matrícula não podem repetir os de outro usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado",
                    content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ou matrícula já cadastrados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> cadastrar(@Valid @RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO criado = usuarioService.cadastrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar usuários", description = "Retorna todos os usuários em ordem alfabética.")
    @ApiResponse(responseCode = "200", description = "Lista de usuários",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsuarioResponseDTO.class))))
    public ResponseEntity<List<UsuarioResponseDTO>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar usuário por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar usuário", description = "Substitui todos os dados do usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuário atualizado",
                    content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "E-mail ou matrícula já usados por outro usuário",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @Parameter(description = "ID do usuário") @PathVariable Long id,
            @Valid @RequestBody UsuarioRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir usuário", description = "Não exclui usuário com empréstimos registrados.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário excluído"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Usuário tem empréstimos registrados",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do usuário") @PathVariable Long id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
