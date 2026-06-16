<script>
  import '../app.css';
  import { onMount } from 'svelte';
  import { helpOpen } from '$lib/ui.js';
  let { children } = $props();

  let isDark = $state(false);

  function toggleTheme() {
    isDark = !isDark;
    const t = isDark ? 'dark' : 'light';
    document.documentElement.dataset.theme = t;
    localStorage.setItem('theme', t);
  }

  function onKey(e) {
    const typing = e.target.matches?.('input, textarea, [contenteditable]');
    if (e.key === '?' && !typing) { e.preventDefault(); helpOpen.update((v) => !v); return; }
    if ((e.key === 't' || e.key === 'T') && !typing) { e.preventDefault(); toggleTheme(); return; }
    if (e.key === 'Escape' && $helpOpen) { e.preventDefault(); helpOpen.set(false); }
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

  const shortcuts = [
    { keys: ['/'], desc: 'Focus search (list)' },
    { keys: ['j', 'k'], desc: 'Move selection' },
    { keys: ['Enter'], desc: 'Open selected session' },
    { keys: ['c'], desc: 'Copy resume command (detail)' },
    { keys: ['h', '←', 'Esc'], desc: 'Back to list (detail)' },
    { keys: ['t'], desc: 'Toggle light / dark theme' },
    { keys: ['?'], desc: 'Toggle this help' }
  ];
</script>

<svelte:window onkeydown={onKey} />

<div class="container">
  <header class="topbar">
    <a class="brand" href="/">
      <span class="dot"></span>
      <h1>Session Ledger</h1>
    </a>
    <div class="topbar-actions">
      <button class="theme-toggle" onclick={() => helpOpen.set(true)} title="Keyboard shortcuts (?)" aria-label="Keyboard shortcuts">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><path d="M9.1 9a3 3 0 0 1 5.8 1c0 2-3 3-3 3"/><line x1="12" y1="17" x2="12" y2="17"/></svg>
      </button>
      <button class="theme-toggle" onclick={toggleTheme} title="Toggle theme" aria-label="Toggle theme">
        {#if isDark}
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="4"/><path d="M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4"/></svg>
        {:else}
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"/></svg>
        {/if}
      </button>
    </div>
  </header>
  {@render children()}
</div>

{#if $helpOpen}
  <div class="help-overlay" onclick={() => helpOpen.set(false)} role="presentation">
    <div class="help-modal" role="dialog" aria-label="Keyboard shortcuts" onclick={(e) => e.stopPropagation()}>
      <h3>Keyboard shortcuts</h3>
      <table>
        {#each shortcuts as s}
          <tr>
            <td class="key">{#each s.keys as k}<kbd>{k}</kbd>{' '}{/each}</td>
            <td>{s.desc}</td>
          </tr>
        {/each}
      </table>
      <button class="btn close" onclick={() => helpOpen.set(false)}>Close</button>
    </div>
  </div>
{/if}
