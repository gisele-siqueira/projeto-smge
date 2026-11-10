import { DollarCircleFilled, PlusOutlined, StopOutlined } from '@ant-design/icons'
import { Alert, App, Button, Popconfirm, Table, Tag, Tooltip, Typography } from 'antd'
import { useState } from 'react'

import { produtosApi, vendasApi } from '../../api/endpoints'
import type { Produto, Venda } from '../../api/tipos'
import { useSessao } from '../../auth/useSessao'
import { Pagina } from '../../layout/Pagina'
import { corDoIndice } from '../../util/cores'
import { formatarDataHora, formatarMoeda } from '../../util/formato'
import { useCarregar } from '../../util/useCarregar'
import { NovaVendaModal } from './NovaVendaModal'

export function VendasPage() {
  const { pode } = useSessao()
  const { message } = App.useApp()
  const podeCriar = pode('VENDA_CRIAR')
  const podeCancelar = pode('VENDA_CANCELAR')

  const vendas = useCarregar<Venda[]>(vendasApi.listar, [])
  const produtos = useCarregar<Produto[]>(produtosApi.listar, [])

  const [modalAberto, setModalAberto] = useState(false)

  async function cancelar(venda: Venda) {
    try {
      await vendasApi.cancelar(venda.vendaId)
      message.success('Venda cancelada e estoque devolvido')
      await vendas.recarregar()
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Erro ao cancelar a venda')
    }
  }

  return (
    <Pagina
      titulo="Vendas"
      descricao="Registro de vendas, com baixa automática no estoque."
      icone={<DollarCircleFilled />}
      cor={corDoIndice(3)}
      acoes={
        podeCriar && (
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setModalAberto(true)}>
            Nova venda
          </Button>
        )
      }
    >
      {vendas.erro && <Alert type="error" showIcon title={vendas.erro} style={{ marginBottom: 16 }} />}

      <Table<Venda>
        rowKey="vendaId"
        loading={vendas.carregando}
        dataSource={vendas.dados}
        pagination={{ pageSize: 10, hideOnSinglePage: true }}
        scroll={{ x: 760 }}
        expandable={{
          rowExpandable: () => true,
          expandedRowRender: (venda) => (
            <Table
              rowKey="produtoId"
              size="small"
              pagination={false}
              dataSource={venda.itens}
              columns={[
                { title: 'Produto', dataIndex: 'produtoNome' },
                { title: 'Código', dataIndex: 'produtoCodigo', width: 120 },
                { title: 'Quantidade', dataIndex: 'quantidade', width: 110 },
                {
                  title: 'Preço unitário',
                  dataIndex: 'precoUnitario',
                  width: 140,
                  render: (preco: number) => formatarMoeda(preco),
                },
                {
                  title: 'Subtotal',
                  dataIndex: 'subtotal',
                  width: 120,
                  render: (subtotal: number) => formatarMoeda(subtotal),
                },
              ]}
            />
          ),
        }}
        columns={[
          {
            title: 'Data',
            dataIndex: 'dataVenda',
            width: 170,
            sorter: (a, b) => a.dataVenda.localeCompare(b.dataVenda),
            defaultSortOrder: 'descend',
            render: (data: string) => formatarDataHora(data),
          },
          {
            title: 'Itens',
            key: 'itens',
            width: 90,
            render: (_, venda) => venda.itens.reduce((soma, item) => soma + item.quantidade, 0),
          },
          {
            title: 'Valor total',
            dataIndex: 'valorTotal',
            width: 140,
            sorter: (a, b) => a.valorTotal - b.valorTotal,
            render: (valor: number) => <Typography.Text strong>{formatarMoeda(valor)}</Typography.Text>,
          },
          {
            title: 'Situação',
            dataIndex: 'status',
            width: 130,
            filters: [
              { text: 'Concluída', value: 'CONCLUIDA' },
              { text: 'Cancelada', value: 'CANCELADA' },
            ],
            onFilter: (valor, venda) => venda.status === valor,
            render: (status: Venda['status']) =>
              status === 'CONCLUIDA' ? (
                <Tag color="green">Concluída</Tag>
              ) : (
                <Tag color="default">Cancelada</Tag>
              ),
          },
          {
            title: 'Ações',
            key: 'acoes',
            width: 100,
            hidden: !podeCancelar,
            render: (_, venda) =>
              venda.status === 'CONCLUIDA' && (
                <Popconfirm
                  title="Cancelar venda?"
                  description="O estoque dos itens vendidos é devolvido."
                  okText="Cancelar venda"
                  okButtonProps={{ danger: true }}
                  cancelText="Voltar"
                  onConfirm={() => cancelar(venda)}
                >
                  <Tooltip title="Cancelar">
                    <Button danger icon={<StopOutlined />} />
                  </Tooltip>
                </Popconfirm>
              ),
          },
        ]}
      />

      <NovaVendaModal
        aberto={modalAberto}
        produtos={produtos.dados}
        aoFechar={() => setModalAberto(false)}
        aoSalvar={() => {
          setModalAberto(false)
          void vendas.recarregar()
          void produtos.recarregar()
        }}
      />
    </Pagina>
  )
}
