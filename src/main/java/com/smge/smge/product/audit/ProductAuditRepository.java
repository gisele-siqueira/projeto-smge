package com.smge.smge.product.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProductAuditRepository extends JpaRepository<ProductAuditModel, UUID> {
    List<ProductAuditModel> findByProductIdOrderByDataAlteracaoDesc(UUID productId);
}
