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
package org.zalando.stups.fullstop.plugin.keypair;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.violation.ViolationStore;

import java.util.Arrays;
import java.util.List;

/**
 * @author mrandi
 */
@Component
public class UnapprovesServicesAndRolePlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(UnapprovesServicesAndRolePlugin.class);

    private static final String EVENT_SOURCE = "iam.amazonaws.com";

    private static final List<String> ROLES = Arrays.asList("Shibboleth-Administrator", "Shibboleth-PowerUser",
            "Shibboleth-PowerUserUS", "Shibboleth-ReadOnly");

    private final ClientProvider cachingClientProvider;

    private final ViolationStore violationStore;

    @Autowired
    public UnapprovesServicesAndRolePlugin(final ClientProvider cachingClientProvider,
            final ViolationStore violationStore) {
        this.cachingClientProvider = cachingClientProvider;
        this.violationStore = violationStore;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();

        return eventSource.equals(EVENT_SOURCE);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

//        Policy policy = new Policy();
//        for (Statement statement : policy.getStatements()) {
//            LOG.info("Role policy");
//        }
//
//
        }

}