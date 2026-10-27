import { App, Col, Form, Input, InputNumber, DatePicker, Modal, Row } from 'antd'
import dayjs, { type Dayjs } from 'dayjs'
import { useState } from 'react'

import { ApiError } from '../../api/cliente'
import { produtosApi } from '../../api/endpoints'
import type { Produto, ProdutoForm } from '../../api/tipos'

interface Props {
  aberto: boolean
  // produto sendo editado; null = criando um novo
  produto: Produto | null
  aoFechar: () => void
  aoSalvar: () => void
}

type FormProduto = Omit<ProdutoForm, 'dataCadastro'> & { dataCadastro: Dayjs }

export function ProdutoModal({ aberto, produto, aoFechar, aoSalvar }: Props) {
  const { message } = App.useApp()
  const [form] = Form.useForm<FormProduto>()
  const [salvando, setSalvando] = useState(false)

  const valoresIniciais: FormProduto = produto
    ? {
        codigo: produto.codigo,
        nome: produto.nome,
        descrição: produto.descrição ?? '',
        categoria: produto.categoria,
        quantidade: produto.quantidade,
        precoUnitario: produto.precoUnitario,
        quantidadeMinima: produto.quantidadeMinima,
        unidadeMedida: produto.unidadeMedida,
        dataCadastro: dayjs(produto.dataCadastro),
      }
    : {
        codigo: '',
        nome: '',
        descrição: '',
        categoria: '',
        quantidade: 0,
        precoUnitario: 0,
        quantidadeMinima: 0,
        unidadeMedida: '',
        dataCadastro: dayjs(),
      }

  async function salvar(valores: FormProduto) {
    const dados: ProdutoForm = { ...valores, dataCadastro: valores.dataCadastro.format('YYYY-MM-DD') }
    setSalvando(true)
    try {
      if (produto) {
        await produtosApi.atualizar(produto.productId, dados)
        message.success('Produto atualizado')
      } else {
        await produtosApi.criar(dados)
        message.success('Produto criado')
      }
      aoSalvar()
    } catch (e) {
      if (e instanceof ApiError && e.status === 409) {
        const campo = e.message.includes('código') ? 'codigo' : 'nome'
        form.setFields([{ name: campo, errors: [e.message] }])
      } else if (e instanceof ApiError && e.erros) {
        form.setFields(
          Object.entries(e.erros).map(([campo, erro]) => ({
            name: campo as keyof FormProduto,
            errors: [erro],
          })),
        )
      } else {
        message.error(e instanceof Error ? e.message : 'Erro ao salvar o produto')
      }
    } finally {
      setSalvando(false)
    }
  }

  return (
    <Modal
      open={aberto}
      title={produto ? `Editar produto: ${produto.nome}` : 'Novo produto'}
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
        <Row gutter={16}>
          <Col xs={24} md={8}>
            <Form.Item
              name="codigo"
              label="Código"
              rules={[
                { required: true, whitespace: true, message: 'Informe o código' },
                { max: 30, message: 'Máximo de 30 caracteres' },
              ]}
            >
              <Input />
            </Form.Item>
          </Col>
          <Col xs={24} md={16}>
            <Form.Item
              name="nome"
              label="Nome"
              rules={[
                { required: true, whitespace: true, message: 'Informe o nome' },
                { max: 150, message: 'Máximo de 150 caracteres' },
              ]}
            >
              <Input />
            </Form.Item>
          </Col>
          <Col xs={24}>
            <Form.Item
              name="descrição"
              label="Descrição"
              rules={[{ max: 255, message: 'Máximo de 255 caracteres' }]}
            >
              <Input.TextArea rows={2} placeholder="Detalhes adicionais do produto" />
            </Form.Item>
          </Col>
          <Col xs={24} md={12}>
            <Form.Item
              name="categoria"
              label="Categoria"
              rules={[
                { required: true, whitespace: true, message: 'Informe a categoria' },
                { max: 60, message: 'Máximo de 60 caracteres' },
              ]}
            >
              <Input placeholder="Ex.: Limpeza, Escritório" />
            </Form.Item>
          </Col>
          <Col xs={24} md={12}>
            <Form.Item
              name="unidadeMedida"
              label="Unidade de medida"
              rules={[
                { required: true, whitespace: true, message: 'Informe a unidade de medida' },
                { max: 20, message: 'Máximo de 20 caracteres' },
              ]}
            >
              <Input placeholder="Ex.: UN, KG, L" />
            </Form.Item>
          </Col>
          <Col xs={24} md={8}>
            <Form.Item
              name="quantidade"
              label="Quantidade em estoque"
              rules={[{ required: true, message: 'Informe a quantidade' }]}
            >
              <InputNumber min={0} precision={0} style={{ width: '100%' }} />
            </Form.Item>
          </Col>
          <Col xs={24} md={8}>
            <Form.Item
              name="quantidadeMinima"
              label="Quantidade mínima"
              rules={[{ required: true, message: 'Informe a quantidade mínima' }]}
            >
              <InputNumber min={0} precision={0} style={{ width: '100%' }} />
            </Form.Item>
          </Col>
          <Col xs={24} md={8}>
            <Form.Item
              name="precoUnitario"
              label="Preço unitário"
              rules={[{ required: true, message: 'Informe o preço unitário' }]}
            >
              <InputNumber
                min={0}
                precision={2}
                decimalSeparator=","
                style={{ width: '100%' }}
                prefix="R$"
              />
            </Form.Item>
          </Col>
          <Col xs={24} md={8}>
            <Form.Item
              name="dataCadastro"
              label="Data de cadastro"
              rules={[{ required: true, message: 'Informe a data de cadastro' }]}
            >
              <DatePicker format="DD/MM/YYYY" style={{ width: '100%' }} />
            </Form.Item>
          </Col>
        </Row>
      </Form>
    </Modal>
  )
}
