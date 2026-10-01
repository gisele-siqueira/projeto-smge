import { Form, Select, Typography } from 'antd'

import type { ModuloPermissoes, Perfil, Permissao } from '../../api/tipos'
import { useSessao } from '../../auth/useSessao'
import { SeletorPermissoes } from '../../componentes/SeletorPermissoes'

interface Props {
  perfis: Perfil[]
  modulos: ModuloPermissoes[]
}

/**
 * Campos "perfisIds" e "permissoesExtras" de um formulário de usuário.
 * Deve ficar dentro de um <Form>.
 */
export function CamposAcessos({ perfis, modulos }: Props) {
  const { pode } = useSessao()
  const perfisSelecionados: string[] = Form.useWatch('perfisIds') ?? []

  // permissões que já vêm dos perfis marcados (para indicar no checklist)
  const viaPerfil: Permissao[] = perfis
    .filter((p) => perfisSelecionados.includes(p.perfilId))
    .flatMap((p) => p.permissoes)

  return (
    <>
      <Form.Item
        name="perfisIds"
        label="Perfis"
        extra="O usuário recebe todas as permissões dos perfis marcados."
      >
        <Select
          mode="multiple"
          allowClear
          placeholder="Selecione um ou mais perfis"
          optionFilterProp="label"
          options={perfis.map((p) => {
            // não dá para atribuir um perfil com permissões que você não tem
            const permitido = p.permissoes.every((permissao) => pode(permissao))
            return {
              value: p.perfilId,
              label: p.nome,
              disabled: !permitido,
              title: permitido ? p.descricao ?? undefined : 'Este perfil tem permissões que você não possui',
            }
          })}
        />
      </Form.Item>

      <Form.Item
        name="permissoesExtras"
        label="Permissões extras"
        extra={
          <Typography.Text type="secondary">
            Funcionalidades avulsas, além das que vêm dos perfis. Ex.: alguém do faturamento que só
            precisa consultar produtos.
          </Typography.Text>
        }
      >
        <SeletorPermissoes modulos={modulos} viaPerfil={viaPerfil} />
      </Form.Item>
    </>
  )
}
