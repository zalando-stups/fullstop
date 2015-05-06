package org.zalando.stups.fullstop.plugin.config;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.plugin.RegionPlugin;
import org.zalando.stups.fullstop.violation.SysOutViolationStore;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationStore;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.CloudTrailEventField;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.UserIdentity;

/**
 * @author  jbellmann
 */
public class RegionPluginTest {

    private ViolationStore violationStore = new SysOutViolationStore();
    ;

    private RegionPluginProperties regionPluginProperties;

    @Before
    public void setUp() {
        violationStore = Mockito.spy(violationStore);
        regionPluginProperties = new RegionPluginProperties();
    }

    @Test
    public void testWhitelistedRegion() {
        CloudTrailEventData data = new RegionPluginTestCloudTrailEventData("/responseElements.json", "eu-west-1");
        UserIdentity userIdentity = new UserIdentity();
        userIdentity.add(CloudTrailEventField.accountId.name(), "0234527346");
        data.add(CloudTrailEventField.userIdentity.name(), userIdentity);

        CloudTrailEvent event = new CloudTrailEvent(data, null);

        //
        RegionPlugin plugin = new RegionPlugin(violationStore, regionPluginProperties);
        plugin.processEvent(event);

        verify(violationStore, never()).save(Mockito.any(Violation.class));
    }

    @Test
    public void testNonWhitelistedRegion() {
        CloudTrailEventData data = new RegionPluginTestCloudTrailEventData("/responseElements.json", "us-west-1");
        UserIdentity userIdentity = new UserIdentity();
        userIdentity.add(CloudTrailEventField.accountId.name(), "0234527346");
        data.add(CloudTrailEventField.userIdentity.name(), userIdentity);

        CloudTrailEvent event = new CloudTrailEvent(data, null);

        //
        RegionPlugin plugin = new RegionPlugin(violationStore, regionPluginProperties);
        plugin.processEvent(event);

        verify(violationStore, atLeastOnce()).save(Mockito.any(Violation.class));
    }

    static class RegionPluginTestCloudTrailEventData extends TestCloudTrailEventData {

        private String region;

        public RegionPluginTestCloudTrailEventData(final String classpathResource, final String region) {
            super(classpathResource);
            this.region = region;
        }

        @Override
        public String getAwsRegion() {
            return region;
        }

    }

}
