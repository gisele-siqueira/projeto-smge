import { KeyOutlined, LoginOutlined, UserOutlined } from '@ant-design/icons'
import { App, Avatar, Button, Col, Divider, Flex, Form, Input, Row, Tag, Typography } from 'antd'
import { useState } from 'react'

import { usuariosApi } from '../api/endpoints'
import { useSessao } from '../auth/useSessao'
import { Pagina } from '../layout/Pagina'
import { corDoIndice } from '../util/cores'
import { diasAte, formatarData } from '../util/formato'
import { regraConfirmarSenha, regrasSenha } from '../util/regrasSenha'

interface FormSenha {
  senhaAtual: string
  novaSenha: string
  confirmarSenha: string
}

export function MinhaContaPage() {
  const { usuario, sair } = useSessao()
  const { message } = App.useApp()
  const [form] = Form.useForm<FormSenha>()
  const [salvando, setSalvando] = useState(false)

  if (!usuario) {
    return null
  }

  async function trocarSenha(valores: FormSenha) {
    setSalvando(true)
    try {
      await usuariosApi.alterarMinhaSenha(valores.senhaAtual, valores.novaSenha)
      // trocar a senha invalida o token atual no back-end: é preciso entrar de novo
      sair('Senha alterada com sucesso. Entre novamente com a nova senha.')
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Erro ao trocar a senha')
    } finally {
      setSalvando(false)
    }
  }

  const diasParaExpirar = diasAte(usuario.senhaExpiraEm)
  const corExpiracao = diasParaExpirar < 0 ? 'red' : diasParaExpirar <= 7 ? 'orange' : 'green'

  return (
    <Pagina titulo="Minha conta" descricao="Seus dados e a troca de senha." icone={<UserOutlined />} cor={corDoIndice(3)}>
      <Flex
        align="center"
        gap={20}
        wrap
        style={{
          padding: 20,
          borderRadius: 12,
          marginBottom: 24,
          background: `${corDoIndice(3)}`,
        }}
      >
        <Avatar size={72} style={{ background: 'rgba(255, 255, 255, 0.25)', flexShrink: 0 }} icon={<UserOutlined />} />
        <div>
          <Typography.Title level={4} style={{ margin: 0, color: '#fff' }}>
            {usuario.nome}
          </Typography.Title>
          <Flex align="center" gap={6} style={{ color: 'rgba(255, 255, 255, 0.85)' }}>
            <LoginOutlined />
            <Typography.Text style={{ color: 'rgba(255, 255, 255, 0.85)' }}>{usuario.login}</Typography.Text>
          </Flex>
        </div>
      </Flex>

      <Flex vertical gap={12} style={{ marginBottom: 24 }}>
        <div>
          <Typography.Text type="secondary">Perfis</Typography.Text>
          <div style={{ marginTop: 4 }}>
            {usuario.perfis.length ? (
              usuario.perfis.map((p) => (
                <Tag key={p.perfilId} color="blue">
                  {p.nome}
                </Tag>
              ))
            ) : (
              <Typography.Text type="secondary">—</Typography.Text>
            )}
          </div>
        </div>
        <div>
          <Typography.Text type="secondary">Senha expira em</Typography.Text>
          <div style={{ marginTop: 4 }}>
            <Tag color={corExpiracao}>{formatarData(usuario.senhaExpiraEm)}</Tag>
          </div>
        </div>
      </Flex>

      <Divider />

      <Flex align="center" gap={10} style={{ marginBottom: 16 }}>
        <div
          style={{
            width: 32,
            height: 32,
            borderRadius: 9,
            background: corDoIndice(3),
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#fff',
            fontSize: 15,
          }}
        >
          <KeyOutlined />
        </div>
        <Typography.Title level={5} style={{ margin: 0 }}>
          Trocar senha
        </Typography.Title>
      </Flex>
      <Row>
        <Col xs={24} md={12} lg={8}>
          <Form form={form} layout="vertical" onFinish={trocarSenha} disabled={salvando}>
            <Form.Item
              name="senhaAtual"
              label="Senha atual"
              rules={[{ required: true, message: 'Informe a senha atual' }]}
            >
              <Input.Password autoComplete="current-password" />
            </Form.Item>
            <Form.Item
              name="novaSenha"
              label="Nova senha"
              rules={regrasSenha}
              extra="De 8 a 64 caracteres, com letras e números."
            >
              <Input.Password autoComplete="new-password" />
            </Form.Item>
            <Form.Item
              name="confirmarSenha"
              label="Confirmar nova senha"
              dependencies={['novaSenha']}
              rules={[{ required: true, message: 'Confirme a nova senha' }, regraConfirmarSenha('novaSenha')]}
            >
              <Input.Password autoComplete="new-password" />
            </Form.Item>
            <Button type="primary" htmlType="submit" loading={salvando}>
              Trocar senha
            </Button>
          </Form>
        </Col>
      </Row>
    </Pagina>
  )
}
