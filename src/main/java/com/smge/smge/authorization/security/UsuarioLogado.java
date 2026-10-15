package com.smge.smge.authorization.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.smge.smge.authorization.model.Permissao;
import com.smge.smge.common.exception.AcessoNegadoException;

public final class UsuarioLogado {

    private static final Set<String> NOMES_PERMISSOES = Arrays.stream(Permissao.values())
            .map(Enum::name)
            .collect(Collectors.toSet());

    private UsuarioLogado() {
    }

    public static String login() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    public static Set<Permissao> permissoes() {
        Set<Permissao> permissoes = EnumSet.noneOf(Permissao.class);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null) {
            return permissoes;
        }

        auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(NOMES_PERMISSOES::contains)
                .map(Permissao::valueOf)
                .forEach(permissoes::add);

        return permissoes;
    }

    // ninguém pode conceder uma permissão que não possui, nem direto nem via perfil
    public static void exigirPodeConceder(Collection<Permissao> permissoesConcedidas) {
        Set<Permissao> faltando = EnumSet.noneOf(Permissao.class);
        faltando.addAll(permissoesConcedidas);
        faltando.removeAll(permissoes());

        if (!faltando.isEmpty()) {
            throw new AcessoNegadoException(
                    "Você não pode conceder permissões que não possui: " + faltando);
        }
    }

    // ninguém gerencia um usuário com mais acessos do que ele mesmo tem
    public static void exigirPodeGerenciar(Collection<Permissao> permissoesDoAlvo) {
        if (!permissoes().containsAll(permissoesDoAlvo)) {
            throw new AcessoNegadoException(
                    "Você não pode gerenciar um usuário com mais acessos que você");
        }
    }
}
