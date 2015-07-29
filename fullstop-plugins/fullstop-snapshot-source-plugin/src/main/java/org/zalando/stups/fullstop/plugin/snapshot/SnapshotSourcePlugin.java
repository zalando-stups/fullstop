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
package org.zalando.stups.fullstop.plugin.snapshot;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.events.UserDataProvider;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.Map;

import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getInstanceIds;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.violationFor;
import static org.zalando.stups.fullstop.violation.ViolationType.*;

/**
 * @author npiccolotto
 */
@Component
public class SnapshotSourcePlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(SnapshotSourcePlugin.class);

    private static final String EVENT_NAME = "RunInstances";

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String SOURCE = "source";

    private static final String SNAPSHOT_REGEX = ".*?-SNAPSHOT$";

    private final UserDataProvider userDataProvider;

    private final ViolationSink violationSink;

    @Autowired
    public SnapshotSourcePlugin(final UserDataProvider userDataProvider, final ViolationSink violationSink) {
        this.userDataProvider = userDataProvider;
        this.violationSink = violationSink;
    }

    @Override
    public void processEvent(CloudTrailEvent event) {
        List<String> instanceIds = getInstanceIds(event);
        for (String id : instanceIds) {
            Map userData;
            final String accountId = event.getEventData().getUserIdentity().getAccountId();
            final String region = event.getEventData().getAwsRegion();
            try {
                userData = userDataProvider.getUserData(accountId, region, id);
            }
            catch (AmazonServiceException e) {
                LOG.error(e.getMessage());
                violationSink.put(violationFor(event).withInstanceId(id).withPluginFullyQualifiedClassName(
                                          SnapshotSourcePlugin.class).withType(MISSING_USER_DATA).withMetaInfo(id).build());
                return;
            }

            if (userData == null) {
                violationSink.put(violationFor(event).withInstanceId(id).withPluginFullyQualifiedClassName(
                                          SnapshotSourcePlugin.class).withType(MISSING_USER_DATA).withMetaInfo(id).build());
            }

            String source = (String) userData.get(SOURCE);
            if (source == null) {
                violationSink.put(violationFor(event).withInstanceId(id).withPluginFullyQualifiedClassName(
                                          SnapshotSourcePlugin.class).withType(MISSING_SOURCE_IN_USER_DATA).withMetaInfo(id).build());
            }
            else if (source.matches(SNAPSHOT_REGEX)) {
                violationSink.put(violationFor(event).withInstanceId(id).withPluginFullyQualifiedClassName(
                                          SnapshotSourcePlugin.class).withType(EC2_WITH_A_SNAPSHOT_IMAGE).withMetaInfo(id).build());
            }
        }
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS) && eventName.equals(EVENT_NAME);
    }

}
