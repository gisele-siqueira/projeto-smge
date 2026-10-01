package com.smge.smge.authorization.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smge.smge.authorization.dto.LoginRequest;
import com.smge.smge.authorization.dto.LoginResponse;
import com.smge.smge.authorization.security.TokenService;
import com.smge.smge.authorization.security.TokenService.TokenGerado;
import com.smge.smge.authorization.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UserService userService;

    public AuthController(
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.userService = userService;
    }

    /**
     * Confere login e senha e devolve o token de acesso.
     * Erros (senha errada, usuário desativado, senha expirada) viram 401
     * no GlobalExceptionHandler.
     */
    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        Authentication autenticacao = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.getLogin(), request.getSenha()));

        TokenGerado token = tokenService.gerarToken(autenticacao.getName());

        return new LoginResponse(
                token.token(),
                "Bearer",
                token.expiraEm(),
                userService.buscarPorLogin(autenticacao.getName())
        );
    }
}
