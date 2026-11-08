package com.np2.bibliotecavirtual.dto;

import com.np2.bibliotecavirtual.model.Conquista;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Uma conquista do catálogo (GET /conquistas): o que é e como desbloquear.
 */
@Schema(description = "Conquista que os alunos podem desbloquear")
public record ConquistaResponseDTO(

        @Schema(example = "MARATONISTA")
        Conquista codigo,

        @Schema(example = "Maratonista")
        String nome,

        @Schema(example = "Terminar a leitura de 5 materiais diferentes")
        String descricao,

        @Schema(description = "Quanto é preciso para desbloquear", example = "5")
        int meta
) {

    public static ConquistaResponseDTO de(Conquista conquista) {
        return new ConquistaResponseDTO(conquista, conquista.getNome(), conquista.getDescricao(), conquista.getMeta());
    }
}
