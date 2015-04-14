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
package org.zalando.stups.fullstop;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.ec2.AmazonEC2Client;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.plugin.core.config.EnablePluginRegistries;

import org.zalando.stups.fullstop.plugin.FullstopPlugin;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author  jbellmann
 */
@SpringBootApplication
@EnablePluginRegistries({ FullstopPlugin.class })
public class Fullstop {

    private static final Logger LOG = getLogger(Fullstop.class);

    public static void main(final String[] args) {

        AmazonEC2Client amazonEC2Client = new AmazonEC2Client();
        LOG.info("instances: {}", amazonEC2Client.describeInstances());

        AmazonEC2Client amazonEC2Client2 = new AmazonEC2Client(new DefaultAWSCredentialsProviderChain());
        LOG.info("default-chain instances: {}", amazonEC2Client2.describeInstances());

        SpringApplication.run(Fullstop.class, args);
    }
}
