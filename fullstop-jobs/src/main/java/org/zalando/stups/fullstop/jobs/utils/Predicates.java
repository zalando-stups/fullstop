package org.zalando.stups.fullstop.jobs.utils;

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.IpRange;
import com.github.jgonian.ipmath.Ipv4Range;
import com.github.jgonian.ipmath.Ipv6Range;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.Arrays.asList;

public final class Predicates {

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

    @SuppressWarnings({"SimplifiableIfStatement"})
    private static boolean opensUnallowedPorts(final IpPermission rule, final Set<Integer> allowedPorts) {
        final String protocol = rule.getIpProtocol();
        if (protocol != null) {
            // match logical names as well as protocol numbers
            switch (protocol.toLowerCase()) {
                case "tcp":
                case "6":
                case "udp":
                case "17":
                    // check port ranges
                    break;

                case "icmp":
                case "1":
                case "icmpv6":
                case "58":
                    return false;

                default:
                    // From http://docs.aws.amazon.com/AWSEC2/latest/APIReference/API_IpPermission.html
                    // [...] specifying -1 or a protocol number other than tcp, udp, icmp, or 58 (ICMPv6)
                    // allows traffic on all ports, regardless of any port range you specify. [...]
                    return true;
            }
        }

        final Integer fromPort = rule.getFromPort();
        final Integer toPort = rule.getToPort();

        // No port range means: All traffic
        if (fromPort == null || toPort == null) {
            return true;
        }

        // Is there at least one non-allowed port?
        return IntStream.rangeClosed(fromPort, toPort).anyMatch(port -> !allowedPorts.contains(port));
    }
}
