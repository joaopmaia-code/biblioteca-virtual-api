package com.np2.bibliotecavirtual.model;

import java.util.function.ToIntFunction;

/**
 * Conquistas (medalhas) da gamificação. Cada uma tem uma meta e diz de qual número das
 * estatísticas do aluno sai o progresso; está desbloqueada quando o progresso chega à meta.
 */
public enum Conquista {

    PRIMEIRA_PAGINA("Primeira Página", "Terminar a leitura do primeiro material", 1,
            EstatisticasLeitor::leiturasConcluidas),
    MARATONISTA("Maratonista", "Terminar a leitura de 5 materiais diferentes", 5,
            EstatisticasLeitor::leiturasConcluidas),
    EXPLORADOR("Explorador", "Terminar materiais de 3 categorias diferentes", 3,
            EstatisticasLeitor::categoriasLidas),
    CRITICO("Crítico", "Fazer 5 avaliações com comentário", 5,
            EstatisticasLeitor::avaliacoesComComentario),
    PONTUAL("Pontual", "Devolver 10 empréstimos dentro do prazo", 10,
            EstatisticasLeitor::devolucoesNoPrazo),
    CLASSICO("Clássico", "Terminar a leitura de um material publicado antes de 1900", 1,
            EstatisticasLeitor::classicosLidos);

    private final String nome;
    private final String descricao;
    private final int meta;
    private final ToIntFunction<EstatisticasLeitor> medidor;

    Conquista(String nome, String descricao, int meta, ToIntFunction<EstatisticasLeitor> medidor) {
        this.nome = nome;
        this.descricao = descricao;
        this.meta = meta;
        this.medidor = medidor;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getMeta() {
        return meta;
    }

    /** Quanto o aluno já tem desta conquista, limitado à meta (ex.: 3 de 5). */
    public int progresso(EstatisticasLeitor estatisticas) {
        return Math.min(medidor.applyAsInt(estatisticas), meta);
    }

    public boolean desbloqueada(EstatisticasLeitor estatisticas) {
        return medidor.applyAsInt(estatisticas) >= meta;
    }
}
