package com.smge.smge.sale.repository;

import com.smge.smge.sale.model.SaleModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SaleRepository extends JpaRepository<SaleModel, UUID> {

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndSaleIdNot(String codigo, UUID saleId);

    Optional<SaleModel> findByCodigoIgnoreCase(String codigo);
}
