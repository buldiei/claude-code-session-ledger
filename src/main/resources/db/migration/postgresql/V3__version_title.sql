-- Short, contextual card name (a few words), distinct from the 1-sentence short_description.
-- Used as the heading in the UI. Nullable so pre-existing versions fall back to short_description.
ALTER TABLE ledger.session_version ADD COLUMN title TEXT;
