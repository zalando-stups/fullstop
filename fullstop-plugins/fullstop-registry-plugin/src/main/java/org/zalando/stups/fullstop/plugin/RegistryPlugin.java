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
package org.zalando.stups.fullstop.plugin;

import static java.lang.String.format;

import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getInstanceIds;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.yaml.snakeyaml.Yaml;

import org.zalando.stups.clients.kio.Application;
import org.zalando.stups.clients.kio.KioOperations;
import org.zalando.stups.clients.kio.Version;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.clients.pierone.PieroneOperations;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

import com.amazonaws.AmazonServiceException;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;

import com.amazonaws.util.Base64;

/**
 * @author  mrandi
 */

@Component
public class RegistryPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(RegistryPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";
    public static final String USER_DATA = "userData";

    private static String APPLICATION_ID = "application_id";
    private static String APPLICATION_VERSION = "application_version";
    private static String SOURCE = "source";

    private final ClientProvider cachingClientProvider;

    private final ViolationSink violationSink;

    private final PieroneOperations pieroneOperations;

    private final KioOperations kioOperations;

    @Autowired
    public RegistryPlugin(final ClientProvider cachingClientProvider, final ViolationSink violationSink,
            final PieroneOperations pieroneOperations, final KioOperations kioOperations) {
        this.cachingClientProvider = cachingClientProvider;
        this.violationSink = violationSink;
        this.pieroneOperations = pieroneOperations;
        this.kioOperations = kioOperations;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return eventSource.equals(EC2_SOURCE_EVENTS) && eventName.equals(EVENT_NAME);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        List<String> instanceIds = getInstanceIds(event);

        for (String instanceId : instanceIds) {
            Map userData = getUserData(event, instanceId);

            if (userData == null) {
                return;
            }

            String applicationId = getApplicationId(event, userData, instanceId);

            String applicationVersion = getApplicationVersion(event, userData, instanceId);

            String source = getSource(event, userData, instanceId);

            if (applicationId != null) {

                Application applicationFromKio = getAndValidateApplicationFromKio(event, applicationId);

                if (applicationFromKio != null) {

                    Version applicationVersionFromKio = getAndValidateApplicationVersionFromKio(event, applicationId,
                            applicationVersion);

                    if (applicationVersionFromKio != null) {

                        validateSourceWithKio(event, applicationId, applicationVersion, applicationFromKio.getTeamId(),
                            source, applicationVersionFromKio.getArtifact());
                    }
                }
            }
        }
    }

    private void validateSourceWithKio(final CloudTrailEvent event, final String applicationId,
            final String applicationVersion, final String team, final String source, final String artifact) {

        if (!source.equals(artifact)) {
            violationSink.put(
                new ViolationBuilder(
                    format("Application: %s has not a valid artifact for version: %s.", applicationId,
                        applicationVersion)).withEventId(getCloudTrailEventId(event)).withRegion(
                    getCloudTrailEventRegion(event)).withAccoundId(getCloudTrailEventAccountId(event)).build());
        }

        Map<String, String> tags = this.pieroneOperations.listTags(team, applicationId);
        if (tags.isEmpty()) {
            violationSink.put(new ViolationBuilder(format("Source: %s is not present in pierone.", source)).withEventId(
                    getCloudTrailEventId(event)).withRegion(getCloudTrailEventRegion(event)).withAccoundId(
                    getCloudTrailEventAccountId(event)).build());
        } else {
            String value = tags.get(applicationVersion);
            if (value == null) {
                violationSink.put(new ViolationBuilder(format("Source: %s is not present in pierone.", source))
                        .withEventId(getCloudTrailEventId(event)).withRegion(getCloudTrailEventRegion(event))
                        .withAccoundId(getCloudTrailEventAccountId(event)).build());
            }
        }
    }

    private Application getAndValidateApplicationFromKio(final CloudTrailEvent event, final String applicationId) {

        Application application = kioOperations.getApplicationById(applicationId);

        return application;
    }

    private Version getAndValidateApplicationVersionFromKio(final CloudTrailEvent event, final String applicationId,
            final String applicationVersion) {

        Version version = kioOperations.getApplicationVersion(applicationId, applicationVersion);

        return version;

    }

    private Map getUserData(final CloudTrailEvent event, final String instanceId) {

        AmazonEC2Client ec2Client = cachingClientProvider.getClient(AmazonEC2Client.class,
                event.getEventData().getUserIdentity().getAccountId(),
                Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));

        DescribeInstanceAttributeRequest describeInstanceAttributeRequest = new DescribeInstanceAttributeRequest();
        describeInstanceAttributeRequest.setInstanceId(instanceId);
        describeInstanceAttributeRequest.setAttribute(USER_DATA);

        DescribeInstanceAttributeResult describeInstanceAttributeResult;
        try {
            describeInstanceAttributeResult = ec2Client.describeInstanceAttribute(describeInstanceAttributeRequest);
        } catch (AmazonServiceException e) {
            LOG.error(e.getMessage());
            violationSink.put(new ViolationBuilder(format("InstanceId: %s doesn't have any userData.", instanceId))
                    .withEventId(getCloudTrailEventId(event)).withRegion(getCloudTrailEventRegion(event)).withAccoundId(
                    getCloudTrailEventAccountId(event)).build());
            return null;
        }

        String userData = describeInstanceAttributeResult.getInstanceAttribute().getUserData();

        if (userData == null) {
            violationSink.put(new ViolationBuilder(format("InstanceId: %s doesn't have any userData.", instanceId))
                    .withEventId(getCloudTrailEventId(event)).withRegion(getCloudTrailEventRegion(event)).withAccoundId(
                    getCloudTrailEventAccountId(event)).build());
            return null;
        }

        byte[] bytesUserData = Base64.decode(userData);
        String decodedUserData = new String(bytesUserData);

        Yaml yaml = new Yaml();

        return (Map) yaml.load(decodedUserData);
    }

    private String getApplicationId(final CloudTrailEvent event, final Map userDataMap, final String instanceId) {
        String applicationId = (String) userDataMap.get(APPLICATION_ID);

        if (applicationId == null) {
            violationSink.put(
                new ViolationBuilder(
                    format(
                        "No 'application_id' defined for this instance %s, "
                            + "please change the userData configuration for this instance and add this information.",
                        instanceId)).withEventId(getCloudTrailEventId(event)).withRegion(
                    getCloudTrailEventRegion(event)).withAccoundId(getCloudTrailEventAccountId(event)).build());
            return null;
        }

        return applicationId;
    }

    private String getApplicationVersion(final CloudTrailEvent event, final Map userDataMap, final String instanceId) {
        String applicationVersion = (String) userDataMap.get(APPLICATION_VERSION);

        if (applicationVersion == null) {
            violationSink.put(
                new ViolationBuilder(
                    format(
                        "No 'application_version' defined for this instance %s, "
                            + "please change the userData configuration for this instance and add this information.",
                        instanceId)).withEventId(getCloudTrailEventId(event)).withRegion(
                    getCloudTrailEventRegion(event)).withAccoundId(getCloudTrailEventAccountId(event)).build());
            return null;
        }

        return applicationVersion;
    }

    private String getSource(final CloudTrailEvent event, final Map userDataMap, final String instanceId) {

        String source = (String) userDataMap.get(SOURCE);

        if (source == null) {
            violationSink.put(
                new ViolationBuilder(
                    format(
                        "No 'source' defined for this instance %s, "
                            + "please change the userData configuration for this instance and add this information.",
                        instanceId)).withEventId(getCloudTrailEventId(event)).withRegion(
                    getCloudTrailEventRegion(event)).withAccoundId(getCloudTrailEventAccountId(event)).build());
            return null;
        }

        return source;
    }
}
