# SMGE — Histórico de Versões

Sistema de gestão empresarial (ERP). Não há auto-cadastro: as contas são criadas pelo administrador.

**Versão atual: 1.1**

## Regra de versionamento

O número da versão tem o formato `MAIOR.MENOR`:

| Tipo de mudança | Exemplo | Versão |
|---|---|---|
| **Menor**: nova funcionalidade, correção ou ajuste compatível com o que já existe | novo endpoint, nova validação, correção de bug | `1.0` → `1.1` |
| **Maior**: mudança grande ou que quebra o que já existe | novo módulo inteiro, troca do método de autenticação, mudança de rotas ou contratos da API, reestruturação do banco | `1.4` → `2.0` |

Toda alteração no projeto deve ganhar uma nova entrada neste arquivo, com a versão mais recente no topo.

---

## Estado atual do projeto

### Módulos

| Módulo | Pacote | Situação |
|---|---|---|
| Usuários e autorização | `authorization` | Cadastro pelo admin, login, perfis, troca e expiração de senha |
| Produtos | `product` | CRUD básico, ainda sem validação e sem DTOs |
| Comum | `common` | Exceções e tratamento global de erros |

### Endpoints de usuários

| Método | Rota | Quem pode |
|---|---|---|
| POST | `/users` | ADMIN |
| GET | `/users`, `/users/{id}` | ADMIN |
| PATCH | `/users/{id}/desativar`, `/users/{id}/reativar` | ADMIN |
| PUT | `/users/{id}/senha` | ADMIN |
| GET | `/users/me` | Usuário logado |
| PUT | `/users/me/senha` | Usuário logado |

As rotas `/products` exigem qualquer usuário logado.

### Como rodar

```bash
./mvnw spring-boot:run   # sobe a aplicação em http://localhost:8080
./mvnw test              # roda os testes
```

- Autenticação: HTTP Basic.
- ADMIN inicial: login `admin`, senha `Admin@123`. Em produção, defina `SMGE_ADMIN_LOGIN` e `SMGE_ADMIN_SENHA`.
- Banco H2 em memória (os dados somem ao reiniciar). Console em `http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:mem:smge`, usuário `sa`, sem senha.

### Pendências e próximos passos

- [ ] Decidir se criar, editar e excluir produtos deve ficar restrito ao ADMIN.
- [ ] Trocar HTTP Basic por JWT quando houver front-end.
- [ ] Obrigar a troca de senha no primeiro acesso.
- [ ] Hoje uma senha expirada bloqueia o login, e só o ADMIN consegue liberar. Avaliar permitir que o próprio usuário troque a senha expirada.
- [ ] `ProductModel`: trocar `float precoUnitario` por `BigDecimal`.
- [ ] `ProductModel`: renomear o campo `descrição` para `descricao` (sem acento).
- [ ] `ProductController`: receber DTOs com validação em vez da entidade.
- [ ] Trocar o H2 por um banco persistente (ex.: PostgreSQL).

---

## [1.1] — 2026-10-01

Segurança, gestão de usuários pelo administrador e tratamento de erros.

### Corrigido
- O cadastro de usuário falhava ao salvar porque `senhaExpiraEm` ficava nulo. O `UserService` usava `setSenha` em vez de `definirSenha`.
- O `POST /users` sempre negava acesso, porque não havia configuração de segurança e o Spring usava um usuário padrão com senha aleatória.
- A resposta do cadastro expunha o hash da senha.
- Login duplicado e produto inexistente retornavam erro 500.

### Adicionado
- Perfis de acesso `ADMIN` e `USER` (enum `Role`), no lugar dos comentários `permission`/`roleLevel`.
- `SecurityConfig` com HTTP Basic sem sessão. `/users/me/**` exige login, o restante de `/users/**` é só para ADMIN e as demais rotas exigem login.
- `SmgeUserDetailsService`: login com os usuários do banco. Usuário desativado ou com senha expirada não consegue entrar.
- `AdminSeeder`: cria o ADMIN inicial na primeira execução, se ainda não houver nenhum.
- Novos endpoints: listar, buscar, desativar e reativar usuário, redefinir senha (ADMIN), ver os próprios dados e trocar a própria senha.
- Regra: o ADMIN não pode desativar a si mesmo.
- DTOs `UserResponse`, `ChangePasswordRequest`, `ResetPasswordRequest` e `PasswordRules`.
- Validação de entrada. O login é salvo em minúsculas e sem espaços. A senha precisa ter de 8 a 64 caracteres, com letras e números.
- Pacote `common.exception` com `RecursoNaoEncontradoException` (404), `ConflitoException` (409), `RegraNegocioException` (400) e `GlobalExceptionHandler`, que responde no padrão Problem Details.
- Campo `criadoEm` em `UserModel`.
- Configuração do H2 e da conta ADMIN no `application.properties`.
- Dependências `spring-boot-starter-validation` e `spring-boot-starter-security-test`.
- Testes `UserServiceTest` (5) e `UserControllerTest` (5).

### Alterado
- Os campos de `UserModel` passaram a ser `private`.
- `ProductService` passou a lançar `RecursoNaoEncontradoException` (404).
- O `POST /users` agora devolve `UserResponse` em vez da entidade.

---

## [1.0] — 2026-09-29

Primeira versão: estrutura inicial do projeto.

### Adicionado
- Projeto Spring Boot com JPA, Web, Security, H2 e Lombok.
- `UserModel` com nome, login, senha e status ativo.
- Expiração de senha em 90 dias (`definirSenha`, `senhaExpirada`) e testes do modelo.
- Criação de usuário: `POST /users`, `UserService`, `UserRepository`, `CreateUserRequest` e senha criptografada com BCrypt.
- `ProductModel` com código, nome, descrição, categoria, quantidade, preço, estoque mínimo e unidade.
- CRUD de produtos: `ProductController`, `ProductService` e `ProductRepository`.
