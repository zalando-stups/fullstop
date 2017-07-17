package org.zalando.stups.fullstop.jobs.utils;

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.github.jgonian.ipmath.Ipv4Range;
import com.github.jgonian.ipmath.Ipv6Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Arrays.asList;

public final class Predicates {

    private static final Logger LOG = LoggerFactory.getLogger(Predicates.class);

    /**
     * https://en.wikipedia.org/wiki/Private_network#Private_IPv4_address_spaces
     */
    private static final List<Ipv4Range> PRIVATE_IPV4_RANGES = asList(
            Ipv4Range.parseCidr("10.0.0.0/8"),
            Ipv4Range.parseCidr("172.16.0.0/12"),
            Ipv4Range.parseCidr("192.168.0.0/16"));

    /**
     * https://en.wikipedia.org/wiki/Unique_local_address
     */
    private static final Ipv6Range PRIVATE_IPV6_RANGE = Ipv6Range.parseCidr("fc00::/7");

    private Predicates() {
    }

    public static Predicate<IpPermission> securityGroupExposesNotAllowedPorts(final Set<Integer> allowedPorts) {
        return rule -> opensUnallowedPorts(rule, allowedPorts) && hasExternalSource(rule);
    }

    private static boolean hasExternalSource(final IpPermission rule) {
        final boolean hasExternalIpv4Range = rule.getIpv4Ranges().stream()
                .map(IpRange::getCidrIp)
                .map(Ipv4Range::parseCidr)
                .anyMatch(range -> PRIVATE_IPV4_RANGES.stream().noneMatch(privateRange -> privateRange.contains(range)));

        final boolean hasExternalIpv6Ranges = rule.getIpv6Ranges().stream()
                .map(com.amazonaws.services.ec2.model.Ipv6Range::getCidrIpv6)
                .map(Ipv6Range::parseCidr)
                .anyMatch(range -> !PRIVATE_IPV6_RANGE.contains(range));

        return hasExternalIpv4Range || hasExternalIpv6Ranges;
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean opensUnallowedPorts(final IpPermission rule, final Set<Integer> allowedPorts) {
        final Integer fromPort = rule.getFromPort();
        final Integer toPort = rule.getToPort();

        // use explicit ports
        if (fromPort == null || toPort == null) {
            return true;
        }

        // port ranges are not allowed
        if (!fromPort.equals(toPort)) {
            return true;
        }

        if (!allowedPorts.contains(fromPort)) {
            return true;
        }

        return false;
    }
}
