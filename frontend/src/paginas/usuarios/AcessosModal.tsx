import { App, Form, Modal } from 'antd'
import { useState } from 'react'

import { usuariosApi } from '../../api/endpoints'
import type { Acessos, ModuloPermissoes, Perfil, Usuario } from '../../api/tipos'
import { CamposAcessos } from './CamposAcessos'

interface Props {
  usuario: Usuario | null
  perfis: Perfil[]
  modulos: ModuloPermissoes[]
  aoFechar: () => void
  aoSalvar: () => void
}

export function AcessosModal({ usuario, perfis, modulos, aoFechar, aoSalvar }: Props) {
  const { message } = App.useApp()
  const [form] = Form.useForm<Acessos>()
  const [salvando, setSalvando] = useState(false)

  async function salvar(acessos: Acessos) {
    if (!usuario) {
      return
    }
    setSalvando(true)
    try {
      await usuariosApi.atualizarAcessos(usuario.userId, acessos)
      message.success(`Acessos de ${usuario.nome} atualizados`)
      aoSalvar()
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Erro ao salvar os acessos')
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open={usuario !== null}
      title={`Acessos de ${usuario?.nome ?? ''}`}
      okText="Salvar acessos"
      cancelText="Cancelar"
      width={800}
      confirmLoading={salvando}
      onOk={() => form.submit()}
      onCancel={aoFechar}
      destroyOnHidden
    >
      {usuario && (
        <Form
          form={form}
          layout="vertical"
          initialValues={{
            perfisIds: usuario.perfis.map((p) => p.perfilId),
            permissoesExtras: usuario.permissoesExtras,
          }}
          onFinish={salvar}
          disabled={salvando}
        >
          <CamposAcessos perfis={perfis} modulos={modulos} />
        </Form>
      )}
    </Modal>
  )
}
