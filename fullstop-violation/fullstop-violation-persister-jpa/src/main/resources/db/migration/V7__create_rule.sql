CREATE TABLE IF NOT EXISTS fullstop_data.rule (
  id                       TEXT   NOT NULL PRIMARY KEY,
  account_id               TEXT,
  region                   TEXT,
  application_id           TEXT,
  application_version      TEXT,
  image_name               TEXT,
  image_owner              TEXT,
  reason                   TEXT,
  expiry_date              TIMESTAMP,
  violation_type_entity_id TEXT   NOT NULL,
  created                  TIMESTAMP,
  created_by               TEXT,
  last_modified            TIMESTAMP,
  last_modified_by         TEXT,
  version                  BIGINT NOT NULL,
  FOREIGN KEY (violation_type_entity_id) REFERENCES fullstop_data.violation_type (id)
);

ALTER TABLE fullstop_data.violation
ADD COLUMN rule_entity_id TEXT,
ADD FOREIGN KEY (rule_entity_id) REFERENCES fullstop_data.rule (id);
