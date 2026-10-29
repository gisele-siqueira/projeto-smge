# ==============================================================================
# Estágio 1: Build da aplicação React com Node.js e TypeScript
# ==============================================================================
FROM node:22-alpine AS builder

WORKDIR /app

# Copia definições de dependências
COPY package.json package-lock.json ./

# Instala todas as dependências de forma limpa e reprodutível
RUN npm ci

# Copia todo o código-fonte do frontend
COPY . .

# Compila o frontend em modo de produção
# Devido à configuração outDir: '../target/classes/static' do vite.config.ts,
# os artefatos compilados serão gerados em /target/classes/static
RUN npm run build

# ==============================================================================
# Estágio 2: Servidor web de produção com Nginx
# ==============================================================================
FROM nginx:1.27-alpine AS runtime

# Remove os arquivos HTML padrão do Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copia os arquivos estáticos gerados no build
COPY --from=builder /target/classes/static /usr/share/nginx/html

# Copia o arquivo de configuração do Nginx (SPA + proxy reverso)
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Porta padrão HTTP
EXPOSE 80

# Inicia o Nginx em primeiro plano
CMD ["nginx", "-g", "daemon off;"]
