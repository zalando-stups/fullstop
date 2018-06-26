package org.zalando.stups.fullstop.aws;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ClientTest {

    @SuppressWarnings("unused")
    @Test
    public void createClient() {
        final AWSCredentialsProvider tempCredentialsProvider = new STSAssumeRoleSessionCredentialsProvider.Builder("", "").build();
        final AmazonEC2 client = AmazonEC2ClientBuilder.standard()
                .withRegion(Regions.EU_CENTRAL_1)
                .withCredentials(tempCredentialsProvider)
                .build();
    }

}
