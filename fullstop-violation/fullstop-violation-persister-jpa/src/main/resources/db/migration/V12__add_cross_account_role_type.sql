INSERT INTO fullstop_data.violation_type (
  id,
  help_text,
  violation_severity,
  priority,
  title,
  created,
  created_by,
  version)
VALUES ('CROSS_ACCOUNT_ROLE',
        'Your account has an IAM policy that allows access from another account. Cross account access is should be avoided',
        0,
        0,
        '',
        now(),
        '#372',
        0);

