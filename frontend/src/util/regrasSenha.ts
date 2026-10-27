import type { FormRule } from 'antd'

// Mesmas regras do back-end (PasswordRules.java e CreateUserRequest.java)

export const regrasSenha: FormRule[] = [
  { required: true, message: 'Informe a senha' },
  { min: 8, max: 64, message: 'A senha deve ter entre 8 e 64 caracteres' },
  { pattern: /^(?=.*[A-Za-z])(?=.*\d).+$/, message: 'A senha deve conter letras e números' },
]

// confirma que o campo é igual ao campo `campoSenha` do mesmo formulário
export function regraConfirmarSenha(campoSenha: string): FormRule {
  return ({ getFieldValue }) => ({
    validator(_, valor) {
      if (!valor || getFieldValue(campoSenha) === valor) {
        return Promise.resolve()
      }
      return Promise.reject(new Error('As senhas não conferem'))
    },
  })
}

export const regrasLogin: FormRule[] = [
  { required: true, message: 'Informe o login' },
  { min: 3, max: 50, message: 'O login deve ter entre 3 e 50 caracteres' },
  { pattern: /^[a-zA-Z0-9._-]+$/, message: "Use apenas letras, números, '.', '_' ou '-'" },
]
