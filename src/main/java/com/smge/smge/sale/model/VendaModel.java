package com.smge.smge.sale.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "vendas")
public class VendaModel {

    public static final String CONCLUIDA = "CONCLUIDA";
    public static final String CANCELADA = "CANCELADA";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID vendaId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataVenda;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private float valorTotal;

    @Column(nullable = false, updatable = false)
    private UUID usuarioId;

    // EAGER: vendas sempre têm poucos itens, e isso evita LazyInitializationException
    // quando a venda é lida fora de uma transação (ex.: no serviço de relatório)
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ItemVendaModel> itens = new ArrayList<>();

    @PrePersist
    void aoCriar() {
        this.dataVenda = LocalDateTime.now();
    }
}
