import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath } from 'node:url'

export default defineConfig({
  plugins: [vue()],
  base:'/dataflow5/',
  define: {
    'global': {}
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    }
  },
  server: {
    port: 18082,  // 端口号
    // proxy: {
    //   '/api': {
    //     target: 'http://localhost:8082',
    //     changeOrigin: true,
    //     rewrite: (path) => path.replace(/^\/api/, ''),
    //   }
    // }
  }
});
