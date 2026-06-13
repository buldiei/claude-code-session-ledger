import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
  plugins: [sveltekit()],
  server: {
    port: 5173,
    proxy: {
      // forward API calls to the Spring Boot backend during dev
      '/api': 'http://localhost:8080'
    }
  }
});
