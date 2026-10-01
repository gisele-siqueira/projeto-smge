import { useCallback, useEffect, useState } from 'react'

/**
 * Carrega dados da API ao abrir a tela e permite recarregar depois de salvar.
 */
export function useCarregar<T>(buscar: () => Promise<T>, inicial: T) {
  const [dados, setDados] = useState<T>(inicial)
  const [carregando, setCarregando] = useState(true)
  const [erro, setErro] = useState<string | null>(null)

  const executar = useCallback(
    () =>
      buscar()
        .then((resultado) => {
          setDados(resultado)
          setErro(null)
        })
        .catch((e: unknown) => setErro(e instanceof Error ? e.message : 'Erro ao carregar os dados'))
        .finally(() => setCarregando(false)),
    [buscar],
  )

  useEffect(() => {
    void executar()
  }, [executar])

  const recarregar = useCallback(async () => {
    setCarregando(true)
    await executar()
  }, [executar])

  return { dados, carregando, erro, recarregar }
}
