package com.smge.smge.sale.repository;

import com.smge.smge.sale.model.VendaModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface VendaRepository extends JpaRepository<VendaModel, UUID> {

    List<VendaModel> findAllByOrderByDataVendaDesc();

    List<VendaModel> findByDataVendaBetweenOrderByDataVendaDesc(LocalDateTime inicio, LocalDateTime fim);
}
