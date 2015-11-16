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
package org.zalando.stups.fullstop.plugin.instance;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.google.common.collect.Lists;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.violation.SystemOutViolationSink;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

/**
 * @author jbellmann
 */
public class RunInstancePluginTest {

    private ClientProvider clientProvider;

    private ViolationSink violationSink;

    public static Function<IpPermission, String> toPortToString() {
        return t -> t.getToPort().toString();
    }

    /**
     * Prepare some {@link SecurityGroup}s.
     */
    protected static Optional<List<SecurityGroup>> getSecurityGroupsForTesting() {
        List<SecurityGroup> result = Lists.newArrayList();
        for (int i = 0; i < 2; i++) {
            SecurityGroup g = new SecurityGroup();
            g.setOwnerId("OWNER_" + i);
            g.setGroupId("GROUP_" + i);

            for (int j = 0; j < 3; j++) {
                IpPermission permission = new IpPermission();
                permission.setToPort(441 + j);
                g.getIpPermissions().add(permission);
            }

            result.add(g);
        }

        return Optional.of(result);
    }

    @Before
    public void setUp() {
        clientProvider = Mockito.mock(ClientProvider.class);
        violationSink = new SystemOutViolationSink();
        violationSink = Mockito.spy(violationSink);
    }

    @Test
    public void hasOnlyPrivateIp() {
        CloudTrailEvent event = createCloudTrailEvent("/responseElements.json");

        RunInstancePlugin plugin = new TestRunInstancePlugin(clientProvider, violationSink);

        Assertions.assertThat(plugin.hasPublicIp(event)).isFalse();

    }

    @Test
    public void filteringSecurityGroups() {

        RunInstancePlugin plugin = new TestRunInstancePlugin(clientProvider, violationSink);

        Set<String> result = plugin.getPorts(getSecurityGroupsForTesting().get());
        Assertions.assertThat(result).isNotEmpty();

    }

    // TODO
    @Ignore
    @Test
    public void testSecurityGroupFiltering() {
        Optional<List<SecurityGroup>> secGroups = getSecurityGroupsForTesting();
        Predicate<IpPermission> filter = IpPermissionPredicates.withToPort(443).negate().and(
                IpPermissionPredicates
                        .withToPort(22).negate());

        List<SecurityGroup> filteredSecGroups = secGroups.get().stream()
                                                         .filter(SecurityGroupPredicates.anyMatch(filter)).collect(
                        toList());

        System.out.println(filteredSecGroups.size());
        assertThat(filteredSecGroups).isEmpty();
    }

    @Ignore
    @Test
    public void testIpPermissionFiltering() {
        Optional<List<SecurityGroup>> secGroups = getSecurityGroupsForTesting();

        Predicate<IpPermission> filter = IpPermissionPredicates.withToPort(443).negate().and(
                IpPermissionPredicates
                        .withToPort(22).negate());

        Assertions.assertThat(secGroups.isPresent()).isTrue();

        for (SecurityGroup group : secGroups.get()) {
            List<IpPermission> permissionsFiltered = group.getIpPermissions().stream().filter(filter).collect(toList());
            List<String> filtered = permissionsFiltered.stream().map(toPortToString()).collect(Collectors.toList());
            System.out.println(filtered);
        }

        System.out.println(secGroups.get().size());
        assertThat(secGroups.get()).isEmpty();
    }

    @Test
    public void runInstancePluginTestWithSubclass() {

        // prepare
        RunInstancePlugin plugin = new TestRunInstancePlugin(clientProvider, violationSink);

        // test
        plugin.processEvent(createCloudTrailEvent("/record-run.json"));

        // verify
        // verify(violationSink, atLeastOnce()).save(Mockito.any());
    }

    static class TestRunInstancePlugin extends RunInstancePlugin {

        public TestRunInstancePlugin(final ClientProvider clientProvider, final ViolationSink violationSink) {
            super(clientProvider, violationSink);
        }

        @Override
        protected Optional<List<SecurityGroup>> getSecurityGroupsForIds(final List<String> securityGroupIds,
                final CloudTrailEvent event) {

            return getSecurityGroupsForTesting();
        }

    }

}
