package org.zalando.stups.fullstop.jobs.utils;

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ec2.model.UserIdGroupPair;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;

public class PredicatesTest {

    private Predicate<SecurityGroup> pred;

    @Before
    public void setUp() throws Exception {
        final Set<Integer> allowedPorts = Stream.of(22, 80, 443).collect(toSet());
        pred = Predicates.securityGroupExposesNotAllowedPorts(allowedPorts);
    }

    @Test
    public void securityGroupExposesNotAllowedPorts() throws Exception {
        final SecurityGroup securityGroup = new SecurityGroup()
                .withGroupId("sg-12345678")
                .withGroupName("TestSecurityGroup")
                .withOwnerId("111222333444")
                .withIpPermissions(
                        // a tcp port opened to some internal network only
                        new IpPermission()
                                .withFromPort(9100)
                                .withToPort(9100)
                                .withIpv4Ranges(new IpRange().withCidrIp("172.31.0.0/16"))
                                .withIpProtocol("tcp"),
                        // ICMP
                        new IpPermission()
                                .withFromPort(-1)
                                .withToPort(-1)
                                .withIpv4Ranges(new IpRange().withCidrIp("0.0.0.0/0"))
                                .withIpProtocol("icmp"),
                        // All traffic accessible from some internal security groups
                        new IpPermission()
                                .withIpProtocol("-1")
                                .withUserIdGroupPairs(
                                        new UserIdGroupPair().withUserId("111222333444").withGroupId("sg-11223344"),
                                        new UserIdGroupPair().withUserId("111222333444").withGroupId("sg-55667788"))
                        // TODO find more examples
                );

        // rejecting meets, that there is no open public port found
        assertThat(pred).rejects(securityGroup);
    }
}
