package com.smge.smge.authorization.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smge.smge.authorization.dto.ModuloPermissoesResponse;
import com.smge.smge.authorization.dto.PerfilRequest;
import com.smge.smge.authorization.dto.PerfilResponse;
import com.smge.smge.authorization.service.PerfilService;

import jakarta.validation.Valid;

/**
 * Perfis de acesso e catálogo de permissões.
 * Consultar é liberado a quem gerencia usuários (para atribuir perfis);
 * criar, editar e excluir exige PERFIL_GERENCIAR.
 */
@RestController
public class PerfilController {

    private static final String PODE_CONSULTAR =
            "hasAnyAuthority('PERFIL_GERENCIAR', 'USUARIO_GERENCIAR')";

    private final PerfilService perfilService;

    public PerfilController(PerfilService perfilService) {
        this.perfilService = perfilService;
    }

    @GetMapping("/permissoes")
    @PreAuthorize(PODE_CONSULTAR)
    public List<ModuloPermissoesResponse> listarPermissoes() {
        return ModuloPermissoesResponse.listarTodos();
    }

    @GetMapping("/perfis")
    @PreAuthorize(PODE_CONSULTAR)
    public List<PerfilResponse> listarPerfis() {
        return perfilService.listarPerfis();
    }

    @GetMapping("/perfis/{id}")
    @PreAuthorize(PODE_CONSULTAR)
    public PerfilResponse buscarPorId(@PathVariable UUID id) {
        return perfilService.buscarPorId(id);
    }

    @PostMapping("/perfis")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('PERFIL_GERENCIAR')")
    public PerfilResponse criarPerfil(@Valid @RequestBody PerfilRequest request) {
        return perfilService.criarPerfil(request);
    }

    @PutMapping("/perfis/{id}")
    @PreAuthorize("hasAuthority('PERFIL_GERENCIAR')")
    public PerfilResponse atualizarPerfil(
            @PathVariable UUID id,
            @Valid @RequestBody PerfilRequest request
    ) {
        return perfilService.atualizarPerfil(id, request);
    }

    @DeleteMapping("/perfis/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('PERFIL_GERENCIAR')")
    public void excluirPerfil(@PathVariable UUID id) {
        perfilService.excluirPerfil(id);
    }
}
