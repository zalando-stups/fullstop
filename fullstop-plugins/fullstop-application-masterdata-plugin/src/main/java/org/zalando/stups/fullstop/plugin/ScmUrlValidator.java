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
/**
 *
 */
package org.zalando.stups.fullstop.plugin;

import org.springframework.stereotype.Component;

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.zalando.stups.clients.kio.Application;

import com.google.common.collect.Lists;

/**
 * @author npiccolotto
 */
@Component
public class ScmUrlValidator extends AbstractApplicationValidator {

    /*
     * (non-Javadoc)
     * 
     * @see org.zalando.stups.fullstop.plugin.NamedValidator#getName()
     */
    @Override
    public String getName() {
        return "scm_url";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.validation.Validator#validate(java.lang.Object, org.springframework.validation.Errors)
     */
    @Override
    public void validate(final Object target, final Errors errors) {
        Application app = (Application) target;
        ValidationUtils.rejectIfEmpty(errors,
                                      "scmUrl",
                                      "scmUrl.missing",
                                      "SCM URL is missing");
        errors.pushNestedPath("scmUrl");
        try {
            (new UrlValidator(Lists.newArrayList("http",
                                                 "https"),
                              false)).validate(app.getScmUrl(),
                                               errors);
        }
        finally {
            errors.popNestedPath();
        }
    }

}
