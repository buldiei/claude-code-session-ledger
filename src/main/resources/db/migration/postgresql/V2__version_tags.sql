-- Optional technology/topic tags per version, shown as chips on the detail page
-- and searchable on the home page. Stored comma-separated; empty/null = no tags.
ALTER TABLE ledger.session_version ADD COLUMN tags TEXT;
