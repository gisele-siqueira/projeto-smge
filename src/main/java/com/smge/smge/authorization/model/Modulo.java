package com.smge.smge.authorization.model;

public enum Modulo {

    USUARIOS("Usuários e acessos"),
    ESTOQUE("Estoque");

    private final String nome;

    Modulo(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
