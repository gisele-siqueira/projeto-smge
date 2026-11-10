// Tipos que espelham os DTOs do back-end (pacote authorization.dto).

// Mantenha em sincronia com o enum Permissao.java
export type Permissao =
  | 'USUARIO_VISUALIZAR'
  | 'USUARIO_GERENCIAR'
  | 'PERFIL_GERENCIAR'
  | 'PRODUTO_VISUALIZAR'
  | 'PRODUTO_CRIAR'
  | 'PRODUTO_EDITAR'
  | 'PRODUTO_EXCLUIR'
  | 'VENDA_VISUALIZAR'
  | 'VENDA_CRIAR'
  | 'VENDA_CANCELAR'

export interface PerfilResumo {
  perfilId: string
  nome: string
}

export interface Usuario {
  userId: string
  nome: string
  login: string
  ativo: boolean
  perfis: PerfilResumo[]
  permissoesExtras: Permissao[]
  // soma final (perfis + extras): é o que vale para mostrar ou esconder telas e botões
  permissoes: Permissao[]
  senhaExpiraEm: string
  criadoEm: string
}

export interface Perfil {
  perfilId: string
  nome: string
  descricao: string | null
  permissoes: Permissao[]
  sistema: boolean
}

export interface ModuloPermissoes {
  modulo: string
  nome: string
  permissoes: { codigo: Permissao; descricao: string }[]
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiraEm: string
  usuario: Usuario
}

export interface NovoUsuario {
  nome: string
  login: string
  senha: string
  perfisIds: string[]
  permissoesExtras: Permissao[]
}

export interface Acessos {
  perfisIds: string[]
  permissoesExtras: Permissao[]
}

export interface PerfilForm {
  nome: string
  descricao?: string
  permissoes: Permissao[]
}

export interface Produto {
  productId: string
  codigo: string
  nome: string
  descrição: string | null
  categoria: string
  quantidade: number
  precoUnitario: number
  quantidadeMinima: number
  unidadeMedida: string
  dataCadastro: string
  estoqueBaixo: boolean
}

export interface ProdutoForm {
  codigo: string
  nome: string
  descrição?: string
  categoria: string
  quantidade: number
  precoUnitario: number
  quantidadeMinima: number
  unidadeMedida: string
  dataCadastro: string
}

// Mantenha em sincronia com ProductAuditModel.java
export type OperacaoAuditoria = 'CRIACAO' | 'ATUALIZACAO' | 'EXCLUSAO'

export interface ProdutoAuditoria {
  auditId: string
  productId: string
  usuarioId: string
  dataAlteracao: string
  operacao: OperacaoAuditoria
  campoAlterado: string
  valorAnterior: string | null
  valorNovo: string | null
}

// Mantenha em sincronia com VendaModel.java
export type StatusVenda = 'CONCLUIDA' | 'CANCELADA'

export interface ItemVenda {
  produtoId: string
  produtoCodigo: string
  produtoNome: string
  quantidade: number
  precoUnitario: number
  subtotal: number
}

export interface Venda {
  vendaId: string
  dataVenda: string
  status: StatusVenda
  valorTotal: number
  itens: ItemVenda[]
}

export interface ItemVendaForm {
  produtoId: string
  quantidade: number
}

export interface VendaForm {
  itens: ItemVendaForm[]
}

export interface VendaResumoRelatorio {
  vendaId: string
  dataVenda: string
  quantidadeItens: number
  valorTotal: number
}

export interface ProdutoRelatorio {
  produtoCodigo: string
  produtoNome: string
  quantidadeVendida: number
  receitaGerada: number
}

export interface RelatorioVendas {
  inicio: string
  fim: string
  totalVendas: number
  totalItens: number
  receitaTotal: number
  vendas: VendaResumoRelatorio[]
  produtosMaisVendidos: ProdutoRelatorio[]
}
