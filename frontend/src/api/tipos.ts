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
