package com.smge.smge.authorization.dto;

/**
 * Regras de senha compartilhadas entre os DTOs.
 */
public final class PasswordRules {

    // pelo menos uma letra e um número
    public static final String REGEX = "^(?=.*[A-Za-z])(?=.*\\d).+$";
    public static final String MENSAGEM = "Senha deve conter letras e números";

    private PasswordRules() {
    }
}
