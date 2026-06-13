-- Run ONCE on your PostgreSQL server as a superuser to provision the database and role
-- for session-ledger (server setup only). Flyway then creates the `ledger` schema and tables.
-- Standalone/SQLite users don't need this — see docs/run-standalone.md.
--
--   psql -h <db-host> -U postgres -f db/bootstrap.sql
--
-- Change the password before running in any real environment.

CREATE ROLE session_ledger WITH LOGIN PASSWORD 'change-me';
CREATE DATABASE session_ledger OWNER session_ledger;

-- Schema-level grants are handled by Flyway (create-schemas + default-schema=ledger),
-- since the role owns the database.
