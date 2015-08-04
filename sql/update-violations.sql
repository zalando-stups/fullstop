BEGIN;

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'EC2_WITH_KEYPAIR')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%KeyPair must be blank, but was %';


UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'LEGACY')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%have no routing information associated';

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'WRONG_REGION')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%are running in the wrong region!%';

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'MODIFIED_ROLE_OR_SERVICE')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%Role:%must not be modified%';

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'WRONG_AMI')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%was started with wrong images:%';

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'MISSING_USER_DATA')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%InstanceId%doesn''t have any userData.%';

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'EC2_WITH_A_SNAPSHOT_IMAGE')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%was started with a mutable SNAPSHOT image.%';

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'VERSION_APPROVAL_NOT_ENOUGH')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%Version%was approved by only%';

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'MISSING_VERSION_APPROVAL')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%Version%is missing approvals%';

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'IMAGE_IN_PIERONE_NOT_FOUND')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%Image%not found in pierone.%';

UPDATE fullstop_data.violation
SET
  (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'APPLICATION_VERSION_DOES_NOT_HAVE_A_VALID_ARTIFACT')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%Application:%has not a valid artifact for version%';

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'WRONG_USER_DATA')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
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
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'WRONG_APPLICATION_MASTERDATA')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND message LIKE '%Masterdata of%has errors%';

UPDATE fullstop_data.violation
SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'LEGACY')
WHERE
  violation_type_entity_id IS NULL
  AND plugin_fully_qualified_class_name IS NULL
  AND
  (
    message LIKE '%AccessDenied%'
    OR
    message LIKE '%Request limit exceeded.%'
  );

--ROLLBACK;
--COMMIT;



-- UPDATE fullstop_data.violation
-- SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'EC2_RUN_IN_PUBLIC_SUBNET')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
--
-- UPDATE fullstop_data.violation
-- SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'ACTIVE_KEY_TO_OLD')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'PASSWORD_USED')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET
--   (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'SECURITY_GROUPS_PORT_NOT_ALLOWED')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'MISSING_SOURCE_IN_USER_DATA')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
--
-- UPDATE fullstop_data.violation
-- SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'SCM_URL_IS_MISSING_IN_KIO')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET
--   (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'SCM_URL_IS_MISSING_IN_SCM_SOURCE_JSON')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'SCM_URL_NOT_MATCH_WITH_KIO')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
--
-- UPDATE fullstop_data.violation
-- SET
--   (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'SCM_SOURCE_JSON_MISSING_FOR_IMAGE')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
--
-- UPDATE fullstop_data.violation
-- SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'SOURCE_NOT_PRESENT_IN_PIERONE')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'APPLICATION_NOT_PRESENT_IN_KIO')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';
--
-- UPDATE fullstop_data.violation
-- SET
--   (plugin_fully_qualified_class_name, violation_type_entity_id) = ('old_violation', 'APPLICATION_VERSION_NOT_PRESENT_IN_KIO')
-- WHERE
--   violation_type_entity_id IS NULL
--   AND plugin_fully_qualified_class_name IS NULL
-- --and message like '%%';