package com.smge.smge.authorization.dto;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.smge.smge.authorization.model.Permissao;
import com.smge.smge.authorization.model.UserModel;

/**
 * Dados do usuário devolvidos pela API (nunca expõe a senha).
 * "permissoes" é o resultado final (perfis + extras): o front-end usa
 * essa lista para decidir quais menus e botões mostrar.
 */
public record UserResponse(
        UUID userId,
        String nome,
        String login,
        boolean ativo,
        List<PerfilResumo> perfis,
        Set<Permissao> permissoesExtras,
        Set<Permissao> permissoes,
        LocalDateTime senhaExpiraEm,
        LocalDateTime criadoEm
) {

    public record PerfilResumo(UUID perfilId, String nome) {
    }

    public static UserResponse from(UserModel user) {
        Set<Permissao> extras = EnumSet.noneOf(Permissao.class);
        extras.addAll(user.getPermissoesExtras());

        return new UserResponse(
                user.getUserId(),
                user.getNome(),
                user.getLogin(),
                user.isActive(),
                user.getPerfis().stream()
                        .map(perfil -> new PerfilResumo(perfil.getPerfilId(), perfil.getNome()))
                        .sorted(Comparator.comparing(PerfilResumo::nome))
                        .toList(),
                extras,
                user.permissoesEfetivas(),
                user.getSenhaExpiraEm(),
                user.getCriadoEm()
        );
    }
}
