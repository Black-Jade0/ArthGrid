import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// Website runs on http://localhost:5173 and talks to the API Gateway on http://localhost:9090
export default defineConfig({
  plugins: [react()],
  server: { port: 5173 },
})
