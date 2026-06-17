#!/usr/bin/env bash
# Install the session-ledger client integration into Claude Code (one command).
#
#   client/install.sh <server-url> <mcp-token>
#   e.g. client/install.sh http://localhost:8080 <token>
#
# Does three things, idempotently:
#   1. installs the /save skill at user scope (~/.claude/skills/save)
#   2. grants the skill read access to ~/.claude/projects (to resolve a session's launch dir)
#   3. registers the MCP server at user scope (available from any directory)
#
# The skill gets the live session id from the ${CLAUDE_SESSION_ID} template variable (no hook).
# Restart Claude Code afterwards so it picks up the skill and MCP tool.
set -euo pipefail

SERVER_URL="${1:?Usage: install.sh <server-url> <mcp-token>}"
TOKEN="${2:?Usage: install.sh <server-url> <mcp-token>}"
SERVER_URL="${SERVER_URL%/}" # strip trailing slash

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CLAUDE_DIR="$HOME/.claude"
SETTINGS="$CLAUDE_DIR/settings.json"
PROJECTS_DIR="$CLAUDE_DIR/projects"
mkdir -p "$CLAUDE_DIR/skills/save"

# 1) skill (user scope)
cp "$REPO_DIR/.claude/skills/save/SKILL.md" "$CLAUDE_DIR/skills/save/SKILL.md"
echo "✓ skill -> $CLAUDE_DIR/skills/save/SKILL.md"

# 2) let the /save skill read the transcript dir so it can resolve a session's launch directory
#    (the dir `claude --resume` needs) even after the cwd has drifted via `cd`.
if command -v jq >/dev/null 2>&1; then
  [ -f "$SETTINGS" ] || echo '{}' > "$SETTINGS"
  tmp="$(mktemp)"
  jq --arg dir "$PROJECTS_DIR" '
    .permissions //= {} |
    .permissions.additionalDirectories //= [] |
    (if (.permissions.additionalDirectories | index($dir)) then .
     else .permissions.additionalDirectories += [$dir] end)
  ' "$SETTINGS" > "$tmp" && mv "$tmp" "$SETTINGS"
  echo "✓ read access to $PROJECTS_DIR (permissions.additionalDirectories)"
else
  echo "! jq not found — add \"$PROJECTS_DIR\" to permissions.additionalDirectories in $SETTINGS"
fi

# 3) MCP server at USER scope (works from any directory)
claude mcp remove session-ledger >/dev/null 2>&1 || true
claude mcp add --scope user --transport http session-ledger "$SERVER_URL/mcp" \
  --header "Authorization: Bearer $TOKEN"
echo "✓ MCP server registered (user scope) -> $SERVER_URL/mcp"

echo
echo "Done. Restart Claude Code, then /save works from any directory."
