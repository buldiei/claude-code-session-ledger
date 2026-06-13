# AGENTS.md — deploying session-ledger

You're an AI coding agent pointed at a fresh clone. Goal: get session-ledger running on this
machine and wire up Claude Code's `/save`. This file is the entry point — follow it, then defer
to the linked guides.

## 1. Choose a mode
- **Standalone** — no database, runs locally, stores data in a single SQLite file. Zero infra.
  → [docs/run-standalone.md](docs/run-standalone.md)
- **Server** — hosted on another machine, backed by PostgreSQL, reachable over the network.
  → [docs/run-server.md](docs/run-server.md)

If the user hasn't said, ask. Default to **standalone**.

## 2. Gather configuration — propose, then WAIT (do not guess)

Before running anything, make sure the required values exist. **Do not** invent them, generate
secrets unprompted, or assume defaults. **Do not** silently rely on a `.env` you find in the
directory — it may be stale or unrelated, and `docker compose` auto-loads `.env` for variable
interpolation, so a leftover file will be picked up without warning. Surface what's needed and
**wait for the user to provide values or approve generating them.**

- **Standalone (SQLite):** the only relevant value is `SESSION_LEDGER_MCP_TOKEN`, which protects
  the `/mcp` endpoint. Present the choice and wait:
  1. the user supplies a token, or
  2. you generate one (`openssl rand -hex 24`) — only after they say yes, or
  3. run with no token (open `/mcp`, acceptable only on a trusted local machine).

  No datasource values are needed (SQLite is a file).

- **Server (Postgres):** needs a real `.env`. If `.env` is missing or still holds placeholders
  (`DB_HOST`, `SERVER_HOST`, `change-me`), list what's required and wait for the user to fill it:
  `SPRING_DATASOURCE_URL/USERNAME/PASSWORD`, `SESSION_LEDGER_MCP_TOKEN`,
  `SESSION_LEDGER_WEB_BASE_URL`. Start from `cp .env.example .env`, then have the user edit it.

- **If a `.env` already exists:** show the user the values that will actually be used and confirm
  they're intended before starting — don't assume the file is yours.

## 3. Run the service
Prefer Docker if present — it builds Java and Node itself, so nothing else needs installing.
- Standalone: `docker compose -f docker-compose.standalone.yml up -d --build`
  (pass `SESSION_LEDGER_MCP_TOKEN` via the environment once the user has decided it).
- Server: `docker compose up -d --build` (after `.env` is filled in).

No Docker? You need JDK 25 and Node 20+. Build the SPA (`cd frontend && npm install && npm run build`),
copy `frontend/build/*` into `src/main/resources/static/`, `mvn -DskipTests package`, then run the
jar (standalone: `SPRING_PROFILES_ACTIVE=sqlite java -jar target/session-ledger-*.jar`).

## 4. Verify
- `GET <url>/` returns the web UI (HTML).
- `GET <url>/api/sessions` returns `[]` (DB reachable, schema created).
- `GET <url>/mcp` returns 401 if a token is set (the MCP endpoint is alive and protected).

## 5. Wire up the client
`client/install.sh <service-url> <mcp-token>` installs the `/save` skill (user scope) and the
MCP server registration. Then tell the user to **restart Claude Code** and check `claude mcp list`.
(Pass the same token the service was started with.) The skill reads the live session id from the
`${CLAUDE_SESSION_ID}` template variable — no hook or extra permissions needed.

## Conventions
- Config is env-driven; real secrets live only in `.env` (gitignored). `.env.example` and
  `db/bootstrap.sql` carry placeholders.
- DB switching is by Spring profile: `postgres` (default, Flyway) vs `sqlite` (Hibernate ddl-update).
- See [CLAUDE.md](CLAUDE.md) for architecture and code conventions before changing code.
