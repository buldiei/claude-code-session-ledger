<script>
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import { get } from 'svelte/store';
  import { getSession, deleteSession } from '$lib/api.js';
  import { relativeTime, tildify, resumeCommand } from '$lib/format.js';
  import { helpOpen } from '$lib/ui.js';

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

  async function copyText(text) {
    // navigator.clipboard exists only in secure contexts (HTTPS/localhost); on a plain-HTTP
    // LAN address it's undefined, so fall back to the legacy execCommand approach.
    try {
      if (navigator.clipboard && window.isSecureContext) {
        await navigator.clipboard.writeText(text);
        return true;
      }
    } catch (e) { /* fall through to legacy */ }
    try {
      const ta = document.createElement('textarea');
      ta.value = text;
      ta.setAttribute('readonly', '');
      ta.style.position = 'fixed';
      ta.style.opacity = '0';
      document.body.appendChild(ta);
      ta.select();
      const ok = document.execCommand('copy');
      document.body.removeChild(ta);
      return ok;
    } catch (e) {
      return false;
    }
  }

  async function copyResume() {
    const ok = await copyText(resumeCommand(state.session.projectDir, state.session.sessionId));
    flash(ok ? 'Resume command copied' : 'Copy failed — select the command manually');
  }

  async function remove() {
    if (!confirm('Delete this session and all its versions? This cannot be undone.')) return;
    await deleteSession(id);
    goto('/');
  }

  function onKey(e) {
    if (get(helpOpen)) return;
    if (e.target.matches?.('input, textarea, [contenteditable]')) return;
    if (e.key === 'Escape' || e.key === 'ArrowLeft' || e.key === 'h') goto('/');
    else if (e.key === 'c' && state.status === 'ready') copyResume();
  }
</script>

<svelte:window onkeydown={onKey} />

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

    {#if tip?.tags?.length}
      <div class="tech-section">
        <span class="label">Tags</span>
        <div class="tags">
          {#each tip.tags as t}<span class="tag">{t}</span>{/each}
        </div>
      </div>
    {/if}

    <div class="resume">
      <code>{resumeCommand(s.projectDir, s.sessionId)}</code>
      <button class="resume-copy" onclick={copyResume} title="Copy resume command" aria-label="Copy resume command">
        <svg width="15" height="15" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">
          <rect x="9" y="9" width="13" height="13" rx="2"/>
          <path d="M5 15H4a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h9a2 2 0 0 1 2 2v1"/>
        </svg>
      </button>
    </div>
    <div class="detail-actions">
      <span class="count">{s.versions.length} version{s.versions.length === 1 ? '' : 's'} · updated {relativeTime(s.updatedAt)}</span>
      <button class="btn danger" onclick={remove} style="margin-left:auto">Delete</button>
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
