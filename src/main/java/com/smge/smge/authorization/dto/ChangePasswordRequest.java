package com.smge.smge.authorization.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Usado pelo próprio usuário para trocar a senha.
 */
@Getter
@Setter
public class ChangePasswordRequest {

    @NotBlank(message = "Senha atual é obrigatória")
    private String senhaAtual;

    @NotBlank(message = "Nova senha é obrigatória")
    @Size(min = 8, max = 64, message = "Senha deve ter entre 8 e 64 caracteres")
    @Pattern(regexp = PasswordRules.REGEX, message = PasswordRules.MENSAGEM)
    private String novaSenha;

}
