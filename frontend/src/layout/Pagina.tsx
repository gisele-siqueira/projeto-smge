import { Card, Flex, Typography } from 'antd'
import { motion } from 'framer-motion'
import type { ReactNode } from 'react'

interface Props {
  titulo: string
  descricao?: string
  // ícone do badge ao lado do título (ex.: <TeamOutlined />)
  icone?: ReactNode
  // gradiente do badge; veja util/cores.ts
  cor?: string
  // botões à direita do título (ex.: "Novo usuário")
  acoes?: ReactNode
  children: ReactNode
}

export function Pagina({ titulo, descricao, icone, cor, acoes, children }: Props) {
  return (
    <Flex vertical gap={16}>
      <Flex justify="space-between" align="center" wrap gap={12}>
        <motion.div
          initial={{ opacity: 0, x: -12 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.3, ease: 'easeOut' }}
        >
          <Flex align="center" gap={14}>
            {icone && (
              <div
                style={{
                  width: 44,
                  height: 44,
                  flexShrink: 0,
                  borderRadius: 12,
                  background: cor ?? 'linear-gradient(135deg, #1f6feb, #3b82f6)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: 20,
                  color: '#fff',
                }}
              >
                {icone}
              </div>
            )}
            <div>
              <Typography.Title level={3} style={{ margin: 0 }}>
                {titulo}
              </Typography.Title>
              {descricao && <Typography.Text type="secondary">{descricao}</Typography.Text>}
            </div>
          </Flex>
        </motion.div>
        {acoes}
      </Flex>
      <motion.div
        initial={{ opacity: 0, y: 14 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.3, delay: 0.05, ease: 'easeOut' }}
      >
        <Card variant="outlined">{children}</Card>
      </motion.div>
    </Flex>
  )
}
