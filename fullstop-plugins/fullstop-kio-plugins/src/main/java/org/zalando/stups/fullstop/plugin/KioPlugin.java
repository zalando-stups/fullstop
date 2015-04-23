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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudtrailEventSupport;
import org.zalando.stups.fullstop.violation.ViolationStore;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.*;
import static java.util.regex.Pattern.compile;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.RequestEntity.get;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

/**
 * @author mrandi
 */

@Component
public class KioPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(KioPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";

    private static Pattern pattern = compile("(application_id: )(.*)");

    private final ClientProvider cachingClientProvider;

    private final ViolationStore violationStore;

    @Value("${fullstop.plugins.kio.url}/apps/{appId}")
    private String kioApplicationUrl;

    @Autowired
    public KioPlugin(final ClientProvider cachingClientProvider, final ViolationStore violationStore) {
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
        List<String> instanceIds = CloudtrailEventSupport.getInstanceIds(event);

        for (String instanceId : instanceIds) {
            String applicationName = getApplicationName(event, instanceId);

            if (applicationName == null) {
                return;
            }

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<JsonNode> response = restTemplate.exchange(get(
                    fromHttpUrl(kioApplicationUrl).buildAndExpand(
                            applicationName).toUri()).accept(APPLICATION_JSON).build(), JsonNode.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                violationStore.save(format("Application: %s is not registered in kio.",applicationName));
            }
        }
    }

    private String getApplicationName(final CloudTrailEvent event, String instanceId) {

        AmazonEC2Client ec2Client = cachingClientProvider.getClient(AmazonEC2Client.class, event.getEventData().getUserIdentity().getAccountId(),
                Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));

        DescribeInstanceAttributeRequest describeInstanceAttributeRequest = new DescribeInstanceAttributeRequest();
        describeInstanceAttributeRequest.setInstanceId(instanceId);
        describeInstanceAttributeRequest.setAttribute("userData");

        DescribeInstanceAttributeResult describeInstanceAttributeResult;
        try {
            describeInstanceAttributeResult = ec2Client.describeInstanceAttribute(describeInstanceAttributeRequest);
        } catch (AmazonServiceException e) {
            LOG.error(e.getMessage());
            violationStore.save(format("InstanceId: %s doesn't have any userData.",instanceId));
            return null;
        }

        String userData = describeInstanceAttributeResult.getInstanceAttribute().getUserData();

        byte[] bytesUserData = Base64.decode(userData);
        String decodedUserData = new String(bytesUserData);

        Matcher matcher = pattern.matcher(decodedUserData);

        if (!matcher.find()) {
            violationStore.save(format("No application_id defined for this instance %s, " +
                    "please change the userData configuration for this instance and add this information.", instanceId));
        }

        try {
            return matcher.group(2);
        } catch (IndexOutOfBoundsException e) {
            violationStore.save(format("No application_id defined for this instance %s, " +
                    "please change the userData configuration for this instance and add this information.", instanceId));
            return null;
        }
    }
}
