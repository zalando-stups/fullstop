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

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.DescribeImagesRequest;
import com.amazonaws.services.ec2.model.Image;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.zalando.stups.fullstop.plugin.AbstractEC2InstancePlugin;
import org.zalando.stups.fullstop.plugin.EC2InstanceContext;
import org.zalando.stups.fullstop.plugin.EC2InstanceContextProvider;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.Set;
import java.util.function.Predicate;

import static java.lang.String.format;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toSet;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.violation.ViolationType.WRONG_AMI;

public class AmiPlugin extends AbstractEC2InstancePlugin {

    private final Logger log = getLogger(getClass());

    private final ViolationSink violationSink;

    @Value("${fullstop.plugins.ami.amiNameStartWith}")
    private String amiNameStartWith;

    @Value("${fullstop.plugins.ami.whitelistedAmiAccount}")
    private String whitelistedAmiAccount;

    @Autowired
    public AmiPlugin(final EC2InstanceContextProvider contextProvider, final ViolationSink violationSink) {
        super(contextProvider);
        this.violationSink = violationSink;
    }

    @Override
    protected Predicate<? super String> supportsEventName() {
        return isEqual(RUN_INSTANCES);
    }

    @Override
    protected void process(EC2InstanceContext context) {
        final Set<String> whiteListedAmiIds;

        // TODO move this to an external class and implement caching
        try {
            whiteListedAmiIds = context.getClient(AmazonEC2Client.class)
                    .describeImages(new DescribeImagesRequest().withOwners(whitelistedAmiAccount))
                    .getImages().stream()
                    .filter(image -> image.getName().startsWith(amiNameStartWith))
                    .map(Image::getImageId)
                    .collect(toSet());
        } catch (AmazonClientException e) {
            log.warn(format("Could not list AMIs for owner %s", whitelistedAmiAccount), e);
            return;
        }

        if (whiteListedAmiIds.isEmpty()) {
            log.warn("No white-listed AMIs found: Owner {}, prefix {}", whitelistedAmiAccount, amiNameStartWith);
            return;
        }

        context.getAmiId().ifPresent(amiId -> {
            if (!whiteListedAmiIds.contains(amiId)) {
                violationSink.put(
                        context.violation()
                                .withType(WRONG_AMI)
                                .withPluginFullyQualifiedClassName(AmiPlugin.class)
                                .withMetaInfo(ImmutableMap.of(
                                        "ami_id", amiId,
                                        "ami_name", context.getAmiName().orElse(null)))
                                .build());
            }
        });
    }
}
