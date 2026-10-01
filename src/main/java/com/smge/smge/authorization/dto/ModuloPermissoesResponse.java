package com.smge.smge.authorization.dto;

import java.util.Arrays;
import java.util.List;

import com.smge.smge.authorization.model.Modulo;
import com.smge.smge.authorization.model.Permissao;

/**
 * Permissões agrupadas por módulo, para montar a tela de perfis.
 */
public record ModuloPermissoesResponse(
        Modulo modulo,
        String nome,
        List<PermissaoItem> permissoes
) {

    public record PermissaoItem(Permissao codigo, String descricao) {
    }

    public static List<ModuloPermissoesResponse> listarTodos() {
        return Arrays.stream(Modulo.values())
                .map(modulo -> new ModuloPermissoesResponse(
                        modulo,
                        modulo.getNome(),
                        Arrays.stream(Permissao.values())
                                .filter(permissao -> permissao.getModulo() == modulo)
                                .map(permissao -> new PermissaoItem(permissao, permissao.getDescricao()))
                                .toList()))
                .toList();
    }
}
