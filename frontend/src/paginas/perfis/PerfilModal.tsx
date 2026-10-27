import { App, Form, Input, Modal } from 'antd'
import { useState } from 'react'

import { ApiError } from '../../api/cliente'
import { perfisApi } from '../../api/endpoints'
import type { ModuloPermissoes, Perfil, PerfilForm } from '../../api/tipos'
import { SeletorPermissoes } from '../../componentes/SeletorPermissoes'

interface Props {
  aberto: boolean
  // perfil sendo editado; null = criando um novo
  perfil: Perfil | null
  modulos: ModuloPermissoes[]
  aoFechar: () => void
  aoSalvar: () => void
}

export function PerfilModal({ aberto, perfil, modulos, aoFechar, aoSalvar }: Props) {
  const { message } = App.useApp()
  const [form] = Form.useForm<PerfilForm>()
  const [salvando, setSalvando] = useState(false)

  const valoresIniciais: PerfilForm = perfil
    ? { nome: perfil.nome, descricao: perfil.descricao ?? '', permissoes: perfil.permissoes }
    : { nome: '', descricao: '', permissoes: [] }

  async function salvar(valores: PerfilForm) {
    setSalvando(true)
    try {
      if (perfil) {
        await perfisApi.atualizar(perfil.perfilId, valores)
        message.success('Perfil atualizado')
      } else {
        await perfisApi.criar(valores)
        message.success('Perfil criado')
      }
      aoSalvar()
    } catch (e) {
      if (e instanceof ApiError && e.status === 409) {
        form.setFields([{ name: 'nome', errors: [e.message] }])
      } else {
        message.error(e instanceof Error ? e.message : 'Erro ao salvar o perfil')
      }
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open={aberto}
      title={perfil ? `Editar perfil: ${perfil.nome}` : 'Novo perfil'}
      okText="Salvar"
      cancelText="Cancelar"
      width={760}
      confirmLoading={salvando}
      onOk={() => form.submit()}
      onCancel={aoFechar}
      destroyOnHidden
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={valoresIniciais}
        onFinish={salvar}
        disabled={salvando}
      >
        <Form.Item
          name="nome"
          label="Nome"
          rules={[
            { required: true, whitespace: true, message: 'Informe o nome' },
            { max: 60, message: 'Máximo de 60 caracteres' },
          ]}
        >
          <Input placeholder="Ex.: Estoquista" />
        </Form.Item>
        <Form.Item name="descricao" label="Descrição" rules={[{ max: 255, message: 'Máximo de 255 caracteres' }]}>
          <Input.TextArea rows={2} placeholder="Para que serve este perfil" />
        </Form.Item>
        <Form.Item
          name="permissoes"
          label="Permissões"
          rules={[{ required: true, type: 'array', min: 1, message: 'Marque ao menos uma permissão' }]}
        >
          <SeletorPermissoes modulos={modulos} />
        </Form.Item>
      </Form>
    </Modal>
  )
}
