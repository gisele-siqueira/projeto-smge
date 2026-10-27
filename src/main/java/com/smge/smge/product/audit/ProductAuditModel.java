package com.smge.smge.product.audit;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Setter @NoArgsConstructor
@Entity
@Table(name = "product_audit", indexes = @Index(name = "idx_product_audit_product", columnList = "productId"))
public class ProductAuditModel {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID auditId;
    @Column(nullable = false, updatable = false)
    private UUID productId;
    @Column(nullable = false, updatable = false)
    private UUID usuarioId;
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataAlteracao;
    @Column(nullable = false, updatable = false)
    private String operacao;
    @Column(nullable = false, updatable = false)
    private String campoAlterado;
    @Column(columnDefinition = "TEXT", updatable = false)
    private String valorAnterior;
    @Column(columnDefinition = "TEXT", updatable = false)
    private String valorNovo;
}
