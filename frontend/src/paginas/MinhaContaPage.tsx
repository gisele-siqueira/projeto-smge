import { App, Button, Col, Descriptions, Divider, Form, Input, Row, Tag, Typography } from 'antd'
import { useState } from 'react'

import { usuariosApi } from '../api/endpoints'
import { useSessao } from '../auth/useSessao'
import { Pagina } from '../layout/Pagina'
import { formatarData } from '../util/formato'
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

  return (
    <Pagina titulo="Minha conta" descricao="Seus dados e a troca de senha.">
      <Descriptions column={{ xs: 1, md: 2 }} bordered size="small">
        <Descriptions.Item label="Nome">{usuario.nome}</Descriptions.Item>
        <Descriptions.Item label="Login">{usuario.login}</Descriptions.Item>
        <Descriptions.Item label="Perfis">
          {usuario.perfis.length
            ? usuario.perfis.map((p) => (
                <Tag key={p.perfilId} color="blue">
                  {p.nome}
                </Tag>
              ))
            : '—'}
        </Descriptions.Item>
        <Descriptions.Item label="Senha expira em">{formatarData(usuario.senhaExpiraEm)}</Descriptions.Item>
      </Descriptions>

      <Divider />

      <Typography.Title level={5}>Trocar senha</Typography.Title>
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
