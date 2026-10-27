import { useContext } from 'react'

import { SessaoContext, type Sessao } from './contexto'

export function useSessao(): Sessao {
  const sessao = useContext(SessaoContext)
  if (!sessao) {
    throw new Error('useSessao deve ser usado dentro de <SessaoProvider>')
  }
  return sessao
}
