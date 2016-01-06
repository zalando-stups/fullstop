CREATE TABLE IF NOT EXISTS fullstop_data.rule_entity (
  rule_name                         TEXT NOT NULL PRIMARY KEY,
  account_id                        TEXT,
  violation_type_entity_id          TEXT      NOT NULL,
  created                           TIMESTAMP,
  created_by                        TEXT,
  last_modified                     TIMESTAMP,
  last_modified_by                  TEXT,
  version                           BIGINT    NOT NULL,
  FOREIGN KEY (violation_type_entity_id) REFERENCES fullstop_data.violation_type (id)
);