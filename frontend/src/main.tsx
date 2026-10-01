import { App as AntApp, ConfigProvider } from 'antd'
import ptBR from 'antd/locale/pt_BR'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'

import { App } from './App'
import { SessaoProvider } from './auth/SessaoProvider'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ConfigProvider
      locale={ptBR}
      theme={{
        token: {
          // cor principal do sistema: troque aqui para mudar a identidade visual
          colorPrimary: '#1f6feb',
          borderRadius: 6,
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
