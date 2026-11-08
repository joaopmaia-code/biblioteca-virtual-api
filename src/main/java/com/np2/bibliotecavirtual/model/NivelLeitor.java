package com.np2.bibliotecavirtual.model;

/**
 * Níveis da gamificação, do menor para o maior, com os pontos mínimos de cada um.
 */
public enum NivelLeitor {

    LEITOR_INICIANTE("Leitor Iniciante", 0),
    LEITOR("Leitor", 50),
    LEITOR_VORAZ("Leitor Voraz", 150),
    DEVORADOR_DE_LIVROS("Devorador de Livros", 300),
    LENDA_DA_BIBLIOTECA("Lenda da Biblioteca", 500);

    private final String nome;
    private final int pontosMinimos;

    NivelLeitor(String nome, int pontosMinimos) {
        this.nome = nome;
        this.pontosMinimos = pontosMinimos;
    }

    public String getNome() {
        return nome;
    }

    public int getPontosMinimos() {
        return pontosMinimos;
    }

    /** O maior nível cujo mínimo o aluno já alcançou. */
    public static NivelLeitor de(int pontos) {
        NivelLeitor nivel = LEITOR_INICIANTE;
        for (NivelLeitor candidato : values()) {
            if (pontos >= candidato.pontosMinimos) {
                nivel = candidato;
            }
        }
        return nivel;
    }

    /** O nível seguinte, ou null se este já é o último. */
    public NivelLeitor proximo() {
        return ordinal() + 1 < values().length ? values()[ordinal() + 1] : null;
    }
}
