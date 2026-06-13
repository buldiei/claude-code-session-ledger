#!/usr/bin/env bash
# Install the session-ledger client integration into Claude Code (one command).
#
#   client/install.sh <server-url> <mcp-token>
#   e.g. client/install.sh http://localhost:8080 <token>
#
# Does two things, idempotently:
#   1. installs the /save skill at user scope (~/.claude/skills/save)
#   2. registers the MCP server at user scope (available from any directory)
#
# The skill gets the live session id from the ${CLAUDE_SESSION_ID} template variable — no hook,
# no extra file permissions. Restart Claude Code afterwards so it picks up the skill and MCP tool.
set -euo pipefail

SERVER_URL="${1:?Usage: install.sh <server-url> <mcp-token>}"
TOKEN="${2:?Usage: install.sh <server-url> <mcp-token>}"
SERVER_URL="${SERVER_URL%/}" # strip trailing slash

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"
CLAUDE_DIR="$HOME/.claude"
mkdir -p "$CLAUDE_DIR/skills/save"

# 1) skill (user scope)
cp "$REPO_DIR/.claude/skills/save/SKILL.md" "$CLAUDE_DIR/skills/save/SKILL.md"
echo "✓ skill -> $CLAUDE_DIR/skills/save/SKILL.md"

# 2) MCP server at USER scope (works from any directory)
claude mcp remove session-ledger >/dev/null 2>&1 || true
claude mcp add --scope user --transport http session-ledger "$SERVER_URL/mcp" \
  --header "Authorization: Bearer $TOKEN"
echo "✓ MCP server registered (user scope) -> $SERVER_URL/mcp"

echo
echo "Done. Restart Claude Code, then /save works from any directory."
