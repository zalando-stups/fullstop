package org.zalando.stups.fullstop.plugin;

import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEvent;
import com.amazonaws.services.cloudtrail.processinglibrary.model.CloudTrailEventData;

import java.util.Optional;
import java.util.function.Predicate;

public abstract class AbstractEC2InstancePlugin extends AbstractFullstopPlugin {

    protected static final String START_INSTANCES = "StartInstances";
    protected static final String RUN_INSTANCES = "RunInstances";
    protected static final String STOP_INSTANCES = "StopInstances";
    protected static final String TERMINATE_INSTANCES = "TerminateInstances";

    private final EC2InstanceContextProvider context;

    protected AbstractEC2InstancePlugin(EC2InstanceContextProvider context) {
        this.context = context;
    }

    @Override
    public boolean supports(CloudTrailEvent cloudTrailEvent) {
        return Optional.ofNullable(cloudTrailEvent)
                .map(CloudTrailEvent::getEventData)
                .filter(e -> "ec2.amazonaws.com".equals(e.getEventSource()))
                .map(CloudTrailEventData::getEventName)
                .filter(supportsEventName())
                .isPresent();
    }

    protected abstract Predicate<? super String> supportsEventName();

    @Override
    public void processEvent(CloudTrailEvent event) {
        context.instancesIn(event).forEach(this::process);
    }

    protected abstract void process(EC2InstanceContext ec2InstanceContext);
}
