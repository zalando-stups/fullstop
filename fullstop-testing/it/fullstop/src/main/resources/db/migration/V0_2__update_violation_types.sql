UPDATE fullstop_data.violation_type
  SET id='ACTIVE_KEY_TOO_OLD'
  WHERE id='ACTIVE_KEY_TO_OLD';

UPDATE fullstop_data.violation
SET violation_type_entity_id = 'ACTIVE_KEY_TOO_OLD'
WHERE violation_type_entity_id ='ACTIVE_KEY_TO_OLD';
