package org.zalando.stups.fullstop.plugin;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer;
import org.zalando.stups.fullstop.s3.S3Service;

import static org.mockito.Mockito.mock;

public class SimplePluginTest {

    @Test
    public void createCloudTrailEvent() {
        SecurityGroupProvider provider = mock(SecurityGroupProvider.class);
        S3Service writer = mock(S3Service.class);

        // we expect RunInstance and ec2 as source not, autoscaling
        SaveSecurityGroupsPlugin plugin = new SaveSecurityGroupsPlugin(provider, writer);
        boolean result = plugin.supports(TestCloudTrailEventSerializer.createCloudTrailEvent("/record.json"));
        Assertions.assertThat(result).isFalse();
    }
}
