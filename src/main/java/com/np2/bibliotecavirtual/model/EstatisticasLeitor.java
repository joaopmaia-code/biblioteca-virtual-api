package com.np2.bibliotecavirtual.model;

/**
 * Números do histórico de um aluno usados na gamificação. Não é tabela: é calculado a cada
 * consulta a partir de empréstimos, progresso de leitura e avaliações.
 *
 * @param leiturasConcluidas      materiais diferentes lidos até a última página (cada material conta uma vez)
 * @param categoriasLidas         categorias diferentes entre os materiais concluídos
 * @param classicosLidos          materiais concluídos publicados antes de 1900
 * @param devolucoesNoPrazo       empréstimos devolvidos até a data prevista
 * @param avaliacoes              avaliações feitas
 * @param avaliacoesComComentario avaliações feitas com comentário
 */
public record EstatisticasLeitor(
        int leiturasConcluidas,
        int categoriasLidas,
        int classicosLidos,
        int devolucoesNoPrazo,
        int avaliacoes,
        int avaliacoesComComentario
) {

    public static final EstatisticasLeitor VAZIA = new EstatisticasLeitor(0, 0, 0, 0, 0, 0);
}
