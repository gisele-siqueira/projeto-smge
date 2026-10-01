import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { useNavigate } from 'react-router-dom'

import { definirAoPerderSessao, tokenStorage } from '../api/cliente'
import { authApi, usuariosApi } from '../api/endpoints'
import type { Permissao, Usuario } from '../api/tipos'
import { SessaoContext, type Sessao } from './contexto'

export function SessaoProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate()
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [carregando, setCarregando] = useState(() => tokenStorage.obter() !== null)

  const sair = useCallback(
    (aviso?: string) => {
      tokenStorage.limpar()
      setUsuario(null)
      navigate('/login', { replace: true, state: aviso ? { aviso } : undefined })
    },
    [navigate],
  )

  const recarregarUsuario = useCallback(async () => {
    setUsuario(await usuariosApi.meusDados())
  }, [])

  // ao abrir o sistema: se há um token salvo, confere se ainda vale
  useEffect(() => {
    if (!tokenStorage.obter()) {
      return
    }
    usuariosApi
      .meusDados()
      .then(setUsuario)
      .catch(() => tokenStorage.limpar())
      .finally(() => setCarregando(false))
  }, [])

  // token recusado pela API no meio do uso (expirou, usuário desativado, senha trocada)
  useEffect(() => {
    definirAoPerderSessao(() => sair('Sua sessão expirou. Entre novamente.'))
  }, [sair])

  const entrar = useCallback(async (login: string, senha: string) => {
    tokenStorage.limpar()
    const resposta = await authApi.login(login, senha)
    tokenStorage.salvar(resposta.accessToken)
    setUsuario(resposta.usuario)
  }, [])

  const pode = useCallback(
    (...permissoes: Permissao[]) =>
      usuario !== null && permissoes.some((p) => usuario.permissoes.includes(p)),
    [usuario],
  )

  const sessao = useMemo<Sessao>(
    () => ({ usuario, carregando, entrar, sair, pode, recarregarUsuario }),
    [usuario, carregando, entrar, sair, pode, recarregarUsuario],
  )

  return <SessaoContext.Provider value={sessao}>{children}</SessaoContext.Provider>
}
