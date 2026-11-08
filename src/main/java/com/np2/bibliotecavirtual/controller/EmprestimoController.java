package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.EmprestimoAtualizacaoDTO;
import com.np2.bibliotecavirtual.dto.EmprestimoRequestDTO;
import com.np2.bibliotecavirtual.dto.EmprestimoResponseDTO;
import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.model.StatusEmprestimo;
import com.np2.bibliotecavirtual.seguranca.UsuarioLogado;
import com.np2.bibliotecavirtual.service.EmprestimoService;
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
import org.springframework.web.bind.annotation.PatchMapping;
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
import java.util.Map;

@RestController
@RequestMapping("/emprestimos")
@RequiredArgsConstructor
@Tag(name = "Empréstimos", description = "Empréstimos de materiais, devolução e controle de atraso")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;
    private final UsuarioLogado usuarioLogado;

    @PostMapping
    @Operation(summary = "Registrar empréstimo",
            description = "Precisa haver licença livre (RN01); o usuário não pode já estar com o material (RN03) "
                    + "nem ter 3 empréstimos em uso (RN04). Sem data prevista, o prazo é de 14 dias (RN02).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Empréstimo registrado",
                    content = @Content(schema = @Schema(implementation = EmprestimoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário ou material não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Sem licenças, material repetido ou limite atingido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<EmprestimoResponseDTO> registrar(@Valid @RequestBody EmprestimoRequestDTO dto) {
        // Aluno e professor só pegam emprestado em nome próprio; o bibliotecário registra para qualquer um (403).
        usuarioLogado.garantirQueEhOProprioOuBibliotecario(dto.usuarioId());
        EmprestimoResponseDTO criado = emprestimoService.registrar(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.id())
                .toUri();
        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping
    @Operation(summary = "Listar empréstimos", description = "Filtros opcionais por status e por usuário.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de empréstimos",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = EmprestimoResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Status ou ID de usuário em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<List<EmprestimoResponseDTO>> listar(
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) StatusEmprestimo status,
            @Parameter(description = "Filtrar por ID do usuário") @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(emprestimoService.listar(status, usuarioId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar empréstimo por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empréstimo encontrado",
                    content = @Content(schema = @Schema(implementation = EmprestimoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<EmprestimoResponseDTO> buscarPorId(
            @Parameter(description = "ID do empréstimo") @PathVariable Long id) {
        return ResponseEntity.ok(emprestimoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Editar empréstimo (renovar prazo)",
            description = "Muda a data prevista de devolução. Um empréstimo ATRASADO renovado volta a ser ATIVO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Empréstimo atualizado",
                    content = @Content(schema = @Schema(implementation = EmprestimoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Empréstimo já devolvido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<EmprestimoResponseDTO> atualizar(
            @Parameter(description = "ID do empréstimo") @PathVariable Long id,
            @Valid @RequestBody EmprestimoAtualizacaoDTO dto) {
        return ResponseEntity.ok(emprestimoService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/devolver")
    @Operation(summary = "Registrar devolução", description = "Marca como DEVOLVIDO e libera a licença do material.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Devolução registrada",
                    content = @Content(schema = @Schema(implementation = EmprestimoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "409", description = "Empréstimo já devolvido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<EmprestimoResponseDTO> devolver(
            @Parameter(description = "ID do empréstimo") @PathVariable Long id) {
        return ResponseEntity.ok(emprestimoService.devolver(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir empréstimo")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Empréstimo excluído"),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Empréstimo não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do empréstimo") @PathVariable Long id) {
        emprestimoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/atualizar-atrasados")
    @Operation(summary = "Atualizar atrasados agora",
            description = "Roda na hora a rotina da RN05 (que também roda sozinha a cada hora): "
                    + "empréstimos ATIVOS com prazo vencido passam a ATRASADO.")
    @ApiResponse(responseCode = "200", description = "Quantidade de empréstimos que mudaram para ATRASADO")
    public ResponseEntity<Map<String, Integer>> atualizarAtrasados() {
        return ResponseEntity.ok(Map.of("atualizados", emprestimoService.atualizarAtrasados()));
    }
}
