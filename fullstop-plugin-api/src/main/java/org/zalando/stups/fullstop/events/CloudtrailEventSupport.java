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
package org.zalando.stups.fullstop.events;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.UserIdentity;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;

/**
 * @author jbellmann
 */
public abstract class CloudtrailEventSupport {

    public static final String IMAGE_ID_JSON_PATH = "$.instancesSet.items[*].imageId";

    public static final String INSTANCE_ID_JSON_PATH = "$.instancesSet.items[*].instanceId";

    public static final String PRIVATE_IP_JSON_PATH = "$.instancesSet.items[*].privateIpAddress";

    public static final String PUBLIC_IP_JSON_PATH = "$.instancesSet.items[*].publicIpAddress";

    public static final String SECURITY_GROUP_IDS_JSON_PATH = "$.instancesSet.items[*].networkInterfaceSet.items[*].groupSet.items[*].groupId";

    public static final String INSTANCE_LAUNCH_TIME = "$.instancesSet.items[*].launchTime";

    private static final String ACCOUNT_ID_SHOULD_NEVER_BE_NULL = "AccountId should never be null";

    private static final String USER_IDENTITY_SHOULD_NEVER_BE_NULL = "UserIdentity should never be null";

    private static final String REGION_STRING_SHOULD_NEVER_BE_NULL_OR_EMPTY = "RegionString should never be null or empty";

    private static final String CLOUD_TRAIL_EVENT_DATA_SHOULD_NEVER_BE_NULL = "CloudTrailEventData should never be null";

    private static final String CLOUD_TRAIL_EVENT_SHOULD_NEVER_BE_NULL = "CloudTrailEvent should never be null";

    /**
     * Extracts list of imageIds from {@link CloudTrailEvent}s 'responseElements'.
     */
    public static List<String> getAmis(final CloudTrailEvent event) {

        CloudTrailEventData eventData = getEventData(event);

        String responseElements = eventData.getResponseElements();
        if (isNullOrEmpty(responseElements)) {
            return newArrayList();
        }

        return read(responseElements, IMAGE_ID_JSON_PATH);
    }

    /**
     * Extracts list of instanceIds from {@link CloudTrailEvent}s 'responseElements'.
     */
    public static List<String> getInstanceIds(final CloudTrailEvent event) {

        CloudTrailEventData eventData = getEventData(event);

        String responseElements = eventData.getResponseElements();
        if (isNullOrEmpty(responseElements)) {
            return newArrayList();
        }

        return read(responseElements, INSTANCE_ID_JSON_PATH);
    }

    public static String getEventId(final CloudTrailEvent event) {
        return event.getEventData().getEventId().toString();
    }

    /**
     * Extracts the 'accountId'.
     */
    public static String getAccountId(final CloudTrailEvent event) {
        CloudTrailEventData eventData = getEventData(event);
        // otherwise we get a java.lang.ClassCastException in tests
        if (eventData instanceof TestCloudTrailEventData) {
            return "";
        }
        UserIdentity userIdentity = checkNotNull(eventData.getUserIdentity(), USER_IDENTITY_SHOULD_NEVER_BE_NULL);

        return checkNotNull(userIdentity.getAccountId(), ACCOUNT_ID_SHOULD_NEVER_BE_NULL);
    }

    /**
     * Extract the 'keyName'.
     */
    public static List<String> containsKeyNames(final String parameters) {

        if (parameters == null) {
            return null;
        }

        return JsonPath.read(parameters, "$.instancesSet.items[*].keyName");
    }

    /**
     * Extracts ids of security-groups.
     */
    public static List<String> readSecurityGroupIds(final String parameters) {
        if (parameters == null) {
            return null;
        }

        return read(parameters, "$.instancesSet.items[*].networkInterfaceSet.items[*].groupSet.items[*].groupId");
    }

    private static CloudTrailEventData getEventData(CloudTrailEvent event) {
        event = checkNotNull(event, CLOUD_TRAIL_EVENT_SHOULD_NEVER_BE_NULL);

        return checkNotNull(event.getEventData(), CLOUD_TRAIL_EVENT_DATA_SHOULD_NEVER_BE_NULL);
    }

    /**
     * Reads the given 'responseElements' and extracts information based on given 'pattern'.<br/>
     * If 'responseElements' is null or empty you can handle the {@link IllegalArgumentException} raised or got an empty
     * list.
     */
    public static List<String> read(final String responseElements, final String pattern,
            final boolean emptyListOnNullOrEmptyResponse) {
        if (Strings.isNullOrEmpty(responseElements) && emptyListOnNullOrEmptyResponse) {
            return Lists.newArrayList();
        }

        return JsonPath.read(responseElements, pattern);
    }

    /**
     * Reads the given 'responseElements' and extracts information based on given 'pattern'.<br/>
     * If 'responseElements' is null or empty raises {@link IllegalArgumentException}.
     */
    public static List<String> read(final String responseElements, final String pattern) {
        return read(responseElements, pattern, false);
    }

    public static List<String> read(final CloudTrailEvent cloudTrailEvent, final String pattern) {
        return read(getEventData(cloudTrailEvent).getResponseElements(), pattern, false);
    }

    public static List<String> read(final CloudTrailEvent cloudTrailEvent, final String pattern,
            final boolean emptyListOnNullOrEmptyResponse) {
        return read(getEventData(cloudTrailEvent).getResponseElements(), pattern, emptyListOnNullOrEmptyResponse);
    }

    public static boolean isEc2EventSource(final CloudTrailEvent cloudTrailEvent) {
        return EventSourcePredicate.EC2_EVENT.test(cloudTrailEvent);
    }

    public static boolean isRunInstancesEvent(final CloudTrailEvent cloudTrailEvent) {
        return EventNamePredicate.RUN_INSTANCES.test(cloudTrailEvent);
    }

    public static List<String> getInstanceLaunchTime(CloudTrailEvent cloudTrailEvent) {
        cloudTrailEvent = checkNotNull(cloudTrailEvent, CLOUD_TRAIL_EVENT_SHOULD_NEVER_BE_NULL);
        CloudTrailEventData eventData = getEventData(cloudTrailEvent);

        String responseElements = eventData.getResponseElements();

        return JsonPath.read(responseElements, INSTANCE_LAUNCH_TIME);
    }

    public static Region getRegion(CloudTrailEvent cloudTrailEvent) {
        cloudTrailEvent = checkNotNull(cloudTrailEvent, CLOUD_TRAIL_EVENT_SHOULD_NEVER_BE_NULL);

        CloudTrailEventData cloudTrailEventData = checkNotNull(cloudTrailEvent.getEventData(),
                CLOUD_TRAIL_EVENT_DATA_SHOULD_NEVER_BE_NULL);

        return getRegion(cloudTrailEventData.getAwsRegion());
    }

    public static Region getRegion(final String regionString) {
        checkState(!isNullOrEmpty(regionString), REGION_STRING_SHOULD_NEVER_BE_NULL_OR_EMPTY);
        return Region.getRegion(Regions.fromName(regionString));
    }

    public static String getRegionAsString(final CloudTrailEvent event) {
        return event.getEventData().getAwsRegion();
    }
}
