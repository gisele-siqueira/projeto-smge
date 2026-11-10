package com.smge.smge.sale.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "itens_venda")
public class ItemVendaModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID itemVendaId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "venda_id", nullable = false)
    private VendaModel venda;

    // sem relação de FK com o produto: a venda guarda uma "foto" dos dados dele no
    // momento da compra, pra continuar correta mesmo se o produto for editado ou excluído depois
    @Column(nullable = false, updatable = false)
    private UUID produtoId;

    @Column(nullable = false, updatable = false)
    private String produtoCodigo;

    @Column(nullable = false, updatable = false)
    private String produtoNome;

    @Column(nullable = false, updatable = false)
    private int quantidade;

    @Column(nullable = false, updatable = false)
    private float precoUnitario;

    @Column(nullable = false, updatable = false)
    private float subtotal;
}
