package org.zalando.stups.fullstop;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;

import java.util.Properties;

/**
 * @author jbellmann
 */
public class SimplePropertiesCredentials implements AWSCredentialsProvider {

    private final AWSCredentials awsCredentials;

    public SimplePropertiesCredentials(final Properties accountProperties) {
        if (accountProperties == null) {
            throw new IllegalArgumentException("AccountProperties should never be null");
        }

        //
        if (accountProperties.getProperty("accessKey") == null || accountProperties.getProperty("secretKey") == null) {
            throw new IllegalArgumentException(
                    "The specified properties (" + accountProperties.toString()
                            + ") doesn't contain the expected properties 'accessKey' " + "and 'secretKey'.");
        }

        String accessKey = accountProperties.getProperty("accessKey");
        String secretKey = accountProperties.getProperty("secretKey");
        awsCredentials = new BasicAWSCredentials(accessKey, secretKey);

    }

    @Override
    public AWSCredentials getCredentials() {
        return awsCredentials;
    }

    @Override
    public void refresh() {
        // ignore
    }

}
