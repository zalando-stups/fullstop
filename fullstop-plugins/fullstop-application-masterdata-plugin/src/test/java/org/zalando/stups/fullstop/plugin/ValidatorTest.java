/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.zalando.stups.clients.kio.Application;

public class ValidatorTest {
    private Application application;

    @Before
    public void setUp() {
        application = new Application();
        application.setId("test_app");
    }

    @Test
    public void testSpecificationUrlValidator() {

        SpecificationUrlValidator validator = new SpecificationUrlValidator();
        Assertions.assertThat(validator.supports(application.getClass()))
                  .isTrue();

        Errors errors = new BeanPropertyBindingResult(
                application,
                "application");
        validator.validate(
                application,
                errors);

        Assertions.assertThat(errors.getErrorCount())
                  .isEqualTo(1);
        Assertions.assertThat(errors.getFieldErrorCount("specificationUrl"))
                  .isEqualTo(1);
        Assertions.assertThat(
                errors.getFieldError("specificationUrl")
                      .getCode())
                  .isEqualTo("specificationUrl.missing");
        Assertions.assertThat(
                errors.getFieldError("specificationUrl")
                      .getDefaultMessage())
                  .isEqualTo(
                          "Specification URL is missing");
    }

    @Test
    public void testDocumentationUrlValidator() {

        DocumentationUrlValidator validator = new DocumentationUrlValidator();
        Assertions.assertThat(validator.supports(application.getClass()))
                  .isTrue();

        Errors errors = new BeanPropertyBindingResult(
                application,
                "application");
        validator.validate(
                application,
                errors);

        Assertions.assertThat(errors.getErrorCount())
                  .isEqualTo(1);
        Assertions.assertThat(errors.getFieldErrorCount("documentationUrl"))
                  .isEqualTo(1);
        Assertions.assertThat(
                errors.getFieldError("documentationUrl")
                      .getCode())
                  .isEqualTo("documentationUrl.missing");
        Assertions.assertThat(
                errors.getFieldError("documentationUrl")
                      .getDefaultMessage())
                  .isEqualTo(
                          "Documentation URL is missing");
    }

    @Test
    public void testScmUrlValidator() {

        ScmUrlValidator validator = new ScmUrlValidator();
        Assertions.assertThat(validator.supports(application.getClass()))
                  .isTrue();

        Errors errors = new BeanPropertyBindingResult(
                application,
                "application");
        validator.validate(
                application,
                errors);

        Assertions.assertThat(errors.getErrorCount())
                  .isEqualTo(1);
        Assertions.assertThat(errors.getFieldErrorCount("scmUrl"))
                  .isEqualTo(1);
        Assertions.assertThat(
                errors.getFieldError("scmUrl")
                      .getCode())
                  .isEqualTo("scmUrl.missing");
        Assertions.assertThat(
                errors.getFieldError("scmUrl")
                      .getDefaultMessage())
                  .isEqualTo(
                          "SCM URL is missing");
    }
}
