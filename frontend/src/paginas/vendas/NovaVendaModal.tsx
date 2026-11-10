import { DeleteOutlined, PlusOutlined } from '@ant-design/icons'
import { App, Button, Divider, Flex, Form, InputNumber, Modal, Select, Typography } from 'antd'
import { useState } from 'react'

import { vendasApi } from '../../api/endpoints'
import type { Produto, VendaForm } from '../../api/tipos'
import { formatarMoeda } from '../../util/formato'

interface Props {
  aberto: boolean
  produtos: Produto[]
  aoFechar: () => void
  aoSalvar: () => void
}

export function NovaVendaModal({ aberto, produtos, aoFechar, aoSalvar }: Props) {
  const { message } = App.useApp()
  const [form] = Form.useForm<VendaForm>()
  const [salvando, setSalvando] = useState(false)
  const itens = Form.useWatch('itens', form) ?? []

  const produtosPorId = new Map(produtos.map((p) => [p.productId, p]))

  // total calculado no front só pra conferência visual; o valor de verdade vem do back-end
  const total = itens.reduce((soma, item) => {
    const produto = item?.produtoId ? produtosPorId.get(item.produtoId) : undefined
    return soma + (produto && item?.quantidade ? produto.precoUnitario * item.quantidade : 0)
  }, 0)

  async function salvar(valores: VendaForm) {
    setSalvando(true)
    try {
      await vendasApi.criar(valores)
      message.success('Venda registrada')
      aoSalvar()
    } catch (e) {
      message.error(e instanceof Error ? e.message : 'Erro ao registrar a venda')
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open={aberto}
      title="Nova venda"
      okText="Registrar venda"
      cancelText="Cancelar"
      width={700}
      confirmLoading={salvando}
      onOk={() => form.submit()}
      onCancel={aoFechar}
      destroyOnHidden
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{ itens: [{ produtoId: undefined, quantidade: 1 }] }}
        onFinish={salvar}
        disabled={salvando}
      >
        <Form.List name="itens">
          {(fields, { add, remove }) => (
            <Flex vertical gap={8}>
              {fields.map((field) => (
                <Flex key={field.key} gap={8} align="start">
                  <Form.Item
                    {...field}
                    name={[field.name, 'produtoId']}
                    style={{ flex: 1, marginBottom: 0 }}
                    rules={[{ required: true, message: 'Selecione o produto' }]}
                  >
                    <Select
                      placeholder="Produto"
                      showSearch
                      optionFilterProp="label"
                      options={produtos.map((p) => ({
                        value: p.productId,
                        label: `${p.nome} (${p.codigo}) — ${p.quantidade} ${p.unidadeMedida} em estoque`,
                      }))}
                    />
                  </Form.Item>
                  <Form.Item
                    {...field}
                    name={[field.name, 'quantidade']}
                    style={{ width: 110, marginBottom: 0 }}
                    rules={[{ required: true, message: 'Qtd.' }]}
                  >
                    <InputNumber min={1} precision={0} style={{ width: '100%' }} placeholder="Qtd." />
                  </Form.Item>
                  <Button
                    danger
                    icon={<DeleteOutlined />}
                    disabled={fields.length === 1}
                    onClick={() => remove(field.name)}
                  />
                </Flex>
              ))}
              <Button
                type="dashed"
                icon={<PlusOutlined />}
                onClick={() => add({ produtoId: undefined, quantidade: 1 })}
              >
                Adicionar item
              </Button>
            </Flex>
          )}
        </Form.List>

        <Divider />

        <Flex justify="flex-end">
          <Typography.Title level={4} style={{ margin: 0 }}>
            Total: {formatarMoeda(total)}
          </Typography.Title>
        </Flex>
      </Form>
    </Modal>
  )
}
