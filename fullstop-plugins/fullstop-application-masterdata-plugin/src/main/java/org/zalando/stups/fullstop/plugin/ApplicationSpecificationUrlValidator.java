package org.zalando.stups.fullstop.plugin;

import org.springframework.stereotype.Component;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * @author  jbellmann
 */
@Component
public class ApplicationSpecificationUrlValidator extends AbstractApplicationValidator {

    @Override
    public void validate(final Object target, final Errors errors) {
        ValidationUtils.rejectIfEmpty(errors, "specificationUrl", "specificationUrl.missing", "Spec-Url is missing");
    }

    @Override
    public String getName() {
        return "specificationUrl";
    }

}
