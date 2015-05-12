CREATE SCHEMA IF NOT EXISTS fullstop_data;

CREATE TABLE IF NOT EXISTS fullstop_data.violation (
  id               SERIAL  NOT NULL PRIMARY KEY,
  account_id       TEXT,
  checked          BOOLEAN,
  comment          TEXT,
  event_id         TEXT,
  message          TEXT    NOT NULL,
  region           TEXT,
  violation_object TEXT,
  created          TIMESTAMP,
  created_by       TEXT,
  last_modified    TIMESTAMP,
  last_modified_by TEXT,
  version          INTEGER NOT NULL,

  -- TODO: we should think about that
  CONSTRAINT unique_violation UNIQUE (account_id, region, event_id, message)
);
