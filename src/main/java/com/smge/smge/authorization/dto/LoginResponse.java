package com.smge.smge.authorization.dto;

import java.time.Instant;

// usuario já vem junto, pro front montar o menu sem uma segunda chamada
public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiraEm,
        UserResponse usuario
) {
}
