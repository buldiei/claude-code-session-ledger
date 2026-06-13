# Run standalone (no server, no database to set up)

The simplest way: everything runs on your own machine and stores data in a single **SQLite
file**. No Postgres, no server. Good for personal use on a laptop/desktop.

## Decide the MCP token first

The `/mcp` endpoint can be protected by a bearer token (`SESSION_LEDGER_MCP_TOKEN`). Pick one
*before* starting — don't leave it to chance:

- **Generate one** (recommended): `export SESSION_LEDGER_MCP_TOKEN=$(openssl rand -hex 24)` — keep
  it, you'll pass the same value to the client installer.
- **Run open** (token unset): fine only on a trusted local machine; anyone who can reach the port
  can call `/mcp`.

> Heads-up: `docker compose` auto-loads a `.env` file from this directory for variable
> interpolation. If a `.env` is already present (e.g. left over from a server setup), its
> `SESSION_LEDGER_MCP_TOKEN` will be used silently. Check with `grep SESSION_LEDGER_MCP_TOKEN .env`
> if you're unsure which token is in effect.

## Option A — Prebuilt image (fastest, no build)

Pull the published multi-arch image (amd64 + arm64) and run it — starts in seconds:

```bash
docker run -d --name session-ledger -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=sqlite \
  -e SESSION_LEDGER_SQLITE_PATH=/app/data/session-ledger.db \
  -e SESSION_LEDGER_MCP_TOKEN="$SESSION_LEDGER_MCP_TOKEN" \
  -v session-ledger-data:/app/data \
  ghcr.io/buldiei/claude-code-session-ledger:latest
```

- Open <http://localhost:8080> — the web UI. API at `/api`, MCP at `/mcp`, same origin.
- Data persists in the `session-ledger-data` volume.
- You still need the repo for the client step (the `/save` skill + installer).

## Option B — Docker Compose (build from source)

Builds Java and Node for you; nothing else to install.

```bash
docker compose -f docker-compose.standalone.yml up -d --build
```

- Open <http://localhost:8080> — the web UI.
- The SQLite DB lives in the `session_ledger_data` Docker volume (survives restarts).
- API at `/api`, MCP at `/mcp`, all on the same origin.

## Option C — No Docker (run the jar)

Needs JDK 25 (`sdk install java 25-amzn`) and Node 20+.

```bash
# 1) build the SPA and bake it into the app
cd frontend && npm install && npm run build && cd ..
mkdir -p src/main/resources/static && cp -r frontend/build/* src/main/resources/static/

# 2) build the jar
mvn -DskipTests package

# 3) run with the SQLite profile (DB file created at ./data/session-ledger.db)
export SPRING_PROFILES_ACTIVE=sqlite
export SESSION_LEDGER_MCP_TOKEN=$(openssl rand -hex 24)
java -jar target/session-ledger-*.jar
```

On the SQLite profile Flyway is off — Hibernate creates the schema automatically.

> macOS, no Docker: if Tomcat logs `Error setting socket options: Invalid argument`, add the
> VM option `-Djava.net.preferIPv4Stack=true`.

## Wire up Claude Code

```bash
client/install.sh http://localhost:8080 "$SESSION_LEDGER_MCP_TOKEN"
```
Restart Claude Code, then run `/save` from any directory. See the main [README](../README.md#client-integration)
for what the installer does.

## Where's my data?

- Docker: the `session_ledger_data` volume. Back it up with `docker run --rm -v session_ledger_data:/d -v "$PWD":/b alpine cp /d/session-ledger.db /b/`.
- Jar: the file `./data/session-ledger.db` (set `SESSION_LEDGER_SQLITE_PATH` to move it).

Outgrown standalone and want it on a server with Postgres? See [run-server.md](run-server.md).
