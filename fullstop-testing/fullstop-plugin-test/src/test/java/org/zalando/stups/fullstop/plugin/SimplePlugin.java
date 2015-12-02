package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import org.zalando.stups.fullstop.violation.ViolationBuilder;
import org.zalando.stups.fullstop.violation.ViolationSink;

/**
 * @author jbellmann
 */
public class SimplePlugin extends AbstractFullstopPlugin {

    private final ViolationSink violationSink;

    public SimplePlugin(final ViolationSink violationSink) {
        this.violationSink = violationSink;
    }

    @Override
    public boolean supports(final CloudTrailEvent delimiter) {
        return true;
    }

    @Override
    public void processEvent(final CloudTrailEvent event) {
        this.violationSink.put(new ViolationBuilder().build());
    }

}
