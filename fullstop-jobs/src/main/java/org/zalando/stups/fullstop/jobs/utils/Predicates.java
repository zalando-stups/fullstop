package org.zalando.stups.fullstop.jobs.utils;

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;

import java.util.Set;
import java.util.function.Predicate;

/**
 * Created by gkneitschel.
 */
public final class Predicates {
    private Predicates() {
    }

    public static Predicate<SecurityGroup> securityGroupExposesNotAllowedPorts(final Set<Integer> allowedPorts) {

        return securityGroup -> {

            for (final IpPermission rule : securityGroup.getIpPermissions()) {
                if (opensUnallowedPorts(rule, allowedPorts) && hasExternalSource(rule)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static boolean hasExternalSource(final IpPermission rule) {
        for (final String ipRange : rule.getIpRanges()) {
            if (!(ipRange.startsWith("sg-") || ipRange.startsWith("172.31"))) {
                return true;
            }
        }

        return false;
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
