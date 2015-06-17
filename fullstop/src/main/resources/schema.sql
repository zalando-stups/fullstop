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

CREATE TABLE IF NOT EXISTS fullstop_data.application (
  id       SERIAL NOT NULL PRIMARY KEY,
  app_name TEXT
);

CREATE TABLE IF NOT EXISTS fullstop_data.app_version (
  id      SERIAL NOT NULL PRIMARY KEY,
  version TEXT
);

CREATE TABLE IF NOT EXISTS fullstop_data.app_has_version(
  id SERIAL NOT NULL PRIMARY KEY,
  app_id INTEGER,
  version_id INTEGER,
  FOREIGN KEY (app_id) REFERENCES fullstop_data.application(id),
  FOREIGN KEY (version_id) REFERENCES fullstop_data.app_version(id)
);

CREATE TABLE IF NOT EXISTS fullstop_data.lifecycle (
  id         SERIAL NOT NULL PRIMARY KEY,
  startdate  DATE,
  enddate    DATE,
  region     TEXT,
  app_version_id     INTEGER,
  event_type TEXT,
  FOREIGN KEY (app_version_id) REFERENCES fullstop_data.app_has_version(id)
);