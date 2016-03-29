INSERT INTO fullstop_data.violation_type (
  id,
  help_text,
  priority,
  violation_severity,
  title,
  created,
  created_by,
  version)
VALUES
  ('ILLEGAL_SCM_REPOSITORY',
   'The deployment artifact has been built from an illegal SCM repository. Please host your code only in officially supported SCM systems.',
   2,
   2,
   'Illegal SCM repository in use',
   now(),
   '#252',
   0),

  ('MISSING_SPEC_LINKS',
   'Some Git commits do not contain valid references to the issue tracking system.',
   2,
   2,
   'Commits w/o ticket references',
   now(),
   '#252',
   0);

