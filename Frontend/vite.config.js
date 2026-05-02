import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

const configRoot = fileURLToPath(new URL('.', import.meta.url))

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, configRoot, '')
  const backendTarget = env.VITE_API_PROXY_TARGET || 'http://localhost:8091'

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': {
          target: backendTarget,
          changeOrigin: true,
          secure: false,
        },
      },
    },
    optimizeDeps: {
      include: [
        '@fullcalendar/react',
        '@fullcalendar/timegrid',
        '@fullcalendar/interaction',
        '@fullcalendar/daygrid',
      ],
    },
  }
})
