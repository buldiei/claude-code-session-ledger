export function relativeTime(iso) {
  if (!iso) return '';
  const then = new Date(iso).getTime();
  const diff = Date.now() - then;
  const min = Math.round(diff / 60000);
  if (min < 1) return 'just now';
  if (min < 60) return `${min}m ago`;
  const hrs = Math.round(min / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.round(hrs / 24);
  if (days < 30) return `${days}d ago`;
  return new Date(iso).toLocaleDateString();
}

export function shortId(id) {
  if (!id) return '';
  return id.length > 12 ? id.slice(0, 8) : id;
}

/** Display a home path as ~/… instead of /Users/<user>/… or /home/<user>/…. */
export function tildify(path) {
  if (!path) return path;
  return path.replace(/^\/(?:Users|home)\/[^/]+(?=\/|$)/, '~');
}

function shellQuote(s) {
  return "'" + String(s).replace(/'/g, "'\\''") + "'";
}

/**
 * Build a shell-safe resume command that uses ~ for the home dir.
 * `cd ~/'rest of path' && claude --resume <id>` — tilde stays unquoted so the shell
 * expands it, the remainder is quoted to survive spaces.
 */
export function resumeCommand(projectDir, sessionId) {
  const dir = projectDir || '';
  const home = dir.match(/^\/(?:Users|home)\/[^/]+(\/.*)?$/);
  let cd;
  if (home) {
    const rest = (home[1] || '').replace(/^\//, '');
    cd = rest ? `cd ~/${shellQuote(rest)}` : 'cd ~';
  } else {
    cd = `cd ${shellQuote(dir)}`;
  }
  return `${cd} && claude --resume ${sessionId}`;
}
