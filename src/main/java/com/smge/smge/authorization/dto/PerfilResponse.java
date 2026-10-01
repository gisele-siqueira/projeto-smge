package com.smge.smge.authorization.dto;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import com.smge.smge.authorization.model.PerfilModel;
import com.smge.smge.authorization.model.Permissao;

public record PerfilResponse(
        UUID perfilId,
        String nome,
        String descricao,
        Set<Permissao> permissoes,
        boolean sistema
) {

    public static PerfilResponse from(PerfilModel perfil) {
        Set<Permissao> permissoes = EnumSet.noneOf(Permissao.class);
        permissoes.addAll(perfil.getPermissoes());

        return new PerfilResponse(
                perfil.getPerfilId(),
                perfil.getNome(),
                perfil.getDescricao(),
                permissoes,
                perfil.isSistema()
        );
    }
}
