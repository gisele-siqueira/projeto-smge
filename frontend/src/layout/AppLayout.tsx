import { DownOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons'
import { Avatar, Dropdown, Flex, Layout, Menu, Typography, theme, type MenuProps } from 'antd'
import { useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'

import { useSessao } from '../auth/useSessao'
import { filtrarMenu, MENU, type ItemMenu } from './menu'

const { Header, Sider, Content } = Layout

function paraItensAntd(itens: ItemMenu[]): MenuProps['items'] {
  return itens.map((item) => ({
    key: item.chave,
    icon: item.icone,
    label: item.titulo,
    children: item.filhos ? paraItensAntd(item.filhos) : undefined,
  }))
}

export function AppLayout() {
  const { usuario, pode, sair } = useSessao()
  const navigate = useNavigate()
  const location = useLocation()
  const { token } = theme.useToken()
  const [recolhido, setRecolhido] = useState(false)

  const menu = filtrarMenu(MENU, pode)
  // deixa aberto o grupo da tela atual
  const gruposAbertos = menu
    .filter((grupo) => grupo.filhos?.some((filho) => location.pathname.startsWith(filho.chave)))
    .map((grupo) => grupo.chave)

  const menuUsuario: MenuProps['items'] = [
    { key: 'conta', icon: <UserOutlined />, label: 'Minha conta' },
    { type: 'divider' },
    { key: 'sair', icon: <LogoutOutlined />, label: 'Sair', danger: true },
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={recolhido}
        onCollapse={setRecolhido}
        breakpoint="lg"
        width={232}
      >
        <Flex align="center" justify="center" style={{ height: 64 }}>
          <Typography.Title level={4} style={{ color: '#fff', margin: 0, letterSpacing: 1 }}>
            {recolhido ? 'S' : 'SMGE'}
          </Typography.Title>
        </Flex>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          defaultOpenKeys={gruposAbertos}
          items={paraItensAntd(menu)}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>

      <Layout>
        <Header
          style={{
            background: token.colorBgContainer,
            padding: '0 24px',
            borderBottom: `1px solid ${token.colorBorderSecondary}`,
          }}
        >
          <Flex justify="flex-end" align="center" style={{ height: '100%' }}>
            <Dropdown
              trigger={['click']}
              menu={{
                items: menuUsuario,
                onClick: ({ key }) => (key === 'sair' ? sair() : navigate('/minha-conta')),
              }}
            >
              <Flex align="center" gap={8} style={{ cursor: 'pointer' }}>
                <Avatar style={{ background: token.colorPrimary }} icon={<UserOutlined />} />
                <Typography.Text>{usuario?.nome}</Typography.Text>
                <DownOutlined style={{ fontSize: 10 }} />
              </Flex>
            </Dropdown>
          </Flex>
        </Header>

        <Content style={{ padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
