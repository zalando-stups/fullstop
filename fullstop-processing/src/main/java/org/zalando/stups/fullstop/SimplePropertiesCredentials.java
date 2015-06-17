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
            throw new IllegalArgumentException("The specified properties (" + accountProperties.toString()
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
