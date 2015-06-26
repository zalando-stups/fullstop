/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin;

import com.amazonaws.auth.policy.Policy;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.jayway.jsonpath.JsonPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static java.lang.String.format;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getAccountId;
import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getRegion;

/**
 * @author mrandi
 */
@Component
public class UnapprovedServicesAndRolePlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(UnapprovedServicesAndRolePlugin.class);

    private static final String EVENT_SOURCE = "iam.amazonaws.com";

    private final PolicyProvider policyProvider;

    private final ViolationSink violationSink;

    @Autowired
    public UnapprovedServicesAndRolePlugin(final PolicyProvider policyProvider, final ViolationSink violationSink) {
        this.policyProvider = policyProvider;
        this.violationSink = violationSink;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();

        return eventSource.equals(EVENT_SOURCE);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

        String roleName = JsonPath.read(event.getEventData().getRequestParameters(), "$.roleName");

        isAComplianceRole(event, roleName);

        Policy policy = policyProvider.getPolicy(roleName, getRegion(event), getAccountId(event));

        Policy policyPowerUser = PolicyTemplate.fromClasspath("/s-PowerUser.json");

        if (policy.equals(policyPowerUser)) {
            LOG.info("are equals");
        }

        LOG.info("policy: {}", policy);
        LOG.info("policyPowerUser: {}", policyPowerUser);

    }

    private void isAComplianceRole(CloudTrailEvent event, String roleName) {
        if(JsonPath.read(event.getEventData().getRequestParameters(), "$.eventName").equals("CreateRole")) {
            violationSink.put(
                    new ViolationBuilder(
                            format("Role: %s cannot be modified", roleName)).withEventId(getCloudTrailEventId(event))
                                                                            .withRegion(getCloudTrailEventRegion(event))
                                                                            .withAccountId(
                                                                                    getCloudTrailEventAccountId(
                                                                                            event))
                                                                            .build());
        }
    }

}
