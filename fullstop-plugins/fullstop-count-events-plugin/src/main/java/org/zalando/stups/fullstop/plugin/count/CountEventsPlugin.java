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
package org.zalando.stups.fullstop.plugin.count;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import org.zalando.stups.fullstop.events.CloudtrailEventSupport;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;

import com.google.common.base.Joiner;

/**
 * Count all events by type an account.
 *
 * @author  jbellmann
 */
@Component
public class CountEventsPlugin extends AbstractFullstopPlugin {

    private static final Joiner JOINER = Joiner.on("_");

    private final CountEventsMetric countEventsMetric;

    @Autowired
    public CountEventsPlugin(final CountEventsMetric countEventsMetric) {

        this.countEventsMetric = countEventsMetric;
    }

    @Override
    public boolean supports(final CloudTrailEvent delimiter) {

        // catch all
        return true;
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

        String source = event.getEventData().getEventSource();
        String type = null;
        if (event.getEventData().getEventType() != null) {
            type = event.getEventData().getEventType();
        } else if (event.getEventData().getEventName() != null) {
            type = event.getEventData().getEventName();
        }

        String accountId = CloudtrailEventSupport.getAccountId(event);
        String counterKey = JOINER.join(source, type, accountId);
        countEventsMetric.markEvent(counterKey);
    }

}
