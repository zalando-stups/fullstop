package org.zalando.stups.fullstop.events;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getInstanceIds;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.getInstances;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

/**
 * @author jbellmann
 */
public class CloudTrailEventSupportTest {

    @Test
    public void getInstanceIdsTest() {
        final List<String> instanceIds = getInstanceIds(createCloudTrailEvent("/responseElements.json"));
        assertThat(instanceIds).isNotEmpty();
    }

    @Test
    public void getInstancesTest() {
        final CloudTrailEvent cloudTrailEvent = createCloudTrailEvent("/responseElements.json");
        final List<String> instances = getInstances(cloudTrailEvent);
        assertThat(instances).isNotEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void getInstanceIdsNullEvent() {
        final List<String> instanceIds = getInstanceIds(null);
        assertThat(instanceIds).isNotEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void getInstanceIdsNullEventData() {
        final List<String> instanceIds = getInstanceIds(new CloudTrailEvent(null, null));
        assertThat(instanceIds).isNotEmpty();
    }

    @Test
    public void testNullResponseElementsAmis() {
        final List<String> instanceIds = getInstanceIds(createCloudTrailEvent("/empty-responseElements.json"));
        assertThat(instanceIds).isEmpty();
    }

    @Test
    public void testReadEmptySecurityGroups() throws Exception {
        final List<String> groups = CloudTrailEventSupport.readSecurityGroupIds(createCloudTrailEvent("/empty-security-groups.json"));
        assertThat(groups).containsOnly("sg-aaaabbbb");

    }
}
