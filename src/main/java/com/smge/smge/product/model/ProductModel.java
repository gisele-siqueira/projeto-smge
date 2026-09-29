package com.smge.smge.product.model;

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
@Table(name = "products")
public class ProductModel {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID productId;
    @Column(nullable = false, unique = true)
    String codigo;
    @Column(nullable = false, unique = true)
    String nome;
    @Column()
    String descrição;
    @Column(nullable = false)
    String categoria;
    @Column(nullable = false)
    int quantidade;
    @Column(nullable = false)
    float precoUnitario;
    @Column(nullable = false)
    int quantidadeMinima = 0;
    @Column(nullable = false)
    String unidadeMedida;
    @Column(nullable = false)
    LocalDate dataCadastro;

    public void atualizarPreco(float novoPreco){
        setPrecoUnitario(novoPreco);
    }

    public boolean isEstoqueBaixo(){
        return (quantidade < quantidadeMinima);
    }
}
