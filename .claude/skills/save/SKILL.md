---
name: save
description: Save (or update) the current Claude Code session as a card in the session-ledger backend via the MCP tool. Invoke explicitly with /save.
disable-model-invocation: true
---

# /save — record this session in the session ledger

Persist the current session as a card so it shows up in the session-ledger web UI and can
be resumed later. Re-running `/save` in the same session **appends a new version** to the
top of that session's history graph (nothing is overwritten).

## Steps

1. **Identify the session** (no hook needed):
   - `sessionId` = `${CLAUDE_SESSION_ID}` — Claude Code substitutes the live session id here.
   - `projectDir` = the directory this session was **launched** in (what `claude --resume` needs).
     Resolve it from the session transcript's first recorded `cwd`, so it stays correct even if the
     working directory drifted via `cd`; falls back to the current dir:

     !`f=$(find "$HOME/.claude/projects" -name "${CLAUDE_SESSION_ID}.jsonl" 2>/dev/null | head -1); d=$([ -n "$f" ] && grep -ohE '"cwd":"[^"]*"' "$f" 2>/dev/null | head -1 | sed -E 's/.*"cwd":"([^"]*)".*/\1/'); [ -n "$d" ] && echo "$d" || pwd`

   Use that printed path as `projectDir`. If `${CLAUDE_SESSION_ID}` did not expand to a real id
   (it still shows the literal `${CLAUDE_SESSION_ID}` or is empty), ask the user and stop.

2. **Compose the card content** from the current conversation:
   - `title`: a short, contextual **name** for the card — a few words (≤ ~6) capturing what the
     session was about, e.g. `"Standalone install + /save fixes"`. **Don't prefix it with the
     project name** — the project is already shown via the working directory. Keep it tight, not a sentence.
   - `short_description`: **one** concise sentence shown under the title as a preview.
     Don't write a paragraph here.
   - `long_description` (optional): the full context for future-you — key decisions, open
     threads, file paths, next steps. This is where detail goes, not in the title/short.
   - `tags` (optional): a short list of technologies/topics, e.g.
     `["Java 25", "Spring Boot 4", "SvelteKit", "Postgres"]`. Tech names, not sentences.

3. **Call the MCP tool** `save_session` (server: `session-ledger`) with:
   - `sessionId`  = the session id from step 1
   - `projectDir` = the cwd from step 1
   - `title` = the short name
   - `shortDescription` = the one-sentence preview
   - `longDescription`  = the longer notes (omit if not useful)
   - `tags` = the technology/topic list (omit if none obvious)

4. **Report back** the version number and the card URL returned by the tool.

## Notes
- This skill only ever creates/updates. Deleting cards is done from the web UI.
- `${CLAUDE_SESSION_ID}` resolves to the session you're running in, so concurrent sessions stay distinct.
