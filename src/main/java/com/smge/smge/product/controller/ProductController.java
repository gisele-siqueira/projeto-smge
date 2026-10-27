package com.smge.smge.product.controller;

import com.smge.smge.product.audit.ProductAuditModel;
import com.smge.smge.product.audit.ProductAuditService;
import com.smge.smge.product.model.ProductModel;
import com.smge.smge.product.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUTO_CRIAR')")
    public ResponseEntity<ProductModel> criar(
            @RequestBody ProductModel product) {

        ProductModel produtoCriado = productService.criar(product);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(produtoCriado);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PRODUTO_VISUALIZAR')")
    public ResponseEntity<List<ProductModel>> listar() {

        return ResponseEntity.ok(productService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_VISUALIZAR')")
    public ResponseEntity<ProductModel> buscarPorId(
            @PathVariable UUID id) {

        return ResponseEntity.ok(productService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_EDITAR')")
    public ResponseEntity<ProductModel> atualizar(
            @PathVariable UUID id,
            @RequestBody ProductModel product) {

        return ResponseEntity.ok(
                productService.atualizar(id, product)
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUTO_EXCLUIR')")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID id) {

        productService.deletar(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasAuthority('PRODUTO_VISUALIZAR')")
    public ResponseEntity<List<ProductAuditModel>> historico(@PathVariable UUID id) {
        // não exige que o produto ainda exista: preserva consulta após exclusão
        return ResponseEntity.ok(auditService.listarPorProduto(id));
    }
}
