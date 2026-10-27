// gradientes alternados usados nos badges de ícone (Início, cabeçalho das telas)
// mesma ordem dos atalhos do menu: Usuários, Perfis, Produtos, ...
export const GRADIENTES = [
  'linear-gradient(135deg, #1f6feb, #3b82f6)',
  'linear-gradient(135deg, #7c3aed, #a78bfa)',
  'linear-gradient(135deg, #0d9488, #2dd4bf)',
  'linear-gradient(135deg, #d97706, #fbbf24)',
]

export function corDoIndice(indice: number): string {
  return GRADIENTES[indice % GRADIENTES.length]
}
