package com.smge.smge.authorization.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    private String nome;
    private String login;
    private String senha;

}