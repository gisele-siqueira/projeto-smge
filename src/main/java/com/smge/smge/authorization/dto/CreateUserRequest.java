package com.smge.smge.authorization.dto;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.smge.smge.authorization.model.Permissao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
    private String nome;

    @NotBlank(message = "Login é obrigatório")
    @Size(min = 3, max = 50, message = "Login deve ter entre 3 e 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Login deve conter apenas letras, números, '.', '_' ou '-'")
    private String login;

    @NotBlank(message = "Senha é obrigatória")
    @Size(min = 8, max = 64, message = "Senha deve ter entre 8 e 64 caracteres")
    @Pattern(regexp = PasswordRules.REGEX, message = PasswordRules.MENSAGEM)
    private String senha;

    // opcionais: um usuário sem perfis nem extras só acessa /users/me
    private Set<UUID> perfisIds = new HashSet<>();

    private Set<Permissao> permissoesExtras = new HashSet<>();

}
