package org.zalando.stups.fullstop.plugin.example;

import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;
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

        final AmazonEC2Client client = getClientForAccount(
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

    private AmazonEC2Client getClientForAccount(final String accountId, final Region region) {
        final AWSSecurityTokenServiceClient stsClient = new AWSSecurityTokenServiceClient(new ProfileCredentialsProvider());

        final AssumeRoleRequest assumeRequest = new AssumeRoleRequest().withRoleArn(
                "arn:aws:iam::ACCOUNT_ID:role/fullstop-role")
                                                                 .withDurationSeconds(3600).withRoleSessionName(
                        "fullstop-role");

        final AssumeRoleResult assumeResult = stsClient.assumeRole(assumeRequest);

        final BasicSessionCredentials temporaryCredentials = new BasicSessionCredentials(
                assumeResult.getCredentials()
                            .getAccessKeyId(), assumeResult.getCredentials().getSecretAccessKey(),
                assumeResult.getCredentials().getSessionToken());

        final AmazonEC2Client amazonEC2Client = new AmazonEC2Client(temporaryCredentials);
        amazonEC2Client.setRegion(region);

        return amazonEC2Client;
    }

    private String getFromParameters(final String parameters) {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PluginMetadata getMetadata() {
        return new DefaultMetadataProvider(getClass().getName()).getMetadata();
    }

}
