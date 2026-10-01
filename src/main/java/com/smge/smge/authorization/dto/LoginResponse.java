package com.smge.smge.authorization.dto;

import java.time.Instant;

/**
 * Resposta do login. O front-end deve enviar o token em todas as requisições:
 * Authorization: Bearer {accessToken}
 * Os dados do usuário (com as permissões) já vêm junto para montar o menu.
 */
public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiraEm,
        UserResponse usuario
) {
}
