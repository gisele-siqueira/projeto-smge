# SMGE — Sistema de Gestão Empresarial

O SMGE é um sistema de gestão empresarial (ERP) com arquitetura monorepo composta por:
- **Backend**: API REST desenvolvida em Java 17 com Spring Boot, Spring Security (OAuth2/JWT) e banco de dados H2.
- **Frontend**: Aplicação web SPA desenvolvida em React 19, TypeScript, Vite e Ant Design, servida via Nginx.

A aplicação é distribuída através de containers Docker e orquestrada com Docker Compose, utilizando imagens publicadas automaticamente no **GitHub Container Registry (GHCR)**. Não é necessário clonar o repositório completo ou instalar Java, Maven ou Node.js para executar o sistema.

---

## 📋 Pré-requisitos

Para executar a aplicação, você precisa apenas ter instalado em sua máquina:
- [Docker Engine](https://docs.docker.com/engine/install/) (versão 24 ou superior)
- [Docker Compose](https://docs.docker.com/compose/install/) (v2 integrado ao Docker CLI via `docker compose`)

---

## 🚀 Como Executar a Aplicação (Distribuição via Docker Compose)

Para quem deseja apenas executar o sistema, basta obter o arquivo `docker-compose.yml`.

### 1. Baixar o arquivo `docker-compose.yml`

Você pode salvar o arquivo diretamente ou baixá-lo via terminal:

```bash
curl -O https://raw.githubusercontent.com/gisele-siqueira/projeto-smge/main/docker-compose.yml
```

### 2. Primeira Execução

No mesmo diretório onde o `docker-compose.yml` está salvo, execute:

```bash
docker compose up -d
```

O Docker baixará automaticamente as imagens mais recentes do frontend e backend no GHCR e inicializará os containers em segundo plano.

### 3. Acessar o Sistema

Assim que os containers estiverem em execução:
- **Aplicação Web (Frontend + API):** [http://localhost](http://localhost) (porta 80)
- **Credenciais padrão do Administrador inicial:**
  - **Login:** `admin`
  - **Senha:** `Admin@123`
- **Acesso direto à API do Backend / Console H2 (opcional):** [http://localhost:8080](http://localhost:8080) e [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

---

## 🔄 Como Atualizar para a Versão Mais Recente

Sempre que novas alterações forem enviadas para o projeto, novas imagens serão geradas no GHCR. Para atualizar sua instalação local:

```bash
docker compose pull
docker compose up -d
```

O comando `docker compose pull` baixa as versões mais recentes das imagens e o `docker compose up -d` reinicia apenas os containers que foram atualizados, sem tempo de inatividade prolongado.

---

## 📜 Como Visualizar os Logs

Para acompanhar a saída e os logs dos serviços em tempo real:

```bash
docker compose logs -f
```

Para visualizar o log de um serviço específico:

```bash
# Apenas o backend
docker compose logs -f backend

# Apenas o frontend / Nginx
docker compose logs -f frontend
```

---

## 🛑 Como Parar e Remover os Containers

Para parar a execução dos serviços e remover a rede interna criada pelo Compose:

```bash
docker compose down
```

---

## ⚙️ Configuração de Variáveis de Ambiente

As configurações da aplicação podem ser customizadas através de um arquivo `.env` colocado no mesmo diretório do `docker-compose.yml` ou exportadas no terminal antes de iniciar o Compose.

Crie um arquivo `.env` para personalizar as opções:

```dotenv
# Credenciais da conta de administrador inicial
SMGE_ADMIN_LOGIN=admin
SMGE_ADMIN_SENHA=MinhaSenhaSegura@2026

# Chave secreta de assinatura dos tokens JWT (mínimo de 32 caracteres)
SMGE_JWT_SEGREDO=chave-super-secreta-producao-smge-32-caracteres

# Validade do token JWT em horas (padrão: 8)
SMGE_JWT_VALIDADE_HORAS=8

# Portas mapeadas no host (caso 80 ou 8080 já estejam em uso)
FRONTEND_PORT=80
BACKEND_PORT=8080

# Origens permitidas no CORS do Spring Boot
SMGE_CORS_ORIGENS=http://localhost,http://localhost:80,http://localhost:5173
```

---

## 🏗️ Arquitetura e Comunicação entre Containers

A comunicação entre frontend e backend foi desenhada para garantir segurança, simplicidade e compatibilidade com navegadores:

1. **Nginx como Servidor Web e Proxy Reverso:**
   - O container do frontend roda uma imagem leve `nginx:alpine`.
   - O usuário acessa a porta `80` pelo navegador.
   - Requisições de navegação do React Router são resolvidas pelo Nginx (`try_files $uri $uri/ /index.html`), permitindo que rotas como `/usuarios` ou `/perfis` sejam recarregadas sem erro 404.
   - Requisições à API iniciadas com `/api/` são repassadas internamente pelo proxy reverso do Nginx para `http://backend:8080/api/` através da rede interna `smge-network`.
2. **Zero Problemas com CORS no Navegador:**
   - Como o navegador faz as requisições para a mesma origem em que o frontend foi carregado (`http://localhost/api/...`), a política de mesma origem (Same-Origin) é satisfeita nativamente.
3. **Isolamento e Segurança:**
   - O backend Spring Boot roda sob usuário não-privilegiado (`smge`), com configurações de memória para container (`-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0`).
   - As imagens Docker utilizam **multi-stage build**, garantindo que compiladores, código-fonte e dependências de desenvolvimento fiquem de fora da imagem final.

---

## 🤖 Publicação Automática via GitHub Actions

O fluxo de CI/CD automatizado está configurado em `.github/workflows/docker-publish.yml`:

- **Gatilho:** Executado automaticamente a cada `push` na branch `main` (ou manualmente via `workflow_dispatch`).
- **Construção Paralela:** Constrói de forma concorrente as imagens do backend e do frontend.
- **Cache Inteligente:** Utiliza cache de camadas do GitHub Actions (`type=gha`) para builds rápidos.
- **Autenticação:** Utiliza o `GITHUB_TOKEN` com escopo mínimo de permissão (`contents: read`, `packages: write`).
- **Tag:** Publica no GitHub Container Registry (`ghcr.io`) sob a tag `latest` e a tag com o hash do commit (`sha-...`).

### Imagens Publicadas:
- `ghcr.io/gisele-siqueira/projeto-smge/backend:latest`
- `ghcr.io/gisele-siqueira/projeto-smge/frontend:latest`

---

## 🔒 Tornando os Pacotes Públicos no GitHub (Configuração Inicial)

Por padrão, quando o GitHub Actions cria pacotes no GHCR pela primeira vez utilizando o `GITHUB_TOKEN`, eles são criados com visibilidade **Privada**. Para que qualquer usuário consiga baixar as imagens com `docker compose up -d` sem precisar autenticar:

1. Acesse o repositório no GitHub: `https://github.com/gisele-siqueira/projeto-smge`.
2. Na lateral direita da página inicial do repositório, clique em **Packages** (ou acesse a aba do seu perfil/organização em *Packages*).
3. Selecione o pacote `projeto-smge/backend`:
   - Clique em **Package settings** (na barra lateral direita).
   - Role até o final da página até a seção **Danger Zone**.
   - Clique no botão **Change package visibility**.
   - Selecione **Public**, digite o nome do pacote para confirmar e clique em **I understand the consequences, make this package public**.
4. Repita o mesmo procedimento para o pacote `projeto-smge/frontend`.

Pronto! A partir deste momento, qualquer pessoa poderá executar o comando `docker compose up -d` sem necessidade de login no Docker.

---

## 💻 Desenvolvimento Local e Construção a partir do Código-Fonte

Caso você queira desenvolver ou compilar as imagens localmente a partir do código-fonte deste repositório:

### Construir e rodar localmente com Docker Compose:
```bash
docker compose -f docker-compose.yml -f docker-compose.build.yml up --build
```

### Construção manual das imagens individuais:
```bash
# Backend
docker build -t smge-backend:latest -f Dockerfile .

# Frontend
docker build -t smge-frontend:latest -f frontend/Dockerfile ./frontend
```
