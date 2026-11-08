package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.Conquista;
import com.np2.bibliotecavirtual.model.EstatisticasLeitor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Uma conquista no perfil do aluno: se já desbloqueou e, se não, quanto falta.
 */
@Schema(description = "Conquista com o progresso do aluno")
public record ConquistaUsuarioDTO(

        @Schema(example = "MARATONISTA")
        Conquista codigo,

        @Schema(example = "Maratonista")
        String nome,

        @Schema(example = "Terminar a leitura de 5 materiais diferentes")
        String descricao,

        @Schema(example = "5")
        int meta,

        @Schema(description = "Quanto o aluno já tem (no máximo a meta)", example = "3")
        int progresso,

        @Schema(example = "false")
        boolean desbloqueada
) {

    public static ConquistaUsuarioDTO de(Conquista conquista, EstatisticasLeitor estatisticas) {
        return new ConquistaUsuarioDTO(conquista, conquista.getNome(), conquista.getDescricao(), conquista.getMeta(),
                conquista.progresso(estatisticas), conquista.desbloqueada(estatisticas));
    }
}
