import {
  CheckCircleOutlined,
  KeyOutlined,
  PlusOutlined,
  SafetyOutlined,
  SearchOutlined,
  StopOutlined,
} from '@ant-design/icons'
import { Alert, App, Button, Flex, Input, Popconfirm, Space, Table, Tag, Tooltip, Typography } from 'antd'
import { useCallback, useState } from 'react'

import { perfisApi, usuariosApi } from '../../api/endpoints'
import type { ModuloPermissoes, Perfil, Usuario } from '../../api/tipos'
import { useSessao } from '../../auth/useSessao'
import { Pagina } from '../../layout/Pagina'
import { diasAte, formatarData } from '../../util/formato'
import { useCarregar } from '../../util/useCarregar'
import { AcessosModal } from './AcessosModal'
import { NovoUsuarioModal } from './NovoUsuarioModal'
import { RedefinirSenhaModal } from './RedefinirSenhaModal'

export function UsuariosPage() {
  const { usuario: eu, pode } = useSessao()
  const { message } = App.useApp()
  const podeGerenciar = pode('USUARIO_GERENCIAR')

  const usuarios = useCarregar<Usuario[]>(usuariosApi.listar, [])
  // perfis e permissões só são necessários (e liberados pela API) para quem gerencia
  const perfis = useCarregar<Perfil[]>(
    useCallback(() => (podeGerenciar ? perfisApi.listar() : Promise.resolve([])), [podeGerenciar]),
    [],
  )
  const modulos = useCarregar<ModuloPermissoes[]>(
    useCallback(
      () => (podeGerenciar ? perfisApi.listarPermissoes() : Promise.resolve([])),
      [podeGerenciar],
    ),
    [],
  )

  const [busca, setBusca] = useState('')
  const [novoAberto, setNovoAberto] = useState(false)
  const [editandoAcessos, setEditandoAcessos] = useState<Usuario | null>(null)
  const [redefinindoSenha, setRedefinindoSenha] = useState<Usuario | null>(null)

  const termo = busca.trim().toLowerCase()
  const filtrados = usuarios.dados.filter(
    (u) => !termo || u.nome.toLowerCase().includes(termo) || u.login.includes(termo),
  )

  // mesma regra do back-end: só gerencia quem não tem mais acessos que você
  function podeGerenciarUsuario(alvo: Usuario) {
    return podeGerenciar && alvo.login !== eu?.login && alvo.permissoes.every((p) => pode(p))
  }

  async function alternarAtivo(alvo: Usuario) {
    try {
      if (alvo.ativo) {
        await usuariosApi.desativar(alvo.userId)
        message.success(`${alvo.nome} foi desativado`)
      } else {
        await usuariosApi.reativar(alvo.userId)
        message.success(`${alvo.nome} foi reativado`)
      }
      await usuarios.recarregar()
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Erro ao alterar o usuário')
    }
  }

  return (
    <Pagina
      titulo="Usuários"
      descricao="Contas de acesso ao sistema. Não há auto-cadastro: as contas são criadas aqui."
      acoes={
        podeGerenciar && (
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setNovoAberto(true)}>
            Novo usuário
          </Button>
        )
      }
    >
      <Flex vertical gap={16}>
        <Input
          allowClear
          prefix={<SearchOutlined />}
          placeholder="Buscar por nome ou login"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          style={{ maxWidth: 360 }}
        />

        {usuarios.erro && <Alert type="error" showIcon title={usuarios.erro} />}

        <Table<Usuario>
          rowKey="userId"
          loading={usuarios.carregando}
          dataSource={filtrados}
          pagination={{ pageSize: 10, hideOnSinglePage: true }}
          scroll={{ x: 900 }}
          columns={[
            {
              title: 'Nome',
              dataIndex: 'nome',
              sorter: (a, b) => a.nome.localeCompare(b.nome),
              defaultSortOrder: 'ascend',
              render: (nome: string, u) => (
                <Space orientation="vertical" size={0}>
                  <Typography.Text strong>{nome}</Typography.Text>
                  <Typography.Text type="secondary">{u.login}</Typography.Text>
                </Space>
              ),
            },
            {
              title: 'Perfis',
              dataIndex: 'perfis',
              render: (perfisDoUsuario: Usuario['perfis'], u) => (
                <Flex wrap gap={4}>
                  {perfisDoUsuario.map((p) => (
                    <Tag key={p.perfilId} color="blue">
                      {p.nome}
                    </Tag>
                  ))}
                  {u.permissoesExtras.length > 0 && (
                    <Tooltip title={u.permissoesExtras.join(', ')}>
                      <Tag color="purple">+{u.permissoesExtras.length} extra(s)</Tag>
                    </Tooltip>
                  )}
                  {perfisDoUsuario.length === 0 && u.permissoesExtras.length === 0 && (
                    <Typography.Text type="secondary">Sem acessos</Typography.Text>
                  )}
                </Flex>
              ),
            },
            {
              title: 'Situação',
              dataIndex: 'ativo',
              width: 110,
              filters: [
                { text: 'Ativo', value: true },
                { text: 'Inativo', value: false },
              ],
              onFilter: (valor, u) => u.ativo === valor,
              render: (ativo: boolean) =>
                ativo ? <Tag color="green">Ativo</Tag> : <Tag color="default">Inativo</Tag>,
            },
            {
              title: 'Senha expira em',
              dataIndex: 'senhaExpiraEm',
              width: 150,
              render: (data: string) => {
                const dias = diasAte(data)
                const cor = dias < 0 ? 'red' : dias <= 7 ? 'orange' : undefined
                return (
                  <Tooltip title={dias < 0 ? 'Senha expirada: redefina para liberar o acesso' : undefined}>
                    <Tag color={cor}>{formatarData(data)}</Tag>
                  </Tooltip>
                )
              },
            },
            {
              title: 'Ações',
              key: 'acoes',
              width: 150,
              hidden: !podeGerenciar,
              render: (_, u) => {
                if (u.login === eu?.login) {
                  return <Typography.Text type="secondary">Você</Typography.Text>
                }
                if (!podeGerenciarUsuario(u)) {
                  return (
                    <Tooltip title="Este usuário tem acessos que você não possui">
                      <Typography.Text type="secondary">—</Typography.Text>
                    </Tooltip>
                  )
                }
                return (
                  <Space>
                    <Tooltip title="Acessos">
                      <Button icon={<SafetyOutlined />} onClick={() => setEditandoAcessos(u)} />
                    </Tooltip>
                    <Tooltip title="Redefinir senha">
                      <Button icon={<KeyOutlined />} onClick={() => setRedefinindoSenha(u)} />
                    </Tooltip>
                    <Popconfirm
                      title={u.ativo ? 'Desativar usuário?' : 'Reativar usuário?'}
                      description={u.ativo ? 'Ele perde o acesso na hora.' : 'Ele volta a poder entrar.'}
                      okText={u.ativo ? 'Desativar' : 'Reativar'}
                      okButtonProps={{ danger: u.ativo }}
                      cancelText="Cancelar"
                      onConfirm={() => alternarAtivo(u)}
                    >
                      <Tooltip title={u.ativo ? 'Desativar' : 'Reativar'}>
                        <Button
                          danger={u.ativo}
                          icon={u.ativo ? <StopOutlined /> : <CheckCircleOutlined />}
                        />
                      </Tooltip>
                    </Popconfirm>
                  </Space>
                )
              },
            },
          ]}
        />
      </Flex>

      <NovoUsuarioModal
        aberto={novoAberto}
        perfis={perfis.dados}
        modulos={modulos.dados}
        aoFechar={() => setNovoAberto(false)}
        aoSalvar={() => {
          setNovoAberto(false)
          void usuarios.recarregar()
        }}
      />
      <AcessosModal
        usuario={editandoAcessos}
        perfis={perfis.dados}
        modulos={modulos.dados}
        aoFechar={() => setEditandoAcessos(null)}
        aoSalvar={() => {
          setEditandoAcessos(null)
          void usuarios.recarregar()
        }}
      />
      <RedefinirSenhaModal usuario={redefinindoSenha} aoFechar={() => setRedefinindoSenha(null)} />
    </Pagina>
  )
}
