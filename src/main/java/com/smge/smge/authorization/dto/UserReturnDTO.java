package com.smge.smge.authorization.dto;

import java.util.UUID;

public record UserReturnDTO(
        UUID id,
        String nome
) {
}
