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
package org.zalando.stups.fullstop.plugins.keyrotation;

import static java.lang.String.format;

import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.violation.ViolationStore;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.GetUserRequest;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysResult;

/**
 * @author  jbellmann
 */
public class KeyRotationPlugin extends AbstractFullstopPlugin {

    private static final String VIOLATION_MESSAGE = "User [%s] has an active key [%s] older than 1 week.";

	private static final String ACTIVE = "Active";

    private final Logger logger = LoggerFactory.getLogger(KeyRotationPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private final ClientProvider clientProvider;

    private final ViolationStore violationStore;

    @Autowired
    public KeyRotationPlugin(final ClientProvider clientProvider, final ViolationStore violationStore) {
        this.clientProvider = clientProvider;
        this.violationStore = violationStore;
    }

    @Override
    public boolean supports(final CloudTrailEvent delimiter) {

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
            ListAccessKeysResult result = identityClient.listAccessKeys();
            List<AccessKeyMetadata> metaDataList = result.getAccessKeyMetadata();
            for (AccessKeyMetadata meta : metaDataList) {
                meta.getAccessKeyId();
                meta.getUserName();
                meta.getCreateDate();
                if (meta.getCreateDate().getTime() > LocalDate.now().minusWeeks(1).toDate().getTime()) {
                    if (ACTIVE.equalsIgnoreCase(meta.getStatus())) {

                        violationStore.save(format(VIOLATION_MESSAGE,
                                meta.getUserName(), meta.getAccessKeyId()));
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

}
