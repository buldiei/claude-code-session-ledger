<script>
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { getSession, deleteSession } from '$lib/api.js';
  import { relativeTime } from '$lib/format.js';

  let id = $derived($page.params.id);
  let state = $state({ status: 'loading', session: null, error: null });
  let toast = $state(null);

  async function load(sessionId) {
    try {
      const session = await getSession(sessionId);
      state = session
        ? { status: 'ready', session, error: null }
        : { status: 'notfound', session: null, error: null };
    } catch (e) {
      state = { status: 'error', session: null, error: e.message };
    }
  }

  onMount(() => load($page.params.id));

  function flash(msg) {
    toast = msg;
    setTimeout(() => (toast = null), 1800);
  }

  async function copyResume() {
    await navigator.clipboard.writeText(state.session.resumeCommand);
    flash('Resume command copied');
  }

  async function remove() {
    if (!confirm('Delete this session and all its versions? This cannot be undone.')) return;
    await deleteSession(id);
    goto('/');
  }
</script>

<a class="back" href="/">← All sessions</a>

{#if state.status === 'loading'}
  <p class="loading">Loading…</p>
{:else if state.status === 'notfound'}
  <p class="empty">Session not found.</p>
{:else if state.status === 'error'}
  <p class="error">{state.error}</p>
{:else}
  {@const s = state.session}
  {@const tip = s.versions[0]}
  <div class="detail-head">
    <h2>{tip?.title ?? tip?.shortDescription ?? 'Session'}</h2>
    {#if tip?.title && tip?.shortDescription}
      <p class="lede">{tip.shortDescription}</p>
    {/if}
    <div class="path">{s.projectDir}</div>

    {#if tip?.tags?.length}
      <div class="tech-section">
        <span class="label">Technologies</span>
        <div class="tags">
          {#each tip.tags as t}<span class="tag">{t}</span>{/each}
        </div>
      </div>
    {/if}

    <div class="resume">
      <code>{s.resumeCommand}</code>
    </div>
    <div class="detail-actions">
      <button class="btn accent" onclick={copyResume}>Copy resume command</button>
      <button class="btn danger" onclick={remove}>Delete</button>
      <span class="count" style="margin-left:auto">{s.versions.length} version{s.versions.length === 1 ? '' : 's'} · updated {relativeTime(s.updatedAt)}</span>
    </div>
  </div>

  <!-- Vertical history graph: newest (tip) on top, oldest at the bottom -->
  <div class="graph">
    {#each s.versions as v, i (v.id)}
      <div class="node {i === 0 ? 'tip' : ''}">
        <div class="node-card">
          <div class="node-head">
            <span class="seq">v{v.seq}{i === 0 ? ' · current' : ''}</span>
            <span class="ts">{relativeTime(v.createdAt)}</span>
          </div>
          {#if v.title}<div class="node-title">{v.title}</div>{/if}
          <div class="short">{v.shortDescription}</div>
          {#if v.longDescription}
            <div class="long">{v.longDescription}</div>
          {/if}
          {#if v.tags?.length}
            <div class="tags">
              {#each v.tags as t}<span class="tag">{t}</span>{/each}
            </div>
          {/if}
        </div>
      </div>
    {/each}
  </div>
{/if}

{#if toast}
  <div class="toast">{toast}</div>
{/if}
