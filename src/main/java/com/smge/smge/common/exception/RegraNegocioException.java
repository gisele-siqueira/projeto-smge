package com.smge.smge.common.exception;

/**
 * Operação viola uma regra de negócio (HTTP 400).
 */
public class RegraNegocioException extends RuntimeException {

    public RegraNegocioException(String mensagem) {
        super(mensagem);
    }
}
