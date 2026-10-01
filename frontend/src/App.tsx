import { Button, Result } from 'antd'
import { Link, Route, Routes } from 'react-router-dom'

import { RotaProtegida } from './auth/RotaProtegida'
import { AppLayout } from './layout/AppLayout'
import { InicioPage } from './paginas/InicioPage'
import { LoginPage } from './paginas/LoginPage'
import { MinhaContaPage } from './paginas/MinhaContaPage'
import { PerfisPage } from './paginas/perfis/PerfisPage'
import { UsuariosPage } from './paginas/usuarios/UsuariosPage'

/**
 * Rotas do sistema. Cada tela exige a mesma permissão do menu (layout/menu.tsx)
 * e do endpoint correspondente no back-end.
 */
export function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />

      <Route
        element={
          <RotaProtegida>
            <AppLayout />
          </RotaProtegida>
        }
      >
        <Route index element={<InicioPage />} />
        <Route path="minha-conta" element={<MinhaContaPage />} />
        <Route
          path="usuarios"
          element={
            <RotaProtegida permissoes={['USUARIO_VISUALIZAR']}>
              <UsuariosPage />
            </RotaProtegida>
          }
        />
        <Route
          path="perfis"
          element={
            <RotaProtegida permissoes={['PERFIL_GERENCIAR']}>
              <PerfisPage />
            </RotaProtegida>
          }
        />
        <Route
          path="*"
          element={
            <Result
              status="404"
              title="Página não encontrada"
              extra={
                <Link to="/">
                  <Button type="primary">Voltar ao início</Button>
                </Link>
              }
            />
          }
        />
      </Route>
    </Routes>
  )
}
