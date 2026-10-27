import { App as AntApp, ConfigProvider } from 'antd'
import ptBR from 'antd/locale/pt_BR'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import { App } from './App'
import { SessaoProvider } from './auth/SessaoProvider'
import './styles/global.css'

const fontFamily =
  "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif"

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ConfigProvider
      locale={ptBR}
      theme={{
        token: {
          // cor principal do sistema: troque aqui para mudar a identidade visual
          colorPrimary: '#1f6feb',
          borderRadius: 10,
          fontFamily,
          // fundo da área de conteúdo, um pouco mais fechado que o branco puro
          colorBgLayout: '#f4f6fb',
        },
        components: {
          Layout: {
            // navy-teal ligado à cor principal, no lugar do preto-azulado padrão do AntD
            siderBg: 'linear-gradient(195deg, #0d1b2e 0%, #10243c 55%, #0f3b3a 100%)',
            triggerBg: '#13284a',
          },
          Menu: {
            darkItemBg: 'transparent',
            darkSubMenuItemBg: 'transparent',
            darkItemColor: 'rgba(255, 255, 255, 0.75)',
            darkItemHoverBg: 'rgba(255, 255, 255, 0.08)',
            darkItemSelectedBg: '#1f6feb',
            darkItemSelectedColor: '#ffffff',
            // pill no modo expandido, círculo perfeito em volta do ícone no modo recolhido
            itemBorderRadius: 999,
            collapsedIconSize: 18,
            iconSize: 18,
            itemMarginInline: 12,
            itemHeight: 46,
            itemMarginBlock: 10,
          },
          Card: {
            borderRadiusLG: 14,
            boxShadowTertiary: '0 2px 8px -2px rgba(16, 24, 40, 0.06)',
          },
          Button: {
            // altura 38 / raio 19 = ponta a ponta arredondada (pill)
            borderRadius: 19,
            controlHeight: 38,
            fontWeight: 600,
          },
          Table: {
            headerBg: '#eef3fc',
            headerColor: '#1a2b4c',
            rowHoverBg: '#f3f7ff',
            borderColor: '#eef0f3',
          },
          Tag: {
            borderRadiusSM: 999,
          },
        },
      }}
    >
      {/* AntApp habilita message/modal com o tema acima (App.useApp) */}
      <AntApp>
        <BrowserRouter>
          <SessaoProvider>
            <App />
          </SessaoProvider>
        </BrowserRouter>
      </AntApp>
    </ConfigProvider>
  </StrictMode>,
)
