package com.smge.smge.authorization.dto;

import java.util.Set;
import java.util.UUID;

import com.smge.smge.authorization.model.Permissao;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Substitui os perfis e as permissões extras de um usuário.
 * Envie listas vazias para remover tudo.
 */
@Getter
@Setter
public class UpdateUserAccessRequest {

    @NotNull(message = "Informe os perfis (lista vazia para nenhum)")
    private Set<UUID> perfisIds;

    @NotNull(message = "Informe as permissões extras (lista vazia para nenhuma)")
    private Set<Permissao> permissoesExtras;

}
