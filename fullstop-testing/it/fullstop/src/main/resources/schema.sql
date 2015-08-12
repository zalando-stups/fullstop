CREATE SCHEMA IF NOT EXISTS fullstop_data;

CREATE TABLE IF NOT EXISTS fullstop_data.violation_type (
  id                 TEXT   NOT NULL PRIMARY KEY,
  help_text          TEXT,
  violation_severity INTEGER,
  is_audit_relevant  BOOLEAN,
  created            TIMESTAMP,
  created_by         TEXT,
  last_modified      TIMESTAMP,
  last_modified_by   TEXT,
  version            BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS fullstop_data.violation (
  id                                BIGSERIAL NOT NULL PRIMARY KEY,
  event_id                          TEXT,
  account_id                        TEXT,
  region                            TEXT,
  instance_id                       TEXT,
  meta_info                         TEXT,
  comment                           TEXT,
  plugin_fully_qualified_class_name TEXT,
  violation_type_entity_id          TEXT      NOT NULL,
  created                           TIMESTAMP,
  created_by                        TEXT,
  last_modified                     TIMESTAMP,
  last_modified_by                  TEXT,
  version                           BIGINT    NOT NULL,
  FOREIGN KEY (violation_type_entity_id) REFERENCES fullstop_data.violation_type (id)
);

CREATE UNIQUE INDEX unique_violation_instance_null ON fullstop_data.violation (account_id, region, event_id, violation_type_entity_id)
  WHERE instance_id IS NULL;

CREATE UNIQUE INDEX unique_violation ON fullstop_data.violation (account_id, region, event_id, violation_type_entity_id, instance_id)
  WHERE instance_id IS NOT NULL;

--ALTER DEFAULT PRIVILEGES IN SCHEMA fullstop_data GRANT SELECT ON SEQUENCES TO xxx;

CREATE TABLE IF NOT EXISTS fullstop_data.application (
  id               BIGSERIAL NOT NULL PRIMARY KEY,
  name             TEXT,
  created          TIMESTAMP,
  created_by       TEXT,
  last_modified    TIMESTAMP,
  last_modified_by TEXT,
  version          BIGINT    NOT NULL,
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


CREATE TABLE IF NOT EXISTS fullstop_data.application_version_entities (
  application_entities_id INTEGER,
  version_entities_id     INTEGER,
  FOREIGN KEY (application_entities_id) REFERENCES fullstop_data.application (id),
  FOREIGN KEY (version_entities_id) REFERENCES fullstop_data.app_version (id),
  PRIMARY KEY (application_entities_id, version_entities_id)
);

CREATE TABLE IF NOT EXISTS fullstop_data.lifecycle (
  id                  BIGSERIAL NOT NULL PRIMARY KEY,
  event_date          DATE,
  account_id          TEXT,
  image_id            TEXT,
  image_name          TEXT,
  region              TEXT,
  application         INTEGER,
  application_version INTEGER,
  userdata_path       TEXT,
  instance_boot_time  TIMESTAMP,
  event_type          TEXT,
  instance_id         TEXT,
  created             TIMESTAMP,
  created_by          TEXT,
  last_modified       TIMESTAMP,
  last_modified_by    TEXT,
  version             BIGINT    NOT NULL,
  FOREIGN KEY (application) REFERENCES fullstop_data.application (id),
  FOREIGN KEY (application_version) REFERENCES fullstop_data.app_version (id)
);
