package com.smge.smge.authorization.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smge.smge.authorization.dto.ChangePasswordRequest;
import com.smge.smge.authorization.dto.CreateUserRequest;
import com.smge.smge.authorization.dto.ResetPasswordRequest;
import com.smge.smge.authorization.dto.UserResponse;
import com.smge.smge.authorization.service.UserService;

import jakarta.validation.Valid;

/**
 * Gestão de usuários.
 * Rotas /users/me/** -> qualquer usuário autenticado.
 * Demais rotas /users/** -> somente ADMIN (ver SecurityConfig).
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ---------- ADMIN ----------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse criarUsuario(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return userService.criarUsuario(request);
    }

    @GetMapping
    public List<UserResponse> listarUsuarios() {
        return userService.listarUsuarios();
    }

    @GetMapping("/{id}")
    public UserResponse buscarPorId(@PathVariable UUID id) {
        return userService.buscarPorId(id);
    }

    @PatchMapping("/{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativarUsuario(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        userService.desativarUsuario(id, authentication.getName());
    }

    @PatchMapping("/{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reativarUsuario(@PathVariable UUID id) {
        userService.reativarUsuario(id);
    }

    @PutMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void redefinirSenha(
            @PathVariable UUID id,
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        userService.redefinirSenha(id, request.getNovaSenha());
    }

    // ---------- USUÁRIO LOGADO ----------

    @GetMapping("/me")
    public UserResponse meusDados(Authentication authentication) {
        return userService.buscarPorLogin(authentication.getName());
    }

    @PutMapping("/me/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarMinhaSenha(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication
    ) {
        userService.alterarPropriaSenha(authentication.getName(), request);
    }
}
