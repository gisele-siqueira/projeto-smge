package com.smge.smge.authorization.security;

import java.time.Duration;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/**
 * Gera o token de acesso devolvido no login.
 * O token só identifica o usuário (subject = login): as permissões são
 * lidas do banco a cada requisição, para que mudanças valham na hora.
 */
@Service
public class TokenService {

    public static final String EMISSOR = "smge";

    private final JwtEncoder jwtEncoder;
    private final Duration validade;

    public TokenService(
            JwtEncoder jwtEncoder,
            @Value("${smge.jwt.validade-horas}") long validadeHoras
    ) {
        this.jwtEncoder = jwtEncoder;
        this.validade = Duration.ofHours(validadeHoras);
    }

    public TokenGerado gerarToken(String login) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(validade);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(EMISSOR)
                .subject(login)
                .issuedAt(agora)
                .expiresAt(expiraEm)
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenGerado(token, expiraEm);
    }

    public record TokenGerado(String token, Instant expiraEm) {
    }
}
