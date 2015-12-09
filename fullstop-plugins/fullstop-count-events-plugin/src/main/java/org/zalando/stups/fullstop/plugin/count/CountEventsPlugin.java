package org.zalando.stups.fullstop.plugin.count;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.google.common.base.Joiner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.stups.fullstop.events.CloudTrailEventSupport;
import org.zalando.stups.fullstop.plugin.AbstractFullstopPlugin;

/**
 * Count all events by type an account.
 *
 * @author jbellmann
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
        }
        else if (event.getEventData().getEventName() != null) {
            type = event.getEventData().getEventName();
        }

        String accountId = CloudTrailEventSupport.getAccountId(event);
        String counterKey = JOINER.join(source, type, accountId);
        countEventsMetric.markEvent(counterKey);
    }

}
