ALTER TABLE fullstop_data.violation_type
ADD COLUMN title VARCHAR(32);

CREATE OR REPLACE FUNCTION fullstop_data.create_or_update_violation_type(id    TEXT, message TEXT, severity INTEGER,
                                                                         title VARCHAR(32))
  RETURNS VOID
AS $$

WITH row_update AS (

  UPDATE fullstop_data.violation_type
  SET help_text        = $2,
    violation_severity = $3,
    title              = $4,
    last_modified      = now(),
    last_modified_by   = 'migration_script',
    version            = 1
  WHERE id = $1
  RETURNING *)

INSERT INTO fullstop_data.violation_type (id, help_text, violation_severity, title, last_modified_by, last_modified, created, created_by, version)
  SELECT
    $1,
    $2,
    $3,
    $4,
    'migration_script',
    now(),
    now(),
    'migration_script',
    1
  WHERE NOT EXISTS(SELECT *
                   FROM row_update)

$$ LANGUAGE SQL;


SELECT fullstop_data.create_or_update_violation_type('ACTIVE_KEY_TOO_OLD',
                                                     'This active IAM access key is too old and should be refreshed. IAM access keys must be rotated regularly.',
                                                     1,
                                                     'IAM Access Key too old');

SELECT fullstop_data.create_or_update_violation_type('APPLICATION_NOT_PRESENT_IN_KIO',
                                                     'An application with this ID could not be found in Kio application registry. Please make sure that every application is registered there, i.e. with YOUR TURN.',
                                                     1,
                                                     'Unknown application ID');

SELECT fullstop_data.create_or_update_violation_type('APPLICATION_VERSION_DOES_NOT_HAVE_A_VALID_ARTIFACT',
                                                     'The deployment artifact (docker image) defined in application version (Kio application registry) is not the same as the one defined in the EC2 instance user data',
                                                     1, 'Deployment artifact mismatch');

SELECT fullstop_data.create_or_update_violation_type('APPLICATION_VERSION_NOT_PRESENT_IN_KIO',
                                                     'This application version could not be found in Kio application registry. Please make sure that it exists and has the required approvals.',
                                                     2, 'Unknown application version');

SELECT fullstop_data.create_or_update_violation_type('EC2_RUN_IN_PUBLIC_SUBNET',
                                                     'EC2 instances should not run in a public subnet, it is recommended to create an ELB instead.',
                                                     0, 'Public EC2 instance');

SELECT fullstop_data.create_or_update_violation_type('EC2_WITH_A_SNAPSHOT_IMAGE',
                                                     'EC2 instances must be deployed with immutable deployment artifacts (Docker images) only.',
                                                     2, 'Snapshot deployment artifact');

SELECT fullstop_data.create_or_update_violation_type('EC2_WITH_KEYPAIR',
                                                     'EC2 instances should not have a SSH key associated. Please use even, odd and piu for SSH access.',
                                                     0, 'EC2 SSH Keypair used');

SELECT fullstop_data.create_or_update_violation_type('IMAGE_IN_PIERONE_NOT_FOUND',
                                                     'Deployment artifact (Docker image) not found in any of the trusted Pier One Docker Registries.',
                                                     2,
                                                     'Unknown deployment artifact');

SELECT fullstop_data.create_or_update_violation_type('MISSING_SOURCE_IN_USER_DATA',
                                                     'The "source" property in this EC2s user data (TaupageConfig in Senza YAML) could not be found. Please make sure to add the tag of your deployment artifact (Docker image) there.',
                                                     0,
                                                     'Incomplete EC2 user data');


SELECT fullstop_data.create_or_update_violation_type('MISSING_USER_DATA',
                                                     'Taupage EC2 instance was started without any userdata.',
                                                     0,
                                                     'EC2 without user data');

SELECT fullstop_data.create_or_update_violation_type('MISSING_VERSION_APPROVAL',
                                                     'The application version was not approved in application registry.',
                                                     1,
                                                     'Unapproved application version');

SELECT fullstop_data.create_or_update_violation_type('MODIFIED_ROLE_OR_SERVICE',
                                                     'The IAM policies must not be changed.',
                                                     1,
                                                     'Possible privilege escalation');

SELECT fullstop_data.create_or_update_violation_type('PASSWORD_USED',
                                                     'Passwords for IAM users must not be used.',
                                                     1,
                                                     'IAM User has password set');

SELECT fullstop_data.create_or_update_violation_type('SCM_SOURCE_JSON_MISSING',
                                                     'The deployment artifact (Docker image) for this application version is missing the scm-source.json. See http://docs.stups.io/en/latest/user-guide/application-development.html?highlight=scm-source.json#docker for more information.',
                                                     1,
                                                     'Missing scm-source.json');

SELECT fullstop_data.create_or_update_violation_type('SCM_URL_IS_MISSING_IN_KIO',
                                                     'SCM url is missing in KIO application registry.',
                                                     0,
                                                     'SCM URL missing in KIO');

SELECT fullstop_data.create_or_update_violation_type('SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON',
                                                     'SCM url is invalid in scm-source.json. See http://docs.stups.io/en/latest/user-guide/application-development.html?highlight=scm-source.json#docker for more information about this topic.',
                                                     1,
                                                     'Incomplete scm-source.json');

SELECT fullstop_data.create_or_update_violation_type('SCM_URL_NOT_MATCH_WITH_KIO',
                                                     'The repository URLs in scm-source.json and Kio do not match. Artifacts must be built from the application’s repository as configured in Kio application registry.',
                                                     0,
                                                     'Built from illegal repository');

SELECT fullstop_data.create_or_update_violation_type('UNSECURED_PUBLIC_ENDPOINT',
                                                     'Unsecured public endpoint found. The public endpoint must serve HTTPS. The endpoint is neither OAuth protected (401 status on /) nor flagged as “publicly accessible” in Kio application registry.',
                                                     4,
                                                     'Unsecured public endpoint');

SELECT fullstop_data.create_or_update_violation_type('WRONG_AMI',
                                                     'EC2 instance must not run an AMI other than Taupage or an approved one.',
                                                     3,
                                                     'EC2 instance using illegal AMI');

SELECT fullstop_data.create_or_update_violation_type('SPEC_URL_IS_MISSING_IN_KIO',
                                                     'Specification URL is missing in Kio application registry.',
                                                     0,
                                                     'Specification URL missing');

SELECT fullstop_data.create_or_update_violation_type('SPEC_TYPE_IS_MISSING_IN_KIO',
                                                     'Specification type is missing in Kio application registry.',
                                                     0,
                                                     'Specification type missing');

SELECT fullstop_data.create_or_update_violation_type('WRONG_REGION',
                                                     'EC2 instance must run in allowed regions only.',
                                                     1,
                                                     'EC2 instance in illegal region');

SELECT fullstop_data.create_or_update_violation_type('MISSING_APPLICATION_ID_IN_USER_DATA',
                                                     'The "application_id" property in this EC2s user data (TaupageConfig in Senza YAML) could not be found. Please make sure to add the tag of your deployment artifact (Docker image) there.',
                                                     1,
                                                     'Missing application id');

SELECT fullstop_data.create_or_update_violation_type('MISSING_APPLICATION_VERSION_IN_USER_DATA',
                                                     'The "application_version" property in this EC2s user data (TaupageConfig in Senza YAML) could not be found. Please make sure that the version matches the one registered in Kio application registry.',
                                                     1,
                                                     'Missing application version');

