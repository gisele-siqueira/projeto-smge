import { Card, Flex, Typography } from 'antd'
import type { ReactNode } from 'react'

interface Props {
  titulo: string
  descricao?: string
  // botões à direita do título (ex.: "Novo usuário")
  acoes?: ReactNode
  children: ReactNode
}

/**
 * Moldura padrão das telas: título, descrição, ações e conteúdo em um card.
 */
export function Pagina({ titulo, descricao, acoes, children }: Props) {
  return (
    <Flex vertical gap={16}>
      <Flex justify="space-between" align="center" wrap gap={12}>
        <div>
          <Typography.Title level={3} style={{ margin: 0 }}>
            {titulo}
          </Typography.Title>
          {descricao && <Typography.Text type="secondary">{descricao}</Typography.Text>}
        </div>
        {acoes}
      </Flex>
      <Card variant="outlined">{children}</Card>
    </Flex>
  )
}
