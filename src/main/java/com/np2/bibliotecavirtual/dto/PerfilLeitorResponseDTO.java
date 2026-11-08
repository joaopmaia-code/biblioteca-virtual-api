package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.EstatisticasLeitor;
import com.np2.bibliotecavirtual.model.NivelLeitor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Perfil de leitor do aluno na gamificação: pontos, nível, extrato e conquistas.
 */
@Schema(description = "Perfil de leitor (gamificação)")
public record PerfilLeitorResponseDTO(

        ResumoDTO usuario,

        @Schema(example = "68")
        int pontos,

        @Schema(example = "LEITOR")
        NivelLeitor nivel,

        @Schema(example = "Leitor")
        String nomeNivel,

        @Schema(description = "Próximo nível (null no último)", example = "Leitor Voraz")
        String proximoNivel,

        @Schema(description = "Pontos que faltam para o próximo nível (null no último)", example = "82")
        Integer pontosParaProximoNivel,

        @Schema(description = "De onde vieram os pontos")
        List<PontuacaoDTO> extrato,

        EstatisticasLeitor estatisticas,

        @Schema(description = "Todas as conquistas, com o progresso do aluno em cada uma")
        List<ConquistaUsuarioDTO> conquistas
) {
}
