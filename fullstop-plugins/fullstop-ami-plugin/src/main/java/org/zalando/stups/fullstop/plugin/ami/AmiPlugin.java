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
package org.zalando.stups.fullstop.plugin.ami;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.DescribeImagesResult;
import com.amazonaws.services.ec2.model.Image;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.aws.ClientProvider;
import org.zalando.stups.fullstop.events.CloudTrailEventPredicate;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static org.zalando.stups.fullstop.events.CloudTrailEventPredicate.fromSource;
import static org.zalando.stups.fullstop.events.CloudTrailEventPredicate.withName;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.*;
import static org.zalando.stups.fullstop.violation.ViolationType.WRONG_AMI;

/**
 * @author mrandi
 */

@Component
public class AmiPlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(AmiPlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String EVENT_NAME = "RunInstances";

    private final CloudTrailEventPredicate eventFilter = fromSource(EC2_SOURCE_EVENTS).andWith(withName(EVENT_NAME));

    private final ClientProvider clientProvider;

    private final ViolationSink violationSink;

    @Value("${fullstop.plugins.ami.amiNameStartWith}")
    private String amiNameStartWith;

    @Value("${fullstop.plugins.ami.whitelistedAmiAccount}")
    private String whitelistedAmiAccount;

    @Autowired
    public AmiPlugin(final ClientProvider clientProvider, final ViolationSink violationSink) {
        this.clientProvider = clientProvider;
        this.violationSink = violationSink;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        return eventFilter.test(event);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        List<String> jsonInstances = getInstances(event);
        List<String> whitelistedAmis = Lists.newArrayList();

        whitelistedAmis = getWhitelistedAmis(event, whitelistedAmis);

        for (String jsonInstance : jsonInstances) {
            String ami = getAmi(jsonInstance);
            if (ami == null) {
                break;
            }
            String instanceId = getInstanceId(jsonInstance);
            if (instanceId == null) {
                break;
            }

            boolean valid = false;

            for (String whitelistedAmi : whitelistedAmis) {

                if (ami.equals(whitelistedAmi)) {
                    valid = true;
                }
            }

            if (!valid) {
                violationSink.put(
                        violationFor(event).withInstanceId(instanceId)
                                           .withType(WRONG_AMI)
                                           .withPluginFullyQualifiedClassName(AmiPlugin.class)
                                           .withMetaInfo(
                                                   newArrayList(ami))
                                           .build());
            }
        }
    }

    private List<String> getWhitelistedAmis(CloudTrailEvent event, List<String> whitelistedAmis) {
        AmazonEC2Client ec2Client = clientProvider.getClient(
                AmazonEC2Client.class, whitelistedAmiAccount,
                Region.getRegion(Regions.fromName(event.getEventData().getAwsRegion())));

        DescribeImagesRequest describeImagesRequest = new DescribeImagesRequest().withOwners(whitelistedAmiAccount);

        DescribeImagesResult describeImagesResult = ec2Client.describeImages(describeImagesRequest);
        List<Image> images = describeImagesResult.getImages();

        whitelistedAmis.addAll(
                images.stream().filter(image -> image.getName().startsWith(amiNameStartWith))
                      .map(Image::getImageId).collect(Collectors.toList()));
        return whitelistedAmis;
    }
}
