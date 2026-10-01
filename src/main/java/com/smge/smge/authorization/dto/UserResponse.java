package com.smge.smge.authorization.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.smge.smge.authorization.model.Role;
import com.smge.smge.authorization.model.UserModel;

/**
 * Dados do usuário devolvidos pela API (nunca expõe a senha).
 */
public record UserResponse(
        UUID userId,
        String nome,
        String login,
        Role role,
        boolean ativo,
        LocalDateTime senhaExpiraEm,
        LocalDateTime criadoEm
) {

    public static UserResponse from(UserModel user) {
        return new UserResponse(
                user.getUserId(),
                user.getNome(),
                user.getLogin(),
                user.getRole(),
                user.isActive(),
                user.getSenhaExpiraEm(),
                user.getCriadoEm()
        );
    }
}
