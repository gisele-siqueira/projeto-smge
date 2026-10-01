import { LockOutlined, UserOutlined } from '@ant-design/icons'
import { Alert, Button, Card, Flex, Form, Input, Typography } from 'antd'
import { useState } from 'react'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'

import { ApiError } from '../api/cliente'
import { useSessao } from '../auth/useSessao'

interface EstadoNavegacao {
  // mensagem vinda de outra tela (ex.: "Senha alterada. Entre novamente.")
  aviso?: string
  // tela que o usuário tentou abrir antes de ser mandado para o login
  voltarPara?: string
}

interface Erro {
  tipo: 'error' | 'warning'
  titulo: string
  descricao?: string
}

export function LoginPage() {
  const { usuario, entrar } = useSessao()
  const navigate = useNavigate()
  const location = useLocation()
  const estado = (location.state ?? {}) as EstadoNavegacao

  const [enviando, setEnviando] = useState(false)
  const [erro, setErro] = useState<Erro | null>(null)

  if (usuario) {
    return <Navigate to="/" replace />
  }

  async function aoEnviar(valores: { login: string; senha: string }) {
    setEnviando(true)
    setErro(null)
    try {
      await entrar(valores.login, valores.senha)
      navigate(estado.voltarPara ?? '/', { replace: true })
    } catch (e) {
      if (e instanceof ApiError && e.codigo === 'SENHA_EXPIRADA') {
        setErro({
          tipo: 'warning',
          titulo: 'Sua senha expirou',
          descricao: 'Peça ao administrador para redefinir a sua senha.',
        })
      } else {
        setErro({ tipo: 'error', titulo: e instanceof Error ? e.message : 'Erro ao entrar' })
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <Flex justify="center" align="center" style={{ minHeight: '100vh', padding: 16, background: '#f0f2f5' }}>
      <Card style={{ width: '100%', maxWidth: 380 }}>
        <Flex vertical align="center" style={{ marginBottom: 24 }}>
          <Typography.Title level={2} style={{ margin: 0, letterSpacing: 2 }}>
            SMGE
          </Typography.Title>
          <Typography.Text type="secondary">Sistema de Gestão Empresarial</Typography.Text>
        </Flex>

        {estado.aviso && !erro && (
          <Alert type="info" showIcon title={estado.aviso} style={{ marginBottom: 16 }} />
        )}
        {erro && (
          <Alert
            type={erro.tipo}
            showIcon
            title={erro.titulo}
            description={erro.descricao}
            style={{ marginBottom: 16 }}
          />
        )}

        <Form layout="vertical" onFinish={aoEnviar} requiredMark={false} disabled={enviando}>
          <Form.Item name="login" label="Login" rules={[{ required: true, message: 'Informe o login' }]}>
            <Input prefix={<UserOutlined />} autoComplete="username" autoFocus size="large" />
          </Form.Item>
          <Form.Item name="senha" label="Senha" rules={[{ required: true, message: 'Informe a senha' }]}>
            <Input.Password prefix={<LockOutlined />} autoComplete="current-password" size="large" />
          </Form.Item>
          <Button type="primary" htmlType="submit" block size="large" loading={enviando}>
            Entrar
          </Button>
        </Form>
      </Card>
    </Flex>
  )
}
