package org.zalando.stups.fullstop.plugin.unapproved;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.zjsonpatch.JsonDiff;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.plugin.unapproved.config.UnapprovedServicesAndRoleProperties;
import org.zalando.stups.fullstop.violation.Violation;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.zalando.stups.fullstop.events.TestCloudTrailEventSerializer.createCloudTrailEvent;

/**
 * Created by mrandi.
 */
public class UnapprovedServicesAndRolePluginTest {

    private static final String POLICY_1 = "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Sid\":\"\",\"Effect\":\"Allow\",\"Principal\":{\"Federated\":\"arn:aws:iam::123:xxx-provider/Shibboleth\"},\"Action\":\"sts:AssumeRoleWithxxx\",\"Condition\":{\"StringEquals\":{\"xxx:au\":\"https://signin.aws.amazon.com/xxx\"}}}]}";
    private static final String POLICY_2 = "{\"Statement\":[{\"Sid\":\"\",\"Effect\":\"Allow\",\"Principal\":{\"Federated\":\"arn:aws:iam::123:xxx-provider/Shibboleth\"},\"Action\":\"sts:AssumeRoleWithxxx\",\"Condition\":{\"StringEquals\":{\"xxx:au\":\"https://signin.aws.amazon.com/xxx\"}}}],\"Version\":\"2012-10-17\"}";

    private PolicyProvider policyProviderMock;

    private ViolationSink violationSinkMock;

    private PolicyTemplatesProvider policyTemplatesProviderMock;

    private CloudTrailEvent event;

    private UnapprovedServicesAndRolePlugin plugin;

    @Before
    public void setUp() throws Exception {
        policyProviderMock = mock(PolicyProvider.class);
        violationSinkMock = mock(ViolationSink.class, withSettings().verboseLogging());
        policyTemplatesProviderMock = mock(PolicyTemplatesProvider.class);

        event = createCloudTrailEvent("/record.json");

        plugin = new UnapprovedServicesAndRolePlugin(
                policyProviderMock,
                violationSinkMock,
                policyTemplatesProviderMock,
                new UnapprovedServicesAndRoleProperties());

    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(policyProviderMock, violationSinkMock, policyTemplatesProviderMock);
    }

    @Test
    public void testSupports() throws Exception {

        when(policyTemplatesProviderMock.getPolicyTemplateNames()).thenReturn(
                newArrayList(
                        "mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ"));

        final boolean result = plugin.supports(event);
        assertThat(result).isTrue();

        verify(policyTemplatesProviderMock).getPolicyTemplateNames();
    }

    @Test
    public void testProcessEventWithPolicyDiff() throws Exception {
        final RolePolicies rolePolicies = new RolePolicies(emptySet(), singleton("mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ"), "{\"eventVersion\": \"1\"}");
        when(policyProviderMock.getRolePolicies(any(), any(), any())).thenReturn(rolePolicies);
        when(policyTemplatesProviderMock.getPolicyTemplate(any())).thenReturn("{\"eventVersion\": \"2\"}");

        plugin.processEvent(event);

        verify(policyProviderMock).getRolePolicies(eq("mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ"), any(), any());
        verify(policyTemplatesProviderMock).getPolicyTemplate(any());
        verify(violationSinkMock).put(any(Violation.class));
    }

    @Test
    public void testProcessEventWithAdditionalAttachedPolicy() throws Exception {
        final String policy = "{\"eventVersion\": \"2\"}";
        final RolePolicies rolePolicies = new RolePolicies(singleton("foo"), singleton("mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ"), policy);
        when(policyProviderMock.getRolePolicies(any(), any(), any())).thenReturn(rolePolicies);
        when(policyTemplatesProviderMock.getPolicyTemplate(any())).thenReturn(policy);

        plugin.processEvent(event);

        verify(policyProviderMock).getRolePolicies(eq("mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ"), any(), any());
        verify(policyTemplatesProviderMock).getPolicyTemplate(any());
        verify(violationSinkMock).put(any(Violation.class));
    }

    @Test
    public void testProcessEventWithChangedInlinePolicies() throws Exception {
        final String policy = "{\"eventVersion\": \"2\"}";
        final RolePolicies rolePolicies = new RolePolicies(emptySet(), newHashSet("mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ", "another-policy"), policy);
        when(policyProviderMock.getRolePolicies(any(), any(), any())).thenReturn(rolePolicies);
        when(policyTemplatesProviderMock.getPolicyTemplate(any())).thenReturn(policy);

        plugin.processEvent(event);

        verify(policyProviderMock).getRolePolicies(eq("mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ"), any(), any());
        verify(policyTemplatesProviderMock).getPolicyTemplate(any());
        verify(violationSinkMock).put(any(Violation.class));
    }


    @Test
    public void testProcessEventNoViolation() throws Exception {
        final RolePolicies rolePolicies = new RolePolicies(emptySet(), singleton("mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ"), POLICY_1);
        when(policyProviderMock.getRolePolicies(any(), any(), any())).thenReturn(rolePolicies);
        when(policyTemplatesProviderMock.getPolicyTemplate(any())).thenReturn(POLICY_2);

        plugin.processEvent(event);

        verify(policyProviderMock).getRolePolicies(eq("mint-worker-b17-AppServerRole-W5WX8WewafwO2MEWZ"), any(), any());
        verify(policyTemplatesProviderMock).getPolicyTemplate(any());
    }

    @Test
    public void testJsonDiff() throws Exception {
        final ObjectMapper om = new ObjectMapper();

        // Policy 1 and 2 are identical. The json has just a different order.
        final JsonNode policy1 = om.readTree(POLICY_1);
        final JsonNode policy2 = om.readTree(POLICY_2);
        final JsonNode diff = JsonDiff.asJson(policy1, policy2);

        assertThat(diff).isEqualTo(om.readTree("[]"));
    }

    @Test
    public void testJsonArrayDiff() throws Exception {
        final ObjectMapper om = new ObjectMapper();
        final JsonNode policy1 = om.readTree("{\"foo\":[\"a\", \"b\", \"c\"]}");
        final JsonNode policy2 = om.readTree("{\"foo\":[\"c\", \"d\", \"e\"]}");
        System.out.println(JsonDiff.asJson(policy1, policy2));
        System.out.println(JsonDiff.asJson(policy2, policy1));
    }
}
