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
package org.zalando.stups.fullstop.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Predicate;

import org.junit.Test;

import com.amazonaws.services.ec2.model.IpPermission;

/**
 * Small tests for {@link IpPermissionPredicates}.
 *
 * @author  jbellmann
 */
public class IpPermissionPredicatesTest {

    @Test
    public void toPort() {
        IpPermission permission = buildIpPermission(443, 5000);
        Predicate<IpPermission> predicate = IpPermissionPredicates.withToPort(443);
        Predicate<IpPermission> predicate2 = IpPermissionPredicates.withToPort(445);

        assertThat(predicate.test(permission)).isTrue();
        assertThat(predicate2.test(permission)).isFalse();
    }

    @Test
    public void fromPort() {
        IpPermission permission = buildIpPermission(443, 6500);
        Predicate<IpPermission> predicate = IpPermissionPredicates.withFromPort(6500);
        Predicate<IpPermission> predicate2 = IpPermissionPredicates.withFromPort(6700);

        assertThat(predicate.test(permission)).isTrue();
        assertThat(predicate2.test(permission)).isFalse();
    }

    protected IpPermission buildIpPermission(final int toPort, final int fromPort) {
        IpPermission permission = new IpPermission();
        permission.setToPort(toPort);
        permission.setFromPort(fromPort);
        return permission;
    }

}
