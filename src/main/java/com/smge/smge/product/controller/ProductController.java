package com.smge.smge.product.controller;

import com.smge.smge.product.audit.ProductAuditModel;
import com.smge.smge.product.audit.ProductAuditService;
import com.smge.smge.product.model.ProductModel;
import com.smge.smge.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final ProductAuditService auditService;

    public ProductController(ProductService productService, ProductAuditService auditService) {
        this.productService = productService;
        this.auditService = auditService;
    }

    // Cabeçalho provisório para desenvolvimento; substituir pela identidade autenticada.
    @PostMapping
    public ResponseEntity<ProductModel> criar(@RequestHeader("X-User-Id") UUID usuarioId,
                                               @RequestBody ProductModel product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.criar(product, usuarioId));
    }

    @GetMapping
    public ResponseEntity<List<ProductModel>> listar() { return ResponseEntity.ok(productService.listar()); }

    @GetMapping("/{id}")
    public ResponseEntity<ProductModel> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductModel> atualizar(@PathVariable UUID id,
                                                   @RequestHeader("X-User-Id") UUID usuarioId,
                                                   @RequestBody ProductModel product) {
        return ResponseEntity.ok(productService.atualizar(id, product, usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable UUID id,
                                         @RequestHeader("X-User-Id") UUID usuarioId) {
        productService.deletar(id, usuarioId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<ProductAuditModel>> historico(@PathVariable UUID id) {
        // Não exige que o produto ainda exista: preserva consulta após exclusão.
        return ResponseEntity.ok(auditService.listarPorProduto(id));
    }
}
