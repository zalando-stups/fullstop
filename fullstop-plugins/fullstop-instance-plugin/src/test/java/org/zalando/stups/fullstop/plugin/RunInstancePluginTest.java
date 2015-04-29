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

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;

import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.TestCloudTrailEventData;
import org.zalando.stups.fullstop.violation.SysOutViolationStore;
import org.zalando.stups.fullstop.violation.ViolationStore;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.internal.UserIdentity;
import com.amazonaws.services.ec2.model.SecurityGroup;

import com.google.common.collect.Lists;

/**
 * @author  jbellmann
 */
public class RunInstancePluginTest {

    private ClientProvider clientProvider;
    private ViolationStore violationStore;

    @Before
    public void setUp() {
        clientProvider = Mockito.mock(ClientProvider.class);
        violationStore = new SysOutViolationStore();
        violationStore = Mockito.spy(violationStore);
    }

    @Test
    public void runInstancePluginTestWithSubclass() {

        // prepare
        RunInstancePlugin plugin = new TestRunInstancePlugin(clientProvider, violationStore);
        UserIdentity userIdentity = Mockito.mock(UserIdentity.class);
        Mockito.when(userIdentity.getAccountId()).thenReturn("1234567");

        TestCloudTrailEventData eventData = new TestCloudTrailEventData("/responseElements.json");
        eventData = spy(eventData);
        Mockito.when(eventData.getUserIdentity()).thenReturn(userIdentity);

        CloudTrailEvent event = new CloudTrailEvent(eventData, null);

        // test
        plugin.processEvent(event);

        // verify
        verify(violationStore, atLeastOnce()).save(Mockito.any());
    }

    /**
     * Prepare some {@link SecurityGroup}s.
     *
     * @return
     */
    protected static List<SecurityGroup> getSecurityGroupsForTesting() {
        List<SecurityGroup> result = Lists.newArrayList();
        for (int i = 0; i < 10; i++) {
            SecurityGroup g = new SecurityGroup();
            g.setOwnerId("OWNER_" + i);
            g.setGroupId("GROUP_" + i);
            result.add(g);
        }

        return result;
    }

    /**
     * @author  jbellmann
     */
    static class TestRunInstancePlugin extends RunInstancePlugin {

        public TestRunInstancePlugin(final ClientProvider clientProvider, final ViolationStore violationStore) {
            super(clientProvider, violationStore);
        }

        @Override
        protected List<SecurityGroup> getSecurityGroupsForIds(final List<String> securityGroupIds,
                final CloudTrailEvent event) {

            return getSecurityGroupsForTesting();
        }

    }

}
