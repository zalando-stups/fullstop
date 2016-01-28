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
VALUES ('OUTDATED_AMI',
        'The AMI you are currently use is older than 60 days. You must update it. See our policy.',
        1,
        'AMI used is older than 60 days.',
        '#335',
        now(),
        now(),
        '#335',
        0);

