CREATE SCHEMA IF NOT EXISTS fullstop_violations;

CREATE TABLE IF NOT EXISTS fullstop (
  id SERIAL PRIMARY KEY NOT NULL,
  account_id character varying(255),
  checked boolean,
  comment character varying(255),
  event_id character varying(255),
  message character varying(255),
  region character varying(255),
  violation_object character varying(255)
);