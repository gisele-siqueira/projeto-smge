import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // em desenvolvimento, as chamadas /api vão para o Spring Boot
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  build: {
    // o build vai para dentro do back-end: o Spring serve as telas e a API juntos
    outDir: '../target/classes/static',
    emptyOutDir: true,
    // o Ant Design deixa o pacote com ~1,1 MB (≈370 KB compactado). Para um sistema
    // interno isso é aceitável: o navegador baixa uma vez e guarda em cache.
    chunkSizeWarningLimit: 1500,
  },
})
