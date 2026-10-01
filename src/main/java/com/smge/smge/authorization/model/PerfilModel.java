package com.smge.smge.authorization.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Perfil de acesso: um grupo de permissões que o admin monta pela aplicação
 * (ex.: "Estoquista", "Faturamento") e atribui aos usuários.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "perfis")
public class PerfilModel {

    public static final String ADMINISTRADOR = "Administrador";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID perfilId;

    @Column(nullable = false, unique = true)
    private String nome;

    @Column
    private String descricao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "perfil_permissoes", joinColumns = @JoinColumn(name = "perfil_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "permissao", nullable = false)
    private Set<Permissao> permissoes = new HashSet<>();

    // perfis do sistema (ex.: Administrador) não podem ser editados nem excluídos
    @Column(nullable = false)
    private boolean sistema = false;
}
