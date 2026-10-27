// Cliente HTTP da API: coloca o token em todas as requisições e
// transforma as respostas de erro (Problem Details do Spring) em ApiError.

const CHAVE_TOKEN = 'smge.token'

export class ApiError extends Error {
  readonly status: number
  // código específico enviado pelo back-end (ex.: SENHA_EXPIRADA)
  readonly codigo?: string
  // erros de validação por campo (ex.: { login: "Login já cadastrado" })
  readonly erros?: Record<string, string>

  constructor(status: number, mensagem: string, codigo?: string, erros?: Record<string, string>) {
    super(mensagem)
    this.name = 'ApiError'
    this.status = status
    this.codigo = codigo
    this.erros = erros
  }
}

export const tokenStorage = {
  obter(): string | null {
    try {
      return localStorage.getItem(CHAVE_TOKEN)
    } catch {
      return null
    }
  },
  salvar(token: string) {
    try {
      localStorage.setItem(CHAVE_TOKEN, token)
    } catch {
      // navegador sem armazenamento: o login vale só até recarregar a página
    }
  },
  limpar() {
    try {
      localStorage.removeItem(CHAVE_TOKEN)
    } catch {
      // nada a fazer
    }
  },
}

// chamado quando o token deixa de valer (expirou, usuário desativado, senha trocada)
let aoPerderSessao: (() => void) | null = null

export function definirAoPerderSessao(callback: () => void) {
  aoPerderSessao = callback
}

function mensagemPadrao(status: number): string {
  switch (status) {
    case 401:
      return 'Sua sessão expirou. Entre novamente.'
    case 403:
      return 'Você não tem permissão para esta ação.'
    case 404:
      return 'Registro não encontrado.'
    default:
      return status >= 500 ? 'Erro no servidor. Tente novamente.' : 'Não foi possível concluir a operação.'
  }
}

interface Opcoes {
  method?: 'GET' | 'POST' | 'PUT' | 'PATCH' | 'DELETE'
  body?: unknown
}

export async function api<T>(caminho: string, opcoes: Opcoes = {}): Promise<T> {
  const headers: Record<string, string> = {}
  const token = tokenStorage.obter()

  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  if (opcoes.body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }

  let resposta: Response
  try {
    resposta = await fetch(`/api${caminho}`, {
      method: opcoes.method ?? 'GET',
      headers,
      body: opcoes.body !== undefined ? JSON.stringify(opcoes.body) : undefined,
    })
  } catch {
    throw new ApiError(0, 'Não foi possível conectar ao servidor.')
  }

  if (!resposta.ok) {
    const problema = await resposta.json().catch(() => null)

    // token inválido em qualquer rota (exceto o próprio login) encerra a sessão
    if (resposta.status === 401 && token && caminho !== '/auth/login') {
      aoPerderSessao?.()
    }

    throw new ApiError(
      resposta.status,
      problema?.detail ?? mensagemPadrao(resposta.status),
      problema?.codigo,
      problema?.erros,
    )
  }

  if (resposta.status === 204) {
    return undefined as T
  }
  return (await resposta.json()) as T
}
