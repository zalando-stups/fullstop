--SET ROLE TO fullstop_app;


ALTER TABLE fullstop_data.lifecycle ADD COLUMN account_id TEXT;

ALTER TABLE fullstop_data.lifecycle ADD COLUMN image_id TEXT, ADD COLUMN image_name TEXT;