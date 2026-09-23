package com.smge.smge.product.controller;

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

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductModel> criar(
            @RequestBody ProductModel product) {

        ProductModel produtoCriado = productService.criar(product);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(produtoCriado);
    }

    @GetMapping
    public ResponseEntity<List<ProductModel>> listar() {

        return ResponseEntity.ok(productService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductModel> buscarPorId(
            @PathVariable UUID id) {

        return ResponseEntity.ok(productService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductModel> atualizar(
            @PathVariable UUID id,
            @RequestBody ProductModel product) {

        return ResponseEntity.ok(
                productService.atualizar(id, product)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(
            @PathVariable UUID id) {

        productService.deletar(id);

        return ResponseEntity.noContent().build();
    }
}