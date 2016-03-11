UPDATE fullstop_data.violation_type
SET
  help_text = 'The deployment artifact (Docker image) for this application version is missing a valid scm-source.json. See http://docs.stups.io/en/latest/user-guide/application-development.html?highlight=scm-source.json#docker for more information.'
WHERE
  violation_type.id = 'SCM_SOURCE_JSON_MISSING';