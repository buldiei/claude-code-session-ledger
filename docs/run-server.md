# Run on a server (PostgreSQL, reachable over the network)

For hosting the service on a machine other than your laptop — a home server, a VPS, a NAS —
backed by PostgreSQL, so the web UI and `/save` work from anywhere on your network.

## 1. Database

Point at an **existing PostgreSQL** (recommended) or let the compose spin up a **bundled** one.

### Existing Postgres
Create the database and role once (edit the password first):
```bash
psql -h <db-host> -U postgres -f db/bootstrap.sql
```
Flyway creates the `ledger` schema and tables on first start.

### Bundled Postgres
Skip the step above; set `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/session_ledger`
in `.env` and start with the `localpg` profile (below).

## 2. Configure

```bash
cp .env.example .env
```
Edit `.env`:
- `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` — your Postgres.
- `SESSION_LEDGER_MCP_TOKEN` — `openssl rand -hex 24`.
- `SESSION_LEDGER_WEB_BASE_URL` — **optional**; the card link is derived from the request host by
  default. Set it (e.g. `http://192.168.1.50:8080`) only behind a reverse proxy or when the public
  URL differs from what clients connect to. `SESSION_LEDGER_CORS_ORIGINS` is only needed if the UI
  is served from a different origin than the API.

## 3. Start

```bash
docker compose up -d --build                     # uses your external Postgres
# or, with the bundled Postgres:
docker compose --profile localpg up -d --build
```
No Java or Node needed on the server — Docker builds everything.

Reachable at `http://<server>:${SERVER_PORT:-8080}` — web UI, `/api`, `/mcp` on one origin.

## 4. Wire up Claude Code (on each client machine)

```bash
client/install.sh http://<server>:8080 <SESSION_LEDGER_MCP_TOKEN>
```
Restart Claude Code; `/save` now works from any directory and talks to the server.

## Notes
- A stable address helps (hostname or static IP) so the MCP registration doesn't break when
  the server's IP changes.
- Web read/delete is unauthenticated — intended for a trusted LAN. Put it behind a reverse
  proxy with auth to expose it more widely.
