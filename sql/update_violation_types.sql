CREATE OR REPLACE FUNCTION fullstop_data.create_or_update_violation_type(id TEXT, message TEXT)
  RETURNS VOID
AS $$

WITH row_update AS (

  UPDATE fullstop_data.violation_type
  SET help_text = $2
  WHERE id = $1
  RETURNING *)

INSERT INTO fullstop_data.violation_type (id, help_text)
  SELECT $1, $2
  WHERE NOT EXISTS(SELECT * FROM row_update)

$$ LANGUAGE SQL;


SELECT fullstop_data.create_or_update_violation_type('ACTIVE_KEY_TOO_OLD',
                                                     'This active IAM access key is too old and should be refreshed. IAM access keys must be rotated regularly.');

SELECT fullstop_data.create_or_update_violation_type('APPLICATION_NOT_PRESENT_IN_KIO',
                                                     'This active IAM access key is too old and should be refreshed. IAM access keys must be rotated regularly.');

SELECT fullstop_data.create_or_update_violation_type('APPLICATION_VERSION_DOES_NOT_HAVE_A_VALID_ARTIFACT',
                                                     'The deployment artifact (docker image) defined in application version (Kio application registry) is not the same as the one defined in the EC2 instance user data');

SELECT fullstop_data.create_or_update_violation_type('APPLICATION_VERSION_NOT_PRESENT_IN_KIO',
                                                     'We could not find this application version in Kio application registry. Please make sure that it exists and has the required approvals.');

SELECT fullstop_data.create_or_update_violation_type('EC2_WITH_A_SNAPSHAPOT_IMAGE',
                                                     'EC2 instances must be deployed with immutable deployment artifacts (Docker images) only.');

SELECT fullstop_data.create_or_update_violation_type('EC2_WITH_KEYPAIR',
                                                     'EC2 instances should not have a SSH key associated. Please use even, odd and piu for SSH access.');

SELECT fullstop_data.create_or_update_violation_type('IMAGE_IN_PIERONE_NOT_FOUND',
                                                     'Deployment artifact (Docker image) not found in any of the trusted Pier One Docker Registries.');

SELECT fullstop_data.create_or_update_violation_type('MISSING_SOURCE_IN_USER_DATA',
                                                     'We could not find the ‘source’ property in this EC2s user data. Please make sure to add the tag of your deployment artifact (Docker image) there.');

SELECT fullstop_data.create_or_update_violation_type('MISSING_USER_DATA',
                                                     'EC2 instance was started without any userdata.');

SELECT fullstop_data.create_or_update_violation_type('MISSING_VERSION_APPROVAL',
                                                     'The application version was not approved in application registry.');

SELECT fullstop_data.create_or_update_violation_type('MODIFIED_ROLE_OR_SERVICE',
                                                     'The IAM policies must not be changed.');

SELECT fullstop_data.create_or_update_violation_type('PASSWORD_USED',
                                                     'Passwords for IAM users must not be used.');

SELECT fullstop_data.create_or_update_violation_type('SCM_SOURCE_JSON_MISSING_FOR_IMAGE',
                                                     '"The deployment artifact (Docker image) for this application version is missing the scm-source.json. See http://docs.stups.io/en/latest/user-guide/application-development.html?highlight=scm-source.json#docker for more information."');

SELECT fullstop_data.create_or_update_violation_type('SCM_URL_NOT_MATCH_WITH_KIO',
                                                     'The repository URLs in scm-source.json and Kio do not match. Artifacts must be built from the application’s repository as configured in Kio application registry.');

SELECT fullstop_data.create_or_update_violation_type('UNSECURED_PUBLIC_ENDPOINT',
                                                     'Unsecured public endpoint found. The public endpoint must serve HTTPS. The endpoint is neither OAuth protected (401 status on /) nor flagged as “publicly accessible” in Kio application registry.');

SELECT fullstop_data.create_or_update_violation_type('WRONG_AMI',
                                                     'EC2 instance must not run an AMI other than Taupage or an approved one.');

SELECT fullstop_data.create_or_update_violation_type('WRONG_REGION',
                                                     'EC2 instance must run in allowed regions only.');

SELECT fullstop_data.create_or_update_violation_type('WRONG_USER_DATA',
                                                     'Cannot parse EC2 instance userdata. expected Taupage configuration YAML. See http://docs.stups.io/en/latest/components/taupage.html for details');
