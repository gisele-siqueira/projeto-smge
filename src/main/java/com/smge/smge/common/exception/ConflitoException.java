package com.smge.smge.common.exception;

/**
 * Conflito com um dado já existente, ex.: login duplicado (HTTP 409).
 */
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
