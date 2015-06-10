/**
 * Copyright (C) 2015 Zalando SE (http://tech.zalando.com)
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zalando.stups.fullstop.plugin.count;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.base.Joiner;
import org.springframework.boot.actuate.metrics.CounterService;
import org.zalando.stups.fullstop.events.CloudtrailEventSupport;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;

/**
 * Count all events by type an account.
 *
 * @author jbellmann
 */
public class CountEventsPlugin extends AbstractFullstopPlugin {

    private static final Joiner JOINER = Joiner.on("_");

    private final CounterService counterService;

    public CountEventsPlugin(final CounterService counterService) {
        this.counterService = counterService;
    }

    @Override
    public boolean supports(final CloudTrailEvent delimiter) {

        // catch all
        return true;
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {

        String source = event.getEventData().getEventSource();
        String type = event.getEventData().getEventType();
        String accountId = CloudtrailEventSupport.getAccountId(event);
        String counterKey = JOINER.join(source, type, accountId);
        counterService.increment(counterKey);
    }

}
