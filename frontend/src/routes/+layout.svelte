<script>
  import '../app.css';
  import { onMount } from 'svelte';
  let { children } = $props();

  let isDark = $state(false);

  function toggleTheme() {
    isDark = !isDark;
    const t = isDark ? 'dark' : 'light';
    document.documentElement.dataset.theme = t;
    localStorage.setItem('theme', t);
  }

  onMount(() => {
    const saved = localStorage.getItem('theme');
    if (saved === 'dark' || saved === 'light') {
      document.documentElement.dataset.theme = saved;
      isDark = saved === 'dark';
    } else {
      isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    }
  });
</script>

<div class="container">
  <header class="topbar">
    <a class="brand" href="/">
      <span class="dot"></span>
      <h1>Session Ledger</h1>
    </a>
    <button class="theme-toggle" onclick={toggleTheme} title="Toggle theme" aria-label="Toggle theme">
      {#if isDark}
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>
      {:else}
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
      {/if}
    </button>
  </header>
  {@render children()}
</div>
