package org.zalando.stups.fullstop.events;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.UserIdentity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.zalando.stups.fullstop.violation.ViolationBuilder;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Optional.ofNullable;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author jbellmann
 */
public abstract class CloudTrailEventSupport {

    private static final Logger LOG = getLogger(CloudTrailEventSupport.class);

    public static final String INSTANCE_ID_JSON_PATH = "$.instancesSet.items[*].instanceId";

    public static final String INSTANCE_JSON_PATH = "$.instancesSet.items[*]";

    public static final String RUN_INSTANCE_DATE_JSON_PATH = "$.launchTime";

    public static final String SECURITY_GROUP_IDS_JSON_PATH =
            "$.groupSet.items[*].groupId";

    public static final String INSTANCE_LAUNCH_TIME = "$.instancesSet.items[*].launchTime";

    private static final String ACCOUNT_ID_SHOULD_NEVER_BE_NULL = "AccountId should never be null";

    private static final String USER_IDENTITY_SHOULD_NEVER_BE_NULL = "UserIdentity should never be null";

    private static final String CLOUD_TRAIL_EVENT_DATA_SHOULD_NEVER_BE_NULL =
            "CloudTrailEventData should never be null";

    private static final String CLOUD_TRAIL_EVENT_SHOULD_NEVER_BE_NULL = "CloudTrailEvent should never be null";

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
        UserIdentity userIdentity = checkNotNull(eventData.getUserIdentity(), USER_IDENTITY_SHOULD_NEVER_BE_NULL);

        return checkNotNull(userIdentity.getAccountId(), ACCOUNT_ID_SHOULD_NEVER_BE_NULL);
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
        if (isNullOrEmpty(responseElements) && emptyListOnNullOrEmptyResponse) {
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

    public static List<String> read(final CloudTrailEvent cloudTrailEvent, final String pattern,
                                    final boolean emptyListOnNullOrEmptyResponse) {
        return read(getEventData(cloudTrailEvent).getResponseElements(), pattern, emptyListOnNullOrEmptyResponse);
    }

    public static boolean isRunInstancesEvent(final CloudTrailEvent cloudTrailEvent) {
        return Optional.ofNullable(cloudTrailEvent)
                .map(CloudTrailEvent::getEventData)
                .filter(e -> "ec2.amazonaws.com".equals(e.getEventSource()))
                .filter(e -> "RunInstances".equals(e.getEventName()))
                .isPresent();
    }

    public static List<String> getInstanceLaunchTime(CloudTrailEvent cloudTrailEvent) {
        cloudTrailEvent = checkNotNull(cloudTrailEvent, CLOUD_TRAIL_EVENT_SHOULD_NEVER_BE_NULL);

        CloudTrailEventData eventData = getEventData(cloudTrailEvent);

        String responseElements = eventData.getResponseElements();

        return JsonPath.read(responseElements, INSTANCE_LAUNCH_TIME);
    }

    public static Region getRegion(final CloudTrailEvent cloudTrailEvent) {
        return Optional.ofNullable(getRegionAsString(cloudTrailEvent))
                .map(Regions::fromName)
                .map(Region::getRegion)
                .orElseThrow(() -> new IllegalArgumentException("Missing awsRegion in CloudTrailEvent " + cloudTrailEvent));
    }

    public static String getRegionAsString(final CloudTrailEvent event) {
        return event.getEventData().getAwsRegion();
    }

    public static ViolationBuilder violationFor(CloudTrailEvent cloudTrailEvent) {
        return new ViolationBuilder()
                .withEventId(getEventId(cloudTrailEvent))
                .withAccountId(getAccountId(cloudTrailEvent))
                .withRegion(getRegionAsString(cloudTrailEvent))
                .withUsername(getUsernameAsString(cloudTrailEvent));
    }

    public static String getUsernameAsString(CloudTrailEvent cloudTrailEvent) {

        return ofNullable(cloudTrailEvent)
                .map(CloudTrailEvent::getEventData)
                .map(CloudTrailEventData::getUserIdentity)
                .map(UserIdentity::getARN)
                .orElse(null);
    }

    public static List<String> getInstances(CloudTrailEvent event) {
        CloudTrailEventData eventData = getEventData(event);
        ObjectMapper mapper = new ObjectMapper();
        List<String> instances = newArrayList();
        String responseElements = eventData.getResponseElements();
        if (isNullOrEmpty(responseElements)) {
            return newArrayList();
        }

        JSONArray items = JsonPath.read(responseElements, INSTANCE_JSON_PATH);
        for (Object item : items) {
            try {
                instances.add(mapper.writeValueAsString(item));
            } catch (JsonProcessingException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        return instances;
    }

    public static DateTime getRunInstanceTime(String instance) {
        return new DateTime((Long) JsonPath.read(instance, RUN_INSTANCE_DATE_JSON_PATH));
    }

    public static DateTime getEventTime(CloudTrailEvent event) {

        event = checkNotNull(event, CLOUD_TRAIL_EVENT_SHOULD_NEVER_BE_NULL);

        Date eventTime = event.getEventData().getEventTime();

        return new DateTime(eventTime);
    }
}
