package org.zalando.stups.fullstop.aws;

import org.junit.Ignore;

import com.amazonaws.auth.STSAssumeRoleSessionCredentialsProvider;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.ec2.AmazonEC2Client;

@Ignore
public class ClientTest {

    private Region region = Region.getRegion(Regions.EU_CENTRAL_1);

    public void createClient() {
        AmazonEC2Client client = region.createClient(AmazonEC2Client.class,
                new STSAssumeRoleSessionCredentialsProvider("", ""), null);
    }

}
