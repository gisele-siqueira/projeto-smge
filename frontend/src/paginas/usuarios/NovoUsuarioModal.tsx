import { App, Col, Divider, Form, Input, Modal, Row } from 'antd'
import { useState } from 'react'

import { ApiError } from '../../api/cliente'
import { usuariosApi } from '../../api/endpoints'
import type { ModuloPermissoes, NovoUsuario, Perfil } from '../../api/tipos'
import { regraConfirmarSenha, regrasLogin, regrasSenha } from '../../util/regrasSenha'
import { CamposAcessos } from './CamposAcessos'

interface Props {
  aberto: boolean
  perfis: Perfil[]
  modulos: ModuloPermissoes[]
  aoFechar: () => void
  aoSalvar: () => void
}

type FormNovoUsuario = NovoUsuario & { confirmarSenha: string }

export function NovoUsuarioModal({ aberto, perfis, modulos, aoFechar, aoSalvar }: Props) {
  const { message } = App.useApp()
  const [form] = Form.useForm<FormNovoUsuario>()
  const [salvando, setSalvando] = useState(false)

  async function salvar(valores: FormNovoUsuario) {
    // confirmarSenha só existe na tela, não vai para a API
    const dados: NovoUsuario = {
      nome: valores.nome,
      login: valores.login,
      senha: valores.senha,
      perfisIds: valores.perfisIds ?? [],
      permissoesExtras: valores.permissoesExtras ?? [],
    }
    setSalvando(true)
    try {
      await usuariosApi.criar(dados)
      message.success(`Usuário ${dados.login} criado`)
      aoSalvar()
    } catch (e) {
      if (e instanceof ApiError && e.status === 409) {
        form.setFields([{ name: 'login', errors: [e.message] }])
      } else if (e instanceof ApiError && e.erros) {
        form.setFields(
          Object.entries(e.erros).map(([campo, erro]) => ({
            name: campo as keyof FormNovoUsuario,
            errors: [erro],
          })),
        )
      } else {
        message.error(e instanceof Error ? e.message : 'Erro ao criar o usuário')
      }
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open={aberto}
      title="Novo usuário"
      okText="Criar usuário"
      cancelText="Cancelar"
      width={800}
      confirmLoading={salvando}
      onOk={() => form.submit()}
      onCancel={aoFechar}
      destroyOnHidden
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{ perfisIds: [], permissoesExtras: [] }}
        onFinish={salvar}
        disabled={salvando}
      >
        <Row gutter={16}>
          <Col xs={24} md={12}>
            <Form.Item
              name="nome"
              label="Nome completo"
              rules={[
                { required: true, whitespace: true, message: 'Informe o nome' },
                { max: 100, message: 'Máximo de 100 caracteres' },
              ]}
            >
              <Input />
            </Form.Item>
          </Col>
          <Col xs={24} md={12}>
            <Form.Item name="login" label="Login" rules={regrasLogin} extra="Salvo em letras minúsculas.">
              <Input autoComplete="off" />
            </Form.Item>
          </Col>
          <Col xs={24} md={12}>
            <Form.Item
              name="senha"
              label="Senha provisória"
              rules={regrasSenha}
              extra="Entregue ao usuário. Ele pode trocar em Minha conta."
            >
              <Input.Password autoComplete="new-password" />
            </Form.Item>
          </Col>
          <Col xs={24} md={12}>
            <Form.Item
              name="confirmarSenha"
              label="Confirmar senha"
              dependencies={['senha']}
              rules={[{ required: true, message: 'Confirme a senha' }, regraConfirmarSenha('senha')]}
            >
              <Input.Password autoComplete="new-password" />
            </Form.Item>
          </Col>
        </Row>

        <Divider titlePlacement="start">Acessos</Divider>
        <CamposAcessos perfis={perfis} modulos={modulos} />
      </Form>
    </Modal>
  )
}
