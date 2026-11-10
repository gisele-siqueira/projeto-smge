package com.smge.smge.sale.controller;

import com.smge.smge.sale.dto.VendaRequest;
import com.smge.smge.sale.dto.VendaResponse;
import com.smge.smge.sale.service.VendaService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sales")
public class VendaController {

    private final VendaService vendaService;

    public VendaController(VendaService vendaService) {
        this.vendaService = vendaService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('VENDA_CRIAR')")
    public VendaResponse criar(@Valid @RequestBody VendaRequest request) {
        return vendaService.criar(request);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDA_VISUALIZAR')")
    public List<VendaResponse> listar() {
        return vendaService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDA_VISUALIZAR')")
    public VendaResponse buscarPorId(@PathVariable UUID id) {
        return vendaService.buscarPorId(id);
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAuthority('VENDA_CANCELAR')")
    public VendaResponse cancelar(@PathVariable UUID id) {
        return vendaService.cancelar(id);
    }
}
