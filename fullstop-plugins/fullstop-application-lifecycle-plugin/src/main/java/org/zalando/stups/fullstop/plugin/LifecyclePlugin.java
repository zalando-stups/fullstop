package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.zalando.stups.fullstop.violation.ViolationSink;
import org.zalando.stups.fullstop.violation.repository.LifecycleRepository;

/**
 * Created by gkneitschel.
 */
public class LifecyclePlugin extends AbstractFullstopPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(LifecyclePlugin.class);

    private static final String EC2_SOURCE_EVENTS = "ec2.amazonaws.com";

    private static final String START_EVENT_NAME = "RunInstances";
    private static final String STOP_EVENT_NAME = "TerminateInstances";


    private LifecycleRepository lifecycleRepository;

    @Autowired
    public LifecyclePlugin(final LifecycleRepository lifecycleRepository) {
        this.lifecycleRepository = lifecycleRepository;
    }

    @Override
    public boolean supports(CloudTrailEvent event) {
        CloudTrailEventData cloudTrailEventData = event.getEventData();
        String eventSource = cloudTrailEventData.getEventSource();
        String eventName = cloudTrailEventData.getEventName();

        return true; //Gotta catchem all!
    }

    @Override
    public void processEvent(CloudTrailEvent event) {
        lifecycleRepository.save();

    }

    private String getApplicationVersion() {

    }
}
