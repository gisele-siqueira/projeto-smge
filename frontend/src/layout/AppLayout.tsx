import { DownOutlined, LogoutOutlined, UserOutlined } from '@ant-design/icons'
import { Avatar, Dropdown, Flex, Layout, Menu, Typography, theme, type MenuProps } from 'antd'
import { AnimatePresence, motion } from 'framer-motion'
import { useEffect, useRef, useState } from 'react'
import { Outlet, useLocation, useNavigate } from 'react-router-dom'

import { useSessao } from '../auth/useSessao'
import { filtrarMenu, MENU, tituloDaRota, type ItemMenu } from './menu'

const { Header, Sider, Content } = Layout

const GRADIENTE_SIDER = 'linear-gradient(195deg, #0d1b2e 0%, #10243c 55%, #0f3b3a 100%)'
// tempo que o mouse pode ficar fora antes da barra recolher sozinha
const ATRASO_RECOLHER_MS = 500
// duração da transição de largura definida em styles/global.css (.ant-layout-sider)
const DURACAO_TRANSICAO_MS = 500

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
  // controla a largura do Sider (anima imediatamente ao entrar/sair o mouse)
  const [recolhido, setRecolhido] = useState(true)
  // controla o modo do menu (ícone só x ícone+texto) e o texto "SMGE": ao expandir, muda
  // junto com a largura; ao recolher, só muda depois que a largura já terminou de encolher
  // — senão os ícones recentralizam enquanto a barra ainda está larga e "escorregam" pra
  // a posição final, dando a impressão de pular pro meio e voltar
  const [menuRecolhido, setMenuRecolhido] = useState(true)
  const timeoutLargura = useRef<number | undefined>(undefined)
  const timeoutMenu = useRef<number | undefined>(undefined)

  useEffect(
    () => () => {
      window.clearTimeout(timeoutLargura.current)
      window.clearTimeout(timeoutMenu.current)
    },
    [],
  )

  function aoPassarMouse() {
    window.clearTimeout(timeoutLargura.current)
    window.clearTimeout(timeoutMenu.current)
    setRecolhido(false)
    setMenuRecolhido(false)
  }

  function aoTirarMouse() {
    timeoutLargura.current = window.setTimeout(() => setRecolhido(true), ATRASO_RECOLHER_MS)
    timeoutMenu.current = window.setTimeout(
      () => setMenuRecolhido(true),
      ATRASO_RECOLHER_MS + DURACAO_TRANSICAO_MS,
    )
  }

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
    <Layout style={{ minHeight: '100vh', background: '#f4f6fb', padding: 12, gap: 12 }}>
      <Sider
        collapsed={recolhido}
        collapsedWidth={76}
        breakpoint="lg"
        width={232}
        onMouseEnter={aoPassarMouse}
        onMouseLeave={aoTirarMouse}
        style={{
          // fixo (não relativo): com poucos itens no menu, a largura e a altura do conteúdo
          // ficam parecidas, e um raio "999" vira círculo em vez de pill
          borderRadius: 38,
          overflow: 'hidden',
          alignSelf: 'center',
          height: 'fit-content',
          maxHeight: 'calc(100vh - 24px)',
          background: GRADIENTE_SIDER,
          boxShadow: '0 24px 48px -20px rgba(13, 27, 46, 0.55)',
          // sem "transition" aqui: deixa o próprio AntD animar a troca de largura (via flex),
          // sobrescrever isso inline anula a transição padrão dele
        }}
      >
        <Flex vertical style={{ padding: '26px 0' }}>
          <Flex
            align="center"
            // sempre alinhado à esquerda: com "center" condicional, o ícone pulava de posição
            // assim que o estado trocava, antes mesmo da largura terminar de animar
            justify="flex-start"
            gap={10}
            style={{ padding: '0 20px 22px', background: 'transparent' }}
          >
            <div
              style={{
                width: 34,
                height: 34,
                flexShrink: 0,
                borderRadius: '50%',
                background: token.colorPrimary,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontWeight: 800,
                color: '#fff',
                fontSize: 16,
                boxShadow: '0 4px 12px -2px rgba(31, 111, 235, 0.5)',
              }}
            >
              S
            </div>
            {!menuRecolhido && (
              <motion.div
                initial={{ opacity: 0, x: -6 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.35, delay: 0.15 }}
                style={{ flexShrink: 0, overflow: 'hidden' }}
              >
                <Typography.Title
                  level={4}
                  style={{ color: '#fff', margin: 0, letterSpacing: 1, whiteSpace: 'nowrap' }}
                >
                  SMGE
                </Typography.Title>
              </motion.div>
            )}
          </Flex>

          <div style={{ margin: '0 16px 16px', borderTop: '1px solid rgba(255, 255, 255, 0.08)' }} />

          <Menu
            theme="dark"
            mode="inline"
            inlineCollapsed={menuRecolhido}
            selectedKeys={[location.pathname]}
            defaultOpenKeys={gruposAbertos}
            items={paraItensAntd(menu)}
            onClick={({ key }) => navigate(key)}
            style={{ background: 'transparent', border: 'none' }}
          />
        </Flex>
      </Sider>

      <Flex vertical gap={12} style={{ flex: 1, minWidth: 0, height: 'calc(100vh - 24px)' }}>
        <Header
          style={{
            background: token.colorBgContainer,
            borderRadius: 16,
            padding: '0 24px',
            height: 64,
            lineHeight: '64px',
            flexShrink: 0,
            boxShadow: '0 1px 2px rgba(16, 24, 40, 0.04), 0 1px 1px rgba(16, 24, 40, 0.03)',
          }}
        >
          <Flex justify="space-between" align="center" style={{ height: '100%' }}>
            <AnimatePresence mode="wait">
              <motion.div
                key={location.pathname}
                initial={{ opacity: 0, y: -6 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: 6 }}
                transition={{ duration: 0.18 }}
              >
                <Typography.Text strong style={{ fontSize: 15 }}>
                  {tituloDaRota(location.pathname)}
                </Typography.Text>
              </motion.div>
            </AnimatePresence>

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

        <Content style={{ padding: 24, flex: 1, overflow: 'auto', borderRadius: 16 }}>
          <AnimatePresence mode="wait">
            <motion.div
              key={location.pathname}
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              transition={{ duration: 0.15 }}
            >
              <Outlet />
            </motion.div>
          </AnimatePresence>
        </Content>
      </Flex>
    </Layout>
  )
}
