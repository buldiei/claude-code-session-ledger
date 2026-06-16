<script>
  import { onMount } from 'svelte';
  import { get } from 'svelte/store';
  import { goto } from '$app/navigation';
  import { listSessions } from '$lib/api.js';
  import { relativeTime, shortId } from '$lib/format.js';
  import { helpOpen } from '$lib/ui.js';

  let state = $state({ status: 'loading', sessions: [], error: null });
  let query = $state('');
  let sel = $state(-1);
  let searchEl;

  async function load() {
    try {
      const sessions = await listSessions();
      state = { status: 'ready', sessions, error: null };
    } catch (e) {
      state = { status: 'error', sessions: [], error: e.message };
    }
  }

  onMount(load);

  function haystack(s) {
    return [s.title, s.latestShortDescription, s.projectDir, ...(s.tags ?? [])]
      .filter(Boolean)
      .join(' ')
      .toLowerCase();
  }

  let filtered = $derived(
    state.status === 'ready'
      ? state.sessions.filter((s) => haystack(s).includes(query.trim().toLowerCase()))
      : []
  );

  function onKey(e) {
    if (get(helpOpen)) return;
    const typing = e.target.matches?.('input, textarea, [contenteditable]');
    if (e.key === '/' && !typing) { e.preventDefault(); searchEl?.focus(); return; }
    if (e.key === 'Escape' && typing) { e.target.blur(); return; }
    if (typing) return;
    if (e.key === 'j' || e.key === 'ArrowDown') {
      e.preventDefault();
      sel = Math.min(sel + 1, filtered.length - 1);
    } else if (e.key === 'k' || e.key === 'ArrowUp') {
      e.preventDefault();
      sel = Math.max(sel - 1, 0);
    } else if (e.key === 'Enter' && sel >= 0 && filtered[sel]) {
      goto(`/sessions/${encodeURIComponent(filtered[sel].sessionId)}`);
    }
  }
</script>

<svelte:window onkeydown={onKey} />

{#if state.status === 'ready'}
  <div class="subhead">
    <span class="count">{filtered.length} of {state.sessions.length} session{state.sessions.length === 1 ? '' : 's'}</span>
    <input
      class="search"
      type="search"
      placeholder="Search description, path or tech…  ( / )"
      bind:value={query}
      bind:this={searchEl}
    />
  </div>
{/if}

{#if state.status === 'loading'}
  <p class="loading">Loading sessions…</p>
{:else if state.status === 'error'}
  <p class="error">Couldn’t load sessions: {state.error}</p>
{:else if state.sessions.length === 0}
  <p class="empty">No sessions saved yet. Run <code>/save</code> in Claude Code to record one.</p>
{:else if filtered.length === 0}
  <p class="empty">Nothing matches “{query}”.</p>
{:else}
  <div class="card-grid">
    {#each filtered as s, i (s.sessionId)}
      <a class="card" class:selected={i === sel} href={`/sessions/${encodeURIComponent(s.sessionId)}`}>
        <div class="meta">
          <span class="pill">v{s.versionCount}</span>
          <span>{relativeTime(s.updatedAt)}</span>
        </div>
        <div class="card-title">{s.title ?? s.latestShortDescription ?? shortId(s.sessionId)}</div>
        {#if s.title && s.latestShortDescription}
          <div class="desc">{s.latestShortDescription}</div>
        {/if}
        {#if s.tags?.length}
          <div class="tags">
            {#each s.tags.slice(0, 4) as t}<span class="tag">{t}</span>{/each}
            {#if s.tags.length > 4}<span class="tag tag-more">+{s.tags.length - 4}</span>{/if}
          </div>
        {/if}
      </a>
    {/each}
  </div>
{/if}
