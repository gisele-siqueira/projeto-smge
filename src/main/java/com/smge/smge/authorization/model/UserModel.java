package com.smge.smge.authorization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
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

    // tokens emitidos antes desta data deixam de valer (ex.: após trocar a senha)
    @Column(nullable = false)
    private LocalDateTime senhaAlteradaEm;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_perfis",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id")
    )
    private Set<PerfilModel> perfis = new HashSet<>();

    // permissões avulsas, além das que vêm dos perfis
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissoes_extras", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permissao", nullable = false)
    private Set<Permissao> permissoesExtras = new HashSet<>();

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
        LocalDateTime agora = LocalDateTime.now();
        this.senha = novaSenha;
        this.senhaAlteradaEm = agora;
        this.senhaExpiraEm = agora.plusDays(DIAS_VALIDADE_SENHA);
    }


    public boolean senhaExpirada() {
        return LocalDateTime.now().isAfter(senhaExpiraEm);
    }


    /**
     * Todas as permissões do usuário: as dos perfis somadas às extras.
     */
    public Set<Permissao> permissoesEfetivas() {
        Set<Permissao> todas = EnumSet.noneOf(Permissao.class);
        perfis.forEach(perfil -> todas.addAll(perfil.getPermissoes()));
        todas.addAll(permissoesExtras);
        return todas;
    }


    public void desativarUser() {
        isActive = false;
    }


    public void ativarUser() {
        isActive = true;
    }
}
