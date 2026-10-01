package com.smge.smge.authorization.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.smge.smge.authorization.dto.UpdateUserAccessRequest;
import com.smge.smge.authorization.dto.UserResponse;
import com.smge.smge.authorization.service.UserService;

import jakarta.validation.Valid;

/**
 * Gestão de usuários.
 * Rotas /users/me/** -> qualquer usuário autenticado.
 * Demais rotas -> conforme a permissão de cada endpoint.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ---------- GESTÃO DE USUÁRIOS ----------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public UserResponse criarUsuario(
            @Valid @RequestBody CreateUserRequest request
    ) {
        return userService.criarUsuario(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_VISUALIZAR')")
    public List<UserResponse> listarUsuarios() {
        return userService.listarUsuarios();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_VISUALIZAR')")
    public UserResponse buscarPorId(@PathVariable UUID id) {
        return userService.buscarPorId(id);
    }

    @PutMapping("/{id}/acessos")
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public UserResponse atualizarAcessos(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserAccessRequest request
    ) {
        return userService.atualizarAcessos(id, request);
    }

    @PatchMapping("/{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public void desativarUsuario(@PathVariable UUID id) {
        userService.desativarUsuario(id);
    }

    @PatchMapping("/{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
    public void reativarUsuario(@PathVariable UUID id) {
        userService.reativarUsuario(id);
    }

    @PutMapping("/{id}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('USUARIO_GERENCIAR')")
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
