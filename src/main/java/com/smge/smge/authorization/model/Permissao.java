package com.smge.smge.authorization.model;

/**
 * Permissões granulares do sistema.
 *
 * Cada permissão protege endpoints reais via @PreAuthorize("hasAuthority('NOME')"),
 * por isso elas são definidas no código e não pela tela.
 * Ao criar uma funcionalidade nova, adicione aqui a permissão correspondente:
 * o perfil "Administrador" recebe automaticamente todas ao iniciar o sistema.
 */
public enum Permissao {

    // ---------- Usuários e acessos ----------
    USUARIO_VISUALIZAR(Modulo.USUARIOS, "Consultar usuários"),
    USUARIO_GERENCIAR(Modulo.USUARIOS, "Criar, desativar, redefinir senha e alterar acessos de usuários"),
    PERFIL_GERENCIAR(Modulo.USUARIOS, "Criar, editar e excluir perfis de acesso"),

    // ---------- Estoque ----------
    PRODUTO_VISUALIZAR(Modulo.ESTOQUE, "Consultar produtos"),
    PRODUTO_CRIAR(Modulo.ESTOQUE, "Cadastrar produtos"),
    PRODUTO_EDITAR(Modulo.ESTOQUE, "Editar produtos"),
    PRODUTO_EXCLUIR(Modulo.ESTOQUE, "Excluir produtos");

    private final Modulo modulo;
    private final String descricao;

    Permissao(Modulo modulo, String descricao) {
        this.modulo = modulo;
        this.descricao = descricao;
    }

    public Modulo getModulo() {
        return modulo;
    }

    public String getDescricao() {
        return descricao;
    }
}
