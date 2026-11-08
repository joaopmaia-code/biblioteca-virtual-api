package com.np2.bibliotecavirtual.exception;

/**
 * Lançada quando a matrícula ou a senha do login não conferem. Vira HTTP 401.
 * A mensagem é a mesma nos dois casos, para não revelar quais matrículas existem.
 */
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
