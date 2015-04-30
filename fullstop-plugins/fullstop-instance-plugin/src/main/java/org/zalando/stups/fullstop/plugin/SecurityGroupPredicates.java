/**
 * Copyright 2015 Zalando SE
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin;

import java.util.function.Predicate;

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;

/**
 * @author  jbellmann
 */
public abstract class SecurityGroupPredicates {

    public static Predicate<SecurityGroup> toPort(final Integer port) {
        return new Predicate<SecurityGroup>() {

            @Override
            public boolean test(final SecurityGroup t) {
                return t.getIpPermissions().stream().anyMatch(IpPermissionPredicates.withToPort(port));
            }
        };
    }

    public static Predicate<SecurityGroup> anyMatch(final Predicate<IpPermission> ipPermissionPredicate) {
        return new Predicate<SecurityGroup>() {
            @Override
            public boolean test(final SecurityGroup t) {
                return t.getIpPermissions().stream().anyMatch(ipPermissionPredicate);
            }
        };
    }

    public static Predicate<SecurityGroup> allMatch(final Predicate<IpPermission> ipPermissionPredicate) {
        return new Predicate<SecurityGroup>() {
            @Override
            public boolean test(final SecurityGroup t) {
                return t.getIpPermissions().stream().allMatch(ipPermissionPredicate);
            }
        };
    }

}
