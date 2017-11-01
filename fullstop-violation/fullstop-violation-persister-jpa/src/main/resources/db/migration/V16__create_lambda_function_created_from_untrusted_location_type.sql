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
VALUES ('LAMBDA_FUNCTION_CREATED_FROM_UNTRUSTED_LOCATION',
        'The Lambda function you created/updated is not stored in our trusted S3 bucket.',
        1,
        'Lambda function created/updated from an untrusted location',
        'pull/512',
        now(),
        now(),
        'pull/512',
        1);

