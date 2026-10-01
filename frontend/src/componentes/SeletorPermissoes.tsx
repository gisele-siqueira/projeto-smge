import { Card, Checkbox, Col, Flex, Row, Tag, Tooltip, Typography } from 'antd'

import type { ModuloPermissoes, Permissao } from '../api/tipos'
import { useSessao } from '../auth/useSessao'

interface Props {
  modulos: ModuloPermissoes[]
  // value/onChange: permite usar dentro de <Form.Item>
  value?: Permissao[]
  onChange?: (permissoes: Permissao[]) => void
  // permissões que o usuário já recebe pelos perfis (só para informar)
  viaPerfil?: Permissao[]
}

/**
 * Checklist de permissões agrupadas por módulo.
 * Permissões que o usuário logado não possui ficam bloqueadas, porque o back-end
 * não deixa ninguém conceder o que não tem (proteção contra escalada de privilégio).
 */
export function SeletorPermissoes({ modulos, value = [], onChange, viaPerfil = [] }: Props) {
  const { pode } = useSessao()
  const selecionadas = new Set(value)

  function alterar(permissao: Permissao, marcada: boolean) {
    const novas = new Set(selecionadas)
    if (marcada) {
      novas.add(permissao)
    } else {
      novas.delete(permissao)
    }
    onChange?.([...novas])
  }

  function alterarModulo(modulo: ModuloPermissoes, marcar: boolean) {
    const novas = new Set(selecionadas)
    modulo.permissoes
      .filter((p) => pode(p.codigo))
      .forEach((p) => (marcar ? novas.add(p.codigo) : novas.delete(p.codigo)))
    onChange?.([...novas])
  }

  return (
    <Flex vertical gap={12}>
      {modulos.map((modulo) => {
        const permitidas = modulo.permissoes.filter((p) => pode(p.codigo))
        const marcadas = permitidas.filter((p) => selecionadas.has(p.codigo)).length

        return (
          <Card
            key={modulo.modulo}
            size="small"
            title={
              <Checkbox
                checked={permitidas.length > 0 && marcadas === permitidas.length}
                indeterminate={marcadas > 0 && marcadas < permitidas.length}
                disabled={permitidas.length === 0}
                onChange={(e) => alterarModulo(modulo, e.target.checked)}
              >
                {modulo.nome}
              </Checkbox>
            }
          >
            <Row gutter={[8, 8]}>
              {modulo.permissoes.map((p) => {
                const bloqueada = !pode(p.codigo)
                const checkbox = (
                  <Checkbox
                    checked={selecionadas.has(p.codigo)}
                    disabled={bloqueada}
                    onChange={(e) => alterar(p.codigo, e.target.checked)}
                  >
                    {p.descricao}
                    {viaPerfil.includes(p.codigo) && (
                      <Tag color="blue" style={{ marginInlineStart: 8 }}>
                        já vem do perfil
                      </Tag>
                    )}
                  </Checkbox>
                )

                return (
                  <Col key={p.codigo} xs={24} md={12}>
                    {bloqueada ? (
                      <Tooltip title="Você não pode conceder uma permissão que não possui">
                        {checkbox}
                      </Tooltip>
                    ) : (
                      checkbox
                    )}
                    <Typography.Text type="secondary" style={{ display: 'block', fontSize: 11, marginInlineStart: 24 }}>
                      {p.codigo}
                    </Typography.Text>
                  </Col>
                )
              })}
            </Row>
          </Card>
        )
      })}
    </Flex>
  )
}
