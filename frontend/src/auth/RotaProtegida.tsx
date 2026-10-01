import { Button, Flex, Result, Spin } from 'antd'
import type { ReactNode } from 'react'
import { Link, Navigate, useLocation } from 'react-router-dom'

import type { Permissao } from '../api/tipos'
import { useSessao } from './useSessao'

interface Props {
  children: ReactNode
  // se informado, exige pelo menos uma destas permissões
  permissoes?: Permissao[]
}

/**
 * Esconde a tela de quem não está logado ou não tem permissão.
 * É só conveniência visual: quem protege os dados de verdade é o back-end.
 */
export function RotaProtegida({ children, permissoes }: Props) {
  const { usuario, carregando, pode } = useSessao()
  const location = useLocation()

  if (carregando) {
    return (
      <Flex justify="center" align="center" style={{ minHeight: '100vh' }}>
        <Spin size="large" />
      </Flex>
    )
  }

  if (!usuario) {
    return <Navigate to="/login" replace state={{ voltarPara: location.pathname }} />
  }

  if (permissoes && !pode(...permissoes)) {
    return (
      <Result
        status="403"
        title="Sem permissão"
        subTitle="Você não tem acesso a esta tela. Fale com o administrador se precisar."
        extra={
          <Link to="/">
            <Button type="primary">Voltar ao início</Button>
          </Link>
        }
      />
    )
  }

  return children
}
