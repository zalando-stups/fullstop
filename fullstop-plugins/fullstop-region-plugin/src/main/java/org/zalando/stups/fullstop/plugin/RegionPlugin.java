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

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.events.CloudTrailEventPredicate;
import org.zalando.stups.fullstop.plugin.config.RegionPluginProperties;
import org.zalando.stups.fullstop.violation.ViolationSink;

import java.util.List;

import static java.util.Collections.singletonMap;
import static org.slf4j.LoggerFactory.getLogger;
import static org.zalando.stups.fullstop.events.CloudTrailEventPredicate.fromSource;
import static org.zalando.stups.fullstop.events.CloudTrailEventPredicate.withName;
import static org.zalando.stups.fullstop.events.CloudTrailEventSupport.*;
import static org.zalando.stups.fullstop.violation.ViolationType.WRONG_REGION;

/**
 * @author gkneitschel
 */
@Component
public class RegionPlugin extends AbstractFullstopPlugin {

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String EVENT_NAME = "RunInstances";

    private final Logger log = getLogger(getClass());

    private final ViolationSink violationSink;

    private final RegionPluginProperties regionPluginProperties;

    private CloudTrailEventPredicate eventFilter = fromSource(EC2_SOURCE_EVENTS).andWith(withName(EVENT_NAME));

    @Autowired
    public RegionPlugin(final ViolationSink violationSink, final RegionPluginProperties regionPluginProperties) {
        this.violationSink = violationSink;
        this.regionPluginProperties = regionPluginProperties;
    }

    @Override
    public boolean supports(final CloudTrailEvent event) {
        return eventFilter.test(event);
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        final List<String> instanceIds = getInstanceIds(event);
        if (instanceIds.isEmpty()) {
            // TODO investigate RunINstances events w/o instance ids. Is it a bug or intentional? Remove this warning in the latter case.
            log.warn("RunInstances event without EC2 instance ids: {}", event.getEventData());
        }

        final String region = getRegionAsString(event);
        final List<String> allowedRegions = regionPluginProperties.getWhitelistedRegions();
        if (!allowedRegions.contains(region)) {
            for (String instance : instanceIds) {
                violationSink.put(
                        violationFor(event)
                                .withInstanceId(instance)
                                .withType(WRONG_REGION)
                                .withPluginFullyQualifiedClassName(RegionPlugin.class)
                                .withMetaInfo(singletonMap("allowed_regions", allowedRegions))
                                .build());
            }
        }
    }
}
