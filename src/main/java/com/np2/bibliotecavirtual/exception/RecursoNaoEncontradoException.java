package com.np2.bibliotecavirtual.exception;

/**
 * Lançada quando um registro buscado pelo ID não existe. Vira HTTP 404.
 */
public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
