INSERT INTO fullstop_data.violation_type (
  id,
  help_text,
  violation_severity,
  priority,
  title,
  created,
  created_by,
  version)
VALUES ('ARTIFACT_BUILT_FROM_DIRTY_REPOSITORY',
        'The deployed docker artifact was built from a git repository in a dirty state. In other words there were uncommitted or untracked local changes that might have influenced the artifact, which were not properly tracked. Artifact builds should only be performed on clean repositories. Hint: Use `git stash` to clean the working directory without losing unfinished features.',
        3,
        1,
        'Dirty artifact build',
        now(),
        '#468',
        0);
