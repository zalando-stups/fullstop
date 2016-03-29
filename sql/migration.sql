--SET ROLE TO fullstop_app;

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

select count(*) from fullstop_data.violation where violation_type_entity_id is null;

select count(*) from fullstop_data.violation where meta_info is not null;

-- migrate messages
UPDATE fullstop_data.violation
SET meta_info = message
WHERE meta_info is null;

ALTER TABLE fullstop_data.violation
DROP COLUMN message;

ALTER TABLE fullstop_data.violation
 ALTER COLUMN violation_type_entity_id SET NOT NULL;


ALTER TABLE fullstop_data.violation
OWNER TO fullstop;
GRANT ALL ON TABLE fullstop_data.violation_type TO fullstop;
GRANT ALL ON TABLE fullstop_data.violation_type TO fullstop_app;
GRANT SELECT ON TABLE fullstop_data.violation_type TO robot_zmon;



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

ALTER TABLE fullstop_data.app_version
OWNER TO fullstop;
GRANT ALL ON TABLE fullstop_data.app_version TO fullstop;
GRANT ALL ON TABLE fullstop_data.app_version TO fullstop_app;
GRANT SELECT ON TABLE fullstop_data.app_version TO robot_zmon;

ALTER TABLE fullstop_data.application
OWNER TO fullstop;
GRANT ALL ON TABLE fullstop_data.application TO fullstop;
GRANT ALL ON TABLE fullstop_data.application TO fullstop_app;
GRANT SELECT ON TABLE fullstop_data.application TO robot_zmon;

ALTER TABLE fullstop_data.application_version_entities
OWNER TO fullstop;
GRANT ALL ON TABLE fullstop_data.application_version_entities TO fullstop;
GRANT ALL ON TABLE fullstop_data.application_version_entities TO fullstop_app;
GRANT SELECT ON TABLE fullstop_data.application_version_entities TO robot_zmon;

ALTER TABLE fullstop_data.lifecycle
OWNER TO fullstop;
GRANT ALL ON TABLE fullstop_data.lifecycle TO fullstop;
GRANT ALL ON TABLE fullstop_data.lifecycle TO fullstop_app;
GRANT SELECT ON TABLE fullstop_data.lifecycle TO robot_zmon;

ALTER TABLE fullstop_data.lifecycle_id_seq
OWNER TO fullstop;
GRANT ALL ON SEQUENCE fullstop_data.lifecycle_id_seq TO fullstop;
GRANT SELECT, USAGE ON SEQUENCE fullstop_data.lifecycle_id_seq TO fullstop_app;


ALTER TABLE fullstop_data.application_id_seq
OWNER TO fullstop;
GRANT ALL ON SEQUENCE fullstop_data.application_id_seq TO fullstop;
GRANT SELECT, USAGE ON SEQUENCE fullstop_data.application_id_seq TO fullstop_app;

ALTER TABLE fullstop_data.app_version_id_seq
OWNER TO fullstop;
GRANT ALL ON SEQUENCE fullstop_data.app_version_id_seq TO fullstop;
GRANT SELECT, USAGE ON SEQUENCE fullstop_data.app_version_id_seq TO fullstop_app;