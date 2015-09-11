/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public static Predicate<SecurityGroup> securityGroupExposesNotAllowedPorts(Set<Integer> allowedPorts) {

        return securityGroup -> {

            for (IpPermission rule : securityGroup.getIpPermissions()) {
                if (opensUnallowedPorts(rule, allowedPorts) && hasExternalSource(rule)) {
                    return true;
                }
            }
            return false;
        };
    }

    private static boolean hasExternalSource(IpPermission rule) {
        for (final String ipRange : rule.getIpRanges()) {
            if (!(ipRange.startsWith("sg-") || ipRange.startsWith("172.31"))) {
                return true;
            }
        }

        return false;
    }

    @SuppressWarnings("RedundantIfStatement")
    private static boolean opensUnallowedPorts(IpPermission rule, Set<Integer> allowedPorts) {
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
