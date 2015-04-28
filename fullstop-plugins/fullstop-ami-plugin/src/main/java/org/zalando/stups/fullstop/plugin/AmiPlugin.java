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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Component;

import org.springframework.util.CollectionUtils;

import org.zalando.stups.fullstop.aws.ClientProvider;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;

import com.google.common.collect.Lists;

import com.jayway.jsonpath.JsonPath;

/**
 * @author mrandi
 */

@Component
public class AmiPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(AmiPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";
    private static final String EVENT_NAME = "RunInstances";
    public static final String TAUPAGE = "Taupage-";

    private final ClientProvider cachingClientProvider;

    @Value("${fullstop.plugins.ami.whitelistedAmiAccount}")
    private String whitelistedAmiAccount;

    @Autowired
    public AmiPlugin(final ClientProvider cachingClientProvider) {
        this.cachingClientProvider = cachingClientProvider;
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

        String parameters = event.getEventData().getResponseElements();

        List<String> amis = getAmi(parameters);

        final List<String> whitelistedAmis = Lists.newArrayList();

        AmazonEC2Client ec2Client = cachingClientProvider.getClient(AmazonEC2Client.class, whitelistedAmiAccount,
                Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));

        DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest().withOwners(whitelistedAmiAccount);

        DescribeImagesResult describeImagesResult = ec2Client.describeImages(describeImagesRequest);
        List<Image> images = describeImagesResult.getImages();

        for (Image image : images) {

            if (image.getName().startsWith(TAUPAGE)) {
                whitelistedAmis.add(image.getImageId());
            }
        }

        List<String> invalidAmis = Lists.newArrayList();

        for (String ami : amis) {

            boolean valid = false;

            for (String whitelistedAmi : whitelistedAmis) {

                if (ami.equals(whitelistedAmi)) {
                    valid = true;
                }
            }

            if (!valid) {
                invalidAmis.add(ami);
            }

        }

        if (!CollectionUtils.isEmpty(invalidAmis)) {
            LOG.info("Instances with ids: {} was started with wrong images: {}", getInstanceId(parameters),
                    invalidAmis);

        } else {
            LOG.info("Ami for instance: {} is whitelisted.", getInstanceId(parameters));
        }

    }

    private List<String> getAmi(final String parameters) {

        if (parameters == null) {
            return Lists.newArrayList();
        }

        return JsonPath.read(parameters, "$.instancesSet.items[*].imageId");
    }

    private List<String> getInstanceId(final String parameters) {

        if (parameters == null) {
            return null;
        }

        return JsonPath.read(parameters, "$.instancesSet.items[*].instanceId");
    }
}
