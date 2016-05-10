package org.zalando.stups.fullstop.events;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.UserIdentity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import net.minidev.json.JSONArray;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.zalando.stups.fullstop.violation.ViolationBuilder;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.emptyList;
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

    public static final String SECURITY_GROUP_IDS_JSON_PATH = "$.groupSet.items[*].groupId";
    public static final String INSTANCES_SECURITY_GROUP_IDS_JSON_PATH = "$.instancesSet.items[*].groupSet.items[*].groupId";

    public static final String INSTANCE_LAUNCH_TIME = "$.instancesSet.items[*].launchTime";

    private static final String ACCOUNT_ID_OR_RECIPIENT_SHOULD_NEVER_BE_NULL = "AccountId or RecipientAccountId should never be null";

    private static final String USER_IDENTITY_SHOULD_NEVER_BE_NULL = "UserIdentity should never be null";

    private static final String CLOUD_TRAIL_EVENT_DATA_SHOULD_NEVER_BE_NULL =
            "CloudTrailEventData should never be null";

    private static final String CLOUD_TRAIL_EVENT_SHOULD_NEVER_BE_NULL = "CloudTrailEvent should never be null";

    /**
     * Extracts list of instanceIds from {@link CloudTrailEvent}s 'responseElements'.
     */
    public static List<String> getInstanceIds(final CloudTrailEvent event) {

        final CloudTrailEventData eventData = getEventData(event);

        final String responseElements = eventData.getResponseElements();
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
        final CloudTrailEventData eventData = getEventData(event);
        final UserIdentity userIdentity = checkNotNull(eventData.getUserIdentity(), USER_IDENTITY_SHOULD_NEVER_BE_NULL);
        final String value = ofNullable(userIdentity.getAccountId()).orElse(eventData.getRecipientAccountId());
        return checkNotNull(value, ACCOUNT_ID_OR_RECIPIENT_SHOULD_NEVER_BE_NULL);
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
            return emptyList();
        }

        try {
            return JsonPath.read(responseElements, pattern);
        } catch (final PathNotFoundException e) {
            if (emptyListOnNullOrEmptyResponse) {
                return emptyList();
            } else {
                throw e;
            }
        }
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

    public static List<String> getInstanceLaunchTime(CloudTrailEvent cloudTrailEvent) {
        cloudTrailEvent = checkNotNull(cloudTrailEvent, CLOUD_TRAIL_EVENT_SHOULD_NEVER_BE_NULL);

        final CloudTrailEventData eventData = getEventData(cloudTrailEvent);

        final String responseElements = eventData.getResponseElements();

        return JsonPath.read(responseElements, INSTANCE_LAUNCH_TIME);
    }

    public static Region getRegion(final CloudTrailEvent cloudTrailEvent) {
        return ofNullable(getRegionAsString(cloudTrailEvent))
                .map(Regions::fromName)
                .map(Region::getRegion)
                .orElseThrow(() -> new IllegalArgumentException("Missing awsRegion in CloudTrailEvent " + cloudTrailEvent));
    }

    public static String getRegionAsString(final CloudTrailEvent event) {
        return event.getEventData().getAwsRegion();
    }

    public static ViolationBuilder violationFor(final CloudTrailEvent cloudTrailEvent) {
        return new ViolationBuilder()
                .withEventId(getEventId(cloudTrailEvent))
                .withAccountId(getAccountId(cloudTrailEvent))
                .withRegion(getRegionAsString(cloudTrailEvent))
                .withUsername(getUsernameAsString(cloudTrailEvent));
    }

    public static String getUsernameAsString(final CloudTrailEvent cloudTrailEvent) {

        return ofNullable(cloudTrailEvent)
                .map(CloudTrailEvent::getEventData)
                .map(CloudTrailEventData::getUserIdentity)
                .map(UserIdentity::getARN)
                .orElse(null);
    }

    public static List<String> getInstances(final CloudTrailEvent event) {
        final CloudTrailEventData eventData = getEventData(event);
        final ObjectMapper mapper = new ObjectMapper();
        final List<String> instances = newArrayList();
        final String responseElements = eventData.getResponseElements();
        if (isNullOrEmpty(responseElements)) {
            return newArrayList();
        }

        final JSONArray items = JsonPath.read(responseElements, INSTANCE_JSON_PATH);
        for (final Object item : items) {
            try {
                instances.add(mapper.writeValueAsString(item));
            } catch (final JsonProcessingException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        return instances;
    }

    public static DateTime getRunInstanceTime(final String instance) {
        return new DateTime((Long) JsonPath.read(instance, RUN_INSTANCE_DATE_JSON_PATH));
    }

    public static DateTime getEventTime(CloudTrailEvent event) {

        event = checkNotNull(event, CLOUD_TRAIL_EVENT_SHOULD_NEVER_BE_NULL);

        final Date eventTime = event.getEventData().getEventTime();

        return new DateTime(eventTime);
    }

    public static List<String> readSecurityGroupIds(final CloudTrailEvent cloudTrailEvent) {
        final LinkedHashSet<String> result = newLinkedHashSet();
        result.addAll(read(cloudTrailEvent, SECURITY_GROUP_IDS_JSON_PATH, true));
        result.addAll(read(cloudTrailEvent, INSTANCES_SECURITY_GROUP_IDS_JSON_PATH, true));
        return newArrayList(result);
    }
}
