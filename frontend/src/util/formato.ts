const formatoData = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short' })
const formatoDataHora = new Intl.DateTimeFormat('pt-BR', { dateStyle: 'short', timeStyle: 'short' })

export function formatarData(iso: string): string {
  return formatoData.format(new Date(iso))
}

export function formatarDataHora(iso: string): string {
  return formatoDataHora.format(new Date(iso))
}

export function diasAte(iso: string): number {
  const umDia = 24 * 60 * 60 * 1000
  return Math.ceil((new Date(iso).getTime() - Date.now()) / umDia)
}
