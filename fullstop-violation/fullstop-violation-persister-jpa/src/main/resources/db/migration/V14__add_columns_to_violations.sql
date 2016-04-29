ALTER TABLE fullstop_data.violation
ADD COLUMN application_id BIGINT,
ADD COLUMN application_version_id BIGINT;

ALTER TABLE fullstop_data.application_version_entities
ALTER COLUMN application_entities_id TYPE BIGINT,
ALTER COLUMN version_entities_id TYPE BIGINT;

ALTER TABLE fullstop_data.lifecycle
ALTER COLUMN application TYPE BIGINT,
ALTER COLUMN application_version TYPE BIGINT;