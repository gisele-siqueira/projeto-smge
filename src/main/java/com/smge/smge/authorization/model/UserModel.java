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

    public static final int DIAS_VALIDADE_SENHA = 90;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID userId;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private LocalDateTime senhaExpiraEm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }


    /**
     * Recebe a senha JÁ CRIPTOGRAFADA e renova a validade.
     */
    public void definirSenha(String novaSenha) {
        this.senha = novaSenha;
        this.senhaExpiraEm = LocalDateTime.now().plusDays(DIAS_VALIDADE_SENHA);
    }


    public boolean senhaExpirada() {
        return LocalDateTime.now().isAfter(senhaExpiraEm);
    }


    public boolean isAdmin() {
        return role == Role.ADMIN;
    }


    public void desativarUser() {
        isActive = false;
    }


    public void ativarUser() {
        isActive = true;
    }
}
