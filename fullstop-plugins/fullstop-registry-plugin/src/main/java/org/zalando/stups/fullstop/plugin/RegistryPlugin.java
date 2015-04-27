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

package org.zalando.stups.fullstop.plugin;

import static java.lang.String.format;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.RequestEntity.get;

import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

import static org.zalando.stups.fullstop.events.CloudtrailEventSupport.getInstanceIds;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Component;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import org.yaml.snakeyaml.Yaml;

import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.violation.ViolationStore;

import com.amazonaws.AmazonServiceException;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeRequest;
import com.amazonaws.services.ec2.model.DescribeInstanceAttributeResult;

import com.amazonaws.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * @author mrandi
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

    private final ViolationStore violationStore;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${fullstop.plugins.kio.url}/apps/{appId}")
    private String kioApplicationUrl;

    @Value("${fullstop.plugins.kio.url}/apps/{appId}/versions/{version_id}")
    private String kioApplicationVersionUrl;

    @Value("${fullstop.plugins.pierone.url}/v1/repositories/{team}/{application}/tags")
    private String pieroneApplicationUrl;

    @Autowired
    public RegistryPlugin(final ClientProvider cachingClientProvider, final ViolationStore violationStore) {
        this.cachingClientProvider = cachingClientProvider;
        this.violationStore = violationStore;
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
                violationStore.save(format("InstanceId: %s doesn't have any userData.", instanceId));
                return;
            }

            String applicationId = getApplicationId(userData, instanceId);

            String applicationVersion = getApplicationVersion(userData, instanceId);

            String source = getSource(userData, instanceId);

            ResponseEntity<JsonNode> applicationFromKio = getAndValidateApplicationFromKio(applicationId);

            if (applicationFromKio != null) {
                ResponseEntity<JsonNode> versionFromKio = getAndValidateApplicationVersionFromKio(applicationId,
                        applicationVersion);

                if (versionFromKio != null) {
                    validateSourceWithKio(applicationId, applicationVersion,
                            applicationFromKio.getBody().get("team_id").asText(), source,
                            versionFromKio.getBody().get("artifact").asText());
                }
            }

        }
    }

    private void validateSourceWithKio(final String applicationId, final String applicationVersion, final String team,
                                       final String source, final String artifact) {

        if (!source.equals(artifact)) {
            violationStore.save(format("Application: %s has not a valid artifact for version: %s.", applicationId,
                    applicationVersion));
        }

        ResponseEntity<JsonNode> response = null;
        try {
            response = restTemplate.exchange(get(
                    fromHttpUrl(pieroneApplicationUrl).buildAndExpand(team, applicationId).toUri()).accept(
                    APPLICATION_JSON).build(), JsonNode.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                violationStore.save(format("Source: %s is not present in pierone.", source));
                return;
            }
        }

        if (response != null && !response.getStatusCode().is2xxSuccessful()) {
                violationStore.save(format("Source: %s is not present in pierone.", source));
        } else if (response != null && response.getBody().get(applicationVersion) == null) {
            violationStore.save(format("Source: %s is not present in pierone.", source));
        }
    }

    private ResponseEntity<JsonNode> getAndValidateApplicationFromKio(final String applicationId) {

        ResponseEntity<JsonNode> response = null;
        try {
            response = restTemplate.exchange(get(fromHttpUrl(kioApplicationUrl).buildAndExpand(applicationId).toUri())
                    .accept(APPLICATION_JSON).build(), JsonNode.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                violationStore.save(format("Application: %s is not registered in kio.", applicationId));
                return null;
            }
        }

        if (response != null && !response.getStatusCode().is2xxSuccessful()) {
            violationStore.save(format("Application: %s is not registered in kio.", applicationId));
        }

        return response;
    }

    private ResponseEntity<JsonNode> getAndValidateApplicationVersionFromKio(final String applicationId,
                                                                             final String applicationVersion) {

        ResponseEntity<JsonNode> response = null;
        try {
            response = restTemplate.exchange(get(
                    fromHttpUrl(kioApplicationVersionUrl).buildAndExpand(applicationId, applicationVersion).toUri())
                    .accept(APPLICATION_JSON).build(), JsonNode.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                violationStore.save(format("Version: %s for Application: %s is not registered in kio.",
                        applicationVersion, applicationId));
                return null;
            }

        }

        if (response != null && !response.getStatusCode().is2xxSuccessful()) {
            violationStore.save(format("Version: %s for Application: %s is not registered in kio.", applicationVersion,
                    applicationId));
        }

        return response;
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
            violationStore.save(format("InstanceId: %s doesn't have any userData.", instanceId));
            return null;
        }

        String userData = describeInstanceAttributeResult.getInstanceAttribute().getUserData();

        byte[] bytesUserData = Base64.decode(userData);
        String decodedUserData = new String(bytesUserData);

        Yaml yaml = new Yaml();

        return (Map) yaml.load(decodedUserData);
    }

    private String getApplicationId(final Map userDataMap, final String instanceId) {
        String applicationId = (String) userDataMap.get(APPLICATION_ID);

        if (applicationId == null) {
            violationStore.save(format(
                    "No 'application_id' defined for this instance %s, "
                            + "please change the userData configuration for this instance and add this information.",
                    instanceId));
            return "";
        }

        return applicationId;
    }

    private String getApplicationVersion(final Map userDataMap, final String instanceId) {
        String applicationVersion = (String) userDataMap.get(APPLICATION_VERSION);

        if (applicationVersion == null) {
            violationStore.save(format(
                    "No 'application_version' defined for this instance %s, "
                            + "please change the userData configuration for this instance and add this information.",
                    instanceId));
            return "";
        }

        return applicationVersion;
    }

    private String getSource(final Map userDataMap, final String instanceId) {

        String source = (String) userDataMap.get(SOURCE);

        if (source == null) {
            violationStore.save(format(
                    "No 'source' defined for this instance %s, "
                            + "please change the userData configuration for this instance and add this information.",
                    instanceId));
            return "";
        }

        return source;
    }
}
