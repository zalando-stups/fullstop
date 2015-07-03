CREATE SCHEMA IF NOT EXISTS fullstop_data;

CREATE TABLE IF NOT EXISTS fullstop_data.violation (
  id               BIGSERIAL NOT NULL PRIMARY KEY,
  event_id         TEXT,
  account_id       TEXT,
  region           TEXT,
  message          TEXT      NOT NULL,
  violation_object TEXT,
  comment          TEXT,
  created          TIMESTAMP,
  created_by       TEXT,
  last_modified    TIMESTAMP,
  last_modified_by TEXT,
  version          BIGINT    NOT NULL,

  -- TODO: we should think about that
  CONSTRAINT unique_violation UNIQUE (account_id, region, event_id, message)
);

--ALTER DEFAULT PRIVILEGES IN SCHEMA fullstop_data GRANT SELECT ON SEQUENCES TO xxx;
CREATE TABLE IF NOT EXISTS fullstop_data.application (
  id               BIGSERIAL NOT NULL PRIMARY KEY,
  name             TEXT,
  created          TIMESTAMP,
  created_by       TEXT,
  last_modified    TIMESTAMP,
  last_modified_by TEXT,
  version          BIGINT NOT NULL,
  CONSTRAINT unique_app_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS fullstop_data.app_version (
  id               BIGSERIAL NOT NULL PRIMARY KEY,
  name             TEXT,
  created          TIMESTAMP,
  created_by       TEXT,
  last_modified    TIMESTAMP,
  last_modified_by TEXT,
  version          BIGINT    NOT NULL,
  CONSTRAINT unique_version_name UNIQUE (name)
);

CREATE TABLE IF NOT EXISTS fullstop_data.app_has_version (
  id               BIGSERIAL NOT NULL PRIMARY KEY,
  app_id           INTEGER,
  app_version_id   INTEGER,
  FOREIGN KEY (app_id) REFERENCES fullstop_data.application (id),
  FOREIGN KEY (app_version_id) REFERENCES fullstop_data.app_version (id)
);

CREATE TABLE IF NOT EXISTS fullstop_data.lifecycle (
  id                 BIGSERIAL NOT NULL PRIMARY KEY,
  event_date         DATE,
  region             TEXT,
  application        INTEGER,
  application_version INTEGER,
  event_type         TEXT,
  instance_id        TEXT,
  created            TIMESTAMP,
  created_by         TEXT,
  last_modified      TIMESTAMP,
  last_modified_by   TEXT,
  version            BIGINT    NOT NULL,
  FOREIGN KEY (application) REFERENCES fullstop_data.application (id),
  FOREIGN KEY (application_version) REFERENCES fullstop_data.app_version (id)
);