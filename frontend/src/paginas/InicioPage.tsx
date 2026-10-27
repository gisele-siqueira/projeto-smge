import { Alert, Card, Col, Empty, Flex, Row, Typography } from 'antd'
import { Link } from 'react-router-dom'

import { useSessao } from '../auth/useSessao'
import { filtrarMenu, MENU, type ItemMenu } from '../layout/menu'
import { diasAte } from '../util/formato'

const DIAS_AVISO_SENHA = 7

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
      <div>
        <Typography.Title level={3} style={{ margin: 0 }}>
          Olá, {usuario.nome.split(' ')[0]}!
        </Typography.Title>
        <Typography.Text type="secondary">Escolha por onde começar.</Typography.Text>
      </div>

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
        <Row gutter={[16, 16]}>
          {atalhos.map((item) => (
            <Col key={item.chave} xs={24} sm={12} lg={8}>
              <Link to={item.chave}>
                <Card hoverable>
                  <Flex gap={16} align="center">
                    <span style={{ fontSize: 28 }}>{item.icone}</span>
                    <Typography.Text strong style={{ fontSize: 16 }}>
                      {item.titulo}
                    </Typography.Text>
                  </Flex>
                </Card>
              </Link>
            </Col>
          ))}
        </Row>
      )}
    </Flex>
  )
}
