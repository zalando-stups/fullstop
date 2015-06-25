package org.zalando.stups.fullstop.plugin;

import org.assertj.core.api.Assertions;

import org.junit.Test;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import org.zalando.stups.clients.kio.Application;

public class ValidatorTest {

    @Test
    public void testSpecificationUrlValidator() {
        Application app = new Application();
        app.setId("test_app");

        ApplicationSpecificationUrlValidator validator = new ApplicationSpecificationUrlValidator();
        Assertions.assertThat(validator.supports(app.getClass())).isTrue();

        Errors errors = new BeanPropertyBindingResult(app, "application");
        validator.validate(app, errors);

        Assertions.assertThat(errors.getErrorCount()).isEqualTo(1);
        Assertions.assertThat(errors.getFieldErrorCount("specificationUrl")).isEqualTo(1);
        Assertions.assertThat(errors.getFieldError("specificationUrl").getCode()).isEqualTo("specificationUrl.missing");
        Assertions.assertThat(errors.getFieldError("specificationUrl").getDefaultMessage()).isEqualTo(
            "Spec-Url is missing");
    }
}
