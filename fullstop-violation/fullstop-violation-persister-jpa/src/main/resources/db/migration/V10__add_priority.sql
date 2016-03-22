ALTER TABLE fullstop_data.violation_type
ADD COLUMN priority INTEGER DEFAULT 4;

UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'MODIFIED_ROLE_OR_SERVICE';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'WRONG_REGION';
UPDATE fullstop_data.violation_type SET priority = 1 WHERE id = 'WRONG_AMI';
UPDATE fullstop_data.violation_type SET priority = 4 WHERE id = 'MISSING_SOURCE_IN_USER_DATA';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'APPLICATION_VERSION_DOES_NOT_HAVE_A_VALID_ARTIFA CT';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'MISSING_APPLICATION_VERSION_IN_USER_DATA';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'MISSING_APPLICATION_ID_IN_USER_DATA';
UPDATE fullstop_data.violation_type SET priority = 3 WHERE id = 'MISSING_USER_DATA';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'OUTDATED_TAUPAGE';
UPDATE fullstop_data.violation_type SET priority = 4 WHERE id = 'EC2_WITH_KEYPAIR';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'SCM_URL_IS_MISSING_IN_KIO';
UPDATE fullstop_data.violation_type SET priority = 3 WHERE id = 'SPEC_TYPE_IS_MISSING_IN_KIO';
UPDATE fullstop_data.violation_type SET priority = 3 WHERE id = 'SPEC_URL_IS_MISSING_IN_KIO';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'APPLICATION_NOT_PRESENT_IN_KIO';
UPDATE fullstop_data.violation_type SET priority = 1 WHERE id = 'APPLICATION_VERSION_NOT_PRESENT_IN_KIO';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'APPLICATION_VERSION_DOES_NOT_HAVE_A_VALID_ARTIFA CT';
UPDATE fullstop_data.violation_type SET priority = 1 WHERE id = 'MISSING_VERSION_APPROVAL';
UPDATE fullstop_data.violation_type SET priority = 1 WHERE id = 'EC2_WITH_A_SNAPSHOT_IMAGE';
UPDATE fullstop_data.violation_type SET priority = 1 WHERE id = 'IMAGE_IN_PIERONE_NOT_FOUND';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'SCM_URL_NOT_MATCH_WITH_KIO';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'SCM_SOURCE_JSON_MISSING';
UPDATE fullstop_data.violation_type SET priority = 1 WHERE id = 'UNSECURED_PUBLIC_ENDPOINT';
UPDATE fullstop_data.violation_type SET priority = 4 WHERE id = 'EC2_RUN_IN_PUBLIC_SUBNET';

UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'ACTIVE_KEY_TOO_OLD';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'ACTIVE_KEY_TO_OLD';

UPDATE fullstop_data.violation_type SET priority = 1 WHERE id = 'PASSWORD_USED';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'ILLEGAL_SCM_REPOSITORY';
UPDATE fullstop_data.violation_type SET priority = 2 WHERE id = 'MISSING_SPEC_LINKS';


UPDATE fullstop_data.violation_type SET priority = 1 WHERE id = 'UNSECURED_ROOT_USER';