import { DeleteOutlined, EditOutlined, LockOutlined, PlusOutlined } from '@ant-design/icons'
import { Alert, App, Button, Popconfirm, Space, Table, Tag, Tooltip, Typography } from 'antd'
import { useState } from 'react'

import { perfisApi } from '../../api/endpoints'
import type { ModuloPermissoes, Perfil } from '../../api/tipos'
import { Pagina } from '../../layout/Pagina'
import { useCarregar } from '../../util/useCarregar'
import { PerfilModal } from './PerfilModal'

export function PerfisPage() {
  const { message } = App.useApp()
  const perfis = useCarregar<Perfil[]>(perfisApi.listar, [])
  const modulos = useCarregar<ModuloPermissoes[]>(perfisApi.listarPermissoes, [])

  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<Perfil | null>(null)

  // código -> descrição, para mostrar nomes legíveis na tabela
  const descricoes = new Map(
    modulos.dados.flatMap((m) => m.permissoes.map((p) => [p.codigo, p.descricao] as const)),
  )

  function abrir(perfil: Perfil | null) {
    setEditando(perfil)
    setModalAberto(true)
  }

  async function excluir(perfil: Perfil) {
    try {
      await perfisApi.excluir(perfil.perfilId)
      message.success('Perfil excluído')
      await perfis.recarregar()
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Erro ao excluir o perfil')
    }
  }

  return (
    <Pagina
      titulo="Perfis de acesso"
      descricao="Grupos de permissões atribuídos aos usuários (ex.: Estoquista, Faturamento)."
      acoes={
        <Button type="primary" icon={<PlusOutlined />} onClick={() => abrir(null)}>
          Novo perfil
        </Button>
      }
    >
      {perfis.erro && <Alert type="error" showIcon title={perfis.erro} style={{ marginBottom: 16 }} />}

      <Table<Perfil>
        rowKey="perfilId"
        loading={perfis.carregando}
        dataSource={perfis.dados}
        pagination={false}
        scroll={{ x: 720 }}
        columns={[
          {
            title: 'Nome',
            dataIndex: 'nome',
            width: 220,
            render: (nome: string, perfil) => (
              <Space>
                <Typography.Text strong>{nome}</Typography.Text>
                {perfil.sistema && (
                  <Tooltip title="Perfil do sistema: tem todas as permissões e não pode ser alterado">
                    <Tag icon={<LockOutlined />}>Sistema</Tag>
                  </Tooltip>
                )}
              </Space>
            ),
          },
          { title: 'Descrição', dataIndex: 'descricao', render: (d: string | null) => d || '—' },
          {
            title: 'Permissões',
            dataIndex: 'permissoes',
            width: 160,
            render: (permissoes: Perfil['permissoes']) => (
              <Tooltip
                title={
                  <ul style={{ margin: 0, paddingInlineStart: 16 }}>
                    {permissoes.map((p) => (
                      <li key={p}>{descricoes.get(p) ?? p}</li>
                    ))}
                  </ul>
                }
              >
                <Tag color="blue">{permissoes.length} permissão(ões)</Tag>
              </Tooltip>
            ),
          },
          {
            title: 'Ações',
            key: 'acoes',
            width: 120,
            render: (_, perfil) =>
              perfil.sistema ? null : (
                <Space>
                  <Tooltip title="Editar">
                    <Button icon={<EditOutlined />} onClick={() => abrir(perfil)} />
                  </Tooltip>
                  <Popconfirm
                    title="Excluir perfil?"
                    description="Só é possível excluir perfis que não estão em uso."
                    okText="Excluir"
                    okButtonProps={{ danger: true }}
                    cancelText="Cancelar"
                    onConfirm={() => excluir(perfil)}
                  >
                    <Tooltip title="Excluir">
                      <Button danger icon={<DeleteOutlined />} />
                    </Tooltip>
                  </Popconfirm>
                </Space>
              ),
          },
        ]}
      />

      <PerfilModal
        aberto={modalAberto}
        perfil={editando}
        modulos={modulos.dados}
        aoFechar={() => setModalAberto(false)}
        aoSalvar={() => {
          setModalAberto(false)
          void perfis.recarregar()
        }}
      />
    </Pagina>
  )
}
