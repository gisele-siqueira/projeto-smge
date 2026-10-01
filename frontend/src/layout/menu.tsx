import { HomeOutlined, SafetyOutlined, SettingOutlined, TeamOutlined } from '@ant-design/icons'
import type { ReactNode } from 'react'

import type { Permissao } from '../api/tipos'

export interface ItemMenu {
  // rota da tela (itens com filhos usam uma chave qualquer)
  chave: string
  titulo: string
  icone?: ReactNode
  // aparece se o usuário tiver pelo menos uma destas permissões (vazio = todos)
  permissoes?: Permissao[]
  filhos?: ItemMenu[]
}

/**
 * Menu do sistema. Para uma tela nova: adicione aqui, com a mesma permissão
 * usada na rota (App.tsx) e no endpoint do back-end.
 */
export const MENU: ItemMenu[] = [
  { chave: '/', titulo: 'Início', icone: <HomeOutlined /> },
  {
    chave: 'administracao',
    titulo: 'Administração',
    icone: <SettingOutlined />,
    filhos: [
      {
        chave: '/usuarios',
        titulo: 'Usuários',
        icone: <TeamOutlined />,
        permissoes: ['USUARIO_VISUALIZAR'],
      },
      {
        chave: '/perfis',
        titulo: 'Perfis de acesso',
        icone: <SafetyOutlined />,
        permissoes: ['PERFIL_GERENCIAR'],
      },
    ],
  },
]

/**
 * Remove os itens que o usuário não pode ver e os grupos que ficaram vazios.
 */
export function filtrarMenu(
  itens: ItemMenu[],
  pode: (...permissoes: Permissao[]) => boolean,
): ItemMenu[] {
  return itens.flatMap((item) => {
    if (item.permissoes?.length && !pode(...item.permissoes)) {
      return []
    }
    if (item.filhos) {
      const filhos = filtrarMenu(item.filhos, pode)
      return filhos.length ? [{ ...item, filhos }] : []
    }
    return [item]
  })
}
