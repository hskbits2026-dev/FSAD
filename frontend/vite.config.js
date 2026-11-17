import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Dev-only forwarder: the browser talks only to :5173, and Vite relays
    // /api and /ws to the Spring Boot backend on :8081. Same apparent origin,
    // so the browser never blocks the calls for CORS reasons.
    proxy: {
      '/api': { target: 'http://localhost:8081', changeOrigin: true },
      '/ws': { target: 'http://localhost:8081', changeOrigin: true, ws: true },
    },
  },
})
