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

import java.util.List;

import javax.annotation.PostConstruct;

import org.assertj.core.util.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.Assert;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.fullstop.plugin.config.ApplicationMasterdataPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;

@Component
public class ApplicationMasterdataPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationMasterdataPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String EVENT_NAME = "RunInstances";

    private final ApplicationMasterdataPluginProperties applicationMasterdataPluginProperties;
    private final List<NamedValidator> namedValidators;

    private Validator chainingValidator;

    private final ViolationSink violationSink;

    @Autowired
    public ApplicationMasterdataPlugin(
            final ApplicationMasterdataPluginProperties applicationMasterdataPluginProperties,
            final List<NamedValidator> namedValidators, final ViolationSink violationSink) {
        this.applicationMasterdataPluginProperties = applicationMasterdataPluginProperties;
        this.namedValidators = namedValidators;
        this.violationSink = violationSink;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

// return eventSource.equals(EC2_SOURCE_EVENTS) && eventName.equals(EVENT_NAME);
        return EC2_SOURCE_EVENTS.equals(eventSource) && EVENT_NAME.equals(eventName);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        Application application = getApplication();

        Errors errors = buildErrorsObject(application);
        this.chainingValidator.validate(application, errors);

        if (errors.hasErrors()) {
            violationSink.put(new ViolationBuilder("Violation").build());
        }
    }

    protected Errors buildErrorsObject(final Application application) {
        return new BeanPropertyBindingResult(application, "application");
    }

    protected Application getApplication() {

        Application app = new Application();
        app.setActive(true);
        app.setId("TestID");

        return app;
    }

    @PostConstruct
    public void init() {
        List<NamedValidator> validators = Lists.newArrayList();
        for (NamedValidator v : this.namedValidators) {
            if (this.applicationMasterdataPluginProperties.getValidatorsEnabled().contains(v.getName())) {
                validators.add(v);
                LOG.info("VALIDATOR : '{}' IS ENABLED", v.getName());
            }
        }

        this.chainingValidator = new ChainingValidator(validators.toArray(new NamedValidator[validators.size()]));
    }

    /**
     * {@link Validator} implementation that wraps {@link Validator} instances and chains their execution.
     */
    private static class ChainingValidator implements Validator {

        private Validator[] validators;

        public ChainingValidator(final Validator... validators) {
            Assert.notNull(validators, "Validators must not be null");
            this.validators = validators;
        }

        @Override
        public boolean supports(final Class<?> clazz) {
            for (Validator validator : this.validators) {
                if (validator.supports(clazz)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public void validate(final Object target, final Errors errors) {
            for (Validator validator : this.validators) {
                if (validator.supports(target.getClass())) {
                    validator.validate(target, errors);
                }
            }
        }

    }
}
