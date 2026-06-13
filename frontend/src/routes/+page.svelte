<script>
  import { onMount } from 'svelte';
  import { listSessions } from '$lib/api.js';
  import { relativeTime, shortId } from '$lib/format.js';

  let state = $state({ status: 'loading', sessions: [], error: null });
  let query = $state('');

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
</script>

{#if state.status === 'ready'}
  <div class="subhead">
    <span class="count">{filtered.length} of {state.sessions.length} session{state.sessions.length === 1 ? '' : 's'}</span>
    <input
      class="search"
      type="search"
      placeholder="Search description, path or tech…"
      bind:value={query}
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
    {#each filtered as s (s.sessionId)}
      <a class="card" href={`/sessions/${encodeURIComponent(s.sessionId)}`}>
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
            {#each s.tags as t}<span class="tag">{t}</span>{/each}
          </div>
        {/if}
        <div class="path">{s.projectDir}</div>
      </a>
    {/each}
  </div>
{/if}
