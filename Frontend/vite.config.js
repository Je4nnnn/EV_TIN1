import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

const backendTarget = process.env.VITE_API_PROXY_TARGET || 'http://localhost:8091';

export default defineConfig({
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
});
