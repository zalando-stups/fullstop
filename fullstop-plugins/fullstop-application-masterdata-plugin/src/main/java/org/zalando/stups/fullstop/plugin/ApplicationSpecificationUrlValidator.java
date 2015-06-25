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

import org.springframework.stereotype.Component;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

/**
 * @author jbellmann
 */
@Component
public class ApplicationSpecificationUrlValidator extends AbstractApplicationValidator {

    @Override
    public void validate(final Object target, final Errors errors) {
        ValidationUtils.rejectIfEmpty(errors,
                                      "specificationUrl",
                                      "specificationUrl.missing",
                                      "Spec-Url is missing");
    }

    @Override
    public String getName() {
        return "specification_url";
    }

}
