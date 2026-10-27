import {
  HomeFilled,
  SafetyCertificateFilled,
  SettingFilled,
  ShopFilled,
  TagsFilled,
  TeamOutlined,
} from '@ant-design/icons'
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

// tela nova: adicione aqui, com a mesma permissão usada na rota (App.tsx) e no endpoint
export const MENU: ItemMenu[] = [
  { chave: '/', titulo: 'Início', icone: <HomeFilled /> },
  {
    chave: 'administracao',
    titulo: 'Administração',
    icone: <SettingFilled />,
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
        icone: <SafetyCertificateFilled />,
        permissoes: ['PERFIL_GERENCIAR'],
      },
    ],
  },
  {
    chave: 'estoque',
    titulo: 'Estoque',
    icone: <ShopFilled />,
    filhos: [
      {
        chave: '/produtos',
        titulo: 'Produtos',
        icone: <TagsFilled />,
        permissoes: ['PRODUTO_VISUALIZAR'],
      },
    ],
  },
]

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

// título da tela atual, para o cabeçalho (ex.: "/usuarios" -> "Usuários")
export function tituloDaRota(caminho: string): string | undefined {
  const folhas = MENU.flatMap((item) => item.filhos ?? [item])
  return folhas.find((item) => item.chave === caminho)?.titulo
}
