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
        'The code that has been used in a Lambda function, is not stored in one of the trusted S3 buckets.',
        1,
        'Untrusted AWS-Lambda function',
        'pull/512',
        now(),
        now(),
        'pull/512',
        1);

