package org.zalando.stups.fullstop.plugin;

import org.springframework.validation.Validator;

/**
 * Name can be the field to validate.
 *
 * @author  jbellmann
 */
public interface NamedValidator extends Validator {

    String getName();

}
