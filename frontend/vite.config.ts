import react from '@vitejs/plugin-react'
import { defineConfig, loadEnv } from 'vite'

// O front chama a API em /api/... e o Vite repassa para o Spring sem o prefixo
// (/api/materiais -> http://localhost:8080/materiais). Assim não é preciso configurar
// CORS no backend, e as rotas do front (/ranking, /acervo...) não se confundem com as da API.
// Para a API em outra porta: crie frontend/.env.local com API_URL=http://localhost:8081
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const api = env.API_URL || 'http://localhost:8080'

  return {
    plugins: [react()],
    server: {
      port: 5173,
      proxy: {
        '/api': {
          target: api,
          changeOrigin: true,
          rewrite: (caminho) => caminho.replace(/^\/api/, ''),
        },
      },
    },
  }
})
