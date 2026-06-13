-- session-ledger initial schema.
-- Lives in a dedicated schema inside the (separately created) database `session_ledger`.

CREATE SCHEMA IF NOT EXISTS ledger;

-- One row per Claude Code session (the card head).
CREATE TABLE ledger.session (
    session_id   TEXT        PRIMARY KEY,           -- real Claude Code session id
    project_dir  TEXT        NOT NULL,              -- cwd the session was started in (for `claude --resume`)
    status       TEXT        NOT NULL DEFAULT 'ACTIVE',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Append-only version chain: the vertical history graph for a session.
-- parent_version_id links a node to its predecessor; the tip = node with no children (max seq).
CREATE TABLE ledger.session_version (
    id                UUID        PRIMARY KEY,
    session_id        TEXT        NOT NULL REFERENCES ledger.session (session_id) ON DELETE CASCADE,
    parent_version_id UUID            NULL REFERENCES ledger.session_version (id) ON DELETE SET NULL,
    seq               INTEGER     NOT NULL,         -- 1-based position in the chain
    short_description TEXT        NOT NULL,         -- 1-2 sentences, shown in the UI
    long_description  TEXT            NULL,         -- optional richer context for Claude
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_session_seq UNIQUE (session_id, seq)
);

CREATE INDEX idx_version_session     ON ledger.session_version (session_id);
CREATE INDEX idx_version_parent      ON ledger.session_version (parent_version_id);
CREATE INDEX idx_session_updated_at  ON ledger.session (updated_at DESC);
