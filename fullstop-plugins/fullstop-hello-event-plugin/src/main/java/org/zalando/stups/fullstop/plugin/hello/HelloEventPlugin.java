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
package org.zalando.stups.fullstop.plugin.hello;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;

/**
 * Just for testing.
 *
 * @author jbellmann
 */
@Component
public class HelloEventPlugin extends AbstractFullstopPlugin {

    private final Logger log = LoggerFactory.getLogger(HelloEventPlugin.class);

    /**
     * Handles every events.
     */
    @Override
    public boolean supports(final CloudTrailEvent delimiter) {
        return true;
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        log.info("HELLO EVENT - {}", event.getEventData().getEventId());
    }
}
