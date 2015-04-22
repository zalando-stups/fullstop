/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugins.nopassword;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.springframework.util.StringUtils;

import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.violation.ViolationStore;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.GetUserRequest;
import com.amazonaws.services.identitymanagement.model.GetUserResult;
import com.amazonaws.services.identitymanagement.model.User;

/**
 * @author  jbellmann
 */
@Component
public class PasswordsNotAllowedPlugin extends AbstractFullstopPlugin {

    private final Logger logger = LoggerFactory.getLogger(PasswordsNotAllowedPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private final ClientProvider clientProvider;
    private final ViolationStore violationStore;

    @Autowired
    public PasswordsNotAllowedPlugin(final ClientProvider clientProvider, final ViolationStore violationStore) {
        this.clientProvider = clientProvider;
        this.violationStore = violationStore;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {

        // every incoming event
        return true;
// CloudTrailEventData eventData = event.getEventData();
//
// String eventSource = eventData.getEventSource();
// String eventName = eventData.getEventName();
//
// return eventSource.equals(EC2_SOURCE_EVENTS) && eventName.equals(EVENT_NAME);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

        String accountId = event.getEventData().getUserIdentity().getAccountId();
        if (!StringUtils.hasText(accountId)) {
            return;
        }

        AmazonIdentityManagementClient identityClient = clientProvider.getClient(AmazonIdentityManagementClient.class,
                accountId, Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));

        String username = event.getEventData().getUserIdentity().getUserName();
        if (!StringUtils.hasText(username)) {
            return;
        }

        GetUserRequest request = new GetUserRequest();
        request.setUserName(username);

        try {
            GetUserResult result = identityClient.getUser(request);

            User user = result.getUser();
            Date date = user.getPasswordLastUsed();
            if (date != null) {
                String message = String.format("Password was used by %s with accountId : %s", username, accountId);
                this.violationStore.save(message);
            }
        } catch (Exception e) {
            // logger.error(e.getMessage(), e);
        }
    }

}
