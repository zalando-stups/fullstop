INSERT INTO fullstop_data.violation_type (
  id,
  help_text,
  violation_severity,
  title,
  last_modified_by,
  last_modified,
  created,
  created_by,
  version)
VALUES ('OUTDATED_TAUPAGE',
        'The AMI you are currently using is expired and no longer supported. Please update your EC2 instances to use a newer AMI',
        2,
        'AMI is expired',
        '#335',
        now(),
        now(),
        '#335',
        1);

