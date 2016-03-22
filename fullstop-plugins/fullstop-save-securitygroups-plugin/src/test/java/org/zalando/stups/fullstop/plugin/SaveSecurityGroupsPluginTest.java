package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.s3.S3Service;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.EU_WEST_1;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.*;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

public class SaveSecurityGroupsPluginTest {

    private SecurityGroupProvider mockSecurityGroupProvider;
    private S3Service mockS3Service;
    private SaveSecurityGroupsPlugin plugin;
    private CloudTrailEvent cloudTrailEvent;

    @Before
    public void setUp() throws Exception {
        mockSecurityGroupProvider = mock(SecurityGroupProvider.class);
        mockS3Service = mock(S3Service.class);

        plugin = new SaveSecurityGroupsPlugin(mockSecurityGroupProvider, mockS3Service, "saved-security-groups");

        cloudTrailEvent = createCloudTrailEvent("/run-instance-record.json");

        when(mockSecurityGroupProvider.getSecurityGroup(any(), any(), any()))
                .thenReturn("{this is a security group test}");

        when(mockS3Service.listCommonPrefixesS3Objects(any(), any()))
                .thenReturn(asList(
                        "123456789111/eu-west-1/2015/06/18/i-111124cer11111-2016-01-15T13:03:02.000Z/",
                        "123456789111/eu-west-1/2015/06/18/i-fdsa33fsd-2016-01-15T07:07:15.000Z/",
                        "123456789111/eu-west-1/2015/06/18/i-fdsafsd-2016-01-15T07:07:15.000Z/",
                        "123456789111/eu-west-1/2015/06/18/i-023d5bf6c26aa18d5-2016-01-15T15:59:54.000Z/",
                        "123456789111/eu-west-1/2015/06/18/i-023d5bf6c26aa18d5-2016-01-15T15:59:54.000Zi-fdsafsd-2016-01-15T07:07:15.000Z"));
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(mockSecurityGroupProvider, mockS3Service);
    }

    @Test
    public void testProcessEvent() throws Exception {
        plugin.processEvent(cloudTrailEvent);

        verify(mockSecurityGroupProvider).getSecurityGroup(eq(singletonList("sg-24051988")), eq(getRegion(EU_WEST_1)), eq("123456789111"));
        verify(mockS3Service, times(5)).listCommonPrefixesS3Objects(eq("saved-security-groups"), eq("123456789111/eu-west-1/2015/06/18/"));
        verify(mockS3Service, times(3)).putObjectToS3(eq("saved-security-groups"), anyString(), anyString(), any(), any());
    }

    @Test
    public void testNullSecurityGroup() throws Exception {
        when(mockSecurityGroupProvider.getSecurityGroup(any(), any(), any()))
                .thenReturn(null);

        plugin.processEvent(cloudTrailEvent);

        verify(mockSecurityGroupProvider).getSecurityGroup(eq(singletonList("sg-24051988")), eq(getRegion(EU_WEST_1)), eq("123456789111"));
    }
}
