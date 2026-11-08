package com.np2.bibliotecavirtual.exception;

/**
 * Lançada quando falta um dado obrigatório ou ele está errado de um jeito que o Bean Validation não
 * consegue checar sozinho (ex.: senha obrigatória só no cadastro, senha atual errada). Vira HTTP 400.
 */
public class RequisicaoInvalidaException extends RuntimeException {

    public RequisicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
