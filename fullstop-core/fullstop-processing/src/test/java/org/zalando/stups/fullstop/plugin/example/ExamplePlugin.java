package org.zalando.stups.fullstop.plugin.example;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.plugin.metadata.PluginMetadata;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.plugin.DefaultMetadataProvider;
import org.zalando.stups.fullstop.plugin.FullstopPlugin;

import java.util.Collections;

/**
 * This plugin only handles EC-2 Events where name of event starts with "Delete".
 *
 * @author jbellmann
 */
@Component
public class ExamplePlugin implements FullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(ExamplePlugin.class);

    private static final String EC2_EVENTS = "ec2.amazonaws.com";

    private static final String DELETE = "Delete";

    /**
     * We use this as a filter for events.
     */
    @Override
    public boolean supports(final CloudTrailEvent event) {
        final CloudTrailEventData eventData = event.getEventData();

        final String eventSource = eventData.getEventSource();
        final String eventName = eventData.getEventName();

        return eventSource.equals(EC2_EVENTS) && eventName.startsWith(DELETE);
    }

    @Override
    // @HystrixCommand(fallback = my coole exception)
    // command for account id and client type -> generate new credentials
    public void processEvent(final CloudTrailEvent event) {

        final String parameters = event.getEventData().getRequestParameters();
        final String instanceId = getFromParameters(parameters);

        final AmazonEC2 client = getClientForAccount(
                event.getEventData().getUserIdentity().getAccountId(),
                Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));

        final DescribeInstancesRequest request = new DescribeInstancesRequest();
        request.setInstanceIds(Collections.singleton(instanceId));

        // try
        final DescribeInstancesResult result = client.describeInstances(request);
        // catch credentials are old
        // throw new my coole exception ( account id, CLIENTTYPE.EC2, exception) -> this will trigger hystrix

        LOG.info("SAVING RESULT INTO MAGIC DB", result);
    }

    private AmazonEC2 getClientForAccount(final String accountId, final Region region) {
        final AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClient.builder()
                .withCredentials(new ProfileCredentialsProvider()).build();
        final String roleArn = String.format("arn:aws:iam::%s:role/fullstop-role", accountId);
        final String sessionName = "fullstop-role";
        final AWSCredentialsProvider tempCredentialsProvider = new STSAssumeRoleSessionCredentialsProvider.Builder(roleArn, sessionName)
                .withStsClient(stsClient)
                .withRoleSessionDurationSeconds(3600)
                .build();
        return AmazonEC2Client.builder().withCredentials(tempCredentialsProvider).withRegion(region.getName()).build();
    }

    private String getFromParameters(@SuppressWarnings("unused") final String parameters) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PluginMetadata getMetadata() {
        return new DefaultMetadataProvider(getClass().getName()).getMetadata();
    }

}
