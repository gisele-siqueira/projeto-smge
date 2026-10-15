package com.smge.smge.common.exception;

// usuário autenticado, mas sem direito de fazer a operação (HTTP 403)
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
