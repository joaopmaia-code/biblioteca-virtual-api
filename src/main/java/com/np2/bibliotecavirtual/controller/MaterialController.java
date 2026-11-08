package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.dto.MaterialFiltroDTO;
import com.np2.bibliotecavirtual.dto.MaterialRequestDTO;
import com.np2.bibliotecavirtual.dto.MaterialResponseDTO;
import com.np2.bibliotecavirtual.dto.OrdenacaoMaterial;
import com.np2.bibliotecavirtual.dto.PaginaResponseDTO;
import com.np2.bibliotecavirtual.model.SerieRecomendada;
import com.np2.bibliotecavirtual.model.TipoMaterial;
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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @GetMapping("/busca")
    @Operation(summary = "Buscar no acervo",
            description = "Filtra os materiais por qualquer combinação dos parâmetros (todos opcionais), ordena e pagina. "
                    + "Sem nenhum filtro, devolve o acervo inteiro, página a página.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página com os materiais encontrados (pode vir vazia)"),
            @ApiResponse(responseCode = "400", description = "Parâmetro inválido (ex.: tipo inexistente, nota fora de 1 a 5, "
                    + "tamanho acima de 50)",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<PaginaResponseDTO<MaterialResponseDTO>> buscar(
            @Parameter(description = "Parte do título (sem diferenciar maiúsculas)", example = "casmurro")
            @RequestParam(required = false) String titulo,
            @Parameter(description = "ID de um dos autores", example = "1")
            @RequestParam(required = false) Long autorId,
            @Parameter(description = "ID da categoria", example = "1")
            @RequestParam(required = false) Long categoriaId,
            @Parameter(description = "ID da editora", example = "1")
            @RequestParam(required = false) Long editoraId,
            @Parameter(description = "Tipo do material")
            @RequestParam(required = false) TipoMaterial tipo,
            @Parameter(description = "Série recomendada (pela categoria do material)")
            @RequestParam(required = false) SerieRecomendada serie,
            @Parameter(description = "Publicado a partir deste ano", example = "1850")
            @RequestParam(required = false) Integer anoMin,
            @Parameter(description = "Publicado até este ano", example = "1950")
            @RequestParam(required = false) Integer anoMax,
            @Parameter(description = "Média das avaliações maior ou igual a (de 1 a 5)", example = "4")
            @RequestParam(required = false) @Min(value = 1, message = "A nota mínima deve ser de 1 a 5")
            @Max(value = 5, message = "A nota mínima deve ser de 1 a 5") Integer notaMinima,
            @Parameter(description = "true: só materiais com licença livre agora; false: só os esgotados")
            @RequestParam(required = false) Boolean disponivel,
            @Parameter(description = "Ordem dos resultados")
            @RequestParam(defaultValue = "TITULO") OrdenacaoMaterial ordenarPor,
            @Parameter(description = "Número da página, começando em 0", example = "0")
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "A página deve ser 0 ou maior") int pagina,
            @Parameter(description = "Itens por página (de 1 a 50)", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "O tamanho da página deve ser de 1 a 50")
            @Max(value = 50, message = "O tamanho da página deve ser de 1 a 50") int tamanho) {
        MaterialFiltroDTO filtro = new MaterialFiltroDTO(titulo, autorId, categoriaId, editoraId, tipo, serie,
                anoMin, anoMax, notaMinima, disponivel);
        return ResponseEntity.ok(materialService.buscar(filtro, ordenarPor, pagina, tamanho));
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
