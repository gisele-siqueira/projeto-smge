package com.smge.smge.authorization.model;

// funcionalidade nova: adicione a permissão aqui; o perfil "Administrador" recebe todas automaticamente
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
