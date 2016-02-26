INSERT INTO fullstop_data.violation_type (
  id,
  help_text,
  violation_severity,
  title,
  created,
  created_by,
  version)
VALUES ('UNSECURED_ROOT_USER',
        'The root user have an active secret key or MFA is not active',
        4,
        'Unsecured root user',
        now(),
        '#362',
        0);

