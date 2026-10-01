package com.smge.smge.authorization.security;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.smge.smge.authorization.model.UserModel;
import com.smge.smge.authorization.repository.UserRepository;

/**
 * Executado a cada requisição com token válido (assinatura e validade já conferidas).
 * Busca o usuário no banco para que desativação, mudança de acessos e troca
 * de senha tenham efeito imediato, sem esperar o token expirar.
 */
@Component
public class JwtUsuarioConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserRepository userRepository;

    public JwtUsuarioConverter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {

        UserModel user = userRepository.findByLogin(jwt.getSubject())
                .orElseThrow(() -> new InvalidBearerTokenException("Usuário do token não existe"));

        if (!user.isActive()) {
            throw new DisabledException("Usuário desativado");
        }

        if (user.senhaExpirada()) {
            throw new CredentialsExpiredException("Senha expirada");
        }

        if (emitidoAntesDaTrocaDeSenha(jwt, user)) {
            throw new InvalidBearerTokenException("Token emitido antes da última troca de senha");
        }

        return new JwtAuthenticationToken(jwt, SmgeUserDetailsService.authoritiesDe(user), user.getLogin());
    }

    private boolean emitidoAntesDaTrocaDeSenha(Jwt jwt, UserModel user) {
        Instant emitidoEm = jwt.getIssuedAt();
        // o token guarda o horário em segundos, então a comparação também é feita em segundos
        Instant senhaAlteradaEm = user.getSenhaAlteradaEm()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .truncatedTo(ChronoUnit.SECONDS);

        return emitidoEm == null || emitidoEm.isBefore(senhaAlteradaEm);
    }
}
