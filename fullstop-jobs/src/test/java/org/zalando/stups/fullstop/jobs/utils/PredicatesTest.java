package org.zalando.stups.fullstop.jobs.utils;

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.Ipv6Range;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class PredicatesTest {

    private Predicate<SecurityGroup> pred;
    private SecurityGroup securityGroup;

    @Before
    public void setUp() throws Exception {
        final Set<Integer> allowedPorts = Stream.of(22, 80, 443).collect(toSet());
        pred = Predicates.securityGroupExposesNotAllowedPorts(allowedPorts);

        securityGroup = new SecurityGroup();
        securityGroup.withGroupId("sg-12345678");
        securityGroup.withGroupName("TestSecurityGroup");
        securityGroup.withOwnerId("111222333444");
        securityGroup.withIpPermissions(emptyList());
    }

    @Test
    public void testAllTrafficFromSecurityGroups() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("-1")
                        .withUserIdGroupPairs(
                                new UserIdGroupPair().withUserId("111222333444").withGroupId("sg-11223344")));

        assertThat(pred).rejects(securityGroup);
    }

    @Test
    public void testAllTrafficFromPrivateNetworks() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("-1")
                        .withIpv4Ranges(
                                new IpRange().withCidrIp("10.0.0.0/8"),
                                new IpRange().withCidrIp("172.31.0.0/16"),
                                new IpRange().withCidrIp("172.16.0.0/12"),
                                new IpRange().withCidrIp("192.168.0.0/16"))
                        .withIpv6Ranges(
                                new Ipv6Range().withCidrIpv6("fc00::/7")));

        assertThat(pred).rejects(securityGroup);
    }

    @Test
    public void testAllTrafficFromPartiallyPrivateNetwork() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("-1")
                        .withIpv4Ranges(
                                new IpRange().withCidrIp("192.168.0.0/15")));

        assertThat(pred).accepts(securityGroup);
    }

    @Test
    public void testAllTrafficFromEverywhereIPv4() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("-1")
                        .withIpv4Ranges(new IpRange().withCidrIp("0.0.0.0/0")));

        assertThat(pred).accepts(securityGroup);
    }

    @Test
    public void testAllTrafficFromEverywhereIPv6() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("-1")
                        .withIpv6Ranges(new Ipv6Range().withCidrIpv6("::/0")));

        assertThat(pred).accepts(securityGroup);
    }

    @Test
    public void testAllTcpFromEverywhereIPv4() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withFromPort(0)
                        .withToPort(65535)
                        .withIpProtocol("tcp")
                        .withIpv4Ranges(new IpRange().withCidrIp("0.0.0.0/0")));
        assertThat(pred).accepts(securityGroup);
    }

    @Test
    public void testAllTcpFromEverywhereIPv6() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withFromPort(0)
                        .withToPort(65535)
                        .withIpProtocol("tcp")
                        .withIpv6Ranges(new Ipv6Range().withCidrIpv6("::/0")));
        assertThat(pred).accepts(securityGroup);
    }

    @Test
    public void testAllUDPFromEverywhereIPv4() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("udp")
                        .withFromPort(0)
                        .withToPort(65535)
                        .withIpv4Ranges(new IpRange().withCidrIp("0.0.0.0/0")));

        assertThat(pred).accepts(securityGroup);
    }

    @Test
    public void testAllUDPFromEverywhereIPv6() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("udp")
                        .withFromPort(0)
                        .withToPort(65535)
                        .withIpv6Ranges(new Ipv6Range().withCidrIpv6("::/0")));

        assertThat(pred).accepts(securityGroup);
    }


    @Test
    public void testAllICMPIPv6FromEverywhereIPv4() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("icmpv6")
                        .withFromPort(-1)
                        .withToPort(-1)
                        .withIpv4Ranges(new IpRange().withCidrIp("0.0.0.0/0")));

        assertThat(pred).rejects(securityGroup);
    }

    @Test
    public void testAllICMPIPv6FromEverywhereIPv6() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("icmpv6")
                        .withFromPort(-1)
                        .withToPort(-1)
                        .withIpv6Ranges(new Ipv6Range().withCidrIpv6("::/0")));

        assertThat(pred).rejects(securityGroup);
    }

    @Test
    public void testAllICMPIPv4FromEverywhereIPv4() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("icmp")
                        .withFromPort(-1)
                        .withToPort(-1)
                        .withIpv4Ranges(new IpRange().withCidrIp("0.0.0.0/0")));

        assertThat(pred).rejects(securityGroup);
    }

    @Test
    public void testAllICMPIPv4FromEverywhereIPv6() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withIpProtocol("icmp")
                        .withFromPort(-1)
                        .withToPort(-1)
                        .withIpv6Ranges(new Ipv6Range().withCidrIpv6("::/0")));

        assertThat(pred).rejects(securityGroup);
    }

    @Test
    public void testUnallowedPortFromEverywhereIPv4() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withFromPort(9100)
                        .withToPort(9100)
                        .withIpProtocol("tcp")
                        .withIpv4Ranges(new IpRange().withCidrIp("0.0.0.0/0")));

        assertThat(pred).accepts(securityGroup);
    }

    @Test
    public void testUnallowedPortFromEverywhereIPv6() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withFromPort(9100)
                        .withToPort(9100)
                        .withIpProtocol("tcp")
                        .withIpv6Ranges(new Ipv6Range().withCidrIpv6("::/0")));

        assertThat(pred).accepts(securityGroup);
    }

    @Test
    public void testAllowedPortFromEverywhereIPv4() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withFromPort(443)
                        .withToPort(443)
                        .withIpProtocol("tcp")
                        .withIpv4Ranges(new IpRange().withCidrIp("0.0.0.0/0")));

        assertThat(pred).rejects(securityGroup);
    }

    @Test
    public void testAllowedPortFromEverywhereIPv6() throws Exception {
        securityGroup.withIpPermissions(
                new IpPermission()
                        .withFromPort(443)
                        .withToPort(443)
                        .withIpProtocol("tcp")
                        .withIpv6Ranges(new Ipv6Range().withCidrIpv6("::/0")));

        assertThat(pred).rejects(securityGroup);
    }
}
