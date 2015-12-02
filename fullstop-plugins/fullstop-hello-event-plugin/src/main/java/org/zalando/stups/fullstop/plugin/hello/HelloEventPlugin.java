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
