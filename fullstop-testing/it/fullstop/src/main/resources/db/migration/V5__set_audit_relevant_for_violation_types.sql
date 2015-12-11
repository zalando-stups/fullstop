UPDATE fullstop_data.violation_type SET is_audit_relevant = false WHERE is_audit_relevant IS NULL;
ALTER TABLE fullstop_data.violation_type
  ALTER COLUMN is_audit_relevant SET NOT NULL,
  ALTER COLUMN is_audit_relevant SET DEFAULT false;