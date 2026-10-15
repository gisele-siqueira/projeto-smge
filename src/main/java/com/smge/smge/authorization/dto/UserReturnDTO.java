package com.smge.smge.authorization.dto;

import com.smge.smge.authorization.model.UserModel;

import java.util.UUID;

public record UserReturnDTO(
        UUID id,
        String nome
) {

    public UserReturnDTO (UserModel user) {
        this(user.getUserId(), user.getNome());
    }
}
