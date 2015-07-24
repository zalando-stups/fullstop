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

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.NotFoundException;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.config.ApplicationMasterdataPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getInstanceIds;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;
import static org.zalando.stups.fullstop.violation.ViolationType.*;

//J-
@Component
public class ApplicationMasterdataPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationMasterdataPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String EVENT_NAME = "RunInstances";

    private static final String APPLICATION_ID = "application_id";

    private final ApplicationMasterdataPluginProperties applicationMasterdataPluginProperties;

    private final List<NamedValidator> namedValidators;

    private final ViolationSink violationSink;

    private KioOperations kioOperations;

    private Validator chainingValidator;

    private UserDataProvider userDataProvider;

    @Autowired
    public ApplicationMasterdataPlugin(
            final KioOperations kioOperations,
            final UserDataProvider userDataProvider,
            final ApplicationMasterdataPluginProperties applicationMasterdataPluginProperties,
            final List<NamedValidator> namedValidators, final ViolationSink violationSink) {
        this.applicationMasterdataPluginProperties = applicationMasterdataPluginProperties;
        this.namedValidators = namedValidators;
        this.violationSink = violationSink;
        this.kioOperations = kioOperations;
        this.userDataProvider = userDataProvider;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return EC2_SOURCE_EVENTS.equals(eventSource) && EVENT_NAME.equals(eventName);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        List<String> instanceIds = getInstanceIds(event);
        for (String instanceId : instanceIds) {
            // 1) get user data
            Map userData;

            final String accountId = event.getEventData().getUserIdentity().getAccountId();
            final String region = event.getEventData().getAwsRegion();
            try {
                userData = userDataProvider.getUserData(
                        accountId, region,
                        instanceId);
            }
            catch (AmazonServiceException ex) {
                violationSink.put(
                        violationFor(event).withType(MISSING_USER_DATA).withPluginFullyQualifiedClassName(
                                ApplicationMasterdataPlugin.class).withMetaInfo(
                                newArrayList(instanceId)).build());
                return;
            }

            if (userData == null) {
                violationSink.put(
                        violationFor(event).withType(MISSING_USER_DATA).withMetaInfo(
                                newArrayList(instanceId)).build());
                return;
            }
            // 2) read application id from user data
            if (userData.get(APPLICATION_ID) == null) {
                violationSink.put(
                        violationFor(event).withType(WRONG_USER_DATA).withMetaInfo(
                                newArrayList(instanceId, APPLICATION_ID)).build());
                return;
            }
            String applicationId = userData.get(APPLICATION_ID)
                                           .toString();
            Application application;
            try {
                application = kioOperations.getApplicationById(applicationId);
            }
            catch (NotFoundException ex) {
                violationSink.put(
                        violationFor(event).withType(APPLICATION_NOT_PRESENT_IN_KIO).withMetaInfo(
                                newArrayList(applicationId)).build());
                return;
            }
            // ACTUAL VALIDATION
            Errors errors = buildErrorsObject(application);
            this.chainingValidator.validate(
                    application,
                    errors);

            if (errors.hasErrors()) {
                String message = errors.getAllErrors()
                                       .stream()
                                       .map(e -> e.getDefaultMessage())
                                       .reduce(
                                               "",
                                               (s, m) -> s.concat(m + "\n"));
                violationSink.put(
                        violationFor(event).withType(WRONG_APPLICATION_MASTERDATA).withMetaInfo(
                                newArrayList(
                                        applicationId,
                                        message)).build());
            }
        }
    }

    protected Errors buildErrorsObject(final Application application) {
        return new BeanPropertyBindingResult(
                application,
                "application");
    }

    @PostConstruct
    public void init() {
        List<NamedValidator> validators = Lists.newArrayList();
        for (NamedValidator v : this.namedValidators) {
            if (this.applicationMasterdataPluginProperties.getDefaultValidatorsIfValidatorsEnabledIsEmpty()
                                                          .contains(v.getName())) {
                validators.add(v);
                LOG.info(
                        "VALIDATOR : '{}' IS ENABLED",
                        v.getName());
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
            Assert.notNull(
                    validators,
                    "Validators must not be null");
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
                    validator.validate(
                            target,
                            errors);
                }
            }
        }

    }
}
//J+
