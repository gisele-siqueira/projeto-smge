package com.smge.smge.authorization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class UserModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID userId;

    @Column(nullable = false)
    String nome;

    @Column(nullable = false, unique = true)
    String login;

    @Column(nullable = false)
    String senha;

    @Column(nullable = false)
    LocalDateTime senhaExpiraEm;

    //permission;
    //roleLevel;

    @Column(nullable = false)
    boolean isActive = true;


    public void definirSenha(String novaSenha) {
        this.senha = novaSenha;
        this.senhaExpiraEm = LocalDateTime.now().plusDays(90);
    }


    public boolean senhaExpirada() {
        return LocalDateTime.now().isAfter(senhaExpiraEm);
    }


    public void desativarUser() {
        // zerar permission
        // zerar roleLevel
        isActive = false;
        // mudar senha
    }
}
