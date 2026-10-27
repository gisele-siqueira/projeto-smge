import { createContext } from 'react'

import type { Permissao, Usuario } from '../api/tipos'

export interface Sessao {
  usuario: Usuario | null
  // true enquanto confere, ao abrir o sistema, se o token salvo ainda vale
  carregando: boolean
  entrar: (login: string, senha: string) => Promise<void>
  sair: (aviso?: string) => void
  // true se o usuário tiver PELO MENOS UMA das permissões informadas
  pode: (...permissoes: Permissao[]) => boolean
  recarregarUsuario: () => Promise<void>
}

export const SessaoContext = createContext<Sessao | null>(null)
