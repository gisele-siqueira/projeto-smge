import { BarChartOutlined, FileExcelOutlined, FilePdfOutlined, SearchOutlined } from '@ant-design/icons'
import { App, Button, Col, DatePicker, Empty, Flex, Row, Statistic, Table, Typography } from 'antd'
import dayjs, { type Dayjs } from 'dayjs'
import { useState } from 'react'

import { vendasApi } from '../../api/endpoints'
import type { RelatorioVendas } from '../../api/tipos'
import { Pagina } from '../../layout/Pagina'
import { corDoIndice } from '../../util/cores'
import { formatarDataHora, formatarMoeda } from '../../util/formato'

export function RelatorioVendasPage() {
  const { message } = App.useApp()
  const [periodo, setPeriodo] = useState<[Dayjs, Dayjs]>([dayjs().startOf('month'), dayjs().endOf('month')])
  const [relatorio, setRelatorio] = useState<RelatorioVendas | null>(null)
  const [carregando, setCarregando] = useState(false)
  const [exportando, setExportando] = useState<'excel' | 'pdf' | null>(null)

  async function gerar() {
    setCarregando(true)
    try {
      const dados = await vendasApi.relatorio(periodo[0].format('YYYY-MM-DD'), periodo[1].format('YYYY-MM-DD'))
      setRelatorio(dados)
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Erro ao gerar o relatório')
    } finally {
      setCarregando(false)
    }
  }

  async function exportar(tipo: 'excel' | 'pdf') {
    setExportando(tipo)
    try {
      const inicio = periodo[0].format('YYYY-MM-DD')
      const fim = periodo[1].format('YYYY-MM-DD')
      await (tipo === 'excel' ? vendasApi.baixarExcel(inicio, fim) : vendasApi.baixarPdf(inicio, fim))
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Erro ao exportar o relatório')
    } finally {
      setExportando(null)
    }
  }

  return (
    <Pagina
      titulo="Relatório de vendas"
      descricao="Resumo de vendas por período, com exportação em Excel e PDF."
      icone={<BarChartOutlined />}
      cor={corDoIndice(4)}
    >
      <Flex vertical gap={20}>
        <Flex gap={12} wrap align="center">
          <DatePicker.RangePicker
            value={periodo}
            allowClear={false}
            format="DD/MM/YYYY"
            onChange={(valores) => {
              if (valores?.[0] && valores[1]) {
                setPeriodo([valores[0], valores[1]])
              }
            }}
          />
          <Button type="primary" icon={<SearchOutlined />} loading={carregando} onClick={gerar}>
            Gerar
          </Button>
          <Button
            icon={<FileExcelOutlined />}
            loading={exportando === 'excel'}
            disabled={!relatorio}
            onClick={() => exportar('excel')}
          >
            Exportar Excel
          </Button>
          <Button
            icon={<FilePdfOutlined />}
            loading={exportando === 'pdf'}
            disabled={!relatorio}
            onClick={() => exportar('pdf')}
          >
            Exportar PDF
          </Button>
        </Flex>

        {!relatorio ? (
          <Empty description="Escolha um período e clique em Gerar" />
        ) : (
          <Flex vertical gap={24}>
            <Row gutter={16}>
              <Col xs={24} sm={8}>
                <Statistic title="Vendas no período" value={relatorio.totalVendas} />
              </Col>
              <Col xs={24} sm={8}>
                <Statistic title="Itens vendidos" value={relatorio.totalItens} />
              </Col>
              <Col xs={24} sm={8}>
                <Statistic
                  title="Receita total"
                  value={relatorio.receitaTotal}
                  formatter={(valor) => formatarMoeda(Number(valor))}
                />
              </Col>
            </Row>

            <div>
              <Typography.Title level={5}>Vendas no período</Typography.Title>
              <Table
                rowKey="vendaId"
                size="small"
                pagination={{ pageSize: 5, hideOnSinglePage: true }}
                dataSource={relatorio.vendas}
                locale={{ emptyText: <Empty description="Nenhuma venda nesse período" /> }}
                columns={[
                  { title: 'Data', dataIndex: 'dataVenda', render: (d: string) => formatarDataHora(d) },
                  { title: 'Itens', dataIndex: 'quantidadeItens', width: 100 },
                  {
                    title: 'Valor total',
                    dataIndex: 'valorTotal',
                    width: 140,
                    render: (v: number) => formatarMoeda(v),
                  },
                ]}
              />
            </div>

            <div>
              <Typography.Title level={5}>Produtos mais vendidos</Typography.Title>
              <Table
                rowKey="produtoCodigo"
                size="small"
                pagination={{ pageSize: 5, hideOnSinglePage: true }}
                dataSource={relatorio.produtosMaisVendidos}
                locale={{ emptyText: <Empty description="Nenhum produto vendido nesse período" /> }}
                columns={[
                  { title: 'Código', dataIndex: 'produtoCodigo', width: 120 },
                  { title: 'Produto', dataIndex: 'produtoNome' },
                  { title: 'Quantidade vendida', dataIndex: 'quantidadeVendida', width: 160 },
                  {
                    title: 'Receita gerada',
                    dataIndex: 'receitaGerada',
                    width: 140,
                    render: (v: number) => formatarMoeda(v),
                  },
                ]}
              />
            </div>
          </Flex>
        )}
      </Flex>
    </Pagina>
  )
}
