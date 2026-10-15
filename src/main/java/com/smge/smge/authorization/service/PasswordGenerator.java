package com.smge.smge.authorization.service;

import java.security.SecureRandom;

public class PasswordGenerator {

    private static final SecureRandom random = new SecureRandom();

    private static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!";

    public static String gerarSenha() {
        int tamanho = 8;
        StringBuilder senha = new StringBuilder(tamanho);

        for (int i = 0; i < tamanho; i++) {
            int index = random.nextInt(CHARACTERS.length());
            senha.append(CHARACTERS.charAt(index));
        }

        return senha.toString();
    }
}