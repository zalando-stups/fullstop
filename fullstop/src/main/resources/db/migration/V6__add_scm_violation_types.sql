SELECT fullstop_data.create_or_update_violation_type('ILLEGAL_SCM_REPOSITORY',
                                                     'The deployment artifact has been built from an illegal SCM repository. Please host your code only in officially supported SCM systems.',
                                                     2,
                                                     'Illegal SCM repository in use');

SELECT fullstop_data.create_or_update_violation_type('MISSING_SPEC_LINKS',
                                                     'Some Git commits do not contain valid references to the issue tracking system.',
                                                     2,
                                                     'Commits w/o ticket references');
