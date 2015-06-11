CREATE SCHEMA IF NOT EXISTS fullstop_data;

CREATE TABLE IF NOT EXISTS fullstop_data.violation (
  id               BIGSERIAL  NOT NULL PRIMARY KEY,
  event_id         TEXT,
  account_id       TEXT,
  region           TEXT,
  message          TEXT    NOT NULL,
  violation_object TEXT,
  comment          TEXT,
  created          TIMESTAMP,
  created_by       TEXT,
  last_modified    TIMESTAMP,
  last_modified_by TEXT,
  version          BIGINT NOT NULL,

  -- TODO: we should think about that
  CONSTRAINT unique_violation UNIQUE (account_id, region, event_id, message)
);
