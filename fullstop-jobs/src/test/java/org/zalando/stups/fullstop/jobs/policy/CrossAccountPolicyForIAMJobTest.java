package org.zalando.stups.fullstop.jobs.policy;

import com.amazonaws.regions.Region;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.ListRolesResult;
import com.amazonaws.services.identitymanagement.model.Role;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.jobs.common.AccountIdSupplier;
import org.zalando.stups.fullstop.jobs.common.AwsApplications;
import org.zalando.stups.fullstop.jobs.config.JobsProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static org.mockito.Mockito.*;

public class CrossAccountPolicyForIAMJobTest {

    public static final String ACCOUNT_ID = "11111111";
    public static final String REGION1 = "eu-west-1";
    public static final String ROLE_ID = "fdsakufsdu";
    public static final String ARN = "arn:dsfasd";
    public static final String ARN_2 = "arn:oiu1237";
    public static final String MANAGEMENT_ACCOUNT = "5555555555";

    public static final String INVALID_POLICY_DOCUMENT = "{\n" +
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

    public static final String VALID_POLICY_DOCUMENT = "{\n" +
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
    public static final String ROLE_NAME = "name";

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

        final Role role = new Role();
        role.setArn(ARN);
        role.setRoleName(ROLE_NAME);
        role.setRoleId(ROLE_ID);
        role.setAssumeRolePolicyDocument(INVALID_POLICY_DOCUMENT);

        final Role role1 = new Role();
        role1.setArn(ARN_2);
        role1.setRoleName(ROLE_NAME);
        role1.setRoleId(ROLE_ID);
        role1.setAssumeRolePolicyDocument(VALID_POLICY_DOCUMENT);

        mockListRolesResult = new ListRolesResult();
        mockListRolesResult.setRoles(newArrayList(role, role1));

        when(clientProviderMock.getClient(any(), any(String.class), any(Region.class))).thenReturn(mockAmazonIdentityManagementClient);
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
        when(mockAmazonIdentityManagementClient.listRoles()).thenReturn(mockListRolesResult);

        final CrossAccountPolicyForIAMJob crossAccountPolicyForIAMJob = new CrossAccountPolicyForIAMJob(
                violationSinkMock,
                clientProviderMock,
                accountIdSupplierMock,
                jobsPropertiesMock);

        crossAccountPolicyForIAMJob.run();

        verify(accountIdSupplierMock).get();
        verify(clientProviderMock).getClient(any(), any(String.class), any(Region.class));
        verify(mockAmazonIdentityManagementClient).listRoles();
        verify(jobsPropertiesMock,times(1)).getManagementAccount();
        verify(violationSinkMock).put(any());
    }
}