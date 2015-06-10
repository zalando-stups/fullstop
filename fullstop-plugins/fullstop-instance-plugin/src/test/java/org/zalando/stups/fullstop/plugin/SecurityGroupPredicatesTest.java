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

import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.function.Predicate;

/**
 * @author jbellmann
 */
public class SecurityGroupPredicatesTest {

    @Test
    public void test() {
        Predicate<SecurityGroup> predicate = SecurityGroupPredicates.anyMatch(IpPermissionPredicates.withToPort(22));

        SecurityGroup sg = buildSecurityGroup();
        Assertions.assertThat(predicate.test(sg)).isFalse();
    }

    @Test
    public void testWithSecurityGroup2() {
        Predicate<SecurityGroup> predicate = SecurityGroupPredicates.anyMatch(IpPermissionPredicates.withToPort(22));

        SecurityGroup sg = buildSecurityGroup2();
        Assertions.assertThat(predicate.test(sg)).isTrue();
    }

    /**
     * Because we have two IpPermission here '22' and '443' not all IpPermissions here match.
     */
    @Test
    public void testWithSecurityGroupWithMultiIpPermissionAndAllShouldMatchButDoNot() {
        Predicate<SecurityGroup> predicate = SecurityGroupPredicates.allMatch(IpPermissionPredicates.withToPort(22));

        SecurityGroup sg = buildSecurityGroup3();
        Assertions.assertThat(predicate.test(sg)).isFalse();
    }

    @Test
    public void testWithSecurityGroupWithMultiIpPermissionAndAllShouldMatch() {
        Predicate<IpPermission> ipPermissionPredicate = IpPermissionPredicates.withToPort(22).or(IpPermissionPredicates
                .withToPort(443));
        Predicate<SecurityGroup> predicate = SecurityGroupPredicates.allMatch(ipPermissionPredicate);

        SecurityGroup sg = buildSecurityGroup3();
        Assertions.assertThat(predicate.test(sg)).isTrue();
    }

    protected SecurityGroup buildSecurityGroup() {
        SecurityGroup sg = new SecurityGroup();
        IpPermission permission1 = new IpPermission();
        permission1.setToPort(23);
        sg.getIpPermissions().add(permission1);
        return sg;
    }

    protected SecurityGroup buildSecurityGroup2() {
        SecurityGroup sg = new SecurityGroup();
        IpPermission permission1 = new IpPermission();
        permission1.setToPort(22);
        sg.getIpPermissions().add(permission1);
        return sg;
    }

    protected SecurityGroup buildSecurityGroup3() {
        SecurityGroup sg = new SecurityGroup();

        IpPermission permission1 = new IpPermission();
        permission1.setToPort(22);
        sg.getIpPermissions().add(permission1);

        IpPermission permission2 = new IpPermission();
        permission2.setToPort(443);
        sg.getIpPermissions().add(permission2);

        return sg;
    }

}
