# SMGE — Histórico de Versões

Sistema de gestão empresarial (ERP). Não há auto-cadastro: as contas são criadas pelo administrador.

**Versão atual: 4.2**

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
| Usuários e acessos | `authorization` | Login com JWT, cadastro pelo admin, perfis e permissões, troca e expiração de senha |
| Estoque (produtos) | `product` | CRUD básico protegido por permissões, ainda sem validação e sem DTOs |
| Comum | `common` | Exceções, tratamento global de erros, prefixo `/api` e entrega do front-end |
| Front-end | `frontend/` | React + TypeScript + Vite + Ant Design: login, menu por permissões, minha conta, usuários e perfis |

### Como funcionam as permissões

- **Permissão:** uma ação específica, definida no código (enum `Permissao`). Ex.: `PRODUTO_CRIAR`.
- **Perfil:** um grupo de permissões montado pelo admin (ex.: "Estoquista"). Fica no banco e pode ser editado pela API.
- **Usuário:** tem um ou mais perfis e, se precisar, permissões extras avulsas. O acesso final é a soma de tudo.
- O perfil **Administrador** é do sistema: tem todas as permissões, não pode ser editado nem excluído e recebe automaticamente qualquer permissão nova.
- **Contra escalada de privilégio:** ninguém concede uma permissão que não tem, nem gerencia (desativa, troca senha, altera acessos) um usuário com mais acesso que o seu, nem altera os próprios acessos.

**Para criar uma funcionalidade nova:** adicione a permissão no enum `Permissao` (com módulo e descrição) e proteja o endpoint com `@PreAuthorize("hasAuthority('NOME_DA_PERMISSAO')")`.

| Permissão | Módulo | O que libera |
|---|---|---|
| `USUARIO_VISUALIZAR` | Usuários | Consultar usuários |
| `USUARIO_GERENCIAR` | Usuários | Criar, desativar, redefinir senha e alterar acessos de usuários |
| `PERFIL_GERENCIAR` | Usuários | Criar, editar e excluir perfis |
| `PRODUTO_VISUALIZAR` | Estoque | Consultar produtos |
| `PRODUTO_CRIAR` | Estoque | Cadastrar produtos |
| `PRODUTO_EDITAR` | Estoque | Editar produtos |
| `PRODUTO_EXCLUIR` | Estoque | Excluir produtos |

### Endpoints

Toda a API fica sob `/api`. O restante dos endereços é o front-end.

| Método | Rota | Permissão necessária |
|---|---|---|
| POST | `/api/auth/login` | Público |
| POST | `/api/users` | `USUARIO_GERENCIAR` |
| GET | `/api/users`, `/api/users/{id}` | `USUARIO_VISUALIZAR` |
| PUT | `/api/users/{id}/acessos` | `USUARIO_GERENCIAR` |
| PATCH | `/api/users/{id}/desativar`, `/api/users/{id}/reativar` | `USUARIO_GERENCIAR` |
| PUT | `/api/users/{id}/senha` | `USUARIO_GERENCIAR` |
| GET | `/api/users/me` | Usuário logado |
| PUT | `/api/users/me/senha` | Usuário logado |
| GET | `/api/permissoes`, `/api/perfis`, `/api/perfis/{id}` | `PERFIL_GERENCIAR` ou `USUARIO_GERENCIAR` |
| POST / PUT / DELETE | `/api/perfis`, `/api/perfis/{id}` | `PERFIL_GERENCIAR` |
| GET | `/api/products`, `/api/products/{id}` | `PRODUTO_VISUALIZAR` |
| POST | `/api/products` | `PRODUTO_CRIAR` |
| PUT | `/api/products/{id}` | `PRODUTO_EDITAR` |
| DELETE | `/api/products/{id}` | `PRODUTO_EXCLUIR` |

### Como rodar

**Desenvolvimento** (dois terminais):

```bash
./mvnw spring-boot:run          # terminal 1: API em http://localhost:8080
cd frontend && npm install      # terminal 2 (só na primeira vez)
npm run dev                     # terminal 2: telas em http://localhost:5173
```

Abra **http://localhost:5173**. O Vite recarrega a tela a cada alteração e repassa as chamadas `/api` para o Spring (proxy em `vite.config.ts`).

**Testes:** `./mvnw test` (back-end) e `cd frontend && npm run lint` (front-end).

**Pacote para a empresa** (um único jar com as telas e a API):

```bash
./mvnw package                  # baixa o Node sozinho e compila o front para dentro do jar
java -jar target/smge-4.2.jar   # tudo em http://localhost:8080
```

- Não é preciso ter o Node instalado para gerar o pacote: o `frontend-maven-plugin` baixa o Node na pasta `frontend/node/`.
- Se o `npm install` falhar com `ENOTEMPTY` ou `EPERM` no Windows, algum programa está usando a pasta `frontend/node_modules`: feche-o e rode de novo. Se o VS Code mostrar erro no `pom.xml`, use o comando **"Java: Reload Projects"**.

- **Autenticação (JWT):**
  1. `POST /api/auth/login` com `{"login": "...", "senha": "..."}`. A resposta traz `accessToken`, `expiraEm` e os dados do usuário (com as permissões).
  2. Nas demais requisições, envie o header `Authorization: Bearer {accessToken}`.
  - O token vale 8 horas (`SMGE_JWT_VALIDADE_HORAS`).
  - Desativar o usuário ou alterar os acessos dele vale na hora, mesmo com o token já emitido.
  - Trocar a senha invalida os tokens antigos: o front deve pedir login de novo.
  - Senha expirada no login devolve 401 com `"codigo": "SENHA_EXPIRADA"`.
- **Variáveis de ambiente obrigatórias em produção:** `SMGE_JWT_SEGREDO` (mínimo de 32 caracteres, diferente para cada empresa), `SMGE_ADMIN_LOGIN`, `SMGE_ADMIN_SENHA` e `SMGE_CORS_ORIGENS` (endereço do front).
- Administrador inicial: login `admin`, senha `Admin@123`. Em produção, defina `SMGE_ADMIN_LOGIN` e `SMGE_ADMIN_SENHA`.
- Banco H2 em memória (os dados somem ao reiniciar). Console em `http://localhost:8080/h2-console`, JDBC URL `jdbc:h2:mem:smge`, usuário `sa`, sem senha.

### Pendências e próximos passos

- [ ] **Produtos (back-end):** DTOs com validação, `BigDecimal` no preço, renomear `descrição` para `descricao`, `dataCadastro` automática, 409 para código ou nome duplicado.
- [ ] Campos opcionais e personalizados de produto (configuração por instância).
- [ ] Movimentação de estoque (entradas e saídas) e alerta de estoque baixo.
- [ ] Obrigar a troca de senha no primeiro acesso.
- [ ] Hoje uma senha expirada bloqueia o login, e só quem tem `USUARIO_GERENCIAR` consegue liberar. Avaliar permitir que o próprio usuário troque a senha expirada.
- [ ] Trocar o H2 por um banco persistente (ex.: PostgreSQL).
- [ ] Limitar tentativas de login (proteção contra força bruta).
- [ ] Telas de produtos (depois do back-end de produtos).
- [ ] Testes automatizados do front-end (ex.: Vitest + Testing Library).

---

## [4.2] — 2026-10-01

Telas de usuários e perfis de acesso.

### Adicionado
- **Usuários** (`/usuarios`, exige `USUARIO_VISUALIZAR`):
  - Lista com busca por nome ou login, filtro de situação, perfis, permissões extras e a data de expiração da senha (em vermelho se já expirou).
  - Cadastro com senha provisória, perfis e permissões extras.
  - Ações para quem tem `USUARIO_GERENCIAR`: alterar acessos, redefinir senha, desativar e reativar.
  - As ações somem para o próprio usuário e para quem tem mais acessos que você, seguindo a mesma regra do back-end.
- **Perfis de acesso** (`/perfis`, exige `PERFIL_GERENCIAR`): lista, criação, edição e exclusão. O perfil Administrador aparece como "Sistema" e não pode ser alterado.
- `SeletorPermissoes`: checklist por módulo com "marcar todas". As permissões que o usuário logado não possui ficam bloqueadas, e as que já vêm de um perfil são indicadas.
- Erros da API (login duplicado, perfil em uso etc.) aparecem no campo ou em uma mensagem.

### Corrigido
- O VS Code (extensão Java) executava o `npm install` e o `npm run build` em segundo plano a cada arquivo salvo, travando a pasta `frontend/node_modules` e mostrando erro no `pom.xml`. O `pom.xml` agora diz à IDE para ignorar o `frontend-maven-plugin`; o `./mvnw package` continua compilando o front normalmente.

---

## [4.1] — 2026-10-01

Estrutura do front-end, login e minha conta.

### Adicionado
- Projeto `frontend/` com React 19, TypeScript, Vite 8, Ant Design 6 (em português) e React Router 7.
- `api/`: cliente HTTP que envia o token e converte os erros do back-end em mensagens, mais uma função por endpoint.
- `auth/`: sessão do usuário (token no `localStorage`). Ao abrir o sistema o token é conferido, e qualquer 401 da API leva de volta ao login com aviso.
- **Login:** mensagens para senha errada, usuário desativado e senha expirada.
- **Layout:** menu lateral que mostra só as telas que o usuário pode acessar (`layout/menu.tsx`) e menu do usuário com Minha conta e Sair.
- **Início:** atalhos para as telas liberadas e aviso quando a senha expira em até 7 dias.
- **Minha conta:** dados do usuário e troca de senha. Depois da troca, o sistema pede login de novo.
- Rotas protegidas por permissão, com tela de "Sem permissão" e de "Página não encontrada".

---

## [4.0] — 2026-10-01

API sob `/api` e front-end servido pelo próprio Spring Boot (um único container por empresa).

### ⚠️ Mudanças que quebram compatibilidade
- Todas as rotas da API ganharam o prefixo `/api` (ex.: `/users` virou `/api/users`, `/auth/login` virou `/api/auth/login`).

### Adicionado
- `WebConfig`: aplica o prefixo `/api` a todos os `@RestController` e serve o front-end de `classpath:/static`. Rotas do React (ex.: `/usuarios`) devolvem o `index.html`, então o F5 funciona em qualquer tela. Rotas inexistentes da API continuam dando 404.
- `frontend-maven-plugin` no `pom.xml`: o `./mvnw package` baixa o Node, instala as dependências e compila o front para dentro do jar. `test` e `spring-boot:run` não executam essa etapa.
- Proxy do Vite para `/api` em desenvolvimento.
- `WebConfigTest` (5). Total: 41 testes.

### Alterado
- `SecurityConfig`: `/api/**` exige token (exceto `POST /api/auth/login`); o resto (telas, JS, CSS) é público.

---

## [3.0] — 2026-10-01

Autenticação por token JWT no lugar do HTTP Basic, preparando a API para o front-end.

### ⚠️ Mudanças que quebram compatibilidade
- HTTP Basic removido. Agora é preciso fazer `POST /auth/login` e enviar `Authorization: Bearer {token}`.
- Nova coluna obrigatória `senha_alterada_em` na tabela `users`.

### Adicionado
- `POST /auth/login` (`AuthController`, `LoginRequest`, `LoginResponse`): devolve o token e os dados do usuário.
- `JwtConfig` (HS256 com o segredo de `smge.jwt.segredo`) e `TokenService`, usando o `spring-boot-starter-security-oauth2-resource-server`, sem biblioteca de terceiros.
- `JwtUsuarioConverter`: a cada requisição, carrega o usuário do banco. Desativação, mudança de acessos e senha expirada valem na hora.
- `UserModel.senhaAlteradaEm`: trocar a senha invalida os tokens emitidos antes.
- Mensagens de erro de login: credenciais inválidas (a mesma mensagem para login inexistente), usuário desativado e senha expirada (`codigo: SENHA_EXPIRADA`).
- CORS configurável por `smge.cors.origens` (padrão `http://localhost:5173`, o Vite).
- Testes: `ApiTestBase` (login real nos testes de API) e `AuthControllerTest` (8). Total: 36 testes.

### Alterado
- `SecurityConfig`: `oauth2ResourceServer().jwt()`, `AuthenticationManager` próprio e CORS.
- `SmgeUserDetailsService` passou a ser usado só no login e expõe `authoritiesDe(user)`.

---

## [2.0] — 2026-10-01

Permissões granulares e perfis de acesso, no lugar dos papéis fixos ADMIN/USER.

### ⚠️ Mudanças que quebram compatibilidade
- O enum `Role` e o campo `role` foram removidos do usuário e da API.
- `POST /users` agora recebe `perfisIds` e `permissoesExtras` em vez de `role`.
- `UserResponse` troca `role` por `perfis`, `permissoesExtras` e `permissoes` (soma final).
- `/products` deixou de aceitar qualquer usuário logado: cada operação exige a sua permissão.
- Novas tabelas no banco: `perfis`, `perfil_permissoes`, `user_perfis`, `user_permissoes_extras`.

### Adicionado
- Enums `Permissao` (com módulo e descrição) e `Modulo`.
- Entidade `PerfilModel` e `PerfilRepository`.
- `UserModel`: perfis, permissões extras e `permissoesEfetivas()`.
- Endpoints de perfis (`/perfis`) e o catálogo de permissões por módulo (`/permissoes`).
- `PUT /users/{id}/acessos` para trocar os perfis e as permissões extras de um usuário.
- `@PreAuthorize` em todos os endpoints de usuários, perfis e produtos (`@EnableMethodSecurity`).
- `UsuarioLogado`: regras contra escalada de privilégio.
- `AcessoNegadoException` (403) no tratamento global de erros.
- Perfil de sistema **Administrador**, mantido pelo `AdminSeeder` sempre com todas as permissões.
- Testes: `PerfilControllerTest` (6), cenários de permissão e escalada em `UserControllerTest` (10) e `UserServiceTest` (8), soma de permissões em `UserModelTest` (3). Total: 28 testes.

### Alterado
- `SecurityConfig` só exige login; a autorização ficou por endpoint.
- Login carrega as permissões efetivas do usuário como authorities.
- Desativar, reativar e redefinir a senha do próprio usuário passaram a ser bloqueados (use `/users/me/senha`).

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
