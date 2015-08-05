UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('EC2_WITH_KEYPAIR')
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%KeyPair must be blank, but was %';

UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('WRONG_REGION')
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%are running in the wrong region!%';

UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('MODIFIED_ROLE_OR_SERVICE')
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%Role:%must not be modified%';

UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('WRONG_AMI')
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%was started with wrong images:%';


UPDATE fullstop_data.violation
SET instance_id = substr(message, 13, 10) --"InstanceId: i-123456789 doesn't have any userData."
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%InstanceId%doesn''t have any userData.%';

UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('MISSING_USER_DATA')
WHERE
  violation_type_entity_id IS NULL
  AND (
    message LIKE '%InstanceId%doesn''t have any userData.%'
    OR
    message LIKE '%Instance%does not have any userData%'
  );

UPDATE fullstop_data.violation
SET instance_id = substr(message, 13, 10) --"InstanceID: i-12345678 was started with a mutable SNAPSHOT image."
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%was started with a mutable SNAPSHOT image.%';

UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('EC2_WITH_A_SNAPSHOT_IMAGE')
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%was started with a mutable SNAPSHOT image.%';

UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('VERSION_APPROVAL_NOT_ENOUGH')
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%Version%was approved by only%';

UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('MISSING_VERSION_APPROVAL')
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%Version%is missing approvals%';

UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('IMAGE_IN_PIERONE_NOT_FOUND')
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%Image%not found in pierone.%';

UPDATE fullstop_data.violation
SET
  ( violation_type_entity_id) = ('APPLICATION_VERSION_DOES_NOT_HAVE_A_VALID_ARTIFACT')
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%Application:%has not a valid artifact for version%';

--
-- -- parse message to get instance id
-- UPDATE fullstop_data.violation
-- SET instance_id = substr(message,22,10) --"userData of instance i-123468 is missing application_id."
-- WHERE
--   violation_type_entity_id IS NULL
--   AND message LIKE '%userData%is missing application_id.%';
--
--
-- UPDATE fullstop_data.violation
-- SET instance_id = substr(message,47,10) --"No 'application_id' defined for this instance i-12345678, please change the userData configuration for this instance and add this information."
-- WHERE
--   violation_type_entity_id IS NULL
--   AND message LIKE
--     '%No ''application_id'' defined%please change the userData configuration for this instance and add this information.%';
--
--
-- UPDATE fullstop_data.violation
-- SET instance_id = substr(message,39,10) --"No 'source' defined for this instance i-12345767, please change the userData configuration for this instance and add this information."
-- WHERE
--   violation_type_entity_id IS NULL
--   AND message LIKE
--     '%No ''source'' defined%please change the userData configuration for this instance and add this information.%';
--
-- UPDATE fullstop_data.violation
-- SET instance_id = substr(message,13,10) --"InstanceID: i-12335678 is missing 'source' property in userData."
-- WHERE
--   violation_type_entity_id IS NULL
--   AND message LIKE '%InstanceID:%is missing ''source'' property in userData.%';
--
-- UPDATE fullstop_data.violation
-- SET instance_id = substr(message,52,10) --"No 'application_version' defined for this instance i-1233567, please change the userData configuration for this instance and add this information."
-- WHERE
--   violation_type_entity_id IS NULL
--   AND  message LIKE
--     '%No ''application_version'' defined%please change the userData configuration for this instance and add this information.%';
--
-- -- finish

-- update instance id with message

UPDATE fullstop_data.violation
SET instance_id = message
WHERE
  violation_type_entity_id IS NULL
  AND
  (
    message LIKE
    '%No ''application_id'' defined%please change the userData configuration for this instance and add this information.%'
    OR
    message LIKE
    '%No ''source'' defined%please change the userData configuration for this instance and add this information.%'
    OR
    message LIKE
    '%No ''application_version'' defined%please change the userData configuration for this instance and add this information.%'
    OR
    message LIKE '%InstanceID:%is missing ''source'' property in userData.%'
    OR
    message LIKE '%userData%is missing application_id.%'
  );
-- finish

UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('WRONG_USER_DATA')
WHERE
  violation_type_entity_id IS NULL
  AND
  (
    message LIKE
    '%No ''application_id'' defined%please change the userData configuration for this instance and add this information.%'
    OR
    message LIKE
    '%No ''source'' defined%please change the userData configuration for this instance and add this information.%'
    OR
    message LIKE
    '%No ''application_version'' defined%please change the userData configuration for this instance and add this information.%'
    OR
    message LIKE '%InstanceID:%is missing ''source'' property in userData.%'
    OR
    message LIKE '%userData%is missing application_id.%'
  );

UPDATE fullstop_data.violation
SET ( violation_type_entity_id) = ('WRONG_APPLICATION_MASTERDATA')
WHERE
  violation_type_entity_id IS NULL
  AND message LIKE '%Masterdata of%has errors%';


DELETE FROM fullstop_data.violation
WHERE
  violation_type_entity_id IS NULL
  AND
  (
    message LIKE '%AccessDenied%'
    OR
    message LIKE '%Request limit exceeded.%'
    OR
    message LIKE '%have no routing information associated'
  );



-- UPDATE fullstop_data.violation
-- SET ( violation_type_entity_id) = ('EC2_RUN_IN_PUBLIC_SUBNET')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
--
-- UPDATE fullstop_data.violation
-- SET ( violation_type_entity_id) = ('ACTIVE_KEY_TO_OLD')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET ( violation_type_entity_id) = ('PASSWORD_USED')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET
--   ( violation_type_entity_id) = ('SECURITY_GROUPS_PORT_NOT_ALLOWED')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET ( violation_type_entity_id) = ('MISSING_SOURCE_IN_USER_DATA')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
--
-- UPDATE fullstop_data.violation
-- SET ( violation_type_entity_id) = ('SCM_URL_IS_MISSING_IN_KIO')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET
--   ( violation_type_entity_id) = ('SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET ( violation_type_entity_id) = ('SCM_URL_NOT_MATCH_WITH_KIO')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
--
-- UPDATE fullstop_data.violation
-- SET
--   ( violation_type_entity_id) = ('SCM_SOURCE_JSON_MISSING_FOR_IMAGE')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
--
-- UPDATE fullstop_data.violation
-- SET ( violation_type_entity_id) = ('SOURCE_NOT_PRESENT_IN_PIERONE')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET ( violation_type_entity_id) = ('APPLICATION_NOT_PRESENT_IN_KIO')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET
--   ( violation_type_entity_id) = ('APPLICATION_VERSION_NOT_PRESENT_IN_KIO')
-- WHERE
--   violation_type_entity_id IS NULL
-- --and message like '%%';