# session-ledger — project rules

A sub-project with its own git. Stores Claude Code sessions as versioned cards. This file is the
**code-level** guide. For deploying/running the service and onboarding, see [AGENTS.md](AGENTS.md)
and [docs/](docs); user-facing overview is in [README.md](README.md).

> All text in this repo is **English only** — no Russian (or other non-English) anywhere:
> code, comments, docs, commit messages, identifiers.

## CRUD boundaries

- **Web (you): R + D only.** REST `/api/sessions` — list, card, delete. Creating/updating via REST is not allowed by design.
- **MCP (the model): C + U only.** The single tool `save_session` creates a card or **appends** a new version to the tip of the history graph. No delete/read on the MCP side.
- Re-running `/save` in one session = **append**, not overwrite. History is immutable.

## Architecture

Clean Architecture, single Maven module, packages `domain` / `application` / `adapter` / `config`.
**Do not pull Spring/JPA into `domain`** — it stays pure. Adapters depend on ports, not vice versa.

- Inbound ports = use case interfaces (`domain/port/in`), implemented by services (`application/service`).
- Outbound port `SessionRepository` (`domain/port/out`) is implemented by `adapter/out/persistence`.
- A new external contract is a new adapter (e.g. `adapter/in/...`), not a method on an existing controller.
- Spring serves the built SvelteKit SPA from `classpath:/static/` with a SPA fallback (`SpaConfig`),
  same origin as `/api` and `/mcp`. The frontend is baked into the jar at Docker-build time;
  `src/main/resources/static/` is gitignored.

## Conventions

- Java 25, Spring Boot 4, Lombok. **Two databases via Spring profiles:** `postgres` (default, prod) — Flyway + `ddl-auto=validate`, schema `ledger`; `sqlite` (standalone) — Flyway off, `ddl-auto=update`, community `SQLiteDialect`, no schema.
- On Postgres, change the schema **only via a Flyway migration** (`db/migration/postgresql/VN__*.sql`; never edit existing ones). Entities are DB-agnostic: no `schema=` in `@Table` (the schema comes from the profile property). When changing the model, check both profiles.
- `session_id` is the real id from Claude Code; `project_dir` is needed for `claude --resume` (web→CLI). Don't drop it.
- Spring AI 2.0 is recent — if you touch the MCP part, check current docs (starter artifactId, `@Tool` vs `@McpTool`).
- Secrets only in `.env` (gitignored). `.env.example` and `db/bootstrap.sql` carry placeholders; never commit real passwords/tokens.

## Local dev & tests

- Backend: `mvn spring-boot:run` (default `postgres` profile; or `SPRING_PROFILES_ACTIVE=sqlite`).
- Frontend: `cd frontend && npm run dev` (Vite on :5173, proxies `/api` to :8080).
- Tests: `mvn test`.
- Don't run `git init` in the Workspaces root; commit only inside this sub-project.

(Deploy commands, the two run scenarios, and the `client/install.sh` flow live in [AGENTS.md](AGENTS.md) — not repeated here.)
