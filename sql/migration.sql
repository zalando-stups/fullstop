--SET ROLE TO fullstop_app;

CREATE TABLE IF NOT EXISTS fullstop_data.violation_type (
  id                 TEXT   NOT NULL PRIMARY KEY,
  help_text          TEXT,
  violation_severity TEXT,
  is_audit_relevant  BOOLEAN,
  created            TIMESTAMP,
  created_by         TEXT,
  last_modified      TIMESTAMP,
  last_modified_by   TEXT,
  version            BIGINT NOT NULL
);


ALTER TABLE fullstop_data.violation DROP CONSTRAINT IF EXISTS unique_violation;

ALTER TABLE fullstop_data.violation
ADD COLUMN plugin_fully_qualified_class_name TEXT,
ADD COLUMN violation_type_entity_id TEXT, -- NOT NULL,
DROP COLUMN message, -- should we wait for that?
ADD CONSTRAINT unique_violation UNIQUE (account_id, region, event_id, violation_type_entity_id),
ADD FOREIGN KEY (violation_type_entity_id) REFERENCES fullstop_data.violation_type (id);

ALTER TABLE fullstop_data.violation RENAME COLUMN violation_object TO meta_info;
