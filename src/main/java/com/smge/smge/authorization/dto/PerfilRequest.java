package com.smge.smge.authorization.dto;

import java.util.Set;

import com.smge.smge.authorization.model.Permissao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 60, message = "Nome deve ter no máximo 60 caracteres")
    private String nome;

    @Size(max = 255, message = "Descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @NotEmpty(message = "Informe ao menos uma permissão")
    private Set<Permissao> permissoes;

}
