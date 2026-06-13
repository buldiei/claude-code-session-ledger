import adapter from '@sveltejs/adapter-static';

/** @type {import('@sveltejs/kit').Config} */
const config = {
  kit: {
    // SPA-style static build; the Spring app or any static host can serve it.
    adapter: adapter({ fallback: 'index.html' }),
    prerender: { entries: [] }
  }
};

export default config;
