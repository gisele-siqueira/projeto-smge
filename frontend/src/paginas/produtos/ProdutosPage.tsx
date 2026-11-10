import {
  DeleteOutlined,
  EditOutlined,
  PlusOutlined,
  SearchOutlined,
  TagsFilled,
  WarningOutlined,
} from '@ant-design/icons'
import { Alert, App, Button, Flex, Input, Popconfirm, Space, Table, Tag, Tooltip, Typography } from 'antd'
import { useState } from 'react'

import { produtosApi } from '../../api/endpoints'
import type { Produto } from '../../api/tipos'
import { useSessao } from '../../auth/useSessao'
import { Pagina } from '../../layout/Pagina'
import { corDoIndice } from '../../util/cores'
import { formatarData, formatarMoeda } from '../../util/formato'
import { useCarregar } from '../../util/useCarregar'
import { ProdutoModal } from './ProdutoModal'

export function ProdutosPage() {
  const { pode } = useSessao()
  const { message } = App.useApp()
  const podeCriar = pode('PRODUTO_CRIAR')
  const podeEditar = pode('PRODUTO_EDITAR')
  const podeExcluir = pode('PRODUTO_EXCLUIR')

  const produtos = useCarregar<Produto[]>(produtosApi.listar, [])

  const [busca, setBusca] = useState('')
  const [modalAberto, setModalAberto] = useState(false)
  const [editando, setEditando] = useState<Produto | null>(null)

  const termo = busca.trim().toLowerCase()
  const filtrados = produtos.dados.filter(
    (p) =>
      !termo ||
      p.nome.toLowerCase().includes(termo) ||
      p.codigo.toLowerCase().includes(termo) ||
      p.categoria.toLowerCase().includes(termo),
  )

  function abrir(produto: Produto | null) {
    setEditando(produto)
    setModalAberto(true)
  }

  async function excluir(produto: Produto) {
    try {
      await produtosApi.excluir(produto.productId)
      message.success('Produto excluído')
      await produtos.recarregar()
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Erro ao excluir o produto')
    }
  }

  return (
    <Pagina
      titulo="Produtos"
      descricao="Itens do estoque: quantidade, preço e unidade de medida."
      icone={<TagsFilled />}
      cor={corDoIndice(2)}
      acoes={
        podeCriar && (
          <Button type="primary" icon={<PlusOutlined />} onClick={() => abrir(null)}>
            Novo produto
          </Button>
        )
      }
    >
      <Flex vertical gap={16}>
        <Input
          allowClear
          prefix={<SearchOutlined />}
          placeholder="Buscar por código, nome ou categoria"
          value={busca}
          onChange={(e) => setBusca(e.target.value)}
          style={{ maxWidth: 360 }}
        />

        {produtos.erro && <Alert type="error" showIcon title={produtos.erro} />}

        <Table<Produto>
          rowKey="productId"
          loading={produtos.carregando}
          dataSource={filtrados}
          pagination={{ pageSize: 10, hideOnSinglePage: true }}
          scroll={{ x: 960 }}
          columns={[
            {
              title: 'Produto',
              dataIndex: 'nome',
              sorter: (a, b) => a.nome.localeCompare(b.nome),
              defaultSortOrder: 'ascend',
              render: (nome: string, p) => (
                <Space orientation="vertical" size={0}>
                  <Typography.Text strong>{nome}</Typography.Text>
                  <Typography.Text type="secondary">{p.codigo}</Typography.Text>
                </Space>
              ),
            },
            {
              title: 'Categoria',
              dataIndex: 'categoria',
              render: (categoria: string) => <Tag color="blue">{categoria}</Tag>,
            },
            {
              title: 'Estoque',
              dataIndex: 'quantidade',
              width: 160,
              sorter: (a, b) => a.quantidade - b.quantidade,
              render: (quantidade: number, p) => (
                <Space>
                  <Typography.Text>
                    {quantidade} {p.unidadeMedida}
                  </Typography.Text>
                  {p.estoqueBaixo && (
                    <Tooltip title={`Mínimo: ${p.quantidadeMinima} ${p.unidadeMedida}`}>
                      <Tag color="red" icon={<WarningOutlined />}>
                        Baixo
                      </Tag>
                    </Tooltip>
                  )}
                </Space>
              ),
            },
            {
              title: 'Preço unitário',
              dataIndex: 'precoUnitario',
              width: 140,
              sorter: (a, b) => a.precoUnitario - b.precoUnitario,
              render: (preco: number) => formatarMoeda(preco),
            },
            {
              title: 'Cadastrado em',
              dataIndex: 'dataCadastro',
              width: 130,
              render: (data: string) => formatarData(data),
            },
            {
              title: 'Ações',
              key: 'acoes',
              width: 110,
              hidden: !podeEditar && !podeExcluir,
              render: (_, p) => (
                <Space>
                  {podeEditar && (
                    <Tooltip title="Editar">
                      <Button icon={<EditOutlined />} onClick={() => abrir(p)} />
                    </Tooltip>
                  )}
                  {podeExcluir && (
                    <Popconfirm
                      title="Excluir produto?"
                      description="Esta ação não pode ser desfeita."
                      okText="Excluir"
                      okButtonProps={{ danger: true }}
                      cancelText="Cancelar"
                      onConfirm={() => excluir(p)}
                    >
                      <Tooltip title="Excluir">
                        <Button danger icon={<DeleteOutlined />} />
                      </Tooltip>
                    </Popconfirm>
                  )}
                </Space>
              ),
            },
          ]}
        />
      </Flex>

      <ProdutoModal
        aberto={modalAberto}
        produto={editando}
        aoFechar={() => setModalAberto(false)}
        aoSalvar={() => {
          setModalAberto(false)
          void produtos.recarregar()
        }}
      />
    </Pagina>
  )
}
