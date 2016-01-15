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
        List<String> instanceIds = getInstanceIds(createCloudTrailEvent("/responseElements.json"));
        assertThat(instanceIds).isNotEmpty();
    }

    @Test
    public void getInstancesTest() {
        CloudTrailEvent cloudTrailEvent = createCloudTrailEvent("/responseElements.json");
        List<String> instances = getInstances(cloudTrailEvent);
        assertThat(instances).isNotEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void getInstanceIdsNullEvent() {
        List<String> instanceIds = getInstanceIds(null);
        assertThat(instanceIds).isNotEmpty();
    }

    @Test(expected = NullPointerException.class)
    public void getInstanceIdsNullEventData() {
        List<String> instanceIds = getInstanceIds(new CloudTrailEvent(null, null));
        assertThat(instanceIds).isNotEmpty();
    }

    @Test
    public void testNullResponseElementsAmis() {
        List<String> instanceIds = getInstanceIds(createCloudTrailEvent("/empty-responseElements.json"));
        assertThat(instanceIds).isEmpty();
    }

    @Test
    public void testReadEmptySecurityGroups() throws Exception {
        final List<String> groups = CloudTrailEventSupport.readSecurityGroupIds(createCloudTrailEvent("/empty-security-groups.json"));
        assertThat(groups).containsOnly("sg-aaaabbbb");

    }
}
