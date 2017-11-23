package org.zalando.stups.fullstop.jobs.policy;

import com.amazonaws.regions.Region;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.ListRolesRequest;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.amazonaws.services.identitymanagement.model.Role;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.common.AwsApplications;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.jobs.exception.JobExceptionHandler;
import org.zalando.stups.fullstop.violation.ViolationMatchers;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.stups.fullstop.violation.ViolationType.CROSS_ACCOUNT_ROLE;

public class CrossAccountPolicyForIAMJobTest {

    private static final String ACCOUNT_ID = "111222333444";
    private static final String MANAGEMENT_ACCOUNT = "999888777666";

    private static final String AWS_SERVICE_POLICY_DOCUMENT = "{\n" +
            "    \"Version\": \"2008-10-17\",\n" +
            "    \"Statement\": [\n" +
            "        {\n" +
            "            \"Sid\": \"\",\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": {\n" +
            "                \"Service\": \"ec2.amazonaws.com\"\n" +
            "            },\n" +
            "            \"Action\": \"sts:AssumeRole\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    private static final String CROSS_ACCOUNT_POLICY_DOCUMENT = "{\n" +
            "    \"Version\": \"2012-10-17\",\n" +
            "    \"Statement\": [\n" +
            "        {\n" +
            "            \"Sid\": \"\",\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": {\n" +
            "                \"AWS\": \"arn:aws:iam::123123123123:root\"\n" +
            "            },\n" +
            "            \"Action\": \"sts:AssumeRole\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    private static final String MANAGEMENT_POLICY_DOCUMENT = "{\n" +
            "    \"Version\": \"2012-10-17\",\n" +
            "    \"Statement\": [\n" +
            "        {\n" +
            "            \"Sid\": \"\",\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": {\n" +
            "                \"AWS\": \"arn:aws:iam::" + MANAGEMENT_ACCOUNT + ":root\"\n" +
            "            },\n" +
            "            \"Action\": \"sts:AssumeRole\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    private static final String SAME_ACCOUNT_POLICY_DOCUMENT = "{\n" +
            "    \"Version\": \"2012-10-17\",\n" +
            "    \"Statement\": [\n" +
            "        {\n" +
            "            \"Sid\": \"\",\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": {\n" +
            "                \"AWS\": \"arn:aws:iam::" + ACCOUNT_ID + ":root\"\n" +
            "            },\n" +
            "            \"Action\": \"sts:AssumeRole\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    /**
     * When referencing a role that has been deleted, AWS won't show its ARN, but only the internal GUID.
     */
    private static final String DELETED_ROLE_POLICY_DOCUMENT = "{\n" +
            "    \"Version\": \"2012-10-17\",\n" +
            "    \"Statement\": [\n" +
            "        {\n" +
            "            \"Sid\": \"\",\n" +
            "            \"Effect\": \"Allow\",\n" +
            "            \"Principal\": {\n" +
            "                \"AWS\": \"AROAIM3TRURL24R6YZAS5\"\n" +
            "            },\n" +
            "            \"Action\": \"sts:AssumeRole\"\n" +
            "        }\n" +
            "    ]\n" +
            "}";

    private ViolationSink violationSinkMock;

    private ClientProvider clientProviderMock;

    private AccountIdSupplier accountIdSupplierMock;

    private JobsProperties jobsPropertiesMock;

    private AmazonIdentityManagementClient mockAmazonIdentityManagementClient;

    private ListRolesResult mockListRolesResult;

    private AwsApplications mockAwsApplications;

    @Before
    public void setUp() throws Exception {
        this.violationSinkMock = mock(ViolationSink.class);
        this.clientProviderMock = mock(ClientProvider.class);
        this.accountIdSupplierMock = mock(AccountIdSupplier.class);
        this.jobsPropertiesMock = mock(JobsProperties.class);
        this.mockAmazonIdentityManagementClient = mock(AmazonIdentityManagementClient.class);
        this.mockAwsApplications = mock(AwsApplications.class);

        mockListRolesResult = new ListRolesResult();
        mockListRolesResult.setRoles(asList(
                createRole("aws-service-role", AWS_SERVICE_POLICY_DOCUMENT),
                createRole("cross-account-role", CROSS_ACCOUNT_POLICY_DOCUMENT),
                createRole("same-account-role", SAME_ACCOUNT_POLICY_DOCUMENT),
                createRole("deleted-role-reference-role", DELETED_ROLE_POLICY_DOCUMENT),
                createRole("management-account-role", MANAGEMENT_POLICY_DOCUMENT)));

        when(clientProviderMock.getClient(any(), any(String.class), any(Region.class))).thenReturn(mockAmazonIdentityManagementClient);
    }

    private Role createRole(String name, String policyDocument) {
        return new Role()
                .withArn("arn:aws:iam::" + ACCOUNT_ID + ":role/" + name)
                .withRoleName(name)
                .withRoleId(randomAlphanumeric(21).toUpperCase()) // IDs look like: "AROAIM3TRURL24R6YZAS5"
                .withAssumeRolePolicyDocument(policyDocument);
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(
                violationSinkMock,
                clientProviderMock,
                accountIdSupplierMock,
                jobsPropertiesMock,
                mockAmazonIdentityManagementClient,
                mockAwsApplications);
    }

    @Test
    public void testCheck() throws Exception {
        when(accountIdSupplierMock.get()).thenReturn(newHashSet(ACCOUNT_ID));
        when(jobsPropertiesMock.getManagementAccount()).thenReturn(MANAGEMENT_ACCOUNT);
        when(mockAmazonIdentityManagementClient.listRoles(any(ListRolesRequest.class))).thenReturn(mockListRolesResult);

        final CrossAccountPolicyForIAMJob crossAccountPolicyForIAMJob = new CrossAccountPolicyForIAMJob(
                violationSinkMock,
                clientProviderMock,
                accountIdSupplierMock,
                jobsPropertiesMock,
                mock(JobExceptionHandler.class));

        crossAccountPolicyForIAMJob.run();

        verify(accountIdSupplierMock).get();
        verify(clientProviderMock).getClient(any(), any(String.class), any(Region.class));
        verify(mockAmazonIdentityManagementClient).listRoles(any(ListRolesRequest.class));
        verify(jobsPropertiesMock, atLeastOnce()).getManagementAccount();
        verify(violationSinkMock, times(1)).put(argThat(ViolationMatchers.hasType(CROSS_ACCOUNT_ROLE)));
    }
}
