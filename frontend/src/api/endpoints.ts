// Uma função por endpoint da API.

import { api } from './cliente'
import type {
  Acessos,
  LoginResponse,
  ModuloPermissoes,
  NovoUsuario,
  Perfil,
  PerfilForm,
  Usuario,
} from './tipos'

export const authApi = {
  login: (login: string, senha: string) =>
    api<LoginResponse>('/auth/login', { method: 'POST', body: { login, senha } }),
}

export const usuariosApi = {
  listar: () => api<Usuario[]>('/users'),
  criar: (dados: NovoUsuario) => api<Usuario>('/users', { method: 'POST', body: dados }),
  atualizarAcessos: (id: string, acessos: Acessos) =>
    api<Usuario>(`/users/${id}/acessos`, { method: 'PUT', body: acessos }),
  desativar: (id: string) => api<void>(`/users/${id}/desativar`, { method: 'PATCH' }),
  reativar: (id: string) => api<void>(`/users/${id}/reativar`, { method: 'PATCH' }),
  redefinirSenha: (id: string, novaSenha: string) =>
    api<void>(`/users/${id}/senha`, { method: 'PUT', body: { novaSenha } }),

  meusDados: () => api<Usuario>('/users/me'),
  alterarMinhaSenha: (senhaAtual: string, novaSenha: string) =>
    api<void>('/users/me/senha', { method: 'PUT', body: { senhaAtual, novaSenha } }),
}

export const perfisApi = {
  listar: () => api<Perfil[]>('/perfis'),
  criar: (dados: PerfilForm) => api<Perfil>('/perfis', { method: 'POST', body: dados }),
  atualizar: (id: string, dados: PerfilForm) =>
    api<Perfil>(`/perfis/${id}`, { method: 'PUT', body: dados }),
  excluir: (id: string) => api<void>(`/perfis/${id}`, { method: 'DELETE' }),
  listarPermissoes: () => api<ModuloPermissoes[]>('/permissoes'),
}
