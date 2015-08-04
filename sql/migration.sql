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
ADD COLUMN instance_id TEXT,
ADD COLUMN violation_type_entity_id TEXT,
ADD FOREIGN KEY (violation_type_entity_id) REFERENCES fullstop_data.violation_type (id);

ALTER TABLE fullstop_data.violation RENAME COLUMN violation_object TO meta_info;


CREATE UNIQUE INDEX unique_violation_instance_null ON fullstop_data.violation (account_id, region, event_id, violation_type_entity_id)
  WHERE instance_id IS NULL;

CREATE UNIQUE INDEX unique_violation ON fullstop_data.violation (account_id, region, event_id, violation_type_entity_id, instance_id)
  WHERE instance_id IS NOT NULL;

-- insert violation types 'violation-types.sql'

-- update violations 'update-violations.sql'

-- migrate messages
UPDATE fullstop_data.violation
SET meta_info = message
WHERE meta_info is null;

ALTER TABLE fullstop_data.violation
DROP COLUMN message;

ALTER TABLE fullstop_data.violation
 violation_type_entity_id NOT NULL;
 
 
