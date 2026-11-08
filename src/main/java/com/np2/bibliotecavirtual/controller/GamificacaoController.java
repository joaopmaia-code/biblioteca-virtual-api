package com.np2.bibliotecavirtual.controller;

import com.np2.bibliotecavirtual.dto.ConquistaResponseDTO;
import com.np2.bibliotecavirtual.dto.ErroResponseDTO;
import com.np2.bibliotecavirtual.dto.PerfilLeitorResponseDTO;
import com.np2.bibliotecavirtual.dto.RankingResponseDTO;
import com.np2.bibliotecavirtual.service.GamificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Gamificação", description = "Pontos, níveis, conquistas e ranking de leitores, calculados pelo histórico de cada aluno")
public class GamificacaoController {

    private final GamificacaoService gamificacaoService;

    @GetMapping("/usuarios/{id}/perfil-leitor")
    @Operation(summary = "Perfil de leitor do aluno",
            description = "Pontos (10 por leitura concluída, 3 por devolução no prazo, 2 por avaliação e +1 se tiver "
                    + "comentário), nível, quanto falta para o próximo, extrato de pontos e o progresso em cada conquista.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Perfil do aluno",
                    content = @Content(schema = @Schema(implementation = PerfilLeitorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "ID em formato inválido",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<PerfilLeitorResponseDTO> perfil(
            @Parameter(description = "ID do usuário", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(gamificacaoService.perfil(id));
    }

    @GetMapping("/ranking")
    @Operation(summary = "Ranking de leitores",
            description = "Alunos com mais pontos, do maior para o menor. Quem tem 0 ponto não aparece; empatados "
                    + "dividem a posição. O nome vem abreviado (primeiro nome e inicial do último).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ranking (pode vir vazio)",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = RankingResponseDTO.class)))),
            @ApiResponse(responseCode = "400", description = "Limite fora de 1 a 50",
                    content = @Content(schema = @Schema(implementation = ErroResponseDTO.class)))
    })
    public ResponseEntity<List<RankingResponseDTO>> ranking(
            @Parameter(description = "Quantos alunos mostrar (de 1 a 50)", example = "10")
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "O limite deve ser de 1 a 50")
            @Max(value = 50, message = "O limite deve ser de 1 a 50") int limite) {
        return ResponseEntity.ok(gamificacaoService.ranking(limite));
    }

    @GetMapping("/conquistas")
    @Operation(summary = "Listar conquistas", description = "Todas as conquistas e o que é preciso para desbloquear cada uma.")
    @ApiResponse(responseCode = "200", description = "Catálogo de conquistas",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ConquistaResponseDTO.class))))
    public ResponseEntity<List<ConquistaResponseDTO>> conquistas() {
        return ResponseEntity.ok(gamificacaoService.conquistas());
    }
}
