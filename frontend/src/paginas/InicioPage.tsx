import { AppstoreOutlined, ClockCircleOutlined, SafetyCertificateOutlined } from '@ant-design/icons'
import { Alert, Card, Col, Empty, Flex, Row, Typography } from 'antd'
import { motion, type Variants } from 'framer-motion'
import { Link } from 'react-router-dom'

import { useSessao } from '../auth/useSessao'
import { filtrarMenu, MENU, type ItemMenu } from '../layout/menu'
import { corDoIndice } from '../util/cores'
import { diasAte } from '../util/formato'

const DIAS_AVISO_SENHA = 7

const containerVariants: Variants = {
  oculto: {},
  visivel: { transition: { staggerChildren: 0.06 } },
}

const itemVariants: Variants = {
  oculto: { opacity: 0, y: 16 },
  visivel: { opacity: 1, y: 0, transition: { duration: 0.35, ease: 'easeOut' } },
}

export function InicioPage() {
  const { usuario, pode } = useSessao()

  if (!usuario) {
    return null
  }

  // atalhos para todas as telas que o usuário pode abrir (menos o próprio Início)
  const atalhos: ItemMenu[] = filtrarMenu(MENU, pode)
    .flatMap((item) => item.filhos ?? [item])
    .filter((item) => item.chave !== '/')

  const diasParaExpirar = diasAte(usuario.senhaExpiraEm)

  return (
    <Flex vertical gap={24}>
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.35, ease: 'easeOut' }}
        style={{
          position: 'relative',
          overflow: 'hidden',
          borderRadius: 20,
          padding: '32px 32px',
          background: 'linear-gradient(135deg, #0d1b2e 0%, #15213b 55%, #0f3b3a 100%)',
        }}
      >
        <div
          className="blob-login"
          style={{ width: 280, height: 280, background: '#2dd4bf', top: '-35%', right: '-8%', opacity: 0.3 }}
        />
        <div style={{ position: 'relative', zIndex: 1 }}>
          <Typography.Title level={3} style={{ margin: 0, color: '#fff' }}>
            Olá, {usuario.nome.split(' ')[0]}!
          </Typography.Title>
          <Typography.Text style={{ color: 'rgba(255, 255, 255, 0.72)' }}>
            Escolha por onde começar.
          </Typography.Text>

          <Flex gap={10} wrap style={{ marginTop: 20 }}>
            <span className="chip-glass">
              <AppstoreOutlined />
              {atalhos.length} {atalhos.length === 1 ? 'tela disponível' : 'telas disponíveis'}
            </span>
            {usuario.perfis.length > 0 && (
              <span className="chip-glass">
                <SafetyCertificateOutlined />
                {usuario.perfis.map((p) => p.nome).join(', ')}
              </span>
            )}
            <span className="chip-glass">
              <ClockCircleOutlined />
              {diasParaExpirar < 0
                ? 'Senha expirada'
                : `Senha expira em ${diasParaExpirar} dia(s)`}
            </span>
          </Flex>
        </div>
      </motion.div>

      {diasParaExpirar <= DIAS_AVISO_SENHA && (
        <Alert
          type="warning"
          showIcon
          title={`Sua senha expira em ${Math.max(diasParaExpirar, 0)} dia(s).`}
          description={
            <>
              Troque agora em <Link to="/minha-conta">Minha conta</Link> para não perder o acesso.
            </>
          }
        />
      )}

      {atalhos.length === 0 ? (
        <Card>
          <Empty description="Você ainda não tem acesso a nenhuma tela. Fale com o administrador." />
        </Card>
      ) : (
        <motion.div variants={containerVariants} initial="oculto" animate="visivel">
          <Row gutter={[16, 16]}>
            {atalhos.map((item, indice) => (
              <Col key={item.chave} xs={24} sm={12} lg={8}>
                <motion.div variants={itemVariants} whileHover={{ y: -4 }} whileTap={{ scale: 0.98 }}>
                  <Link to={item.chave}>
                    <Card hoverable styles={{ body: { padding: 20 } }}>
                      <Flex gap={16} align="center">
                        <div
                          style={{
                            width: 48,
                            height: 48,
                            flexShrink: 0,
                            borderRadius: 12,
                            background: corDoIndice(indice),
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            fontSize: 22,
                            color: '#fff',
                          }}
                        >
                          {item.icone}
                        </div>
                        <Typography.Text strong style={{ fontSize: 16 }}>
                          {item.titulo}
                        </Typography.Text>
                      </Flex>
                    </Card>
                  </Link>
                </motion.div>
              </Col>
            ))}
          </Row>
        </motion.div>
      )}
    </Flex>
  )
}
