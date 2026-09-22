package com.smge.smge.authorization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    //permission;
    //roleLevel;
    @Column(nullable = false)
    boolean isActive = true;

    public void desativarUser(){
        // zerar permission
        // zerar roleLevel
        isActive = false;
        // mudar senha
    }


}
