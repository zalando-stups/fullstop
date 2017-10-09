package org.zalando.stups.fullstop.jobs.common.impl;

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsResult;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.Ipv6Range;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.SecurityGroupCheckDetails;

import java.util.Map;
import java.util.function.Predicate;

import static com.amazonaws.regions.Region.getRegion;
import static com.amazonaws.regions.Regions.EU_WEST_1;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

public class SecurityGroupsCheckerImplTest {

    private final Logger log = getLogger(getClass());

    private Predicate<IpPermission> mockPredicate;

    private SecurityGroupsCheckerImpl securityGroupsChecker;

    @SuppressWarnings("unchecked")
    @Before
    public void setUp() throws Exception {
        final ClientProvider mockClientProvider = mock(ClientProvider.class);
        final AmazonEC2Client mockEC2 = mock(AmazonEC2Client.class);
        mockPredicate = (Predicate<IpPermission>) mock(Predicate.class);

        when(mockClientProvider.getClient(any(), any(), any())).thenReturn(mockEC2);

        securityGroupsChecker = new SecurityGroupsCheckerImpl(mockClientProvider, mockPredicate);

        final DescribeSecurityGroupsResult securityGroups = new DescribeSecurityGroupsResult()
                .withSecurityGroups(new SecurityGroup()
                        .withGroupId("sg-12345678")
                        .withGroupName("my-sec-group")
                        .withIpPermissions(new IpPermission()
                                .withIpProtocol("tcp")
                                .withIpv4Ranges(new IpRange().withCidrIp("0.0.0.0/0"))
                                .withFromPort(0)
                                .withToPort(65535)
                                .withIpv6Ranges(new Ipv6Range().withCidrIpv6("::/0"))
                                .withUserIdGroupPairs(new UserIdGroupPair()
                                        .withUserId("111222333444")
                                        .withGroupId("sg-11223344"))));
        when(mockEC2.describeSecurityGroups(any())).thenReturn(securityGroups);
    }

    @Test
    public void check() throws Exception {
        when(mockPredicate.test(any())).thenReturn(true);

        final Map<String, SecurityGroupCheckDetails> checkResult = securityGroupsChecker.check(
                singleton("sg-12345678"), "111222333444", getRegion(EU_WEST_1));
        log.info("Check result: {}", checkResult);
        assertThat(checkResult).containsKey("sg-12345678");
    }

    @Test
    public void checkNoMatch() throws Exception {
        when(mockPredicate.test(any())).thenReturn(false);
        final Map<String, SecurityGroupCheckDetails> checkResult = securityGroupsChecker.check(
                singleton("sg-12345678"), "111222333444", getRegion(EU_WEST_1));
        log.info("Check result: {}", checkResult);
        assertThat(checkResult).isEmpty();
    }
}
