import { Alert, Empty, Modal, Space, Table, Tag, Typography } from 'antd'
import { useCallback } from 'react'

import { produtosApi } from '../../api/endpoints'
import type { OperacaoAuditoria, Produto, ProdutoAuditoria } from '../../api/tipos'
import { formatarDataHora } from '../../util/formato'
import { useCarregar } from '../../util/useCarregar'

interface Props {
  // produto cujo histórico está sendo visto; null = modal fechado
  produto: Produto | null
  aoFechar: () => void
}

const ROTULO_CAMPO: Record<string, string> = {
  produto: 'Produto',
  codigo: 'Código',
  nome: 'Nome',
  descricao: 'Descrição',
  categoria: 'Categoria',
  quantidade: 'Quantidade',
  precoUnitario: 'Preço unitário',
  quantidadeMinima: 'Quantidade mínima',
  unidadeMedida: 'Unidade de medida',
  dataCadastro: 'Data de cadastro',
}

const OPERACAO: Record<OperacaoAuditoria, { cor: string; rotulo: string }> = {
  CRIACAO: { cor: 'green', rotulo: 'Criação' },
  ATUALIZACAO: { cor: 'blue', rotulo: 'Atualização' },
  EXCLUSAO: { cor: 'red', rotulo: 'Exclusão' },
}

export function HistoricoProdutoModal({ produto, aoFechar }: Props) {
  const buscar = useCallback(
    () => (produto ? produtosApi.historico(produto.productId) : Promise.resolve([])),
    [produto],
  )
  const historico = useCarregar<ProdutoAuditoria[]>(buscar, [])

  return (
    <Modal
      open={produto !== null}
      title={`Histórico de alterações: ${produto?.nome ?? ''}`}
      width={720}
      footer={null}
      onCancel={aoFechar}
      destroyOnHidden
    >
      {historico.erro && <Alert type="error" showIcon title={historico.erro} style={{ marginBottom: 16 }} />}

      <Table<ProdutoAuditoria>
        rowKey="auditId"
        size="small"
        loading={historico.carregando}
        dataSource={historico.dados}
        pagination={{ pageSize: 8, hideOnSinglePage: true }}
        locale={{ emptyText: <Empty description="Nenhuma alteração registrada ainda" /> }}
        columns={[
          {
            title: 'Quando',
            dataIndex: 'dataAlteracao',
            width: 150,
            render: (data: string) => formatarDataHora(data),
          },
          {
            title: 'Operação',
            dataIndex: 'operacao',
            width: 110,
            render: (operacao: OperacaoAuditoria) => (
              <Tag color={OPERACAO[operacao].cor}>{OPERACAO[operacao].rotulo}</Tag>
            ),
          },
          {
            title: 'Campo',
            dataIndex: 'campoAlterado',
            render: (campo: string) => ROTULO_CAMPO[campo] ?? campo,
          },
          {
            title: 'De → Para',
            key: 'valores',
            render: (_, registro) => (
              <Space size={4}>
                <Typography.Text type="secondary" delete={registro.valorAnterior !== null}>
                  {registro.valorAnterior ?? '—'}
                </Typography.Text>
                <Typography.Text type="secondary">→</Typography.Text>
                <Typography.Text>{registro.valorNovo ?? '—'}</Typography.Text>
              </Space>
            ),
          },
        ]}
      />
    </Modal>
  )
}
