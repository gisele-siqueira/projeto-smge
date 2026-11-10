package com.smge.smge.sale.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sales")
public class SaleModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID saleId;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private String cliente;

    @Column(nullable = false)
    private float valorTotal;

    @Column(nullable = false)
    private String formaPagamento;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private int quantidadeItens;

    @Column(nullable = false)
    private LocalDate dataVenda;

    @Column
    private String observacoes;

    public void cancelar() {
        this.status = "CANCELADA";
    }

    public boolean isCancelada() {
        return "CANCELADA".equalsIgnoreCase(this.status);
    }
}
