package com.np2.bibliotecavirtual.exception;

/**
 * Lançada quando a requisição é válida, mas fere uma regra de negócio
 * (ex.: nome de categoria duplicado, material sem licenças). Vira HTTP 409.
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
