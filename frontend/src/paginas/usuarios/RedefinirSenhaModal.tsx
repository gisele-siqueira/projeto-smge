import { App, Form, Input, Modal, Typography } from 'antd'
import { useState } from 'react'

import { usuariosApi } from '../../api/endpoints'
import type { Usuario } from '../../api/tipos'
import { regraConfirmarSenha, regrasSenha } from '../../util/regrasSenha'

interface Props {
  usuario: Usuario | null
  aoFechar: () => void
}

export function RedefinirSenhaModal({ usuario, aoFechar }: Props) {
  const { message } = App.useApp()
  const [form] = Form.useForm<{ novaSenha: string; confirmarSenha: string }>()
  const [salvando, setSalvando] = useState(false)

  async function salvar({ novaSenha }: { novaSenha: string }) {
    if (!usuario) {
      return
    }
    setSalvando(true)
    try {
      await usuariosApi.redefinirSenha(usuario.userId, novaSenha)
      message.success(`Senha de ${usuario.nome} redefinida`)
      aoFechar()
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Erro ao redefinir a senha')
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open={usuario !== null}
      title={`Redefinir senha de ${usuario?.nome ?? ''}`}
      okText="Redefinir"
      cancelText="Cancelar"
      confirmLoading={salvando}
      onOk={() => form.submit()}
      onCancel={aoFechar}
      destroyOnHidden
    >
      <Typography.Paragraph type="secondary">
        A sessão atual do usuário será encerrada e ele precisará entrar com a nova senha.
      </Typography.Paragraph>
      <Form form={form} layout="vertical" onFinish={salvar} disabled={salvando}>
        <Form.Item name="novaSenha" label="Nova senha" rules={regrasSenha}>
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
      </Form>
    </Modal>
  )
}
