CREATE SCHEMA IF NOT EXISTS fullstop_data;

CREATE TABLE IF NOT EXISTS fullstop_data.violation (
  id               SERIAL PRIMARY KEY NOT NULL,
  account_id       CHARACTER VARYING(255),
  checked          BOOLEAN,
  comment          CHARACTER VARYING(255),
  event_id         CHARACTER VARYING(255),
  message          CHARACTER VARYING(255),
  region           CHARACTER VARYING(255),
  violation_object CHARACTER VARYING(255),
  created          TIMESTAMP,
  created_by       VARCHAR(255),
  last_modified    TIMESTAMP,
  last_modified_by VARCHAR(255),
  version          INTEGER            NOT NULL
);
