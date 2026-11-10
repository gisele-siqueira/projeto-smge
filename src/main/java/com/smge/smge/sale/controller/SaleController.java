package com.smge.smge.sale.controller;

import com.smge.smge.sale.model.SaleModel;
import com.smge.smge.sale.service.SaleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sales")
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VENDA_CRIAR')")
    public ResponseEntity<SaleModel> criar(@RequestBody SaleModel sale) {
        SaleModel vendaCriada = saleService.criar(sale);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(vendaCriada);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VENDA_VISUALIZAR')")
    public ResponseEntity<List<SaleModel>> listar() {
        return ResponseEntity.ok(saleService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDA_VISUALIZAR')")
    public ResponseEntity<SaleModel> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(saleService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDA_EDITAR')")
    public ResponseEntity<SaleModel> atualizar(
            @PathVariable UUID id,
            @RequestBody SaleModel sale) {
        return ResponseEntity.ok(saleService.atualizar(id, sale));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VENDA_EXCLUIR')")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        saleService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
