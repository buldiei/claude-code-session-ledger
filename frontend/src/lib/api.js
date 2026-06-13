// Thin REST client. The web side is read + delete only (CRUD = _R_D).
const BASE = '/api/sessions';

export async function listSessions() {
  const res = await fetch(BASE);
  if (!res.ok) throw new Error(`List failed: ${res.status}`);
  return res.json();
}

export async function getSession(id) {
  const res = await fetch(`${BASE}/${encodeURIComponent(id)}`);
  if (res.status === 404) return null;
  if (!res.ok) throw new Error(`Get failed: ${res.status}`);
  return res.json();
}

export async function deleteSession(id) {
  const res = await fetch(`${BASE}/${encodeURIComponent(id)}`, { method: 'DELETE' });
  if (!res.ok && res.status !== 404) throw new Error(`Delete failed: ${res.status}`);
  return res.status === 204;
}
